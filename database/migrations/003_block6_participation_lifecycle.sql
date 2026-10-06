-- Block 6 integrity rule: one final participation/result record per application.
-- Existing duplicate rows must be resolved deliberately before this migration is applied.
USE `srm_event_management`;

DROP PROCEDURE IF EXISTS `ensure_block6_lifecycle_constraints`;
DELIMITER $$
CREATE PROCEDURE `ensure_block6_lifecycle_constraints`()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'achievements'
          AND INDEX_NAME = 'uq_achievements_application_id'
    ) THEN
        ALTER TABLE `achievements`
            ADD CONSTRAINT `uq_achievements_application_id` UNIQUE (`application_id`);
    END IF;
END$$
DELIMITER ;

CALL `ensure_block6_lifecycle_constraints`();
DROP PROCEDURE `ensure_block6_lifecycle_constraints`;
