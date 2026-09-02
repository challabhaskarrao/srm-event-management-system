# SRM Event Management System

Advanced Programming Practice Semester Project

## Problem overview

The main objective of the SRM Event Management System is to provide centralized management of external student event participation, mentor and HOD approval, supporting documents, participation confirmation, achievements, and department analytics. Students discover and register for events independently; this system begins with the institutional participation request and continues through approval and proof submission.

This repository currently contains Coding Block C0 only: the project foundation. It intentionally has no business features or database schema.

## Project status

- Current: C0 — Project Foundation Complete
- Next: C1 — MySQL Schema + JDBC Foundation

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

- `backend/` — Spring Boot REST API, prepared for raw JDBC in C1.
- `frontend/` — React and Vite web client.
- `swing-admin/` — Java Swing administrative client shell.
- `database/` — schema and future migration/seed locations.
- `storage/` — local development upload location reserved for later blocks.
- `docs/` — project documentation.

The frontend, backend, and Swing application can be run independently. MySQL is deliberately not required in C0.

## Technology stack

- Java 21 target
- Spring Boot 4.1.1
- Maven
- Spring Web and Spring JDBC support
- MySQL Connector/J (reserved for C1)
- JUnit 5
- React 19.2
- Vite 8.1
- JavaScript and CSS
- Java Swing
- MySQL (planned from C1)

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

The development health endpoint is available at `http://localhost:8080/api/health`.

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

## Database configuration

MySQL setup begins in C1. The backend excludes data-source auto-configuration in C0 so it starts without a database. Future environment variable names are documented in `.env.example`; real passwords and credentials must never be committed.
