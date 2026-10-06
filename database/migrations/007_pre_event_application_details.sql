-- Additive pre-event application details. Existing events/applications remain intact.
USE `srm_event_management`;

DELIMITER $$
CREATE PROCEDURE ensure_pre_event_application_details()
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='events' AND column_name='city') THEN
    ALTER TABLE events ADD COLUMN city VARCHAR(120) NULL AFTER venue;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='events' AND column_name='state') THEN
    ALTER TABLE events ADD COLUMN state VARCHAR(120) NULL AFTER city;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='events' AND column_name='country') THEN
    ALTER TABLE events ADD COLUMN country VARCHAR(120) NULL AFTER state;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='events' AND column_name='other_category') THEN
    ALTER TABLE events ADD COLUMN other_category VARCHAR(150) NULL AFTER category_id;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='events' AND column_name='registration_fee_required') THEN
    ALTER TABLE events ADD COLUMN registration_fee_required BOOLEAN NOT NULL DEFAULT FALSE AFTER description;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='events' AND column_name='registration_fee_amount') THEN
    ALTER TABLE events ADD COLUMN registration_fee_amount DECIMAL(10,2) NULL AFTER registration_fee_required;
  END IF;

  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='applications' AND column_name='destination_city') THEN
    ALTER TABLE applications ADD COLUMN destination_city VARCHAR(120) NULL AFTER outside_state;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='applications' AND column_name='destination_state') THEN
    ALTER TABLE applications ADD COLUMN destination_state VARCHAR(120) NULL AFTER destination_city;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='applications' AND column_name='destination_country') THEN
    ALTER TABLE applications ADD COLUMN destination_country VARCHAR(120) NULL AFTER destination_state;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='applications' AND column_name='venue_address') THEN
    ALTER TABLE applications ADD COLUMN venue_address VARCHAR(255) NULL AFTER destination_country;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='applications' AND column_name='mode_of_travel') THEN
    ALTER TABLE applications ADD COLUMN mode_of_travel VARCHAR(40) NULL AFTER venue_address;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='applications' AND column_name='departure_at') THEN
    ALTER TABLE applications ADD COLUMN departure_at DATETIME NULL AFTER mode_of_travel;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='applications' AND column_name='return_at') THEN
    ALTER TABLE applications ADD COLUMN return_at DATETIME NULL AFTER departure_at;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='applications' AND column_name='accommodation_required') THEN
    ALTER TABLE applications ADD COLUMN accommodation_required BOOLEAN NOT NULL DEFAULT FALSE AFTER return_at;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='applications' AND column_name='accommodation_name') THEN
    ALTER TABLE applications ADD COLUMN accommodation_name VARCHAR(150) NULL AFTER accommodation_required;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='applications' AND column_name='accommodation_address') THEN
    ALTER TABLE applications ADD COLUMN accommodation_address VARCHAR(255) NULL AFTER accommodation_name;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='applications' AND column_name='check_in_date') THEN
    ALTER TABLE applications ADD COLUMN check_in_date DATE NULL AFTER accommodation_address;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='applications' AND column_name='check_out_date') THEN
    ALTER TABLE applications ADD COLUMN check_out_date DATE NULL AFTER check_in_date;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='applications' AND column_name='emergency_contact_name') THEN
    ALTER TABLE applications ADD COLUMN emergency_contact_name VARCHAR(150) NULL AFTER check_out_date;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='applications' AND column_name='emergency_contact_number') THEN
    ALTER TABLE applications ADD COLUMN emergency_contact_number VARCHAR(30) NULL AFTER emergency_contact_name;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='applications' AND column_name='guardian_contact_number') THEN
    ALTER TABLE applications ADD COLUMN guardian_contact_number VARCHAR(30) NULL AFTER emergency_contact_number;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='applications' AND column_name='travel_remarks') THEN
    ALTER TABLE applications ADD COLUMN travel_remarks VARCHAR(1000) NULL AFTER guardian_contact_number;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='applications' AND column_name='registration_fee_paid') THEN
    ALTER TABLE applications ADD COLUMN registration_fee_paid BOOLEAN NOT NULL DEFAULT FALSE AFTER travel_remarks;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='applications' AND column_name='registration_fee_amount') THEN
    ALTER TABLE applications ADD COLUMN registration_fee_amount DECIMAL(10,2) NULL AFTER registration_fee_paid;
  END IF;
END$$
DELIMITER ;
CALL ensure_pre_event_application_details();
DROP PROCEDURE ensure_pre_event_application_details;

INSERT IGNORE INTO document_types (document_name, document_phase, is_active) VALUES
 ('FEE_RECEIPT','PRE_EVENT',TRUE),
 ('TRAVEL_PROOF','PRE_EVENT',TRUE),
 ('ACCOMMODATION_CONFIRMATION','PRE_EVENT',TRUE),
 ('PARENT_CONSENT','PRE_EVENT',TRUE),
 ('ADDITIONAL_SUPPORTING_DOCUMENT','PRE_EVENT',TRUE);
