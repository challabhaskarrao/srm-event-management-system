-- Additive workflow evolution: legacy admin-created events and applications remain intact.
USE `srm_event_management`;

CREATE TABLE IF NOT EXISTS `student_registry` (
  `registry_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `registration_number` VARCHAR(30) NOT NULL,
  `department_id` BIGINT UNSIGNED NOT NULL,
  `section_id` BIGINT UNSIGNED NOT NULL,
  `academic_year_id` BIGINT UNSIGNED NOT NULL,
  `student_year` TINYINT UNSIGNED NOT NULL,
  `semester` TINYINT UNSIGNED NOT NULL,
  `batch` VARCHAR(20) NOT NULL,
  `is_eligible` BOOLEAN NOT NULL DEFAULT TRUE,
  `claimed_user_id` BIGINT UNSIGNED NULL,
  `claimed_email` VARCHAR(254) NULL,
  `email_verified_at` TIMESTAMP NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`registry_id`), UNIQUE KEY `uq_student_registry_registration` (`registration_number`),
  UNIQUE KEY `uq_student_registry_claimed_user` (`claimed_user_id`),
  KEY `idx_student_registry_routing` (`department_id`,`section_id`,`academic_year_id`,`is_eligible`),
  CONSTRAINT `fk_registry_department` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`),
  CONSTRAINT `fk_registry_section` FOREIGN KEY (`section_id`) REFERENCES `sections` (`section_id`),
  CONSTRAINT `fk_registry_academic_year` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_years` (`academic_year_id`),
  CONSTRAINT `fk_registry_user` FOREIGN KEY (`claimed_user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB;

DROP PROCEDURE IF EXISTS `ensure_student_initiated_participation`;
DELIMITER $$
CREATE PROCEDURE `ensure_student_initiated_participation`()
BEGIN
 IF NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='events' AND COLUMN_NAME='description') THEN ALTER TABLE `events` ADD COLUMN `description` VARCHAR(2000) NULL AFTER `event_url`; END IF;
 IF NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='events' AND COLUMN_NAME='is_student_initiated') THEN ALTER TABLE `events` ADD COLUMN `is_student_initiated` BOOLEAN NOT NULL DEFAULT FALSE AFTER `description`; END IF;
 IF NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='applications' AND COLUMN_NAME='participation_status') THEN ALTER TABLE `applications` ADD COLUMN `participation_status` VARCHAR(40) NOT NULL DEFAULT 'NOT_YET_OCCURRED' AFTER `current_status`; END IF;
 IF NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='applications' AND COLUMN_NAME='post_event_reported_at') THEN ALTER TABLE `applications` ADD COLUMN `post_event_reported_at` TIMESTAMP NULL AFTER `hod_reviewed_at`; END IF;
 IF NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='applications' AND COLUMN_NAME='outcome_notes') THEN ALTER TABLE `applications` ADD COLUMN `outcome_notes` VARCHAR(2000) NULL AFTER `post_event_reported_at`; END IF;
 IF NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='applications' AND COLUMN_NAME='result_url') THEN ALTER TABLE `applications` ADD COLUMN `result_url` VARCHAR(500) NULL AFTER `outcome_notes`; END IF;
 IF NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='applications' AND INDEX_NAME='idx_applications_participation_status') THEN CREATE INDEX `idx_applications_participation_status` ON `applications` (`participation_status`); END IF;
END$$
DELIMITER ;
CALL `ensure_student_initiated_participation`();
DROP PROCEDURE `ensure_student_initiated_participation`;

INSERT IGNORE INTO `event_categories` (`category_name`,`is_active`) VALUES
('HACKATHON',TRUE),('WORKSHOP',TRUE),('SEMINAR',TRUE),('CONFERENCE',TRUE),('COMPETITION',TRUE),('TECHNICAL_EVENT',TRUE),('CULTURAL_EVENT',TRUE),('SPORTS',TRUE),('PAPER_PRESENTATION',TRUE),('PROJECT_COMPETITION',TRUE),('TRAINING',TRUE),('INDUSTRIAL_VISIT',TRUE),('SYMPOSIUM',TRUE),('OTHER',TRUE);
INSERT IGNORE INTO `document_types` (`document_name`,`document_phase`,`is_active`) VALUES
('EVENT_POSTER','PRE_EVENT',TRUE),('BROCHURE','PRE_EVENT',TRUE),('INVITATION','PRE_EVENT',TRUE),('REGISTRATION_PROOF','PRE_EVENT',TRUE),('PARTICIPATION_CERTIFICATE','POST_EVENT',TRUE),('RESULT_PROOF','POST_EVENT',TRUE),('EVENT_PHOTO','POST_EVENT',TRUE);
