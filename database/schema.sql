-- Fresh local setup entry point for the SRM Event Management System database.
-- Run this file from the repository root with the MySQL command-line client.

CREATE DATABASE IF NOT EXISTS `srm_event_management`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE `srm_event_management`;

-- The initial migration is the single canonical copy of the table definitions.
SOURCE database/migrations/001_initial_schema.sql;
SOURCE database/migrations/002_application_review_fields.sql;
SOURCE database/migrations/003_block6_participation_lifecycle.sql;
SOURCE database/migrations/004_account_security.sql;
SOURCE database/migrations/005_student_initiated_participation.sql;
SOURCE database/migrations/006_account_provisioning_otp.sql;
SOURCE database/migrations/007_pre_event_application_details.sql;
SOURCE database/migrations/008_event_type_master.sql;
SOURCE database/migrations/009_student_achievement_foundation.sql;
