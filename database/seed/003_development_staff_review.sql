-- Local-development staff credentials and review routing only.
-- The BCrypt value is for the documented local development credential.
-- Do not use this seed or credential in production.
USE `srm_event_management`;

UPDATE `users` u
JOIN `roles` r ON r.`role_id` = u.`role_id`
SET u.`password_hash` = '$2a$10$YursfnBh/lPdtD6wl4eiU.4NgFSA5H/0ANdUpHvjCq7FHTBA4/Hhu',
    u.`is_active` = TRUE
WHERE u.`email` IN ('mentor.cse@srmist.edu.in', 'hod.cse@srmist.edu.in')
  AND r.`role_name` IN ('MENTOR', 'HOD');

INSERT INTO `mentor_assignments` (
    `faculty_id`, `department_id`, `section_id`, `academic_year_id`,
    `student_year`, `active_from`, `active_until`, `is_active`
)
SELECT
    f.`faculty_id`, s.`department_id`, s.`section_id`, s.`academic_year_id`,
    s.`student_year`, CURRENT_DATE, NULL, TRUE
FROM `faculty` f
JOIN `users` u ON u.`user_id` = f.`user_id`
JOIN `roles` r ON r.`role_id` = u.`role_id` AND r.`role_name` = 'MENTOR'
JOIN `students` s ON s.`department_id` = f.`department_id`
WHERE u.`email` = 'mentor.cse@srmist.edu.in'
  AND NOT EXISTS (
      SELECT 1
      FROM `mentor_assignments` ma
      WHERE ma.`faculty_id` = f.`faculty_id`
        AND ma.`department_id` = s.`department_id`
        AND ma.`section_id` = s.`section_id`
        AND ma.`academic_year_id` = s.`academic_year_id`
        AND ma.`student_year` = s.`student_year`
        AND ma.`is_active` = TRUE
        AND (ma.`active_until` IS NULL OR ma.`active_until` >= CURRENT_DATE)
  );
