# Bakery Web Application: End-to-End Docker Deployment
## Cloud Architecture on AWS: EC2, RDS, and Docker

This project demonstrates a containerized bakery web application deployed on AWS using Docker and Managed RDS.

---

## Phase 1: Infrastructure Setup
### Section 1: AWS RDS (Managed Database)
Using RDS ensures data persistence and high availability.

1. Create a **MySQL 8.0** instance in RDS (Free Tier).
2. **Database Name**: `bakery_db`.
3. **Connectivity**: Public Access: **No** (Recommended for production).
4. **Initialization**: Run the following SQL to create tables:

```sql
CREATE DATABASE IF NOT EXISTS bakery_db;
USE bakery_db;

CREATE TABLE products (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), price DECIMAL(10,2), image VARCHAR(255));
CREATE TABLE team (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), role VARCHAR(255), image VARCHAR(255));
CREATE TABLE contact_messages (id INT AUTO_INCREMENT PRIMARY KEY, payload JSON, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);

INSERT INTO products (name, price, image) VALUES ('Chocolate Cake', 49.99, 'img/product-1.jpg'), ('French Bread', 14.99, 'img/product-2.jpg');
```

---

## Phase 2: EC2 Deployment Server Setup
### Section 1: Installing Docker
Run these commands on your Ubuntu EC2 instance to prepare the environment.

```bash
sudo apt update -y && sudo apt install docker.io -y
sudo usermod -aG docker ubuntu
# Log out and log back in for group changes to take effect
```

---

## Phase 3: Containerization & Deployment
### Section 1: Running with Docker Compose
The application uses Docker Compose to orchestrate the frontend and backend services.

1. Clone the repository to your EC2 instance.
2. Update the `DB_URL`, `DB_USER`, and `DB_PASS` in `docker-compose.yml` with your RDS credentials.
3. Start the application:

```bash
docker-compose up --build -d
```

### Section 2: Verification
- Access the frontend at `http://<EC2-Public-IP>`
- The backend API is available at `http://<EC2-Public-IP>:8080/api/products`

---

## System Documentation & Diagrams
Detailed architecture diagrams, class diagrams, and ERDs can be found in the [Containerized Bakery Web Application](./BakeryProject_new-main/Containerized%20Bakery%20Web%20Application/README.md) directory.
