-- Block 12: preserve the student's academic context at submission time.
-- Additive and intentionally nullable: existing rows remain unknown rather than
-- being backfilled from today's student profile. Review and execute manually.
USE `srm_event_management`;

DROP PROCEDURE IF EXISTS ensure_historical_analytics_context;
DELIMITER $$
CREATE PROCEDURE ensure_historical_analytics_context()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'applications'
      AND COLUMN_NAME = 'application_semester'
  ) THEN
    ALTER TABLE `applications`
      ADD COLUMN `application_semester` TINYINT UNSIGNED NULL
        COMMENT 'Semester captured at application submission; NULL means historical context unknown'
        AFTER `academic_year_id`;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'applications'
      AND COLUMN_NAME = 'application_student_year'
  ) THEN
    ALTER TABLE `applications`
      ADD COLUMN `application_student_year` TINYINT UNSIGNED NULL
        COMMENT 'Student year captured at application submission; NULL means historical context unknown'
        AFTER `application_semester`;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'student_achievements'
      AND COLUMN_NAME = 'achievement_semester'
  ) THEN
    ALTER TABLE `student_achievements`
      ADD COLUMN `achievement_semester` TINYINT UNSIGNED NULL
        COMMENT 'Semester captured at achievement submission; NULL means historical context unknown'
        AFTER `academic_year_id`;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'student_achievements'
      AND COLUMN_NAME = 'achievement_student_year'
  ) THEN
    ALTER TABLE `student_achievements`
      ADD COLUMN `achievement_student_year` TINYINT UNSIGNED NULL
        COMMENT 'Student year captured at achievement submission; NULL means historical context unknown'
        AFTER `achievement_semester`;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'applications'
      AND INDEX_NAME = 'idx_applications_academic_semester'
  ) THEN
    CREATE INDEX `idx_applications_academic_semester`
      ON `applications` (`academic_year_id`, `application_semester`);
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'student_achievements'
      AND INDEX_NAME = 'idx_student_achievements_academic_semester'
  ) THEN
    CREATE INDEX `idx_student_achievements_academic_semester`
      ON `student_achievements` (`academic_year_id`, `achievement_semester`);
  END IF;
END$$
DELIMITER ;

CALL ensure_historical_analytics_context();
DROP PROCEDURE ensure_historical_analytics_context;
