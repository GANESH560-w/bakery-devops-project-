import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DB {
    static Connection connect() throws SQLException {
        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER"); 
        String pass = System.getenv("DB_PASS");
        if (url == null || user == null || pass == null) throw new SQLException("missing env");
        return DriverManager.getConnection(url, user, pass);
    }

    static String productsJson() {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement("SELECT id,name,price,image,quantity FROM products ORDER BY id ASC");
             ResultSet rs = ps.executeQuery()) {
            List<String> items = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                String image = rs.getString("image");
                int quantity = rs.getInt("quantity");
                items.add("{\"id\":" + id + ",\"name\":\"" + esc(name) + "\",\"price\":" + price + ",\"image\":\"" + esc(image) + "\",\"quantity\":" + quantity + "}");
            }
            return "[" + String.join(",", items) + "]";
        } catch (Exception e) {
            return "[" +
                    "{\"id\":1,\"name\":\"Cake\",\"price\":49.99,\"image\":\"/img/product-1.jpg\",\"quantity\":10}," +
                    "{\"id\":2,\"name\":\"Bread\",\"price\":14.99,\"image\":\"/img/product-2.jpg\",\"quantity\":20}," +
                    "{\"id\":3,\"name\":\"Cookies\",\"price\":24.49,\"image\":\"/img/product-3.jpg\",\"quantity\":30}," +
                    "{\"id\":4,\"name\":\"Pastry\",\"price\":15.00,\"image\":\"/img/product-1.jpg\",\"quantity\":15}," +
                    "{\"id\":5,\"name\":\"Donuts\",\"price\":10.00,\"image\":\"/img/product-2.jpg\",\"quantity\":25}," +
                    "{\"id\":6,\"name\":\"Croissants\",\"price\":12.00,\"image\":\"/img/product-3.jpg\",\"quantity\":12}" +
                    "]";
        }
    }

    static String adminLogin(String user, String pass) {
        if ("admin".equals(user) && "admin123".equals(pass)) {
            return "{\"status\":\"ok\",\"message\":\"Login successful\"}";
        }
        return "{\"status\":\"error\",\"message\":\"Invalid credentials\"}";
    }

    static String addProduct(String name, double price, String image, int quantity) {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement("INSERT INTO products(name, price, image, quantity) VALUES (?,?,?,?)")) {
            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setString(3, image);
            ps.setInt(4, quantity);
            ps.executeUpdate();
            return "{\"status\":\"ok\",\"message\":\"Product added\"}";
        } catch (Exception e) {
            return "{\"status\":\"error\",\"message\":\"" + esc(e.getMessage()) + "\"}";
        }
    }

    static String updateProduct(int id, String name, double price, String image, int quantity) {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement("UPDATE products SET name=?, price=?, image=?, quantity=? WHERE id=?")) {
            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setString(3, image);
            ps.setInt(4, quantity);
            ps.setInt(5, id);
            ps.executeUpdate();
            return "{\"status\":\"ok\",\"message\":\"Product updated\"}";
        } catch (Exception e) {
            return "{\"status\":\"error\",\"message\":\"" + esc(e.getMessage()) + "\"}";
        }
    }

    static String deleteProduct(int id) {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement("DELETE FROM products WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return "{\"status\":\"ok\",\"message\":\"Product deleted\"}";
        } catch (Exception e) {
            return "{\"status\":\"error\",\"message\":\"" + esc(e.getMessage()) + "\"}";
        }
    }

    static String placeOrder(String name, String email, String items) {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement("INSERT INTO orders(customer_name, customer_email, items, status, message) VALUES (?,?,?,?,?)")) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, items);
            ps.setString(4, "Pending");
            ps.setString(5, "");
            ps.executeUpdate();
            return "{\"status\":\"ok\",\"message\":\"Order placed successfully\"}";
        } catch (Exception e) {
            // Mock success if DB fails for demo purposes
            return "{\"status\":\"ok\",\"message\":\"Order placed successfully (Mock)\"}";
        }
    }

    static String getOrders() {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement("SELECT id,customer_name,customer_email,items,status,message FROM orders ORDER BY id DESC");
             ResultSet rs = ps.executeQuery()) {
            List<String> items = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("customer_name");
                String email = rs.getString("customer_email");
                String orderItems = rs.getString("items");
                String status = rs.getString("status");
                String message = rs.getString("message");
                items.add("{\"id\":" + id + ",\"name\":\"" + esc(name) + "\",\"email\":\"" + esc(email) + "\",\"items\":" + orderItems + ",\"status\":\"" + esc(status) + "\",\"message\":\"" + esc(message) + "\"}");
            }
            return "[" + String.join(",", items) + "]";
        } catch (Exception e) {
            return "[]";
        }
    }

    static String updateOrder(int id, String status, String message) {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement("UPDATE orders SET status=?, message=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setString(2, message);
            ps.setInt(3, id);
            ps.executeUpdate();
            return "{\"status\":\"ok\",\"message\":\"Order updated\"}";
        } catch (Exception e) {
            return "{\"status\":\"error\",\"message\":\"" + esc(e.getMessage()) + "\"}";
        }
    }

    static String teamJson() {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement("SELECT id,name,role,image FROM team ORDER BY id ASC");
             ResultSet rs = ps.executeQuery()) {
            List<String> items = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String role = rs.getString("role");
                String image = rs.getString("image");
                items.add("{\"id\":" + id + ",\"name\":\"" + esc(name) + "\",\"role\":\"" + esc(role) + "\",\"image\":\"" + esc(image) + "\"}");
            }
            return "[" + String.join(",", items) + "]";
        } catch (Exception e) {
            return "[" +
                    "{\"id\":1,\"name\":\"ganesh jadhav\",\"role\":\"Master Chef\",\"image\":\"/img/team-1.jpg\"}," +
                    "{\"id\":2,\"name\":\"akshay malviya\",\"role\":\"Bakery Specialist\",\"image\":\"/img/team-2.jpg\"}," +
                    "{\"id\":3,\"name\":\"krushna kharat\",\"role\":\"Cake Decorator\",\"image\":\"/img/team-3.jpg\"}," +
                    "{\"id\":4,\"name\":\"rushikesh yadhav\",\"role\":\"Pastry Expert\",\"image\":\"/img/team-4.jpg\"}" +
                    "]";
        }
    }

    static String testimonialsJson() {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement("SELECT id,name,text FROM testimonials ORDER BY id ASC");
             ResultSet rs = ps.executeQuery()) {
            List<String> items = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String text = rs.getString("text");
                items.add("{\"id\":" + id + ",\"name\":\"" + esc(name) + "\",\"text\":\"" + esc(text) + "\"}");
            }
            return "[" + String.join(",", items) + "]";
        } catch (Exception e) {
            return "[" +
                    "{\"id\":1,\"name\":\"John\",\"text\":\"Best bakery in town\"}," +
                    "{\"id\":2,\"name\":\"Emma\",\"text\":\"Amazing croissants\"}," +
                    "{\"id\":3,\"name\":\"Liam\",\"text\":\"Great service and coffee\"}" +
                    "]";
        }
    }

    static String saveContact(String body) {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement("INSERT INTO contact_messages(payload) VALUES (?)")) {
            ps.setString(1, body);
            ps.executeUpdate();
            return "{\"status\":\"ok\",\"message\":\"saved to database\"}";
        } catch (Exception e) {
            return "{\"status\":\"error\",\"message\":\"" + esc(e.getMessage()) + "\"}";
        }
    }

    static String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
