
# MyCampus – Full Stack Application (Dockerized)

MyCampus is a full-stack academic management platform built with **Spring Boot**, **React (Vite)**, and **MySQL**, fully containerized using **Docker Compose**.

This document explains how to **run the project locally** step by step.

---

## 🧰 Tech Stack

* **Backend**: Spring Boot (Java 17)
* **Frontend**: React + Vite
* **Chatbot**: n8n
* **Database**: MySQL 8
* **DB Admin**: phpMyAdmin
* **Containerization**: Docker & Docker Compose

---

## 📦 Project Structure

```
myCampus/
├─ backend/                 # Backend Spring Boot (API REST, sécurité, WebSocket, JPA)
│  ├─ src/
│  │  ├─ main/
│  │  │  ├─ java/          # Code source (controllers, métier, repositories, config…)
│  │  │  └─ resources/     # application.properties, emplois JSON, etc.
│  │  └─ test/             # Tests (profil test H2)
│  ├─ pom.xml              # Dépendances Maven
│  └─ Dockerfile
│
├─ frontend-web/            # Frontend Web React + Vite
│  ├─ src/                 # Pages, composants (dont ChatBot), services (apiAuth…)
│  ├─ package.json
│  └─ Dockerfile
│
├─ my_campus_mobile/        # Application mobile Flutter
│  ├─ lib/                 # Code Flutter
│  ├─ test/                # Tests Flutter
│  └─ pubspec.yaml
│
├─ n8n/                     # Workflows n8n (chatbot/RAG)
│  └─ workflows/
│     └─ Rag Agent.json
│
├─ docker-compose.yml       # Orchestration : mysql, backend, frontend, phpMyAdmin, n8n
├─ README.md                # Instructions d’exécution
└─ (autres dossiers)        # ex: fichiers/, jointures/, insert etudiant/
(ressources/SQL/données)

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
* n8n
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
* `mycampus-n8n`

---
##  n8n Configuration (Chatbot/RAG)
### 1️⃣ Access n8n

* Start MyCampus using Docker Compose (as described in the startup guide).
* Open n8n at: `http://localhost:5679`
* If this is the first run, n8n will ask you to create an admin (owner) account to access the interface.


### 2️⃣ Importer le workflow n8n

The project includes the workflow at:

`D:\PFE\myCampus\n8n\workflows\Rag Agent.json`
in n8n :
*Go to **Workflows**.
*Click  **Import**.
*Import the file **Rag Agent.json**.
*Open the imported workflow and make sure it is **Active**.

### 3️⃣ Configure MySQL and Mistral Credentials in n8n (Required)

The workflow runs MySQL queries (MySQL nodes) to retrieve:

*the user context (grades, attendance, upcoming sessions, announcements)
*the list of accessible PDF documents
in n8n :

1)Go to **Credentials**.
2)Create a **MySQL** credential (or edit the one used by the workflow).
3)Fill in the Docker Compose parameters:
**Host** : mysql
**Port** : 3306
**Database** : mycampus
**User** : root
**Password** : root
4)Create a **Mistral** credential
5)Return to the workflow and verify that all **MySQL** and **Mistral** nodes are linked to this credential.

### 4️⃣ Configurer l’accès aux fichiers (PDF) pour le RAG

The workflow reads session resources (PDFs) from the shared volume uploads_data, then extracts text from PDF files.

Important checks:

* **Shared volume**: n8n and the backend must share the same uploads storage (in Docker Compose this is uploads_data mounted to /data/uploads).
* **n8n file access restriction**: the n8n container is configured to restrict file system access (via N8N_RESTRICT_FILE_ACCESS_TO).
File paths stored in the database must point to an allowed location (typically /data/uploads/...).
* **Quick test**: in n8n, execute the workflow with a “document/pdf/course” question and ensure the following nodes run without errors:
Read/Write Files from Disk
Extract from File (PDF)
### 5️⃣ Configurer le frontend (URL du webhook n8n)

On the React side, the chatbot uses an environment variable.

*Create/edit:
```D:\PFE\myCampus\frontend-web\.env```
*Set:
```VITE_N8N_CHAT_WEBHOOK_URL=http://localhost:5679/webhook/chat```
Restart/rebuild containers if needed:
```docker compose up --build```


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


---

