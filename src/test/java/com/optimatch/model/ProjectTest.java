package com.optimatch.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Project model class.
 */
@DisplayName("Project Model Tests")
class ProjectTest {

    private Project project;

    @BeforeEach
    void setUp() {
        project = new Project();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor sets default values")
        void defaultConstructor() {
            assertEquals(1, project.getMinCapacity());
            assertEquals(0.0, project.getRequiredGpa());
        }

        @Test
        @DisplayName("Full constructor sets all fields")
        void fullConstructor() {
            Project p = new Project(1, "CS101", "Intro CS", "Description", 5, 20, 2.5);

            assertEquals(1, p.getId());
            assertEquals("CS101", p.getCode());
            assertEquals("Intro CS", p.getName());
            assertEquals("Description", p.getDescription());
            assertEquals(5, p.getMinCapacity());
            assertEquals(20, p.getMaxCapacity());
            assertEquals(2.5, p.getRequiredGpa());
        }
    }

    @Nested
    @DisplayName("Capacity Tests")
    class CapacityTests {

        @Test
        @DisplayName("isWithinCapacity returns true for valid count")
        void withinCapacity() {
            project.setMinCapacity(5);
            project.setMaxCapacity(10);

            assertTrue(project.isWithinCapacity(5));
            assertTrue(project.isWithinCapacity(7));
            assertTrue(project.isWithinCapacity(10));
        }

        @Test
        @DisplayName("isWithinCapacity returns false for count below minimum")
        void belowMinCapacity() {
            project.setMinCapacity(5);
            project.setMaxCapacity(10);

            assertFalse(project.isWithinCapacity(4));
            assertFalse(project.isWithinCapacity(0));
        }

        @Test
        @DisplayName("isWithinCapacity returns false for count above maximum")
        void aboveMaxCapacity() {
            project.setMinCapacity(5);
            project.setMaxCapacity(10);

            assertFalse(project.isWithinCapacity(11));
            assertFalse(project.isWithinCapacity(100));
        }
    }

    @Nested
    @DisplayName("GPA Requirement Tests")
    class GpaRequirementTests {

        @Test
        @DisplayName("meetsGpaRequirement returns true when GPA meets requirement")
        void meetsGpa() {
            project.setRequiredGpa(3.0);

            assertTrue(project.meetsGpaRequirement(3.0));
            assertTrue(project.meetsGpaRequirement(3.5));
            assertTrue(project.meetsGpaRequirement(4.0));
        }

        @Test
        @DisplayName("meetsGpaRequirement returns false when GPA is below requirement")
        void belowGpa() {
            project.setRequiredGpa(3.0);

            assertFalse(project.meetsGpaRequirement(2.9));
            assertFalse(project.meetsGpaRequirement(0.0));
        }

        @Test
        @DisplayName("No GPA requirement (0.0) allows any GPA")
        void noGpaRequirement() {
            project.setRequiredGpa(0.0);

            assertTrue(project.meetsGpaRequirement(0.0));
            assertTrue(project.meetsGpaRequirement(2.0));
            assertTrue(project.meetsGpaRequirement(4.0));
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Projects with same ID are equal")
        void equalsWithSameId() {
            Project p1 = new Project(1, "CS101", "Name1", null, 1, 10, 0.0);
            Project p2 = new Project(1, "CS102", "Name2", null, 5, 20, 3.0);

            assertEquals(p1, p2);
            assertEquals(p1.hashCode(), p2.hashCode());
        }

        @Test
        @DisplayName("Projects with different IDs are not equal")
        void notEqualsWithDifferentId() {
            Project p1 = new Project(1, "CS101", "Name", null, 1, 10, 0.0);
            Project p2 = new Project(2, "CS101", "Name", null, 1, 10, 0.0);

            assertNotEquals(p1, p2);
        }
    }

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString contains project information")
        void toStringContainsInfo() {
            Project p = new Project(1, "CS101", "Intro CS", null, 5, 10, 0.0);
            String str = p.toString();

            assertTrue(str.contains("id=1"));
            assertTrue(str.contains("code='CS101'"));
            assertTrue(str.contains("5-10"));
        }
    }
}
