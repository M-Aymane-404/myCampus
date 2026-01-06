
# MyCampus – Full Stack Application (Dockerized)

MyCampus is a full-stack academic management platform built with **Spring Boot**, **React (Vite)**, and **MySQL**, fully containerized using **Docker Compose**.

This document explains how to **run the project locally** step by step.

---

## 🧰 Tech Stack

* **Backend**: Spring Boot (Java 17)
* **Frontend**: React + Vite
* **Database**: MySQL 8
* **DB Admin**: phpMyAdmin
* **Containerization**: Docker & Docker Compose

---

## 📦 Project Structure

```
MyCampus/
│
├── backend/            # Spring Boot application
│   └── Dockerfile
│
├── frontend-web/       # React (Vite) application
│   └── Dockerfile
│
├── docker-compose.yml
└── README.md
```

---

## ✅ Prerequisites

Make sure you have installed:

* **Docker Desktop**
* **Docker Compose**
* **Git**

Verify installation:

```bash
docker --version
docker compose version
```

---

## 🚀 How to Run the Project

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/M-Aymane-404/myCampus.git
cd MyCampus
```

---

### 2️⃣ Start All Services with Docker

```bash
docker compose up --build
```

This will start:

* MySQL
* Backend (Spring Boot)
* Frontend (React)
* phpMyAdmin

---

### 3️⃣ Verify Running Containers

```bash
docker ps
```

You should see:

* `mycampus-mysql`
* `mycampus-backend`
* `mycampus-frontend`
* `mycampus-phpmyadmin`

---

## 🌐 Access the Application

### Frontend (React)

```
http://localhost:5173
```

### Backend API (Spring Boot)

```
http://localhost:8080
```

⚠️ Note:
Spring Boot does **not** expose `/` by default.
Use a real API endpoint such as:

```
http://localhost:8080/api/test/ping
```

---

### phpMyAdmin

```
http://localhost:8081
```

**Login credentials:**

* Server: `mysql`
* Username: `myuser`
* Password: `mypassword`

---

## 🗄️ Database Configuration

The database is automatically created at startup.

**Database name:** `mycampus`

Backend connection:

```
jdbc:mysql://mysql:3306/mycampus
```

---

## 🧪 Testing the Setup

### Test Backend Health

```bash
curl http://localhost:8080/api/test/ping
```

Expected response:

```
Backend is working
```

---

### Test MySQL Connection

```bash
docker exec -it mycampus-mysql mysql -u myuser -p
```

Then:

```sql
SHOW DATABASES;
USE mycampus;
```

---

## 🛑 Stop the Project

```bash
docker compose down
```

To also remove database data:

```bash
docker compose down -v
```

---

## 🧯 Troubleshooting

### Frontend container keeps restarting

Ensure `package.json` contains:

```json
"scripts": {
  "dev": "vite",
  "build": "vite build",
  "preview": "vite preview"
}
```

Docker should run:

```bash
npm run dev
```

---

### Backend shows 404 on `/`

This is normal.
Use a valid controller endpoint (e.g. `/api/...`).

---

## 📌 Notes

* The frontend and backend communicate through Docker network
* MySQL hostname inside Docker is **`mysql`**, not `localhost`
* This setup is intended for **local development**

---

## 👤 Authors

* Project developed by **MyCampus Team**
* Student project – Software Engineering & Cybersecurity


