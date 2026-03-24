# 🥖 Pune Bakery - Professional Cloud Deployment Guide

A complete guide to deploying a containerized bakery application on **AWS (EC2 & RDS)**. Follow these steps sequentially to launch the project from scratch.

---

## 🏗️ Phase 1: AWS RDS Setup (The Database)
This ensures your data is stored persistently in a managed cloud database.

1. **Create RDS Instance**:
   - Go to **RDS Console** -> **Create Database**.
   - **Engine**: MySQL 8.0.
   - **Template**: **Free Tier** (Important).
   - **Identifier**: `bakery-db`.
   - **Master Username**: `root`.
   - **Master Password**: `admin123` (or your preferred password).
   - **Public Access**: **Yes** (to allow initial schema import).
   - **Connectivity**: Allow port **3306** in the security group.

2. **Initialize Database Schema**:
   - Copy the **RDS Endpoint** (e.g., `bakery-db.xyz.us-east-1.rds.amazonaws.com`).
   - From your local machine (using MySQL Workbench) or EC2 (using `mysql-client`), run:
     ```bash
     mysql -h <rds-endpoint> -u root -p
     # Enter password
     mysql> CREATE DATABASE bakery_db;
     mysql> USE bakery_db;
     mysql> SOURCE path/to/bakery_schema.sql;
     ```

---

## ☁️ Phase 2: AWS EC2 Setup (The Web Server)
1. **Launch Instance**:
   - **AMI**: Ubuntu Server (Free Tier).
   - **Instance Type**: `t2.micro`.
   - **Security Group (Inbound Rules)**:
     - **SSH (22)**: From your IP.
     - **HTTP (80)**: From anywhere (0.0.0.0/0).
     - **Custom TCP (8080)**: From anywhere (Required for Backend API).

2. **Install Docker & Compose**:
   ```bash
   sudo apt update
   sudo apt install docker.io -y
   sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
   sudo chmod +x /usr/local/bin/docker-compose
   sudo ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose
   ```

---

## �️ Phase 3: Project Configuration
Before launching, you must update the configuration to link the components.

1. **Clone Repository**:
   ```bash
   git clone https://github.com/siddheshsomvanshi1/Bakery_Project_Docker.git
   cd Bakery_Project_Docker/BakeryProject_new-main/"Containerized Bakery Web Application"
   ```

2. **Update `docker-compose.yml`**:
   Edit the `backend` environment variables:
   ```yaml
   backend:
     environment:
       DB_HOST: <YOUR_RDS_ENDPOINT>
       DB_USER: root
       DB_PASS: <YOUR_RDS_PASSWORD>
       DB_NAME: bakery_db
   ```

---

## 🚀 Phase 4: Final Deployment
1. **Launch Containers**:
   ```bash
   sudo docker-compose up --build -d
   ```

2. **Verify Access**:
   - Visit `http://<EC2_PUBLIC_IP>` for the store.
   - Visit `http://<EC2_PUBLIC_IP>/admin_login.html` for Admin.
     - **Admin**: `admin` / `admin123`

---

## 📊 Troubleshooting Checklist
- **Connection Refused?** Ensure Port **8080** is open in the EC2 Security Group.
- **Database Error?** Ensure the RDS Security Group allows Port **3306** from the EC2 instance's Private IP or Security Group.
- **Frontend not loading products?** Clear browser cache and ensure the dynamic IP detection in the JS files is working.

---
*Created by Siddhesh Somvanshi - End-to-End DevOps Lifecycle Project*
