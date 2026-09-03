package com.srm.events.database;

import org.junit.jupiter.api.Test;

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
