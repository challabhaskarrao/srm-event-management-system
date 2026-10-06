-- Idempotent Block 6 document vocabulary derived from the documented workflow.
-- Rules remain configurable and are intentionally not fabricated here.
USE `srm_event_management`;

INSERT INTO `document_types` (`document_name`, `document_phase`, `is_active`) VALUES
    ('Supporting Document', 'PRE_EVENT', TRUE),
    ('Participation Proof', 'POST_EVENT', TRUE),
    ('Result Proof', 'POST_EVENT', TRUE)
ON DUPLICATE KEY UPDATE `is_active` = VALUES(`is_active`);
