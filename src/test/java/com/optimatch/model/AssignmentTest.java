package com.optimatch.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Assignment model class.
 */
@DisplayName("Assignment Model Tests")
class AssignmentTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor creates empty assignment")
        void defaultConstructor() {
            Assignment assignment = new Assignment();

            assertEquals(0, assignment.getId());
            assertEquals(0, assignment.getRunId());
            assertEquals(0, assignment.getStudentId());
            assertEquals(0, assignment.getProjectId());
            assertNull(assignment.getPreferenceRank());
        }

        @Test
        @DisplayName("Constructor without ID sets fields correctly")
        void constructorWithoutId() {
            Assignment assignment = new Assignment(1, 2, 3, 2);

            assertEquals(1, assignment.getRunId());
            assertEquals(2, assignment.getStudentId());
            assertEquals(3, assignment.getProjectId());
            assertEquals(2, assignment.getPreferenceRank());
        }

        @Test
        @DisplayName("Full constructor sets all fields")
        void fullConstructor() {
            Assignment assignment = new Assignment(10, 1, 2, 3, 1);

            assertEquals(10, assignment.getId());
            assertEquals(1, assignment.getRunId());
            assertEquals(2, assignment.getStudentId());
            assertEquals(3, assignment.getProjectId());
            assertEquals(1, assignment.getPreferenceRank());
        }

        @Test
        @DisplayName("Constructor with null preference rank")
        void constructorWithNullRank() {
            Assignment assignment = new Assignment(1, 2, 3, null);

            assertNull(assignment.getPreferenceRank());
        }
    }

    @Nested
    @DisplayName("Preference Tests")
    class PreferenceTests {

        @Test
        @DisplayName("wasInPreferences returns true when rank is set")
        void wasInPreferencesTrue() {
            Assignment assignment = new Assignment(1, 2, 3, 2);

            assertTrue(assignment.wasInPreferences());
        }

        @Test
        @DisplayName("wasInPreferences returns false when rank is null")
        void wasInPreferencesFalse() {
            Assignment assignment = new Assignment(1, 2, 3, null);

            assertFalse(assignment.wasInPreferences());
        }
    }

    @Nested
    @DisplayName("Satisfaction Score Tests")
    class SatisfactionScoreTests {

        @Test
        @DisplayName("First choice gives highest satisfaction")
        void firstChoiceSatisfaction() {
            Assignment assignment = new Assignment(1, 2, 3, 1);

            assertEquals(Preference.WEIGHT_FIRST_CHOICE, assignment.getSatisfactionScore());
        }

        @Test
        @DisplayName("Second choice gives second highest satisfaction")
        void secondChoiceSatisfaction() {
            Assignment assignment = new Assignment(1, 2, 3, 2);

            assertEquals(Preference.WEIGHT_SECOND_CHOICE, assignment.getSatisfactionScore());
        }

        @Test
        @DisplayName("No preference gives zero satisfaction")
        void noPreferenceSatisfaction() {
            Assignment assignment = new Assignment(1, 2, 3, null);

            assertEquals(Preference.WEIGHT_NO_PREFERENCE, assignment.getSatisfactionScore());
        }

        @Test
        @DisplayName("All preference ranks give correct scores")
        void allPreferenceScores() {
            assertEquals(100, new Assignment(1, 2, 3, 1).getSatisfactionScore());
            assertEquals(80, new Assignment(1, 2, 3, 2).getSatisfactionScore());
            assertEquals(60, new Assignment(1, 2, 3, 3).getSatisfactionScore());
            assertEquals(40, new Assignment(1, 2, 3, 4).getSatisfactionScore());
            assertEquals(20, new Assignment(1, 2, 3, 5).getSatisfactionScore());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Assignments with same runId and studentId are equal")
        void equalsWithSameRunAndStudent() {
            Assignment a1 = new Assignment(1, 1, 2, 3, 1);
            Assignment a2 = new Assignment(2, 1, 2, 4, 2);

            assertEquals(a1, a2);
            assertEquals(a1.hashCode(), a2.hashCode());
        }

        @Test
        @DisplayName("Assignments with different runId are not equal")
        void notEqualsWithDifferentRun() {
            Assignment a1 = new Assignment(1, 2, 3, 1);
            Assignment a2 = new Assignment(2, 2, 3, 1);

            assertNotEquals(a1, a2);
        }

        @Test
        @DisplayName("Assignments with different studentId are not equal")
        void notEqualsWithDifferentStudent() {
            Assignment a1 = new Assignment(1, 2, 3, 1);
            Assignment a2 = new Assignment(1, 3, 3, 1);

            assertNotEquals(a1, a2);
        }
    }

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString contains assignment information")
        void toStringContainsInfo() {
            Assignment assignment = new Assignment(10, 1, 2, 3, 1);

            String str = assignment.toString();

            assertTrue(str.contains("runId=1"));
            assertTrue(str.contains("studentId=2"));
            assertTrue(str.contains("projectId=3"));
        }
    }
}
