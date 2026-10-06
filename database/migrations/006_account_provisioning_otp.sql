-- Account provisioning: registry claims and purpose-bound email OTP challenges.
-- This migration is additive and does not create login accounts from master data.
DELIMITER $$
CREATE PROCEDURE ensure_account_provisioning_schema()
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='student_registry' AND column_name='student_name') THEN
    ALTER TABLE student_registry ADD COLUMN student_name VARCHAR(150) NULL AFTER registration_number;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='users' AND column_name='email_verified') THEN
    ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE AFTER is_active;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='faculty_registry') THEN
    CREATE TABLE faculty_registry (
      registry_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
      faculty_code VARCHAR(30) NOT NULL,
      faculty_name VARCHAR(150) NULL,
      department_id BIGINT UNSIGNED NOT NULL,
      role_name VARCHAR(50) NOT NULL,
      is_eligible BOOLEAN NOT NULL DEFAULT TRUE,
      claimed_user_id BIGINT UNSIGNED NULL,
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      PRIMARY KEY (registry_id),
      UNIQUE KEY uq_faculty_registry_code (faculty_code),
      UNIQUE KEY uq_faculty_registry_claimed_user (claimed_user_id),
      KEY idx_faculty_registry_eligibility (department_id, role_name, is_eligible),
      CONSTRAINT fk_faculty_registry_department FOREIGN KEY (department_id) REFERENCES departments(department_id),
      CONSTRAINT fk_faculty_registry_user FOREIGN KEY (claimed_user_id) REFERENCES users(user_id)
    ) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='account_otp_challenges') THEN
    CREATE TABLE account_otp_challenges (
      challenge_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
      purpose VARCHAR(40) NOT NULL,
      registry_type VARCHAR(20) NOT NULL,
      registry_id BIGINT UNSIGNED NOT NULL,
      pending_user_id BIGINT UNSIGNED NOT NULL,
      email VARCHAR(254) NOT NULL,
      otp_hash CHAR(64) NOT NULL,
      expires_at TIMESTAMP NOT NULL,
      attempts TINYINT UNSIGNED NOT NULL DEFAULT 0,
      consumed_at TIMESTAMP NULL,
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (challenge_id),
      UNIQUE KEY uq_account_otp_hash (otp_hash),
      KEY idx_account_otp_lookup (purpose, registry_type, registry_id, pending_user_id, expires_at),
      CONSTRAINT fk_account_otp_user FOREIGN KEY (pending_user_id) REFERENCES users(user_id)
    ) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
  END IF;
END$$
DELIMITER ;
CALL ensure_account_provisioning_schema();
DROP PROCEDURE ensure_account_provisioning_schema;
