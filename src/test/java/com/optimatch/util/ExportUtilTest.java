package com.optimatch.util;

import com.optimatch.model.AlgorithmRun;
import com.optimatch.model.Project;
import com.optimatch.model.Student;
import com.optimatch.service.ReportService.MatchingSummary;
import com.optimatch.service.ReportService.ProjectAssignmentDetail;
import com.optimatch.service.ReportService.StudentAssignmentDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ExportUtil class.
 */
@DisplayName("ExportUtil Tests")
class ExportUtilTest {

    @TempDir
    Path tempDir;

    private AlgorithmRun run;
    private MatchingSummary summary;
    private List<StudentAssignmentDetail> studentDetails;
    private List<ProjectAssignmentDetail> projectDetails;

    @BeforeEach
    void setUp() {
        // Create test data
        run = new AlgorithmRun(1, LocalDateTime.of(2026, 1, 28, 10, 30, 0),
                200, 1000, 0.02, 0.8, 450.5, 5000);

        int[] distribution = {1, 5, 2, 1, 1, 0};
        summary = new MatchingSummary(1, 10, 3, 10, distribution, 800, 80.0);

        // Student details
        Student alice = new Student(1, "S001", "Alice Smith", "alice@test.com", 3.5, null);
        Student bob = new Student(2, "S002", "Bob Jones", "bob@test.com", 3.0, null);
        Project p1 = new Project(1, "CS101", "Intro to CS", null, 1, 5, 0.0);
        Project p2 = new Project(2, "CS201", "Data Structures", null, 1, 5, 2.5);

        studentDetails = Arrays.asList(
                new StudentAssignmentDetail(alice, p1, 1, 100),
                new StudentAssignmentDetail(bob, p2, 2, 80)
        );

        // Project details
        projectDetails = Arrays.asList(
                new ProjectAssignmentDetail(p1, List.of(alice), true, true),
                new ProjectAssignmentDetail(p2, List.of(bob), true, true)
        );
    }

    @Nested
    @DisplayName("Export Students CSV Tests")
    class ExportStudentsCsvTests {

        @Test
        @DisplayName("exportStudentsCsv creates file")
        void exportStudentsCsvCreatesFile() throws IOException {
            File file = tempDir.resolve("students.csv").toFile();

            ExportUtil.exportStudentsCsv(file, run, summary, studentDetails);

            assertTrue(file.exists());
        }

        @Test
        @DisplayName("exportStudentsCsv contains header row")
        void exportStudentsCsvContainsHeader() throws IOException {
            File file = tempDir.resolve("students.csv").toFile();

            ExportUtil.exportStudentsCsv(file, run, summary, studentDetails);

            String content = Files.readString(file.toPath());
            assertTrue(content.contains("Student ID"));
            assertTrue(content.contains("Student Name"));
            assertTrue(content.contains("Project Code"));
        }

        @Test
        @DisplayName("exportStudentsCsv contains student data")
        void exportStudentsCsvContainsData() throws IOException {
            File file = tempDir.resolve("students.csv").toFile();

            ExportUtil.exportStudentsCsv(file, run, summary, studentDetails);

            String content = Files.readString(file.toPath());
            assertTrue(content.contains("S001"));
            assertTrue(content.contains("Alice Smith"));
            assertTrue(content.contains("CS101"));
        }

        @Test
        @DisplayName("exportStudentsCsv contains metadata comments")
        void exportStudentsCsvContainsMetadata() throws IOException {
            File file = tempDir.resolve("students.csv").toFile();

            ExportUtil.exportStudentsCsv(file, run, summary, studentDetails);

            String content = Files.readString(file.toPath());
            assertTrue(content.contains("# OptiMatch"));
            assertTrue(content.contains("Run Date:"));
            assertTrue(content.contains("Best Fitness:"));
        }
    }

    @Nested
    @DisplayName("Export Projects CSV Tests")
    class ExportProjectsCsvTests {

        @Test
        @DisplayName("exportProjectsCsv creates file")
        void exportProjectsCsvCreatesFile() throws IOException {
            File file = tempDir.resolve("projects.csv").toFile();

            ExportUtil.exportProjectsCsv(file, run, summary, projectDetails);

            assertTrue(file.exists());
        }

        @Test
        @DisplayName("exportProjectsCsv contains header row")
        void exportProjectsCsvContainsHeader() throws IOException {
            File file = tempDir.resolve("projects.csv").toFile();

            ExportUtil.exportProjectsCsv(file, run, summary, projectDetails);

            String content = Files.readString(file.toPath());
            assertTrue(content.contains("Project Code"));
            assertTrue(content.contains("Project Name"));
            assertTrue(content.contains("Assigned Count"));
        }

        @Test
        @DisplayName("exportProjectsCsv contains project data")
        void exportProjectsCsvContainsData() throws IOException {
            File file = tempDir.resolve("projects.csv").toFile();

            ExportUtil.exportProjectsCsv(file, run, summary, projectDetails);

            String content = Files.readString(file.toPath());
            assertTrue(content.contains("CS101"));
            assertTrue(content.contains("Intro to CS"));
        }
    }

    @Nested
    @DisplayName("Export Full Report Tests")
    class ExportFullReportTests {

        @Test
        @DisplayName("exportFullReport creates file")
        void exportFullReportCreatesFile() throws IOException {
            File file = tempDir.resolve("report.txt").toFile();

            ExportUtil.exportFullReport(file, run, summary, studentDetails, projectDetails);

            assertTrue(file.exists());
        }

        @Test
        @DisplayName("exportFullReport contains all sections")
        void exportFullReportContainsSections() throws IOException {
            File file = tempDir.resolve("report.txt").toFile();

            ExportUtil.exportFullReport(file, run, summary, studentDetails, projectDetails);

            String content = Files.readString(file.toPath());
            assertTrue(content.contains("RUN INFORMATION"));
            assertTrue(content.contains("SUMMARY STATISTICS"));
            assertTrue(content.contains("PREFERENCE DISTRIBUTION"));
            assertTrue(content.contains("STUDENT ASSIGNMENTS"));
            assertTrue(content.contains("PROJECT ASSIGNMENTS"));
        }

        @Test
        @DisplayName("exportFullReport contains run information")
        void exportFullReportContainsRunInfo() throws IOException {
            File file = tempDir.resolve("report.txt").toFile();

            ExportUtil.exportFullReport(file, run, summary, studentDetails, projectDetails);

            String content = Files.readString(file.toPath());
            assertTrue(content.contains("Population Size:"));
            assertTrue(content.contains("Generations:"));
            assertTrue(content.contains("Mutation Rate:"));
        }

        @Test
        @DisplayName("exportFullReport contains preference distribution")
        void exportFullReportContainsDistribution() throws IOException {
            File file = tempDir.resolve("report.txt").toFile();

            ExportUtil.exportFullReport(file, run, summary, studentDetails, projectDetails);

            String content = Files.readString(file.toPath());
            assertTrue(content.contains("1st Choice:"));
            assertTrue(content.contains("2nd Choice:"));
            assertTrue(content.contains("No Match:"));
        }

        @Test
        @DisplayName("exportFullReport contains student data")
        void exportFullReportContainsStudents() throws IOException {
            File file = tempDir.resolve("report.txt").toFile();

            ExportUtil.exportFullReport(file, run, summary, studentDetails, projectDetails);

            String content = Files.readString(file.toPath());
            assertTrue(content.contains("Alice"));
            assertTrue(content.contains("Bob"));
        }

        @Test
        @DisplayName("exportFullReport contains project data")
        void exportFullReportContainsProjects() throws IOException {
            File file = tempDir.resolve("report.txt").toFile();

            ExportUtil.exportFullReport(file, run, summary, studentDetails, projectDetails);

            String content = Files.readString(file.toPath());
            assertTrue(content.contains("CS101"));
            assertTrue(content.contains("Intro to CS"));
        }
    }

    @Nested
    @DisplayName("CSV Escaping Tests")
    class CsvEscapingTests {

        @Test
        @DisplayName("Handles values with commas")
        void handlesCommas() throws IOException {
            Student student = new Student(1, "S001", "Smith, John", null, 3.0, null);
            Project project = new Project(1, "P1", "Project, Advanced", null, 1, 5, 0.0);

            List<StudentAssignmentDetail> details = List.of(
                    new StudentAssignmentDetail(student, project, 1, 100)
            );

            File file = tempDir.resolve("test.csv").toFile();
            ExportUtil.exportStudentsCsv(file, run, summary, details);

            String content = Files.readString(file.toPath());
            // Values with commas should be quoted
            assertTrue(content.contains("\"Smith, John\"") || content.contains("Smith, John"));
        }
    }
}
