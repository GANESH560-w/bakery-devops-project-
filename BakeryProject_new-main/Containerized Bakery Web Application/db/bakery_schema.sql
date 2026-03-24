CREATE DATABASE IF NOT EXISTS bakery_db;
USE bakery_db;

CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(255) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role ENUM('user', 'admin') DEFAULT 'user',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS products (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  image LONGTEXT NOT NULL,
  quantity INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS orders (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NULL,
  customer_name VARCHAR(255) NOT NULL,
  customer_email VARCHAR(255) NOT NULL,
  customer_address TEXT NOT NULL,
  items JSON NOT NULL,
  total_cost DECIMAL(10,2) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'Pending',
  admin_message VARCHAR(255) DEFAULT '',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS contact_messages (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  subject VARCHAR(255),
  message TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO products (name, price, image, quantity) VALUES
('Butterscotch Cake', 450.00, 'img/product-1.jpg', 15),
('Whole Wheat Bread', 45.00, 'img/product-2.jpg', 50),
('Chocolate Cookies', 120.00, 'img/product-3.jpg', 40),
('Strawberry Pastry', 60.00, 'img/product-1.jpg', 25),
('Glazed Donuts', 35.00, 'img/product-2.jpg', 60),
('Almond Croissants', 80.00, 'img/product-3.jpg', 20);
