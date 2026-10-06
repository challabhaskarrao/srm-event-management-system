-- Block 7: revocable JWT session generation for password-change/reset safety.
-- Existing accounts and data are preserved; current sessions start at version 0.
USE `srm_event_management`;

DROP PROCEDURE IF EXISTS `ensure_block7_account_security`;
DELIMITER $$
CREATE PROCEDURE `ensure_block7_account_security`()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'users'
          AND COLUMN_NAME = 'session_version'
    ) THEN
        ALTER TABLE `users`
            ADD COLUMN `session_version` BIGINT UNSIGNED NOT NULL DEFAULT 0
            AFTER `last_login_at`;
    END IF;
END$$
DELIMITER ;

CALL `ensure_block7_account_security`();
DROP PROCEDURE `ensure_block7_account_security`;
