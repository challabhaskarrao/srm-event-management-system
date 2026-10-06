# Block 6 event and participation lifecycle

## Capability boundary

The current schema supports event fields, applications, normalized teams,
configurable pre/post-event documents, approval history, and one final
achievement/result per application. It does not define event cancellation,
archival, application deadlines, or public registration, so Block 6 does not
invent those concepts.

## Workflow and locks

- Admin may create and edit supported event fields. Student, Mentor, and HOD
  cannot use those endpoints.
- Team members are existing Students resolved by registration number. The
  submitting Student is always persisted as leader/member. Team data is created
  atomically with the application and has no post-submission edit endpoint.
- Pre-event documents may be uploaded by the owning Student while the
  application is `SUBMITTED`; Mentor review locks that phase.
- Post-event documents and results require an `APPROVED` application and an
  event whose end date has passed.
- Students can read only their own records. Assigned Mentors, department HODs,
  and Admin have scoped read access to document/result records.
- Admin result verification does not grant application approval authority.

## Document security

PDF, PNG, and JPEG files up to 5 MB are accepted. Validation checks filename,
extension, declared MIME, and file signature. Files receive generated storage
keys and live outside public web roots. API responses never contain filesystem
paths. All downloads are authenticated and authorized before the storage object
is opened.
