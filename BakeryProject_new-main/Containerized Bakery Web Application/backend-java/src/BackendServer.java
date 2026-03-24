import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackendServer {
    public static void main(String[] args) throws IOException {
        tryRegisterMySqlDriver();
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/products", new JsonHandler(() -> DB.productsJson()));
        server.createContext("/api/team", new JsonHandler(() -> DB.teamJson()));
        server.createContext("/api/testimonials", new JsonHandler(() -> DB.testimonialsJson()));
        server.createContext("/api/contact", new ContactHandler());
        server.createContext("/api/admin/login", new AdminLoginHandler());
        server.createContext("/api/admin/products", new ProductAdminHandler());
        server.createContext("/api/orders", new OrderHandler());
        server.createContext("/api/admin/orders", new AdminOrderHandler());
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        server.setExecutor(executor);
        server.start();
    }

    interface Supplier {
        String get();
    }

    static class JsonHandler implements HttpHandler {
        private final Supplier supplier;

        JsonHandler(Supplier supplier) {
            this.supplier = supplier;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                byte[] methodNotAllowed = "{\"error\":\"method not allowed\"}".getBytes(StandardCharsets.UTF_8);
                setJsonHeaders(exchange.getResponseHeaders());
                exchange.sendResponseHeaders(405, methodNotAllowed.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(methodNotAllowed);
                }
                return;
            }
            String json = supplier.get();
            byte[] payload = json.getBytes(StandardCharsets.UTF_8);
            setJsonHeaders(exchange.getResponseHeaders());
            exchange.sendResponseHeaders(200, payload.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(payload);
            }
        }
    }

    static class ContactHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                byte[] methodNotAllowed = "{\"error\":\"method not allowed\"}".getBytes(StandardCharsets.UTF_8);
                setJsonHeaders(exchange.getResponseHeaders());
                exchange.sendResponseHeaders(405, methodNotAllowed.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(methodNotAllowed);
                }
                return;
            }
            String body = readBody(exchange.getRequestBody());
            String response = DB.saveContact(body);
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            setJsonHeaders(exchange.getResponseHeaders());
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    static class AdminLoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendError(exchange, 405, "method not allowed");
                return;
            }
            String body = readBody(exchange.getRequestBody());
            String user = getJsonValue(body, "username");
            String pass = getJsonValue(body, "password");
            sendResponse(exchange, DB.adminLogin(user, pass));
        }
    }

    static class ProductAdminHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }
            String method = exchange.getRequestMethod();
            String body = readBody(exchange.getRequestBody());
            if ("POST".equalsIgnoreCase(method)) {
                String name = getJsonValue(body, "name");
                double price = Double.parseDouble(getJsonValue(body, "price"));
                String image = getJsonValue(body, "image");
                int quantity = Integer.parseInt(getJsonValue(body, "quantity"));
                sendResponse(exchange, DB.addProduct(name, price, image, quantity));
            } else if ("PUT".equalsIgnoreCase(method)) {
                int id = Integer.parseInt(getJsonValue(body, "id"));
                String name = getJsonValue(body, "name");
                double price = Double.parseDouble(getJsonValue(body, "price"));
                String image = getJsonValue(body, "image");
                int quantity = Integer.parseInt(getJsonValue(body, "quantity"));
                sendResponse(exchange, DB.updateProduct(id, name, price, image, quantity));
            } else if ("DELETE".equalsIgnoreCase(method)) {
                int id = Integer.parseInt(getJsonValue(body, "id"));
                sendResponse(exchange, DB.deleteProduct(id));
            } else if ("GET".equalsIgnoreCase(method)) {
                sendResponse(exchange, DB.productsJson());
            } else {
                sendError(exchange, 405, "method not allowed");
            }
        }
    }

    static class OrderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendError(exchange, 405, "method not allowed");
                return;
            }
            String body = readBody(exchange.getRequestBody());
            String name = getJsonValue(body, "customer_name");
            String email = getJsonValue(body, "customer_email");
            String items = getJsonRawValue(body, "items"); // Extract raw JSON array
            sendResponse(exchange, DB.placeOrder(name, email, items));
        }
    }

    static class AdminOrderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                sendResponse(exchange, DB.getOrders());
            } else if ("PUT".equalsIgnoreCase(method)) {
                String body = readBody(exchange.getRequestBody());
                int id = Integer.parseInt(getJsonValue(body, "id"));
                String status = getJsonValue(body, "status");
                String message = getJsonValue(body, "message");
                sendResponse(exchange, DB.updateOrder(id, status, message));
            } else {
                sendError(exchange, 405, "method not allowed");
            }
        }
    }

    static String getJsonValue(String json, String key) {
        String pattern = "\"" + key + "\":\"(.*?)\"";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(json);
        if (m.find()) return m.group(1);
        // Try without quotes for numbers
        pattern = "\"" + key + "\":(\\d+\\.?\\d*)";
        r = java.util.regex.Pattern.compile(pattern);
        m = r.matcher(json);
        if (m.find()) return m.group(1);
        return "";
    }

    static String getJsonRawValue(String json, String key) {
        String pattern = "\"" + key + "\":(\\[.*?\\])";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(json);
        if (m.find()) return m.group(1);
        return "[]";
    }

    static void sendResponse(HttpExchange exchange, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        setJsonHeaders(exchange.getResponseHeaders());
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    static void sendError(HttpExchange exchange, int code, String error) throws IOException {
        byte[] payload = ("{\"error\":\"" + error + "\"}").getBytes(StandardCharsets.UTF_8);
        setJsonHeaders(exchange.getResponseHeaders());
        exchange.sendResponseHeaders(code, payload.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(payload);
        }
    }

    static void addCors(HttpExchange exchange) {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");
    }

    static void setJsonHeaders(Headers headers) {
        headers.set("Content-Type", "application/json; charset=utf-8");
    }

    static String readBody(InputStream is) throws IOException {
        byte[] buf = is.readAllBytes();
        return new String(buf, StandardCharsets.UTF_8);
    }

    static void tryRegisterMySqlDriver() {
        try {
            Class<?> cls = Class.forName("com.mysql.cj.jdbc.Driver");
            Driver d = (Driver) cls.getDeclaredConstructor().newInstance();
            DriverManager.registerDriver(d);
        } catch (Exception ignored) {
        }
    }
}
