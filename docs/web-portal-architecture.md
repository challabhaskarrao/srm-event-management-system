# Web portal architecture

## Decision

The project uses one React/Vite frontend with two browser-facing route groups:

- `/student/*` for students
- `/staff/*` for Mentors, HODs, and Administrators

This preserves the existing student application, reuses one authentication context, API client, navigation system, and visual layer, and avoids the deployment and maintenance cost of a second frontend package. The portals remain distinct in navigation and authorization behavior without duplicating the codebase.

The `swing-admin/` module is retained as a frozen legacy/milestone client. It is not the target staff interface and is not required for the web runtime.

## Runtime boundary

Both route groups call the same Spring Boot REST API. Only the backend connects to MySQL. The browser never connects directly to the database.

The backend is the authorization authority. After login, the frontend restores and validates a stored JWT through `GET /api/auth/me`; route checks are only a user-experience boundary. Mentor, HOD, and Admin permissions remain enforced by the application endpoints and service layer.

## Route map

| Portal | Route | Authorized role |
| --- | --- | --- |
| Student | `/login` | Public login |
| Student | `/student/dashboard` | `STUDENT` |
| Student | `/student/apply` | `STUDENT` |
| Student | `/student/applications` | `STUDENT` |
| Student | `/student/applications/:code` | `STUDENT` |
| Staff | `/staff/login` | Public login; rejects `STUDENT` responses |
| Staff | `/staff/mentor` | `MENTOR` |
| Staff | `/staff/hod` | `HOD` |
| Staff | `/staff/admin` | `ADMIN` |

Legacy student URLs redirect to their `/student/*` equivalents so existing bookmarks continue to work.

## Local and Android demo configuration

The frontend reads `VITE_API_BASE_URL`, defaulting to
`http://localhost:8080/api`. All frontend API calls use this centralized
value; no service embeds its own API host.

For the first LAN-only Android demo:

1. Keep the laptop and phone on the same Wi-Fi/private network.
2. Run `ipconfig` and identify the laptop's Wi-Fi IPv4 address.
3. Start the backend from `backend/` with its LAN-only binding override:

   ```powershell
   .\run-local.ps1 -BindAddress 0.0.0.0
   ```

   The normal launcher default remains `127.0.0.1`.
4. Set the frontend API base only for that demo build, replacing the
   placeholder at build time:

   ```text
   VITE_API_BASE_URL=http://<LAPTOP_LAN_IP>:8080/api
   ```

5. From the phone browser, verify
   `http://<LAPTOP_LAN_IP>:8080/api/health` before building the APK.
6. Windows Firewall may require allowing Java/Spring Boot on Private
   networks for TCP port 8080. Do not automate firewall changes.

The backend reads comma-separated `CORS_ALLOWED_ORIGINS`, defaulting to the
local web origins `http://localhost:5173,http://127.0.0.1:5173`. Supply the
actual controlled demo origin(s) through environment configuration when the
Android container's origin is known; do not use `*`.

The LAN demo intentionally uses HTTP only on the private development network.
The generated Capacitor Android project uses `https://localhost` as its actual
local WebView origin. The demo backend therefore needs this controlled CORS
allowlist value at startup:

```powershell
$env:CORS_ALLOWED_ORIGINS="http://localhost:5173,http://127.0.0.1:5173,https://localhost"
```

The debug Android variant contains a debug-only Network Security Config that
permits cleartext HTTP for the LAN demo. Production APKs must use HTTPS and a
deployed API; release builds must not rely on debug cleartext behavior.
