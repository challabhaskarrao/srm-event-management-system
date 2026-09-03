# Database foundation

`migrations/001_initial_schema.sql` is the canonical C1 table definition. The root `schema.sql` creates and selects `srm_event_management`, then delegates to that migration with the MySQL client's `SOURCE` command. This avoids maintaining two copies of the schema.

Run `schema.sql` from the repository root for a fresh local setup. Apply `seed/001_roles.sql` afterwards only when the four reference roles are wanted. The seed is idempotent and contains no users, students, passwords, or tokens.

Later schema changes should be added as new numbered migration files instead of rewriting a migration that has already been applied to a shared database.
