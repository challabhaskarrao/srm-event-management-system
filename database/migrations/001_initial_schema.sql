-- C1 initial schema. Contains structure only; no student or production data.
USE `srm_event_management`;

-- Identity and organization
CREATE TABLE IF NOT EXISTS `roles` (
    `role_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `role_name` VARCHAR(50) NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`role_id`),
    CONSTRAINT `uq_roles_role_name` UNIQUE (`role_name`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `users` (
    `user_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `email` VARCHAR(254) NOT NULL,
    `password_hash` VARCHAR(255) NULL,
    `role_id` BIGINT UNSIGNED NOT NULL,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `last_login_at` TIMESTAMP NULL DEFAULT NULL,
    PRIMARY KEY (`user_id`),
    CONSTRAINT `uq_users_email` UNIQUE (`email`),
    INDEX `idx_users_role_id` (`role_id`),
    CONSTRAINT `fk_users_role`
        FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `departments` (
    `department_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `department_code` VARCHAR(20) NOT NULL,
    `department_name` VARCHAR(150) NOT NULL,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`department_id`),
    CONSTRAINT `uq_departments_code` UNIQUE (`department_code`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `academic_years` (
    `academic_year_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `year_label` VARCHAR(20) NOT NULL,
    `start_date` DATE NOT NULL,
    `end_date` DATE NOT NULL,
    `is_current` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`academic_year_id`),
    CONSTRAINT `uq_academic_years_label` UNIQUE (`year_label`),
    CONSTRAINT `chk_academic_years_dates` CHECK (`end_date` >= `start_date`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `sections` (
    `section_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `department_id` BIGINT UNSIGNED NOT NULL,
    `student_year` TINYINT UNSIGNED NOT NULL,
    `section_name` VARCHAR(30) NOT NULL,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`section_id`),
    CONSTRAINT `uq_sections_department_year_name`
        UNIQUE (`department_id`, `student_year`, `section_name`),
    CONSTRAINT `chk_sections_student_year` CHECK (`student_year` BETWEEN 1 AND 6),
    CONSTRAINT `fk_sections_department`
        FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `students` (
    `student_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `register_number` VARCHAR(30) NOT NULL,
    `name` VARCHAR(150) NOT NULL,
    `department_id` BIGINT UNSIGNED NOT NULL,
    `section_id` BIGINT UNSIGNED NOT NULL,
    `academic_year_id` BIGINT UNSIGNED NOT NULL,
    `student_year` TINYINT UNSIGNED NOT NULL,
    `semester` TINYINT UNSIGNED NOT NULL,
    `batch` VARCHAR(20) NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`student_id`),
    CONSTRAINT `uq_students_user_id` UNIQUE (`user_id`),
    CONSTRAINT `uq_students_register_number` UNIQUE (`register_number`),
    INDEX `idx_students_section_id` (`section_id`),
    INDEX `idx_students_department_id` (`department_id`),
    INDEX `idx_students_academic_year_id` (`academic_year_id`),
    CONSTRAINT `chk_students_student_year` CHECK (`student_year` BETWEEN 1 AND 6),
    CONSTRAINT `chk_students_semester` CHECK (`semester` BETWEEN 1 AND 12),
    CONSTRAINT `fk_students_user`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_students_department`
        FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_students_section`
        FOREIGN KEY (`section_id`) REFERENCES `sections` (`section_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_students_academic_year`
        FOREIGN KEY (`academic_year_id`) REFERENCES `academic_years` (`academic_year_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `faculty` (
    `faculty_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `faculty_code` VARCHAR(30) NULL,
    `name` VARCHAR(150) NOT NULL,
    `department_id` BIGINT UNSIGNED NOT NULL,
    `designation` VARCHAR(100) NULL,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`faculty_id`),
    CONSTRAINT `uq_faculty_user_id` UNIQUE (`user_id`),
    CONSTRAINT `uq_faculty_code` UNIQUE (`faculty_code`),
    INDEX `idx_faculty_department_id` (`department_id`),
    CONSTRAINT `fk_faculty_user`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_faculty_department`
        FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `mentor_assignments` (
    `mentor_assignment_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `faculty_id` BIGINT UNSIGNED NOT NULL,
    `department_id` BIGINT UNSIGNED NOT NULL,
    `section_id` BIGINT UNSIGNED NOT NULL,
    `academic_year_id` BIGINT UNSIGNED NOT NULL,
    `student_year` TINYINT UNSIGNED NOT NULL,
    `active_from` DATE NOT NULL,
    `active_until` DATE NULL,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`mentor_assignment_id`),
    INDEX `idx_mentor_assignments_routing`
        (`section_id`, `academic_year_id`, `is_active`),
    INDEX `idx_mentor_assignments_faculty_id` (`faculty_id`),
    INDEX `idx_mentor_assignments_department_id` (`department_id`),
    CONSTRAINT `chk_mentor_assignments_student_year` CHECK (`student_year` BETWEEN 1 AND 6),
    CONSTRAINT `chk_mentor_assignments_dates`
        CHECK (`active_until` IS NULL OR `active_until` >= `active_from`),
    CONSTRAINT `fk_mentor_assignments_faculty`
        FOREIGN KEY (`faculty_id`) REFERENCES `faculty` (`faculty_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_mentor_assignments_department`
        FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_mentor_assignments_section`
        FOREIGN KEY (`section_id`) REFERENCES `sections` (`section_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_mentor_assignments_academic_year`
        FOREIGN KEY (`academic_year_id`) REFERENCES `academic_years` (`academic_year_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- Events and applications
CREATE TABLE IF NOT EXISTS `event_categories` (
    `category_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `category_name` VARCHAR(100) NOT NULL,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`category_id`),
    CONSTRAINT `uq_event_categories_name` UNIQUE (`category_name`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `events` (
    `event_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `event_name` VARCHAR(200) NOT NULL,
    `category_id` BIGINT UNSIGNED NOT NULL,
    `organizer` VARCHAR(200) NOT NULL,
    `start_date` DATE NOT NULL,
    `end_date` DATE NOT NULL,
    `venue` VARCHAR(255) NULL,
    `mode` VARCHAR(20) NOT NULL,
    `event_url` VARCHAR(500) NULL,
    `created_by_user_id` BIGINT UNSIGNED NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`event_id`),
    INDEX `idx_events_category_id` (`category_id`),
    INDEX `idx_events_start_date` (`start_date`),
    INDEX `idx_events_created_by_user_id` (`created_by_user_id`),
    CONSTRAINT `chk_events_dates` CHECK (`end_date` >= `start_date`),
    CONSTRAINT `chk_events_mode` CHECK (`mode` IN ('ONLINE', 'OFFLINE', 'HYBRID')),
    CONSTRAINT `fk_events_category`
        FOREIGN KEY (`category_id`) REFERENCES `event_categories` (`category_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_events_created_by_user`
        FOREIGN KEY (`created_by_user_id`) REFERENCES `users` (`user_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `applications` (
    `application_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `application_code` VARCHAR(50) NOT NULL,
    `event_id` BIGINT UNSIGNED NOT NULL,
    `created_by_student_id` BIGINT UNSIGNED NOT NULL,
    `assigned_mentor_id` BIGINT UNSIGNED NULL,
    `academic_year_id` BIGINT UNSIGNED NOT NULL,
    `participation_type` VARCHAR(20) NOT NULL,
    `travel_required` BOOLEAN NOT NULL DEFAULT FALSE,
    `outside_state` BOOLEAN NOT NULL DEFAULT FALSE,
    `current_status` VARCHAR(40) NOT NULL,
    `correction_return_stage` VARCHAR(40) NULL,
    `submitted_at` TIMESTAMP NULL DEFAULT NULL,
    `mentor_reviewed_at` TIMESTAMP NULL DEFAULT NULL,
    `hod_reviewed_at` TIMESTAMP NULL DEFAULT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`application_id`),
    CONSTRAINT `uq_applications_code` UNIQUE (`application_code`),
    INDEX `idx_applications_status` (`current_status`),
    INDEX `idx_applications_student_id` (`created_by_student_id`),
    INDEX `idx_applications_mentor_id` (`assigned_mentor_id`),
    INDEX `idx_applications_event_id` (`event_id`),
    INDEX `idx_applications_academic_year_id` (`academic_year_id`),
    CONSTRAINT `chk_applications_participation_type`
        CHECK (`participation_type` IN ('INDIVIDUAL', 'TEAM')),
    CONSTRAINT `fk_applications_event`
        FOREIGN KEY (`event_id`) REFERENCES `events` (`event_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_applications_student`
        FOREIGN KEY (`created_by_student_id`) REFERENCES `students` (`student_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_applications_mentor`
        FOREIGN KEY (`assigned_mentor_id`) REFERENCES `faculty` (`faculty_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_applications_academic_year`
        FOREIGN KEY (`academic_year_id`) REFERENCES `academic_years` (`academic_year_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `teams` (
    `team_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `application_id` BIGINT UNSIGNED NOT NULL,
    `team_name` VARCHAR(150) NOT NULL,
    `leader_student_id` BIGINT UNSIGNED NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`team_id`),
    CONSTRAINT `uq_teams_application_id` UNIQUE (`application_id`),
    INDEX `idx_teams_leader_student_id` (`leader_student_id`),
    CONSTRAINT `fk_teams_application`
        FOREIGN KEY (`application_id`) REFERENCES `applications` (`application_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_teams_leader_student`
        FOREIGN KEY (`leader_student_id`) REFERENCES `students` (`student_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `team_members` (
    `team_member_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `team_id` BIGINT UNSIGNED NOT NULL,
    `student_id` BIGINT UNSIGNED NOT NULL,
    `joined_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`team_member_id`),
    CONSTRAINT `uq_team_members_team_student` UNIQUE (`team_id`, `student_id`),
    INDEX `idx_team_members_student_id` (`student_id`),
    CONSTRAINT `fk_team_members_team`
        FOREIGN KEY (`team_id`) REFERENCES `teams` (`team_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_team_members_student`
        FOREIGN KEY (`student_id`) REFERENCES `students` (`student_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- Configurable document requirements and uploaded document metadata
CREATE TABLE IF NOT EXISTS `document_types` (
    `document_type_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `document_name` VARCHAR(120) NOT NULL,
    `document_phase` VARCHAR(20) NOT NULL,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`document_type_id`),
    CONSTRAINT `uq_document_types_name_phase` UNIQUE (`document_name`, `document_phase`),
    CONSTRAINT `chk_document_types_phase`
        CHECK (`document_phase` IN ('PRE_EVENT', 'POST_EVENT'))
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `document_rules` (
    `document_rule_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `document_type_id` BIGINT UNSIGNED NOT NULL,
    `category_id` BIGINT UNSIGNED NULL,
    `outside_state_required` BOOLEAN NULL,
    `participation_type` VARCHAR(20) NULL,
    `is_mandatory` BOOLEAN NOT NULL DEFAULT TRUE,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`document_rule_id`),
    INDEX `idx_document_rules_resolution`
        (`document_type_id`, `category_id`, `outside_state_required`, `participation_type`, `is_active`),
    INDEX `idx_document_rules_category_id` (`category_id`),
    CONSTRAINT `chk_document_rules_participation_type`
        CHECK (`participation_type` IS NULL OR `participation_type` IN ('INDIVIDUAL', 'TEAM')),
    CONSTRAINT `fk_document_rules_type`
        FOREIGN KEY (`document_type_id`) REFERENCES `document_types` (`document_type_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_document_rules_category`
        FOREIGN KEY (`category_id`) REFERENCES `event_categories` (`category_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `documents` (
    `document_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `application_id` BIGINT UNSIGNED NOT NULL,
    `document_type_id` BIGINT UNSIGNED NOT NULL,
    `uploaded_by_user_id` BIGINT UNSIGNED NOT NULL,
    `original_file_name` VARCHAR(255) NOT NULL,
    `stored_file_name` VARCHAR(255) NOT NULL,
    `file_path` VARCHAR(500) NOT NULL,
    `mime_type` VARCHAR(150) NOT NULL,
    `file_size` BIGINT UNSIGNED NOT NULL,
    `verification_status` VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    `uploaded_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `verified_by_user_id` BIGINT UNSIGNED NULL,
    `verified_at` TIMESTAMP NULL DEFAULT NULL,
    `verification_remarks` VARCHAR(1000) NULL,
    PRIMARY KEY (`document_id`),
    INDEX `idx_documents_application_id` (`application_id`),
    INDEX `idx_documents_document_type_id` (`document_type_id`),
    INDEX `idx_documents_uploaded_by_user_id` (`uploaded_by_user_id`),
    INDEX `idx_documents_verified_by_user_id` (`verified_by_user_id`),
    CONSTRAINT `chk_documents_verification_status`
        CHECK (`verification_status` IN ('PENDING', 'VERIFIED', 'REJECTED', 'RESUBMISSION_REQUIRED')),
    CONSTRAINT `fk_documents_application`
        FOREIGN KEY (`application_id`) REFERENCES `applications` (`application_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_documents_type`
        FOREIGN KEY (`document_type_id`) REFERENCES `document_types` (`document_type_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_documents_uploaded_by_user`
        FOREIGN KEY (`uploaded_by_user_id`) REFERENCES `users` (`user_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_documents_verified_by_user`
        FOREIGN KEY (`verified_by_user_id`) REFERENCES `users` (`user_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- Review history, achievements, audit history, and reset-token structure
CREATE TABLE IF NOT EXISTS `approval_actions` (
    `approval_action_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `application_id` BIGINT UNSIGNED NOT NULL,
    `approver_user_id` BIGINT UNSIGNED NOT NULL,
    `approval_level` VARCHAR(20) NOT NULL,
    `action` VARCHAR(40) NOT NULL,
    `previous_status` VARCHAR(40) NULL,
    `new_status` VARCHAR(40) NOT NULL,
    `remarks` VARCHAR(1000) NULL,
    `action_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`approval_action_id`),
    INDEX `idx_approval_actions_application_id` (`application_id`),
    INDEX `idx_approval_actions_approver_user_id` (`approver_user_id`),
    CONSTRAINT `chk_approval_actions_level` CHECK (`approval_level` IN ('MENTOR', 'HOD')),
    CONSTRAINT `fk_approval_actions_application`
        FOREIGN KEY (`application_id`) REFERENCES `applications` (`application_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_approval_actions_approver_user`
        FOREIGN KEY (`approver_user_id`) REFERENCES `users` (`user_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `achievements` (
    `achievement_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `application_id` BIGINT UNSIGNED NOT NULL,
    `result_type` VARCHAR(30) NOT NULL,
    `position` VARCHAR(100) NULL,
    `prize_details` VARCHAR(500) NULL,
    `verification_status` VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    `submitted_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `verified_by_user_id` BIGINT UNSIGNED NULL,
    `verified_at` TIMESTAMP NULL DEFAULT NULL,
    `remarks` VARCHAR(1000) NULL,
    PRIMARY KEY (`achievement_id`),
    INDEX `idx_achievements_application_id` (`application_id`),
    INDEX `idx_achievements_verified_by_user_id` (`verified_by_user_id`),
    CONSTRAINT `chk_achievements_result_type`
        CHECK (`result_type` IN ('PARTICIPANT', 'FINALIST', 'WINNER', 'RUNNER_UP', 'POSITION', 'OTHER')),
    CONSTRAINT `fk_achievements_application`
        FOREIGN KEY (`application_id`) REFERENCES `applications` (`application_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_achievements_verified_by_user`
        FOREIGN KEY (`verified_by_user_id`) REFERENCES `users` (`user_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `audit_logs` (
    `audit_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `application_id` BIGINT UNSIGNED NULL,
    `entity_type` VARCHAR(100) NOT NULL,
    `entity_id` BIGINT UNSIGNED NOT NULL,
    `action` VARCHAR(100) NOT NULL,
    `old_value` JSON NULL,
    `new_value` JSON NULL,
    `remarks` VARCHAR(1000) NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`audit_id`),
    INDEX `idx_audit_logs_application_id` (`application_id`),
    INDEX `idx_audit_logs_user_id` (`user_id`),
    INDEX `idx_audit_logs_entity` (`entity_type`, `entity_id`),
    CONSTRAINT `fk_audit_logs_user`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_audit_logs_application`
        FOREIGN KEY (`application_id`) REFERENCES `applications` (`application_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `password_reset_tokens` (
    `token_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `token_hash` VARCHAR(255) NOT NULL,
    `expires_at` TIMESTAMP NOT NULL,
    `used_at` TIMESTAMP NULL DEFAULT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`token_id`),
    CONSTRAINT `uq_password_reset_tokens_hash` UNIQUE (`token_hash`),
    INDEX `idx_password_reset_tokens_user_id` (`user_id`),
    INDEX `idx_password_reset_tokens_expires_at` (`expires_at`),
    CONSTRAINT `fk_password_reset_tokens_user`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
