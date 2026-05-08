package com.optimatch.algorithm;

import com.optimatch.model.Preference;
import com.optimatch.model.Project;
import com.optimatch.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the FitnessCalculator class.
 */
@DisplayName("FitnessCalculator Tests")
class FitnessCalculatorTest {

    private List<Student> students;
    private List<Project> projects;
    private List<Preference> preferences;
    private FitnessCalculator calculator;

    @BeforeEach
    void setUp() {
        // Create test students
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

        // Create test preferences
        preferences = Arrays.asList(
                // Alice prefers P1 > P2 > P3
                new Preference(1, 1, 1),
                new Preference(1, 2, 2),
                new Preference(1, 3, 3),
                // Bob prefers P2 > P1
                new Preference(2, 2, 1),
                new Preference(2, 1, 2),
                // Carol prefers P1
                new Preference(3, 1, 1),
                // Dave prefers P3 > P2
                new Preference(4, 3, 1),
                new Preference(4, 2, 2),
                // Eve prefers P3 > P1
                new Preference(5, 3, 1),
                new Preference(5, 1, 2)
        );

        calculator = new FitnessCalculator(students, projects, preferences);
    }

    @Nested
    @DisplayName("Preference Score Tests")
    class PreferenceScoreTests {

        @Test
        @DisplayName("All first choices gives maximum score")
        void allFirstChoices() {
            // Assign each student to their first choice
            // Alice -> P1, Bob -> P2, Carol -> P1, Dave -> P3, Eve -> P3
            Chromosome c = new Chromosome(new int[]{1, 2, 1, 3, 3});

            double score = calculator.calculatePreferenceScore(c);

            // 5 students * 100 (first choice weight) = 500
            assertEquals(500.0, score);
        }

        @Test
        @DisplayName("Second choices give lower score")
        void secondChoices() {
            // Alice -> P2 (2nd), Bob -> P1 (2nd), Carol -> P1 (1st), Dave -> P2 (2nd), Eve -> P1 (2nd)
            Chromosome c = new Chromosome(new int[]{2, 1, 1, 2, 1});

            double score = calculator.calculatePreferenceScore(c);

            // Alice: 80, Bob: 80, Carol: 100, Dave: 80, Eve: 80 = 420
            assertEquals(420.0, score);
        }

        @Test
        @DisplayName("Assignment not in preferences gives zero")
        void notInPreferences() {
            // Assign all students to projects not in their preferences
            // Dave has no preference for P1
            Chromosome c = new Chromosome(new int[]{1, 2, 1, 1, 3});

            double score = calculator.calculatePreferenceScore(c);

            // Alice: 100 (P1 is 1st), Bob: 100 (P2 is 1st), Carol: 100 (P1 is 1st),
            // Dave: 0 (P1 not in prefs), Eve: 100 (P3 is 1st) = 400
            assertEquals(400.0, score);
        }
    }

    @Nested
    @DisplayName("Capacity Penalty Tests")
    class CapacityPenaltyTests {

        @Test
        @DisplayName("Within capacity gives no penalty")
        void withinCapacity() {
            // P1: 2 students (min 1, max 3) - OK
            // P2: 2 students (min 1, max 3) - OK
            // P3: 1 student (min 1, max 3) - OK
            Chromosome c = new Chromosome(new int[]{1, 2, 1, 2, 3});

            double penalty = calculator.calculateCapacityPenalty(c);

            assertEquals(0.0, penalty);
        }

        @Test
        @DisplayName("Over capacity gives penalty")
        void overCapacity() {
            // All 5 students to P1 (max 3) -> 2 over
            Chromosome c = new Chromosome(new int[]{1, 1, 1, 1, 1});

            double penalty = calculator.calculateCapacityPenalty(c);

            // Default penalty weight is 50, 2 over = 100
            // Also P2 and P3 are under (0 students each, min 1) -> 2 * 50 = 100
            // Total: 100 + 100 = 200
            assertEquals(200.0, penalty);
        }

        @Test
        @DisplayName("Under capacity gives penalty")
        void underCapacity() {
            // P1: 5 students (4 over max 3) - but projects 2,3 have 0 (under min 1)
            Chromosome c = new Chromosome(new int[]{1, 1, 1, 1, 1});

            double penalty = calculator.calculateCapacityPenalty(c);

            // P1: 2 over -> 100
            // P2: 1 under -> 50
            // P3: 1 under -> 50
            assertEquals(200.0, penalty);
        }
    }

    @Nested
    @DisplayName("GPA Penalty Tests")
    class GpaPenaltyTests {

        @Test
        @DisplayName("Students meeting GPA requirements give no penalty")
        void meetsGpa() {
            // All students to P1 which has no GPA requirement
            Chromosome c = new Chromosome(new int[]{1, 1, 1, 1, 1});

            double penalty = calculator.calculateGpaPenalty(c);

            assertEquals(0.0, penalty);
        }

        @Test
        @DisplayName("Student below GPA requirement gives penalty")
        void belowGpa() {
            // Dave (GPA 2.0) to P2 (requires 2.5) -> violation
            // Dave (GPA 2.0) to P3 (requires 3.0) -> violation
            Chromosome c = new Chromosome(new int[]{1, 1, 1, 3, 1}); // Dave -> P3

            double penalty = calculator.calculateGpaPenalty(c);

            // Default GPA penalty is 30
            assertEquals(30.0, penalty);
        }

        @Test
        @DisplayName("Multiple GPA violations accumulate")
        void multipleGpaViolations() {
            // Carol (2.5) -> P3 (requires 3.0) -> violation
            // Dave (2.0) -> P3 (requires 3.0) -> violation
            Chromosome c = new Chromosome(new int[]{3, 3, 3, 3, 3});

            double penalty = calculator.calculateGpaPenalty(c);

            // Carol and Dave violate P3's 3.0 requirement
            // 2 violations * 30 = 60
            assertEquals(60.0, penalty);
        }
    }

    @Nested
    @DisplayName("Partner Penalty Tests")
    class PartnerPenaltyTests {

        @Test
        @DisplayName("No partners gives no penalty")
        void noPartners() {
            Chromosome c = new Chromosome(new int[]{1, 2, 1, 2, 3});

            double penalty = calculator.calculatePartnerPenalty(c);

            assertEquals(0.0, penalty);
        }

        @Test
        @DisplayName("Partners in same project gives no penalty")
        void partnersTogetherNoPenalty() {
            // Set Alice and Bob as partners
            students.get(0).setPartnerId(2);
            students.get(1).setPartnerId(1);

            // Recreate calculator with updated students
            calculator = new FitnessCalculator(students, projects, preferences);

            // Both in P1
            Chromosome c = new Chromosome(new int[]{1, 1, 1, 2, 3});

            double penalty = calculator.calculatePartnerPenalty(c);

            assertEquals(0.0, penalty);
        }

        @Test
        @DisplayName("Partners in different projects gives penalty")
        void partnersSeparatedPenalty() {
            // Set Alice and Bob as partners
            students.get(0).setPartnerId(2);
            students.get(1).setPartnerId(1);

            calculator = new FitnessCalculator(students, projects, preferences);

            // Alice in P1, Bob in P2 (separated)
            Chromosome c = new Chromosome(new int[]{1, 2, 1, 2, 3});

            double penalty = calculator.calculatePartnerPenalty(c);

            // Default partner penalty is 40
            assertEquals(40.0, penalty);
        }
    }

    @Nested
    @DisplayName("Total Fitness Tests")
    class TotalFitnessTests {

        @Test
        @DisplayName("Calculate fitness combines all components")
        void calculateFitness() {
            Chromosome c = new Chromosome(new int[]{1, 2, 1, 2, 3});

            double fitness = calculator.calculateFitness(c);

            // Preference: Alice 100, Bob 100, Carol 100, Dave 80, Eve 100 = 480
            // Capacity: P1=2, P2=2, P3=1 -> all OK = 0
            // GPA: Dave (2.0) in P2 (req 2.5) -> 30
            // Partner: none -> 0
            // Total: 480 - 0 - 30 - 0 = 450
            assertEquals(450.0, fitness);
            assertTrue(c.isFitnessCalculated());
        }

        @Test
        @DisplayName("getBreakdown returns all components")
        void getBreakdown() {
            Chromosome c = new Chromosome(new int[]{1, 2, 1, 2, 3});

            FitnessCalculator.FitnessBreakdown breakdown = calculator.getBreakdown(c);

            assertNotNull(breakdown);
            assertTrue(breakdown.getPreferenceScore() > 0);
            assertEquals(breakdown.getTotalFitness(),
                    breakdown.getPreferenceScore() -
                            breakdown.getCapacityPenalty() -
                            breakdown.getGpaPenalty() -
                            breakdown.getPartnerPenalty());
        }
    }

    @Nested
    @DisplayName("Preference Distribution Tests")
    class PreferenceDistributionTests {

        @Test
        @DisplayName("Count preference distribution correctly")
        void countPreferenceDistribution() {
            // Alice -> P1 (1st), Bob -> P2 (1st), Carol -> P1 (1st),
            // Dave -> P2 (2nd), Eve -> P3 (1st)
            Chromosome c = new Chromosome(new int[]{1, 2, 1, 2, 3});

            int[] distribution = calculator.countPreferenceDistribution(c);

            // distribution[0] = no match, [1-5] = ranks 1-5
            assertEquals(0, distribution[0]);  // No unmatched
            assertEquals(4, distribution[1]);  // 4 first choices
            assertEquals(1, distribution[2]);  // 1 second choice
            assertEquals(0, distribution[3]);
            assertEquals(0, distribution[4]);
            assertEquals(0, distribution[5]);
        }
    }

}
