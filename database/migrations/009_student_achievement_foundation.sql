-- Block 9 foundation: standalone Student achievement records.
-- Additive only. Review this migration and execute it manually when approved.
USE `srm_event_management`;

DROP PROCEDURE IF EXISTS ensure_student_achievement_foundation;
DELIMITER $$
CREATE PROCEDURE ensure_student_achievement_foundation()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='event_types' AND COLUMN_NAME='workflow_category'
  ) THEN
    ALTER TABLE `event_types`
      ADD COLUMN `workflow_category` VARCHAR(20) NOT NULL DEFAULT 'ACTIVITY' AFTER `name`;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='documents' AND COLUMN_NAME='achievement_id'
  ) THEN
    ALTER TABLE `documents`
      MODIFY COLUMN `application_id` BIGINT UNSIGNED NULL,
      ADD COLUMN `achievement_id` BIGINT UNSIGNED NULL AFTER `application_id`;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='documents' AND INDEX_NAME='idx_documents_achievement_id'
  ) THEN
    CREATE INDEX `idx_documents_achievement_id` ON `documents` (`achievement_id`);
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA=DATABASE() AND TABLE_NAME='documents' AND CONSTRAINT_NAME='fk_documents_achievement'
  ) THEN
    ALTER TABLE `documents`
      ADD CONSTRAINT `fk_documents_achievement`
      FOREIGN KEY (`achievement_id`) REFERENCES `student_achievements` (`achievement_id`)
      ON UPDATE RESTRICT ON DELETE RESTRICT;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA=DATABASE() AND TABLE_NAME='documents' AND CONSTRAINT_NAME='chk_documents_single_owner'
  ) THEN
    ALTER TABLE `documents`
      ADD CONSTRAINT `chk_documents_single_owner`
      CHECK ((`application_id` IS NOT NULL AND `achievement_id` IS NULL)
          OR (`application_id` IS NULL AND `achievement_id` IS NOT NULL));
  END IF;
END$$
DELIMITER ;

CREATE TABLE IF NOT EXISTS `student_achievements` (
  `achievement_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `student_id` BIGINT UNSIGNED NOT NULL,
  `event_type_id` BIGINT UNSIGNED NOT NULL,
  `academic_year_id` BIGINT UNSIGNED NOT NULL,
  `assigned_mentor_id` BIGINT UNSIGNED NOT NULL,
  `title` VARCHAR(220) NOT NULL,
  `organization` VARCHAR(220) NOT NULL,
  `description` TEXT NULL,
  `start_date` DATE NULL,
  `end_date` DATE NULL,
  `official_url` VARCHAR(500) NULL,
  `participation_type` VARCHAR(20) NOT NULL DEFAULT 'INDIVIDUAL',
  `type_specific_details` JSON NULL,
  `verification_status` VARCHAR(40) NOT NULL DEFAULT 'PENDING_MENTOR_VERIFICATION',
  `mentor_remarks` TEXT NULL,
  `hod_remarks` TEXT NULL,
  `mentor_reviewed_by_user_id` BIGINT UNSIGNED NULL,
  `mentor_reviewed_at` TIMESTAMP NULL DEFAULT NULL,
  `hod_reviewed_by_user_id` BIGINT UNSIGNED NULL,
  `hod_reviewed_at` TIMESTAMP NULL DEFAULT NULL,
  `submitted_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`achievement_id`),
  INDEX `idx_student_achievements_student` (`student_id`, `created_at`),
  INDEX `idx_student_achievements_mentor_queue` (`assigned_mentor_id`, `verification_status`, `created_at`),
  INDEX `idx_student_achievements_type_status` (`event_type_id`, `verification_status`),
  INDEX `idx_student_achievements_academic_year` (`academic_year_id`, `verification_status`),
  CONSTRAINT `chk_student_achievements_participation_type`
    CHECK (`participation_type` IN ('INDIVIDUAL', 'TEAM')),
  CONSTRAINT `chk_student_achievements_status`
    CHECK (`verification_status` IN ('PENDING_MENTOR_VERIFICATION', 'PENDING_HOD_VERIFICATION', 'VERIFIED', 'REJECTED', 'RESUBMISSION_REQUIRED')),
  CONSTRAINT `chk_student_achievements_dates`
    CHECK (`end_date` IS NULL OR `start_date` IS NULL OR `end_date` >= `start_date`),
  CONSTRAINT `fk_student_achievements_student`
    FOREIGN KEY (`student_id`) REFERENCES `students` (`student_id`)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT `fk_student_achievements_event_type`
    FOREIGN KEY (`event_type_id`) REFERENCES `event_types` (`event_type_id`)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT `fk_student_achievements_academic_year`
    FOREIGN KEY (`academic_year_id`) REFERENCES `academic_years` (`academic_year_id`)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT `fk_student_achievements_mentor`
    FOREIGN KEY (`assigned_mentor_id`) REFERENCES `faculty` (`faculty_id`)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT `fk_student_achievements_mentor_reviewer`
    FOREIGN KEY (`mentor_reviewed_by_user_id`) REFERENCES `users` (`user_id`)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT `fk_student_achievements_hod_reviewer`
    FOREIGN KEY (`hod_reviewed_by_user_id`) REFERENCES `users` (`user_id`)
    ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `achievement_teams` (
  `achievement_team_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `achievement_id` BIGINT UNSIGNED NOT NULL,
  `team_name` VARCHAR(150) NOT NULL,
  `leader_student_id` BIGINT UNSIGNED NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`achievement_team_id`),
  UNIQUE KEY `uq_achievement_teams_achievement` (`achievement_id`),
  CONSTRAINT `fk_achievement_teams_achievement`
    FOREIGN KEY (`achievement_id`) REFERENCES `student_achievements` (`achievement_id`)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT `fk_achievement_teams_leader`
    FOREIGN KEY (`leader_student_id`) REFERENCES `students` (`student_id`)
    ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `achievement_team_members` (
  `achievement_team_member_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `achievement_team_id` BIGINT UNSIGNED NOT NULL,
  `student_id` BIGINT UNSIGNED NOT NULL,
  `joined_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`achievement_team_member_id`),
  UNIQUE KEY `uq_achievement_team_members` (`achievement_team_id`, `student_id`),
  INDEX `idx_achievement_team_members_student` (`student_id`),
  CONSTRAINT `fk_achievement_team_members_team`
    FOREIGN KEY (`achievement_team_id`) REFERENCES `achievement_teams` (`achievement_team_id`)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT `fk_achievement_team_members_student`
    FOREIGN KEY (`student_id`) REFERENCES `students` (`student_id`)
    ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CALL ensure_student_achievement_foundation();
DROP PROCEDURE ensure_student_achievement_foundation;

UPDATE `event_types`
SET `workflow_category` = CASE `code`
  WHEN 'HACKATHON' THEN 'ACTIVITY'
  WHEN 'PAPER_PRESENTATION' THEN 'ACTIVITY'
  WHEN 'PROJECT_EXPO' THEN 'ACTIVITY'
  WHEN 'WORKSHOP' THEN 'ACTIVITY'
  WHEN 'SYMPOSIUM' THEN 'ACTIVITY'
  WHEN 'COMMUNITY_CONNECT' THEN 'ACTIVITY'
  WHEN 'STUDENT_DEVELOPMENT_PROGRAM' THEN 'ACTIVITY'
  WHEN 'WEBINAR' THEN 'ACTIVITY'
  ELSE 'ACHIEVEMENT'
END;

