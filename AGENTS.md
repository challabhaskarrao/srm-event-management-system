# Repository Guidance

## Architecture

- `backend/` is the Java 21 / Spring Boot REST API and authorization boundary.
- `frontend/` is the React/Vite web application and Capacitor Android project.
- `database/` contains schema, append-only migrations, seeds, and development tools.
- `storage/` contains local private document data and is not source code.
- `swing-admin/` is frozen legacy code and must not be changed unless explicitly requested.

The browser and Android app call the backend API; only the backend connects to
MySQL. The Android project uses Capacitor with Vite `dist` as its web directory.

## Data and security rules

- Never fabricate official institutional data.
- Keep development fixtures isolated and removable.
- Never expose database, JWT, email, API-key, or temporary-password secrets.
- Migrations are append-only; never rerun an already-applied migration blindly.
- Backend authorization is authoritative; frontend route guards are not security controls.
- Admin must not gain Mentor/HOD application-approval permissions.
- Public self-signup is Student-only; staff accounts are institution-provisioned.
- Faculty roles come from `faculty_registry`, not client-supplied role fields.
- Preserve Activity approval and Achievement verification workflows.
- Preserve authenticated document upload, ownership, and download authorization.

## Validation and change boundaries

- Run backend, frontend, and Android checks before finalizing relevant changes.
- Preserve the existing web API configuration and use build-time environment values for demos.
- Keep demo HTTP/cleartext settings debug-only; production requires HTTPS.
- Do not modify `swing-admin` unless explicitly requested.
- Do not commit or push unless explicitly requested.
