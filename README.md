# SRM Event Management System

Advanced Programming Practice Semester Project

## Problem overview

The main objective of the SRM Event Management System is to provide centralized management of external student event participation, mentor and HOD approval, supporting documents, participation confirmation, achievements, and department analytics. Students discover and register for events independently; this system begins with the institutional participation request and continues through approval and proof submission.

The repository contains the completed web-portal workflow through the current
activity, achievement, analytics, notification, and security blocks.
Authentication and the Mentor/HOD application-review workflow are provided by
the Spring Boot API, while the React frontend contains separate Student and
Staff route groups.

## Project status

- Current: Web portal functionally frozen through Block 21; Capacitor Android
  source is present, but APK generation is paused until the Android SDK and
  Gradle distribution are available.
- Deferred: Block 5 institutional master-data import, pending official data
- Next: publication review, then production deployment, observability, and
  operational hardening

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
- `swing-admin/` — frozen legacy/milestone client; retained, but no longer the intended staff interface.
- `database/` — MySQL schema, numbered migrations, and safe reference seeds.
- `storage/` — ignored local document storage used through the authenticated storage abstraction.
- `docs/` — project documentation.

The frontend and backend run independently and both Student and Staff web portals use the same API. The backend still starts when MySQL is unavailable; only the dedicated database health endpoint reports that failure.

Block 6 adds Admin event creation/editing, normalized team membership using
existing Student registration numbers, authenticated supporting/proof document
storage, and post-event achievement/result records. Event cancellation and
application deadlines are intentionally absent because the current schema does
not define them.

Block 7 adds authenticated password change, revocable JWT sessions, hashed
single-use recovery tokens, bounded login throttling, centralized API security
headers, and shared Student/staff account-security screens. Institutional email
delivery is not configured, so production recovery notifications remain
intentionally disabled behind a replaceable notifier interface.

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

Set `DB_USERNAME`, `DB_PASSWORD`, and a Base64-encoded `JWT_SECRET` containing
at least 32 random bytes in the current PowerShell session, then use the
checked-in launcher:

```powershell
cd backend
.\run-local.ps1
```

The launcher binds Spring Boot to `127.0.0.1` and gives the JDK's Windows
Unix-domain selector socket a short temporary path. This avoids the
`Unable to establish loopback connection` failure that can occur when Java's
default temporary directory is nested under a long synced-user path. To bind
elsewhere, pass `-BindAddress` explicitly.

The application health endpoint is available at `http://localhost:8080/api/health`. Database connectivity is checked separately at `http://localhost:8080/api/health/database`.

## Run the frontend

```powershell
cd frontend
npm install
npm run dev -- --host 127.0.0.1
```

Use the local URL printed by Vite, normally `http://localhost:5173`.

### Android LAN demo

The Capacitor Android project is under `frontend/android/` and uses the Vite
production output. For the local demo, keep the phone and laptop on the same
private network, start the backend with the opt-in LAN binding, and verify the
health endpoint from the phone before opening the APK:

```powershell
cd backend
.\run-local.ps1 -BindAddress 0.0.0.0

cd ..\frontend
$env:VITE_API_BASE_URL="http://<LAPTOP_LAN_IP>:8080/api"
npm run build
npx cap sync android
cd android
.\gradlew.bat assembleDebug
```

The Android WebView origin is `https://localhost`; include it in the
development `CORS_ALLOWED_ORIGINS` value together with the browser origins.
The debug APK permits the demo's private-LAN HTTP API. Production APKs must
use HTTPS. If the laptop IP changes, rebuild with the new
`VITE_API_BASE_URL`; no LAN address is committed to source.

## Legacy Swing client

The `swing-admin/` module is retained as a completed milestone only. Browser-based staff access under `/staff/*` is the supported direction, so Swing is not part of the active runtime or Block 3 verification.

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

Set variables in the PowerShell session used to run Spring Boot. Generate a
Base64-encoded random JWT signing key containing at least 32 bytes:

```powershell
$env:DB_HOST = "localhost"
$env:DB_PORT = "3306"
$env:DB_NAME = "srm_event_management"
$env:DB_USERNAME = "your_local_user"
$env:DB_PASSWORD = "your_local_password"
$jwtKeyBytes = New-Object byte[] 32
[Security.Cryptography.RandomNumberGenerator]::Fill($jwtKeyBytes)
$env:JWT_SECRET = [Convert]::ToBase64String($jwtKeyBytes)
$env:JWT_EXPIRATION_MS = "86400000"
$env:JWT_ISSUER = "srm-event-management"
$env:DOCUMENT_STORAGE_ROOT = "..\storage\documents"
```

The names and safe non-secret defaults are also documented in `.env.example`. Never commit the real values.

### 4. Apply the schema

From the repository root, run:

```powershell
mysql --host="$env:DB_HOST" --port="$env:DB_PORT" --user="$env:DB_USERNAME" --password --execute="SOURCE database/schema.sql"
```

`database/schema.sql` is the fresh-setup entry point. It creates and selects the database, then delegates to the canonical `database/migrations/001_initial_schema.sql` migration.

For an existing Block 4 database, apply the Block 6 integrity migration and
the documented workflow vocabulary:

```powershell
mysql --user="$env:DB_USERNAME" --password --execute="SOURCE database/migrations/003_block6_participation_lifecycle.sql"
mysql --user="$env:DB_USERNAME" --password --execute="SOURCE database/seed/004_document_types.sql"
```

Then apply the additive Block 7 account-session migration:

```powershell
mysql --user="$env:DB_USERNAME" --password --execute="SOURCE database/migrations/004_account_security.sql"
```

For a later existing database, review and apply the remaining additive
migrations in order: `005_student_initiated_participation.sql`,
`006_account_provisioning_otp.sql`, `007_pre_event_application_details.sql`,
`008_event_type_master.sql`, `009_student_achievement_foundation.sql`,
`010_historical_analytics_context.sql`, and
`011_in_app_notifications.sql`. Execute them manually only after confirming
which migrations are already applied; the application never runs migrations
automatically. The development-only account and password tools under
`database/seed/` and `database/dev/` must never be used with production data.

Uploaded files use generated opaque storage keys. Only metadata and relative
keys are stored in MySQL; downloads always pass through authenticated API
authorization. The local `storage/` contents remain ignored by Git. A future
production implementation can replace the `DocumentStorage` interface with
object storage without changing the document API.

The optional reference-role seed contains only `STUDENT`, `MENTOR`, `HOD`, and `ADMIN`:

```powershell
mysql --host="$env:DB_HOST" --port="$env:DB_PORT" --user="$env:DB_USERNAME" --password --execute="SOURCE database/seed/001_roles.sql"
```

### 5. Run and verify the backend

```powershell
cd backend
.\run-local.ps1
```

In another PowerShell window:

```powershell
Invoke-RestMethod http://localhost:8080/api/health
Invoke-RestMethod http://localhost:8080/api/health/database
```

A reachable database returns `UP` and `MySQL`. Missing configuration or unavailable MySQL returns HTTP 503 with a generic response that does not expose credentials or connection details.

## JDBC and transaction direction

DAO implementations use `java.sql.Connection`, `PreparedStatement`, and `ResultSet` directly. The shared connection manager owns connection creation so later multi-step services can use `connection.setAutoCommit(false)`, `commit()`, and `rollback()` without duplicating connection setup. No ORM is used.
