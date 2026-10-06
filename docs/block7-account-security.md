# Block 7 account-security foundation

## Authentication decisions

- Email addresses are trimmed and normalized to lowercase before lookup.
- BCrypt cost 12 is used for new passwords. Existing BCrypt hashes retain their
  embedded cost and remain compatible.
- Passwords require at least 12 characters, uppercase, lowercase, and a digit,
  and must not exceed BCrypt's 72-byte input boundary.
- JWTs use HS256, a Base64 signing key of at least 32 bytes, configured issuer,
  issued/expiry timestamps, a random token ID, role, user ID, and session
  version. Password change/reset increments the session version, invalidating
  every previously issued token.
- `/api/auth/me` validates the account and session but does not echo the bearer
  token in its response.

## Token storage decision

The milestone retains bearer JWTs in `localStorage`. A partial cookie migration
would add CSRF and cross-origin deployment risk without completing the required
same-site/CSRF design. The known consequence is that a successful XSS attack can
read the token. React output escaping, no `dangerouslySetInnerHTML`, dependency
review, short configurable token expiry, centralized session clearing, and a
strict frontend CSP at the production hosting layer are therefore required.

The API sends `frame-ancestors 'none'`, `X-Frame-Options: DENY`, `nosniff`,
`Referrer-Policy: no-referrer`, and `Cache-Control: no-store`. A full frontend
CSP belongs at the Vite hosting/reverse-proxy layer because the API does not
serve the built application.

## Password recovery and delivery

Recovery tokens are 256-bit random URL-safe values. Only SHA-256 token digests
are stored. Tokens expire after 30 minutes by default, are single-use, and a new
request invalidates prior unused tokens. Reset completion atomically updates the
BCrypt hash, increments the session version, and consumes every outstanding
token for the user.

`PasswordRecoveryNotifier` is the delivery boundary. The current implementation
does not send or log tokens because institutional SMTP/email configuration has
not been supplied. Automated tests inject a notifier and verify the complete
token lifecycle. Production recovery remains unavailable until a real provider
is implemented and configured.

## Future institutional provisioning

Block 5 can import institution-approved identities with `password_hash = NULL`;
such accounts cannot authenticate. After the institution defines activation
eligibility and provides mail delivery, the same opaque-token machinery can be
used to let users establish their first password. Passwords must never appear in
the institutional spreadsheet. Role or activation changes must increment
`session_version` so existing sessions are revoked.

## Backend authorization matrix

| Capability | Anonymous | Student | Mentor | HOD | Admin |
|---|---:|---:|---:|---:|---:|
| Login / forgot / reset request | Yes | Yes | Yes | Yes | Yes |
| `/auth/me` / change password | No | Own account | Own account | Own account | Own account |
| Student profile | No | Own profile | No | No | No |
| Event reads | Yes | Yes | Yes | Yes | Yes |
| Event create/update | No | No | No | No | Yes |
| Application submit/read | No | Own | Assigned scope | Department scope | Read-only overview |
| Document upload | No | Own eligible application | No | No | No |
| Document/result read | No | Own | Assigned scope | Department scope | Administrative scope |
| Achievement submit | No | Own approved/completed event | No | No | No |
| Achievement verification | No | No | No | No | Yes |
| Mentor review | No | No | Assigned Mentor only | No | No |
| HOD review | No | No | No | Department HOD only | No |

Frontend routing is convenience only; controllers and services enforce this
matrix independently.

## Abuse and audit behavior

Login attempts are bounded per normalized account and client address. Five
failures in the default 15-minute window trigger a temporary 15-minute block.
The in-memory implementation is suitable for this single-node milestone; a
shared gateway/rate limiter is required before horizontal scaling.

Login success/failure for known users, password changes, reset requests, and
reset completion create best-effort audit events without passwords, hashes,
JWTs, reset tokens, document content, or unnecessary email values.
