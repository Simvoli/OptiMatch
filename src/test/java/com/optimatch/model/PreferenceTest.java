package com.optimatch.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Preference model class.
 */
@DisplayName("Preference Model Tests")
class PreferenceTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor creates empty preference")
        void defaultConstructor() {
            Preference pref = new Preference();
            assertEquals(0, pref.getId());
            assertEquals(0, pref.getStudentId());
            assertEquals(0, pref.getProjectId());
            assertEquals(0, pref.getRank());
        }

        @Test
        @DisplayName("Constructor without ID sets fields correctly")
        void constructorWithoutId() {
            Preference pref = new Preference(1, 2, 3);

            assertEquals(1, pref.getStudentId());
            assertEquals(2, pref.getProjectId());
            assertEquals(3, pref.getRank());
        }

        @Test
        @DisplayName("Full constructor sets all fields")
        void fullConstructor() {
            Preference pref = new Preference(10, 1, 2, 3);

            assertEquals(10, pref.getId());
            assertEquals(1, pref.getStudentId());
            assertEquals(2, pref.getProjectId());
            assertEquals(3, pref.getRank());
        }
    }

    @Nested
    @DisplayName("Preference Weight Tests")
    class WeightTests {

        @Test
        @DisplayName("First choice has weight 100")
        void firstChoiceWeight() {
            assertEquals(100, Preference.WEIGHT_FIRST_CHOICE);
            assertEquals(100, Preference.getWeightForRank(1));
        }

        @Test
        @DisplayName("Second choice has weight 80")
        void secondChoiceWeight() {
            assertEquals(80, Preference.WEIGHT_SECOND_CHOICE);
            assertEquals(80, Preference.getWeightForRank(2));
        }

        @Test
        @DisplayName("Third choice has weight 60")
        void thirdChoiceWeight() {
            assertEquals(60, Preference.WEIGHT_THIRD_CHOICE);
            assertEquals(60, Preference.getWeightForRank(3));
        }

        @Test
        @DisplayName("Fourth choice has weight 40")
        void fourthChoiceWeight() {
            assertEquals(40, Preference.WEIGHT_FOURTH_CHOICE);
            assertEquals(40, Preference.getWeightForRank(4));
        }

        @Test
        @DisplayName("Fifth choice has weight 20")
        void fifthChoiceWeight() {
            assertEquals(20, Preference.WEIGHT_FIFTH_CHOICE);
            assertEquals(20, Preference.getWeightForRank(5));
        }

        @Test
        @DisplayName("No preference has weight 0")
        void noPreferenceWeight() {
            assertEquals(0, Preference.WEIGHT_NO_PREFERENCE);
        }

        @Test
        @DisplayName("Invalid rank returns 0 weight")
        void invalidRankWeight() {
            assertEquals(0, Preference.getWeightForRank(0));
            assertEquals(0, Preference.getWeightForRank(6));
            assertEquals(0, Preference.getWeightForRank(-1));
        }
    }

    @Nested
    @DisplayName("getWeight Method Tests")
    class GetWeightTests {

        @Test
        @DisplayName("getWeight returns correct weight based on rank")
        void getWeightReturnsCorrectValue() {
            Preference pref1 = new Preference(1, 1, 1);
            Preference pref2 = new Preference(1, 1, 2);
            Preference pref3 = new Preference(1, 1, 3);
            Preference pref4 = new Preference(1, 1, 4);
            Preference pref5 = new Preference(1, 1, 5);

            assertEquals(100, pref1.getWeight());
            assertEquals(80, pref2.getWeight());
            assertEquals(60, pref3.getWeight());
            assertEquals(40, pref4.getWeight());
            assertEquals(20, pref5.getWeight());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Preferences with same student and project are equal")
        void equalsWithSameStudentAndProject() {
            Preference p1 = new Preference(1, 1, 2, 1);
            Preference p2 = new Preference(2, 1, 2, 3);

            assertEquals(p1, p2);
            assertEquals(p1.hashCode(), p2.hashCode());
        }

        @Test
        @DisplayName("Preferences with different student are not equal")
        void notEqualsWithDifferentStudent() {
            Preference p1 = new Preference(1, 2, 1);
            Preference p2 = new Preference(2, 2, 1);

            assertNotEquals(p1, p2);
        }

        @Test
        @DisplayName("Preferences with different project are not equal")
        void notEqualsWithDifferentProject() {
            Preference p1 = new Preference(1, 2, 1);
            Preference p2 = new Preference(1, 3, 1);

            assertNotEquals(p1, p2);
        }
    }
}
