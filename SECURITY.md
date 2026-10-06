# Security Policy

## Project status

This repository contains a student activity and achievement management system
with a local development/demo deployment. It is not a production deployment
until HTTPS, production database/storage controls, monitoring, and operational
security review are completed.

## Reporting a vulnerability

Report security issues privately to the project maintainer rather than opening
a public GitHub issue. Do not include passwords, JWT secrets, database
credentials, personal student/faculty data, or uploaded evidence in reports.

## Sensitive areas

- Authentication, password recovery, JWT validation, and session invalidation.
- Student, faculty, registry, mentor-assignment, and department data.
- Uploaded evidence and generated reports.
- Environment variables containing database, JWT, email, or storage secrets.
- Backend authorization, which is the security authority for every role.

## Development versus production

Development fixtures and local demo credentials must remain isolated from
production data and must be removable. The Android demo may use a private-LAN
HTTP API with debug-only cleartext support; production clients must use HTTPS
with cleartext disabled. Production database and document storage must not be
exposed directly to clients.

Never commit `.env` files, passwords, JWT signing keys, API keys, email
credentials, private keys, or temporary credentials. If a secret is exposed,
revoke and rotate it immediately, then invalidate affected sessions where
appropriate.
