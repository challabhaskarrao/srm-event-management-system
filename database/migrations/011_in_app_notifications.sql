-- Block 14: private in-app workflow notifications.
-- Review and execute manually only after application code review. The application
-- never runs migrations automatically.
USE `srm_event_management`;

CREATE TABLE IF NOT EXISTS `notifications` (
    `notification_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `recipient_user_id` BIGINT UNSIGNED NOT NULL,
    `notification_type` VARCHAR(80) NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `message` VARCHAR(600) NOT NULL,
    `record_type` VARCHAR(40) NULL,
    `record_id` BIGINT UNSIGNED NULL,
    `action_path` VARCHAR(255) NULL,
    `dedup_key` VARCHAR(255) NOT NULL,
    `is_read` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `read_at` TIMESTAMP NULL DEFAULT NULL,
    PRIMARY KEY (`notification_id`),
    UNIQUE KEY `uq_notifications_recipient_dedup` (`recipient_user_id`, `dedup_key`),
    INDEX `idx_notifications_recipient_read` (`recipient_user_id`, `is_read`, `created_at`),
    INDEX `idx_notifications_created_at` (`created_at`),
    CONSTRAINT `fk_notifications_recipient`
        FOREIGN KEY (`recipient_user_id`) REFERENCES `users` (`user_id`)
        ON UPDATE RESTRICT ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
