-- Optional idempotent C1 reference data. No users or students are inserted.
USE `srm_event_management`;

INSERT IGNORE INTO `roles` (`role_name`)
VALUES
    ('STUDENT'),
    ('MENTOR'),
    ('HOD'),
    ('ADMIN');
