const http = require('http');
const mysql = require('mysql2/promise');
const bcrypt = require('bcryptjs');

let pool = null;
(async () => {
    try {
        const host = process.env.DB_HOST || '127.0.0.1';
        const user = process.env.DB_USER || 'root';
        const password = process.env.DB_PASS || 'root';
        const database = process.env.DB_NAME || 'bakery_db';
        pool = mysql.createPool({host, user, password, database, waitForConnections: true, connectionLimit: 10, queueLimit: 0});
        
        // Ensure admin user exists
        const adminUser = 'admin';
        const adminPass = 'admin123';
        const [adminRows] = await pool.query('SELECT id FROM users WHERE role = ?', ['admin']);
        if (adminRows.length === 0) {
            const hash = await bcrypt.hash(adminPass, 10);
            await pool.query('INSERT INTO users (username, email, password_hash, role) VALUES (?, ?, ?, ?)', [adminUser, 'admin@bakery.com', hash, 'admin']);
        }
        console.log('Connected to Database and Admin checked');
    } catch (e) {
        console.error('DB Error:', e.message);
    }
})();

const server = http.createServer((req, res) => {
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

    if (req.method === 'OPTIONS') {
        res.writeHead(204);
        res.end();
        return;
    }

    let body = '';
    req.on('data', chunk => { body += chunk.toString(); });
    req.on('end', async () => {
        const url = req.url;
        const method = req.method;
        res.setHeader('Content-Type', 'application/json');

        try {
            if (!pool) throw new Error('Database not connected');

            // --- AUTH ---
            if (url === '/api/register' && method === 'POST') {
                const { username, email, password } = JSON.parse(body);
                const [exists] = await pool.query('SELECT id FROM users WHERE email = ?', [email]);
                if (exists.length > 0) return res.end(JSON.stringify({status:'error', message:'Email already registered'}));
                const hash = await bcrypt.hash(password, 10);
                await pool.query('INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)', [username, email, hash]);
                res.end(JSON.stringify({status:'ok', message:'Registered successfully'}));
            }
            else if (url === '/api/login' && method === 'POST') {
                const { email, password } = JSON.parse(body);
                const [rows] = await pool.query('SELECT id, username, password_hash, role FROM users WHERE email = ?', [email]);
                if (rows.length === 0) return res.end(JSON.stringify({status:'error', message:'User not found'}));
                const ok = await bcrypt.compare(password, rows[0].password_hash);
                if (!ok) return res.end(JSON.stringify({status:'error', message:'Invalid password'}));
                res.end(JSON.stringify({status:'ok', user: {id: rows[0].id, username: rows[0].username, role: rows[0].role}}));
            }
            else if (url === '/api/admin/login' && method === 'POST') {
                const { username, password } = JSON.parse(body);
                const [rows] = await pool.query('SELECT id, username, password_hash, role FROM users WHERE username = ? AND role = "admin"', [username]);
                if (rows.length === 0) return res.end(JSON.stringify({status:'error', message:'Admin not found'}));
                const ok = await bcrypt.compare(password, rows[0].password_hash);
                if (!ok) return res.end(JSON.stringify({status:'error', message:'Invalid password'}));
                res.end(JSON.stringify({status:'ok', message: 'Login successful'}));
            }

            // --- PRODUCTS ---
            else if (url === '/api/products' && method === 'GET') {
                const [rows] = await pool.query('SELECT * FROM products');
                res.end(JSON.stringify(rows));
            }
            else if (url === '/api/admin/products' && method === 'POST') {
                const { name, price, image, quantity } = JSON.parse(body);
                await pool.query('INSERT INTO products (name, price, image, quantity) VALUES (?, ?, ?, ?)', [name, price, image, quantity]);
                res.end(JSON.stringify({status:'ok', message:'Product added'}));
            }
            else if (url === '/api/admin/products' && method === 'PUT') {
                const { id, name, price, image, quantity } = JSON.parse(body);
                await pool.query('UPDATE products SET name=?, price=?, image=?, quantity=? WHERE id=?', [name, price, image, quantity, id]);
                res.end(JSON.stringify({status:'ok', message:'Product updated'}));
            }
            else if (url === '/api/admin/products' && method === 'DELETE') {
                const { id } = JSON.parse(body);
                await pool.query('DELETE FROM products WHERE id=?', [id]);
                res.end(JSON.stringify({status:'ok', message:'Product deleted'}));
            }

            // --- ORDERS ---
            else if (url === '/api/orders' && method === 'POST') {
                const order = JSON.parse(body);
                const { user_id, customer_name, customer_email, customer_address, items, total_cost } = order;
                const [res_order] = await pool.query(
                    'INSERT INTO orders (user_id, customer_name, customer_email, customer_address, items, total_cost) VALUES (?, ?, ?, ?, ?, ?)',
                    [user_id || null, customer_name, customer_email, customer_address, JSON.stringify(items), total_cost]
                );
                res.end(JSON.stringify({status:'ok', orderId: res_order.insertId}));
            }
            else if (url.startsWith('/api/user/orders') && method === 'GET') {
                const userId = url.split('/').pop();
                const [rows] = await pool.query('SELECT * FROM orders WHERE user_id = ? ORDER BY created_at DESC', [userId]);
                res.end(JSON.stringify(rows));
            }
            else if (url === '/api/admin/orders' && method === 'GET') {
                const [rows] = await pool.query('SELECT * FROM orders ORDER BY created_at DESC');
                res.end(JSON.stringify(rows));
            }
            else if (url === '/api/admin/orders' && method === 'PUT') {
                const { id, status, message } = JSON.parse(body);
                await pool.query('UPDATE orders SET status=?, admin_message=? WHERE id=?', [status, message, id]);
                res.end(JSON.stringify({status:'ok', message:'Order updated'}));
            }

            // --- CONTACT ---
            else if (url === '/api/contact' && method === 'POST') {
                const { name, email, subject, message } = JSON.parse(body);
                await pool.query('INSERT INTO contact_messages (name, email, subject, message) VALUES (?, ?, ?, ?)', [name, email, subject, message]);
                res.end(JSON.stringify({status:'ok', message:'Message sent'}));
            }

            else {
                res.writeHead(404);
                res.end(JSON.stringify({error: 'Not Found'}));
            }
        } catch (e) {
            console.error('Request Error:', e.message);
            res.writeHead(500);
            res.end(JSON.stringify({error: e.message}));
        }
    });
});

const PORT = 8080;
server.listen(PORT, '0.0.0.0', () => console.log(`Server running on port ${PORT}`));
