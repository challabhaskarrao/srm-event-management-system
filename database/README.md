# Database foundation

`migrations/001_initial_schema.sql` is the canonical C1 table definition. The root `schema.sql` creates and selects `srm_event_management`, then delegates to that migration with the MySQL client's `SOURCE` command. This avoids maintaining two copies of the schema.

Run `schema.sql` from the repository root for a fresh local setup. Apply `seed/001_roles.sql` afterwards when the four reference roles are wanted; that seed remains idempotent and contains no users, students, passwords, or tokens.

For local authentication testing only, apply `seed/002_development_admin.sql` after the roles seed. It idempotently creates or repairs the documented development Admin account with a Spring-compatible BCrypt hash. Do not use that development credential or seed in production.

Later schema changes should be added as new numbered migration files instead of rewriting a migration that has already been applied to a shared database.

Apply `migrations/002_application_review_fields.sql` after the initial schema to add the normalized HOD reviewer reference and Mentor/HOD remarks used by the review workflow. The migration is additive, preserves existing applications, and may be re-run safely.

Apply `migrations/003_block6_participation_lifecycle.sql` to enforce one final
result/achievement record per application. It adds only a unique constraint and
does not rewrite application, document, or achievement data. If a legacy
database already contains duplicate achievement rows for one application,
resolve those records deliberately before applying the migration.

Apply `migrations/004_account_security.sql` to add the non-destructive
`users.session_version` column used to invalidate JWTs after password changes or
resets. The canonical `password_reset_tokens` table stores only token digests.

Apply `migrations/005_student_initiated_participation.sql` to add the registry
and student-created participation workflow. Apply
`migrations/006_account_provisioning_otp.sql` to add institution-backed account
provisioning and purpose-bound OTP challenges. Both migrations are additive;
review their applied state before executing them on an existing database.

`seed/004_document_types.sql` idempotently enables the three document concepts
explicitly present in the documented workflow: Supporting Document
(`PRE_EVENT`), Participation Proof (`POST_EVENT`), and Result Proof
(`POST_EVENT`). Document rules remain configurable and are not fabricated.

For local Mentor/HOD workflow testing only, apply `seed/003_development_staff_review.sql`. It repairs the two staff development credentials and idempotently creates review routing for existing sample students. It is not production seed data.

Apply `migrations/007_pre_event_application_details.sql` after the account
provisioning migration when enabling the complete student pre-event form. It
adds nullable event, travel, accommodation, and fee fields and idempotently
seeds additional pre-event document types without recreating or deleting data.

Apply `migrations/008_event_type_master.sql` before the achievement foundation;
it creates the HOD-controlled event type master and its type-specific metadata.

Apply `migrations/009_student_achievement_foundation.sql` to add the separate
Student achievement workflow. It classifies existing event types as ACTIVITY or
ACHIEVEMENT, adds standalone achievement records and teams, and extends secure
document ownership to achievements without changing existing application
records. This migration must be reviewed and executed manually; the
application never runs migrations automatically.

Apply `migrations/010_historical_analytics_context.sql` before using the HOD
analytics dashboard. It adds nullable submission-time semester and student-year
snapshots for applications and standalone achievements. Existing rows are left
NULL deliberately; the analytics dashboard excludes those rows from exact
semester/student-year slices instead of fabricating historical context. Review
and execute this migration manually; the application never runs migrations
automatically.

Apply `migrations/011_in_app_notifications.sql` before using the private
notification center. It adds notification records and recipient state
additively; review and execute it manually, and never run migrations from the
application.

`dev/reset_demo_passwords.ps1` is a manual, development-only password reset
tool for existing review accounts. It requires explicit execution flags,
validates the exact development identities, uses the application's BCrypt cost,
increments session versions, and does not print password hashes. Do not use it
against production data.
