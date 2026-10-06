-- Add the reviewer fields used by the application review workflow.
-- This migration is additive and safe to re-run on MySQL 8.4.
USE `srm_event_management`;

DROP PROCEDURE IF EXISTS `ensure_application_review_constraints`;
DELIMITER $$
CREATE PROCEDURE `ensure_application_review_constraints`()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'applications' AND COLUMN_NAME = 'hod_id'
    ) THEN
        ALTER TABLE `applications`
            ADD COLUMN `hod_id` BIGINT UNSIGNED NULL AFTER `assigned_mentor_id`;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'applications' AND COLUMN_NAME = 'mentor_remarks'
    ) THEN
        ALTER TABLE `applications`
            ADD COLUMN `mentor_remarks` TEXT NULL AFTER `correction_return_stage`;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'applications' AND COLUMN_NAME = 'hod_remarks'
    ) THEN
        ALTER TABLE `applications`
            ADD COLUMN `hod_remarks` TEXT NULL AFTER `mentor_remarks`;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'applications'
          AND INDEX_NAME = 'idx_applications_hod_id'
    ) THEN
        ALTER TABLE `applications`
            ADD INDEX `idx_applications_hod_id` (`hod_id`);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.REFERENTIAL_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = DATABASE()
          AND TABLE_NAME = 'applications'
          AND CONSTRAINT_NAME = 'fk_applications_hod'
    ) THEN
        ALTER TABLE `applications`
            ADD CONSTRAINT `fk_applications_hod`
            FOREIGN KEY (`hod_id`) REFERENCES `faculty` (`faculty_id`)
            ON UPDATE RESTRICT ON DELETE RESTRICT;
    END IF;
END$$
DELIMITER ;

CALL `ensure_application_review_constraints`();
DROP PROCEDURE `ensure_application_review_constraints`;
