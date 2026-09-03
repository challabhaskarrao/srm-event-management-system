-- Fresh local setup entry point for the SRM Event Management System database.
-- Run this file from the repository root with the MySQL command-line client.

CREATE DATABASE IF NOT EXISTS `srm_event_management`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE `srm_event_management`;

-- The initial migration is the single canonical copy of the table definitions.
SOURCE database/migrations/001_initial_schema.sql;
