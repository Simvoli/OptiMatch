package com.optimatch.algorithm;

import com.optimatch.model.Project;
import com.optimatch.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ConstraintChecker class.
 */
@DisplayName("ConstraintChecker Tests")
class ConstraintCheckerTest {

    private List<Student> students;
    private List<Project> projects;
    private ConstraintChecker checker;
    private Random random;

    @BeforeEach
    void setUp() {
        random = new Random(42);

        // Create test students (null partnerId = no partner)
        students = Arrays.asList(
                new Student(1, "S001", "Alice", null, 3.5, null),
                new Student(2, "S002", "Bob", null, 3.0, null),
                new Student(3, "S003", "Carol", null, 2.5, null),
                new Student(4, "S004", "Dave", null, 2.0, null),
                new Student(5, "S005", "Eve", null, 3.8, null)
        );

        // Create test projects
        projects = Arrays.asList(
                new Project(1, "P1", "Project 1", null, 1, 3, 0.0),
                new Project(2, "P2", "Project 2", null, 1, 3, 2.5),
                new Project(3, "P3", "Project 3", null, 1, 3, 3.0)
        );

        checker = new ConstraintChecker(students, projects, random);
    }

    @Nested
    @DisplayName("Capacity Constraint Tests")
    class CapacityConstraintTests {

        @Test
        @DisplayName("checkCapacity returns true when all projects within limits")
        void checkCapacityWithinLimits() {
            // P1: 2, P2: 2, P3: 1
            Chromosome c = new Chromosome(new int[]{1, 2, 1, 2, 3});

            assertTrue(checker.checkCapacity(c));
        }

        @Test
        @DisplayName("checkCapacity returns false when project over max")
        void checkCapacityOverMax() {
            // All 5 students to P1 (max 3)
            Chromosome c = new Chromosome(new int[]{1, 1, 1, 1, 1});

            assertFalse(checker.checkCapacity(c));
        }

        @Test
        @DisplayName("checkCapacity returns false when project under min")
        void checkCapacityUnderMin() {
            // All to P1, P2 and P3 have 0 (min 1)
            Chromosome c = new Chromosome(new int[]{1, 1, 1, 1, 1});

            assertFalse(checker.checkCapacity(c));
        }

        @Test
        @DisplayName("checkCapacity at exact boundaries passes")
        void checkCapacityAtBoundaries() {
            // P1: 3 (max), P2: 1 (min), P3: 1 (min)
            Chromosome c = new Chromosome(new int[]{1, 1, 1, 2, 3});

            assertTrue(checker.checkCapacity(c));
        }
    }

    @Nested
    @DisplayName("GPA Constraint Tests")
    class GpaConstraintTests {

        @Test
        @DisplayName("checkGpa returns true when all students meet requirements")
        void checkGpaAllMeetRequirements() {
            // All to P1 which has no GPA requirement
            Chromosome c = new Chromosome(new int[]{1, 1, 1, 1, 1});

            assertTrue(checker.checkGpa(c));
        }

        @Test
        @DisplayName("checkGpa returns false when student below requirement")
        void checkGpaStudentBelowRequirement() {
            // Dave (GPA 2.0) to P3 (requires 3.0)
            Chromosome c = new Chromosome(new int[]{1, 1, 1, 3, 1});

            assertFalse(checker.checkGpa(c));
        }

        @Test
        @DisplayName("checkGpa student exactly at requirement passes")
        void checkGpaExactlyAtRequirement() {
            // Carol (GPA 2.5) to P2 (requires 2.5)
            Chromosome c = new Chromosome(new int[]{1, 1, 2, 1, 1});

            assertTrue(checker.checkGpa(c));
        }
    }

    @Nested
    @DisplayName("Partner Constraint Tests")
    class PartnerConstraintTests {

        @Test
        @DisplayName("checkPartners returns true when no partners exist")
        void checkPartnersNoPartners() {
            Chromosome c = new Chromosome(new int[]{1, 2, 1, 2, 3});

            assertTrue(checker.checkPartners(c));
        }

        @Test
        @DisplayName("checkPartners returns true when partners together")
        void checkPartnersTogether() {
            // Set Alice and Bob as partners
            students.get(0).setPartnerId(2);
            students.get(1).setPartnerId(1);
            checker = new ConstraintChecker(students, projects, random);

            // Both in P1
            Chromosome c = new Chromosome(new int[]{1, 1, 2, 2, 3});

            assertTrue(checker.checkPartners(c));
        }

        @Test
        @DisplayName("checkPartners returns false when partners separated")
        void checkPartnersSeparated() {
            // Set Alice and Bob as partners
            students.get(0).setPartnerId(2);
            students.get(1).setPartnerId(1);
            checker = new ConstraintChecker(students, projects, random);

            // Alice in P1, Bob in P2
            Chromosome c = new Chromosome(new int[]{1, 2, 1, 2, 3});

            assertFalse(checker.checkPartners(c));
        }
    }

    @Nested
    @DisplayName("Check All Tests")
    class CheckAllTests {

        @Test
        @DisplayName("checkAll returns true when all constraints satisfied")
        void checkAllSatisfied() {
            // Valid assignment: Dave (GPA 2.0) goes to P1 (no GPA requirement)
            // P1 gets 3 students, P2 gets 1, P3 gets 1 (all within capacity)
            Chromosome c = new Chromosome(new int[]{1, 2, 1, 1, 3});

            boolean valid = checker.checkAll(c);

            assertTrue(valid);
            assertTrue(c.isValid());
        }

        @Test
        @DisplayName("checkAll returns false when any constraint violated")
        void checkAllViolated() {
            // Over capacity on P1
            Chromosome c = new Chromosome(new int[]{1, 1, 1, 1, 1});

            boolean valid = checker.checkAll(c);

            assertFalse(valid);
            assertFalse(c.isValid());
        }
    }

    @Nested
    @DisplayName("Get Violations Tests")
    class GetViolationsTests {

        @Test
        @DisplayName("getViolations returns no violations for valid chromosome")
        void getViolationsNone() {
            // Valid assignment: Dave (GPA 2.0) goes to P1 (no GPA requirement)
            Chromosome c = new Chromosome(new int[]{1, 2, 1, 1, 3});

            ConstraintChecker.ConstraintViolations violations = checker.getViolations(c);

            assertFalse(violations.hasViolations());
            assertEquals(0, violations.getTotalViolations());
        }

        @Test
        @DisplayName("getViolations returns capacity violations")
        void getViolationsCapacity() {
            // All to P1 (over), none to P2, P3 (under)
            Chromosome c = new Chromosome(new int[]{1, 1, 1, 1, 1});

            ConstraintChecker.ConstraintViolations violations = checker.getViolations(c);

            assertTrue(violations.hasViolations());
            assertEquals(3, violations.getCapacityViolations().size()); // P1 over, P2 under, P3 under
        }

        @Test
        @DisplayName("getViolations returns GPA violations")
        void getViolationsGpa() {
            // Dave (2.0) to P3 (requires 3.0)
            Chromosome c = new Chromosome(new int[]{1, 2, 1, 3, 3});

            ConstraintChecker.ConstraintViolations violations = checker.getViolations(c);

            assertTrue(violations.hasViolations());
            assertEquals(1, violations.getGpaViolations().size());
        }

        @Test
        @DisplayName("getViolations returns partner violations")
        void getViolationsPartner() {
            students.get(0).setPartnerId(2);
            students.get(1).setPartnerId(1);
            checker = new ConstraintChecker(students, projects, random);

            // Alice in P1, Bob in P2
            Chromosome c = new Chromosome(new int[]{1, 2, 1, 2, 3});

            ConstraintChecker.ConstraintViolations violations = checker.getViolations(c);

            assertTrue(violations.hasViolations());
            assertEquals(1, violations.getPartnerViolations().size());
        }
    }

    @Nested
    @DisplayName("Repair Tests")
    class RepairTests {

        @Test
        @DisplayName("repairGpa moves students to valid projects")
        void repairGpaMoves() {
            // Dave (2.0) to P3 (requires 3.0)
            Chromosome c = new Chromosome(new int[]{1, 1, 1, 3, 3});

            checker.repairGpa(c);

            // Dave should now be in a project with no/lower GPA requirement
            int daveProject = c.getAssignment(3);
            Project assignedProject = projects.stream()
                    .filter(p -> p.getId() == daveProject)
                    .findFirst().orElse(null);
            assertNotNull(assignedProject);
            assertTrue(assignedProject.meetsGpaRequirement(2.0));
        }

        @Test
        @DisplayName("repairPartners puts partners together")
        void repairPartnersTogether() {
            students.get(0).setPartnerId(2);
            students.get(1).setPartnerId(1);
            checker = new ConstraintChecker(students, projects, random);

            // Alice in P1, Bob in P2
            Chromosome c = new Chromosome(new int[]{1, 2, 1, 2, 3});

            checker.repairPartners(c);

            // Alice and Bob should be in same project
            assertEquals(c.getAssignment(0), c.getAssignment(1));
        }

        @Test
        @DisplayName("repair fixes all violations when possible")
        void repairFixesAll() {
            // Create a chromosome with multiple violations
            students.get(0).setPartnerId(2);
            students.get(1).setPartnerId(1);
            checker = new ConstraintChecker(students, projects, random);

            Chromosome c = new Chromosome(new int[]{1, 2, 1, 2, 3});

            checker.repair(c);

            // Should be valid now
            assertTrue(checker.checkPartners(c));
        }
    }

    @Nested
    @DisplayName("Project IDs Tests")
    class ProjectIdsTests {

        @Test
        @DisplayName("getProjectIds returns correct IDs")
        void getProjectIds() {
            int[] ids = checker.getProjectIds();

            assertEquals(3, ids.length);
            assertTrue(Arrays.stream(ids).anyMatch(id -> id == 1));
            assertTrue(Arrays.stream(ids).anyMatch(id -> id == 2));
            assertTrue(Arrays.stream(ids).anyMatch(id -> id == 3));
        }

        @Test
        @DisplayName("getProjectIds returns copy")
        void getProjectIdsCopy() {
            int[] ids1 = checker.getProjectIds();
            int[] ids2 = checker.getProjectIds();

            assertNotSame(ids1, ids2);
        }
    }

    @Nested
    @DisplayName("Violation Classes Tests")
    class ViolationClassesTests {

        @Test
        @DisplayName("CapacityViolation toString contains relevant info")
        void capacityViolationToString() {
            ConstraintChecker.CapacityViolation violation =
                    new ConstraintChecker.CapacityViolation(1, 5, 1, 3, false);

            String str = violation.toString();

            assertTrue(str.contains("project=1"));
            assertTrue(str.contains("actual=5"));
            assertTrue(str.contains("OVER"));
        }

        @Test
        @DisplayName("GpaViolation toString contains relevant info")
        void gpaViolationToString() {
            ConstraintChecker.GpaViolation violation =
                    new ConstraintChecker.GpaViolation(1, 2, 2.0, 3.0);

            String str = violation.toString();

            assertTrue(str.contains("student=1"));
            assertTrue(str.contains("project=2"));
            // Use locale-agnostic checks (some locales use comma as decimal separator)
            assertTrue(str.contains("studentGpa") || str.contains("2"));
            assertTrue(str.contains("requiredGpa") || str.contains("3"));
        }

        @Test
        @DisplayName("PartnerViolation toString contains relevant info")
        void partnerViolationToString() {
            ConstraintChecker.PartnerViolation violation =
                    new ConstraintChecker.PartnerViolation(1, 2, 1, 2);

            String str = violation.toString();

            assertTrue(str.contains("student=1"));
            assertTrue(str.contains("partner=2"));
        }
    }
}
