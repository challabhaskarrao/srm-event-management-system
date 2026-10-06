-- Block 8: controlled HOD event/achievement types and type-specific application metadata.
-- Additive only. This migration is intentionally not executed by the application.
USE `srm_event_management`;

CREATE TABLE IF NOT EXISTS `event_types` (
  `event_type_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(80) NOT NULL,
  `name` VARCHAR(180) NOT NULL,
  `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
  `sort_order` INT NOT NULL DEFAULT 0,
  `config_json` JSON NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`event_type_id`),
  UNIQUE KEY `uq_event_types_code` (`code`),
  KEY `idx_event_types_active_order` (`is_active`, `sort_order`)
) ENGINE=InnoDB;

DROP PROCEDURE IF EXISTS `ensure_event_type_application_columns`;
DELIMITER $$
CREATE PROCEDURE `ensure_event_type_application_columns`()
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='events' AND COLUMN_NAME='event_type_id') THEN
    ALTER TABLE `events` ADD COLUMN `event_type_id` BIGINT UNSIGNED NULL AFTER `category_id`;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='events' AND COLUMN_NAME='type_specific_details') THEN
    ALTER TABLE `events` ADD COLUMN `type_specific_details` JSON NULL AFTER `description`;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='events' AND INDEX_NAME='idx_events_event_type') THEN
    CREATE INDEX `idx_events_event_type` ON `events` (`event_type_id`);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA=DATABASE() AND TABLE_NAME='events' AND CONSTRAINT_NAME='fk_events_event_type') THEN
    ALTER TABLE `events` ADD CONSTRAINT `fk_events_event_type` FOREIGN KEY (`event_type_id`) REFERENCES `event_types` (`event_type_id`);
  END IF;
END$$
DELIMITER ;
CALL `ensure_event_type_application_columns`();
DROP PROCEDURE `ensure_event_type_application_columns`;

INSERT INTO `event_types` (`code`, `name`, `is_active`, `sort_order`, `config_json`) VALUES
 ('HACKATHON','Hackathon',TRUE,10,'{"kind":"EVENT","team":true,"travel":true,"documents":["EVENT_POSTER","REGISTRATION_PROOF","INVITATION","FEE_RECEIPT","ADDITIONAL_SUPPORTING_DOCUMENT"]}'),
 ('DUAL_DEGREE_OTHER_UNIVERSITY','Dual Degree from other University',TRUE,20,'{"kind":"DUAL_DEGREE","team":false,"travel":false,"documents":["OFFER_LETTER","ADDITIONAL_SUPPORTING_DOCUMENT"]}'),
 ('PATENT','Patent',TRUE,30,'{"kind":"PATENT","team":true,"travel":false,"documents":["PATENT_SUPPORTING_DOCUMENT","ADDITIONAL_SUPPORTING_DOCUMENT"]}'),
 ('CONFERENCE_PUBLICATION','Conference Publications (National & International)',TRUE,40,'{"kind":"PUBLICATION","team":true,"travel":true,"documents":["REGISTRATION_PROOF","INVITATION","ADDITIONAL_SUPPORTING_DOCUMENT"]}'),
 ('GLOBAL_CERTIFICATION','Global Certifications',TRUE,50,'{"kind":"COURSE","team":false,"travel":false,"documents":["ENROLLMENT_CONFIRMATION","ADDITIONAL_SUPPORTING_DOCUMENT"]}'),
 ('PAPER_PRESENTATION','Paper Presentation',TRUE,60,'{"kind":"PUBLICATION","team":true,"travel":true,"documents":["EVENT_POSTER","REGISTRATION_PROOF","INVITATION","ADDITIONAL_SUPPORTING_DOCUMENT"]}'),
 ('STARTUP','Startups',TRUE,70,'{"kind":"STARTUP","team":true,"travel":false,"documents":["ADDITIONAL_SUPPORTING_DOCUMENT"]}'),
 ('PROJECT_EXPO','Project Expo',TRUE,80,'{"kind":"EVENT","team":true,"travel":true,"documents":["EVENT_POSTER","REGISTRATION_PROOF","INVITATION","ADDITIONAL_SUPPORTING_DOCUMENT"]}'),
 ('NPTEL_COURSE','NPTEL Course',TRUE,90,'{"kind":"COURSE","team":false,"travel":false,"documents":["ENROLLMENT_CONFIRMATION","ADDITIONAL_SUPPORTING_DOCUMENT"]}'),
 ('COMMUNITY_CONNECT','Community Connect',TRUE,100,'{"kind":"EVENT","team":true,"travel":true,"documents":["EVENT_POSTER","REGISTRATION_PROOF","ADDITIONAL_SUPPORTING_DOCUMENT"]}'),
 ('STUDENT_DEVELOPMENT_PROGRAM','SDP (Student Development Program)',TRUE,110,'{"kind":"EVENT","team":true,"travel":true,"documents":["EVENT_POSTER","REGISTRATION_PROOF","ADDITIONAL_SUPPORTING_DOCUMENT"]}'),
 ('WORKSHOP','Workshop',TRUE,120,'{"kind":"EVENT","team":true,"travel":true,"documents":["EVENT_POSTER","REGISTRATION_PROOF","INVITATION","FEE_RECEIPT","ADDITIONAL_SUPPORTING_DOCUMENT"]}'),
 ('SYMPOSIUM','Symposium',TRUE,130,'{"kind":"EVENT","team":true,"travel":true,"documents":["EVENT_POSTER","REGISTRATION_PROOF","INVITATION","ADDITIONAL_SUPPORTING_DOCUMENT"]}'),
 ('WEBINAR','Webinar',TRUE,140,'{"kind":"EVENT","team":false,"travel":false,"documents":["REGISTRATION_PROOF","ADDITIONAL_SUPPORTING_DOCUMENT"]}'),
 ('ONLINE_COURSE_GT_20_HOURS','Online Courses (Participation More than 20 hours)',TRUE,150,'{"kind":"COURSE","team":false,"travel":false,"documents":["ENROLLMENT_CONFIRMATION","ADDITIONAL_SUPPORTING_DOCUMENT"]}'),
 ('INTERNSHIP_STIPEND_ONLINE','Internship with Stipend (Online)',TRUE,160,'{"kind":"INTERNSHIP","team":false,"travel":false,"mode":"ONLINE","stipend":true,"documents":["OFFER_LETTER","ADDITIONAL_SUPPORTING_DOCUMENT"]}'),
 ('INTERNSHIP_STIPEND_OFFLINE','Internship with Stipend (Offline)',TRUE,170,'{"kind":"INTERNSHIP","team":false,"travel":true,"mode":"OFFLINE","stipend":true,"documents":["OFFER_LETTER","ADDITIONAL_SUPPORTING_DOCUMENT"]}'),
 ('INTERNSHIP_NO_STIPEND_ONLINE','Internship without Stipend (Online)',TRUE,180,'{"kind":"INTERNSHIP","team":false,"travel":false,"mode":"ONLINE","stipend":false,"documents":["OFFER_LETTER","ADDITIONAL_SUPPORTING_DOCUMENT"]}'),
 ('INTERNSHIP_NO_STIPEND_OFFLINE','Internship without Stipend (Offline)',TRUE,190,'{"kind":"INTERNSHIP","team":false,"travel":true,"mode":"OFFLINE","stipend":false,"documents":["OFFER_LETTER","ADDITIONAL_SUPPORTING_DOCUMENT"]}')
ON DUPLICATE KEY UPDATE
  `name`=VALUES(`name`), `sort_order`=VALUES(`sort_order`), `config_json`=VALUES(`config_json`);

INSERT IGNORE INTO `document_types` (`document_name`, `document_phase`, `is_active`) VALUES
 ('OFFER_LETTER','PRE_EVENT',TRUE),
 ('ENROLLMENT_CONFIRMATION','PRE_EVENT',TRUE),
 ('PATENT_SUPPORTING_DOCUMENT','PRE_EVENT',TRUE),
 ('TRAVEL_TICKET','PRE_EVENT',TRUE),
 ('PARENT_CONSENT','PRE_EVENT',TRUE);
