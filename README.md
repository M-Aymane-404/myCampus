
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


---

Tu es un assistant technique et fonctionnel expert en génie logiciel, architecture backend Spring Boot, API REST, sécurité et systèmes académiques.

Je travaille sur un Projet de Fin d’Études (PFE) intitulé :

« Plateforme académique intelligente pour la gestion des étudiants, enseignants et structures pédagogiques »

🎓 Contexte du projet

Le projet vise à digitaliser entièrement la gestion académique d’un établissement d’enseignement supérieur (école/EST) en remplaçant les processus manuels (emails, fichiers Excel, documents papier).

La plateforme est web + mobile, sécurisée, multi-rôles et centralisée.

🎯 Objectif global

Concevoir une plateforme permettant :

aux étudiants de consulter leurs notes, absences, cours, examens,

aux enseignants de gérer les cours, présences, notes et examens,

à l’administration de gérer toute la structure académique et les utilisateurs.

👥 Rôles gérés

Administrateur

Professeur

Étudiant

Chaque rôle dispose d’un accès strictement contrôlé (RBAC).

🧩 Architecture technique

Backend : Java Spring Boot (API REST)

Frontend Web : React.js

Frontend Mobile : Flutter

Base de données : MySQL

ORM : JPA / Hibernate

Sécurité : BCrypt + JWT (en cours / prévu)

Documentation API : Swagger (springdoc-openapi)

Containerisation : Docker + Docker Compose

🏗️ Architecture métier (Backend)

Le backend est structuré en couches :

Controller (REST)

Métier / Service (logique business)

Repository (JPA)

DTO Entrée / Sortie

🏫 Modèle académique (entités principales)

Département

Filière

Classe

Module

Matière

AffectationEnseignement

Étudiant

Professeur

Administrateur

🔑 Choix d’architecture IMPORTANT

La clé primaire en base reste id (Long)

MAIS toutes les APIs utilisent des identifiants métier lisibles :

codeDepartement

codeFiliere

codeClasse

codeModule

codeMatiere

ppr (professeur / administrateur)

apogee (étudiant)

👉 Les id ne sont jamais exposés dans les URLs ni les DTO, sauf pour AffectationEnseignement.

🔄 Affectation d’enseignement (cas spécial)

Création / modification : via DTO

Activation / désactivation / suppression : via id uniquement

Exemple :

PUT /affectations/{id} → modifier (DTO)

PATCH /affectations/{id}/activer

PATCH /affectations/{id}/desactiver

DELETE /affectations/{id}

📦 DTO (Data Transfer Objects)

Les DTO d’entrée n’ont pas de id

Les relations sont faites par codes métier

Exemples :

FiliereEntrerDTO → codeDepartement, chefFilierePpr

ClasseEntrerDTO → codeFiliere

EtudiantEntrerDTO → codeClasse

⚙️ Fonctionnalités implémentées

✔ CRUD complet pour toute la structure académique
✔ CRUD utilisateurs (admin, prof, étudiant)
✔ Affectation enseignant → classe → matière
✔ Swagger documenté
✔ Dockerisé (MySQL + Backend + Frontend)
✔ Seeder via CommandLineRunner (exécutable via --seed)

🧪 Avancement actuel

Backend fonctionnel et structuré

APIs validées via Swagger

Docker opérationnel

Frontend en cours d’intégration

Sécurité avancée (JWT, permissions fines) prévue

🧠 Ce que j’attends de toi

Quand je te pose une question :

Respecte cette architecture

Propose des solutions propres, professionnelles et scalables

Ne suggère pas de casser le modèle métier (pas d’IDs exposés partout)

Raisonne comme pour un projet académique réel de fin d’études

Réponds comme un architecte logiciel / encadrant technique.


