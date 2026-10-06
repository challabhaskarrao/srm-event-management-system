-- Local-development authentication seed only.
-- The BCrypt value is for the documented development Admin credential.
-- Replace it through an environment-specific provisioning process outside local development.
USE `srm_event_management`;

INSERT INTO `users` (`email`, `password_hash`, `role_id`, `is_active`)
SELECT
    'admin@srmist.edu.in',
    '$2a$10$YursfnBh/lPdtD6wl4eiU.4NgFSA5H/0ANdUpHvjCq7FHTBA4/Hhu',
    `role_id`,
    TRUE
FROM `roles`
WHERE `role_name` = 'ADMIN'
ON DUPLICATE KEY UPDATE
    `password_hash` = VALUES(`password_hash`),
    `role_id` = VALUES(`role_id`),
    `is_active` = TRUE;
