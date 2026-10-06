package com.srm.events.database;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseSchemaTest {

    private static final Set<String> EXPECTED_TABLES = Set.of(
            "roles", "users", "departments", "academic_years", "sections",
            "students", "faculty", "mentor_assignments", "event_categories", "events",
            "applications", "teams", "team_members", "document_types", "document_rules",
            "documents", "approval_actions", "achievements", "audit_logs", "password_reset_tokens"
    );

    @Test
    void freshSchemaDelegatesToCanonicalMigration() throws IOException {
        String schema = Files.readString(databasePath("schema.sql"));

        assertTrue(schema.contains("CREATE DATABASE IF NOT EXISTS `srm_event_management`"));
        assertTrue(schema.contains("SOURCE database/migrations/001_initial_schema.sql;"));
        assertTrue(schema.contains("SOURCE database/migrations/004_account_security.sql;"));
        assertFalse(schema.contains("CREATE TABLE"));
    }

    @Test
    void migrationDefinesExpectedNormalizedTablesAndStorageRules() throws IOException {
        String migration = Files.readString(databasePath("migrations", "001_initial_schema.sql"));
        Pattern tablePattern = Pattern.compile("CREATE TABLE IF NOT EXISTS `([^`]+)`");
        Matcher matcher = tablePattern.matcher(migration);
        Set<String> actualTables = new java.util.HashSet<>();
        while (matcher.find()) {
            actualTables.add(matcher.group(1));
        }

        assertEquals(EXPECTED_TABLES, actualTables);
        assertEquals(20, countOccurrences(migration, "ENGINE = InnoDB"));
        assertEquals(20, countOccurrences(migration, "DEFAULT CHARACTER SET = utf8mb4"));
        assertTrue(migration.contains("UNIQUE (`department_id`, `student_year`, `section_name`)"));
        assertTrue(migration.contains("UNIQUE (`team_id`, `student_id`)"));
        assertTrue(migration.contains("UNIQUE (`application_id`)"));
        assertTrue(migration.contains("ON UPDATE RESTRICT ON DELETE RESTRICT"));
        assertFalse(migration.contains(" ON DELETE CASCADE"));
        assertFalse(migration.contains("BLOB"));
    }

    @Test
    void referenceSeedContainsOnlyExpectedRoles() throws IOException {
        String seed = Files.readString(databasePath("seed", "001_roles.sql"));

        for (String role : List.of("STUDENT", "MENTOR", "HOD", "ADMIN")) {
            assertTrue(seed.contains("('" + role + "')"));
        }
        assertTrue(seed.contains("INSERT IGNORE INTO `roles`"));
        assertFalse(seed.toLowerCase().contains("insert into `students`"));
        assertFalse(seed.toLowerCase().contains("password_hash"));
    }

    @Test
    void developmentAdminSeedContainsCompatibleBcryptHashWithoutPlaintextPassword() throws IOException {
        String seed = Files.readString(databasePath("seed", "002_development_admin.sql"));
        Matcher hashMatcher = Pattern.compile("\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}").matcher(seed);
        String expectedDevelopmentPassword = new String(new char[]{
                'A', 'd', 'm', 'i', 'n', '@', '1', '2', '3'
        });

        assertTrue(seed.contains("admin@srmist.edu.in"));
        assertTrue(seed.contains("ON DUPLICATE KEY UPDATE"));
        assertFalse(seed.contains(expectedDevelopmentPassword));
        assertTrue(hashMatcher.find());
        assertTrue(new BCryptPasswordEncoder().matches(expectedDevelopmentPassword, hashMatcher.group()));
    }

    @Test
    void applicationReviewMigrationDefinesNormalizedReviewerFields() throws IOException {
        String migration = Files.readString(databasePath("migrations", "002_application_review_fields.sql"));

        assertTrue(migration.contains("`hod_id` BIGINT UNSIGNED NULL"));
        assertTrue(migration.contains("`mentor_remarks` TEXT NULL"));
        assertTrue(migration.contains("`hod_remarks` TEXT NULL"));
        assertTrue(migration.contains("REFERENCES `faculty` (`faculty_id`)"));
        assertTrue(migration.contains("idx_applications_hod_id"));
        assertTrue(migration.contains("information_schema.REFERENTIAL_CONSTRAINTS"));
    }

    @Test
    void developmentStaffSeedUsesCompatibleHashAndIdempotentAssignment() throws IOException {
        String seed = Files.readString(databasePath("seed", "003_development_staff_review.sql"));
        Matcher hashMatcher = Pattern.compile("\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}").matcher(seed);
        String expectedDevelopmentPassword = new String(new char[]{
                'A', 'd', 'm', 'i', 'n', '@', '1', '2', '3'
        });

        assertTrue(seed.contains("mentor.cse@srmist.edu.in"));
        assertTrue(seed.contains("hod.cse@srmist.edu.in"));
        assertTrue(seed.contains("NOT EXISTS"));
        assertTrue(hashMatcher.find());
        assertTrue(new BCryptPasswordEncoder().matches(expectedDevelopmentPassword, hashMatcher.group()));
    }

    @Test
    void accountSecurityMigrationAddsRevocableSessionVersionWithoutReplacingUsers() throws IOException {
        String migration = Files.readString(databasePath("migrations", "004_account_security.sql"));
        assertTrue(migration.contains("information_schema.COLUMNS"));
        assertTrue(migration.contains("`session_version` BIGINT UNSIGNED NOT NULL DEFAULT 0"));
        assertFalse(migration.toLowerCase().contains("drop table"));
        assertFalse(migration.toLowerCase().contains("delete from"));
    }

    @Test
    void studentAchievementMigrationIsAdditiveAndKeepsWorkflowClassificationExplicit() throws IOException {
        String migration = Files.readString(databasePath("migrations", "009_student_achievement_foundation.sql"));
        assertTrue(migration.contains("CREATE TABLE IF NOT EXISTS `student_achievements`"));
        assertTrue(migration.contains("CREATE TABLE IF NOT EXISTS `achievement_teams`"));
        assertTrue(migration.contains("workflow_category"));
        assertTrue(migration.contains("'ACHIEVEMENT'"));
        assertTrue(migration.contains("'ACTIVITY'"));
        assertFalse(migration.toLowerCase().contains("drop table"));
        assertFalse(migration.toLowerCase().contains("delete from"));
    }

    private static Path databasePath(String first, String... more) {
        Path workingDirectory = Path.of("").toAbsolutePath().normalize();
        Path databaseDirectory = Files.isDirectory(workingDirectory.resolve("database"))
                ? workingDirectory.resolve("database")
                : workingDirectory.resolve("..").resolve("database").normalize();
        return databaseDirectory.resolve(Path.of(first, more));
    }

    private static int countOccurrences(String value, String target) {
        return (value.length() - value.replace(target, "").length()) / target.length();
    }
}
