# SRM Event Management System

Advanced Programming Practice Semester Project

## Problem overview

The main objective of the SRM Event Management System is to provide centralized management of external student event participation, mentor and HOD approval, supporting documents, participation confirmation, achievements, and department analytics. Students discover and register for events independently; this system begins with the institutional participation request and continues through approval and proof submission.

This repository currently contains Coding Block C1: the MySQL schema and raw JDBC foundation. It intentionally has no authentication, workflow, dashboard, upload, or other business features.

## Project status

- Current: C1 — MySQL Schema + JDBC Foundation Complete
- Next: C2 — Not started

The workflow and capabilities described below are planned project direction unless explicitly identified as part of the completed C0 foundation.

## Core workflow

The planned workflow is:

1. A student registers independently for an external event.
2. The student creates a participation request and provides the required documents.
3. The section mentor reviews the request.
4. The HOD reviews requests approved by the mentor.
5. After participating, the student submits participation, result, or achievement proof.

The application is not an event discovery platform.

## Architecture overview

- `backend/` — Spring Boot REST API with an explicit raw JDBC connection and health-check foundation.
- `frontend/` — React and Vite web client.
- `swing-admin/` — Java Swing administrative client shell.
- `database/` — MySQL schema, numbered migrations, and safe reference seeds.
- `storage/` — local development upload location reserved for later blocks.
- `docs/` — project documentation.

The frontend, backend, and Swing application can be run independently. The backend still starts when MySQL is unavailable; only the dedicated database health endpoint reports that failure.

## Technology stack

- Java 21 target
- Spring Boot 4.1.1
- Maven
- Spring Web and Spring JDBC support
- MySQL Connector/J
- JUnit 5
- React 19.2
- Vite 8.1
- JavaScript and CSS
- Java Swing
- MySQL 8 Community Server

No JPA or Hibernate dependencies are used.

## Repository structure

```text
.
├── backend/
├── frontend/
├── swing-admin/
├── database/
│   ├── migrations/
│   ├── seed/
│   └── schema.sql
├── storage/
└── docs/
```

## Prerequisites

- JDK 21 or newer (up to Java 26 for the selected Spring Boot release)
- Maven 3.6.3 or newer
- Node.js 20.19+, 22.12+, or a newer supported release
- npm

## Run the backend

```powershell
cd backend
mvn spring-boot:run
```

The application health endpoint is available at `http://localhost:8080/api/health`. Database connectivity is checked separately at `http://localhost:8080/api/health/database`.

## Run the frontend

```powershell
cd frontend
npm install
npm run dev
```

Use the local URL printed by Vite, normally `http://localhost:5173`.

## Run the Swing admin shell

```powershell
cd swing-admin
mvn compile exec:java
```

## Run tests and builds

Backend tests and package:

```powershell
cd backend
mvn test
mvn package
```

Frontend production build:

```powershell
cd frontend
npm install
npm run build
```

Swing compilation:

```powershell
cd swing-admin
mvn compile
```

## Local MySQL setup on Windows

### 1. Install MySQL Community Server

Install MySQL 8 Community Server and the MySQL command-line client. Keep the administrator password in a password manager; do not place it in this repository.

### 2. Create the database and a local application user

Open MySQL as a local administrator:

```powershell
mysql --user=root --password
```

At the MySQL prompt, replace the example username and password locally:

```sql
CREATE DATABASE IF NOT EXISTS srm_event_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;
CREATE USER IF NOT EXISTS 'your_local_user'@'localhost'
    IDENTIFIED BY 'your_local_password';
GRANT ALL PRIVILEGES ON srm_event_management.*
    TO 'your_local_user'@'localhost';
```

### 3. Configure the backend environment

Set variables in the PowerShell session used to run Spring Boot:

```powershell
$env:DB_HOST = "localhost"
$env:DB_PORT = "3306"
$env:DB_NAME = "srm_event_management"
$env:DB_USERNAME = "your_local_user"
$env:DB_PASSWORD = "your_local_password"
```

The names and safe non-secret defaults are also documented in `.env.example`. Never commit the real values.

### 4. Apply the schema

From the repository root, run:

```powershell
mysql --host="$env:DB_HOST" --port="$env:DB_PORT" --user="$env:DB_USERNAME" --password --execute="SOURCE database/schema.sql"
```

`database/schema.sql` is the fresh-setup entry point. It creates and selects the database, then delegates to the canonical `database/migrations/001_initial_schema.sql` migration.

The optional reference-role seed contains only `STUDENT`, `MENTOR`, `HOD`, and `ADMIN`:

```powershell
mysql --host="$env:DB_HOST" --port="$env:DB_PORT" --user="$env:DB_USERNAME" --password --execute="SOURCE database/seed/001_roles.sql"
```

### 5. Run and verify the backend

```powershell
cd backend
mvn spring-boot:run
```

In another PowerShell window:

```powershell
Invoke-RestMethod http://localhost:8080/api/health
Invoke-RestMethod http://localhost:8080/api/health/database
```

A reachable database returns `UP` and `MySQL`. Missing configuration or unavailable MySQL returns HTTP 503 with a generic response that does not expose credentials or connection details.

## JDBC and transaction direction

DAO implementations use `java.sql.Connection`, `PreparedStatement`, and `ResultSet` directly. The shared connection manager owns connection creation so later multi-step services can use `connection.setAutoCommit(false)`, `commit()`, and `rollback()` without duplicating connection setup. No ORM is used.
