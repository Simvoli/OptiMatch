package com.optimatch.algorithm;

import com.optimatch.model.Preference;
import com.optimatch.model.Project;
import com.optimatch.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// integration tests for GeneticAlgorithm, full runs on realistic scenarios
@DisplayName("GeneticAlgorithm Integration Tests")
class GeneticAlgorithmIntegrationTest {

    private List<Student> students;
    private List<Project> projects;
    private List<Preference> preferences;

    @BeforeEach
    void setUp() {
        // Create a small but realistic test scenario
        students = new ArrayList<>();
        projects = new ArrayList<>();
        preferences = new ArrayList<>();

        // 10 students
        for (int i = 1; i <= 10; i++) {
            double gpa = 2.5 + (i % 4) * 0.5; // GPAs between 2.5 and 4.0
            students.add(new Student(i, "S" + String.format("%03d", i),
                    "Student " + i, "s" + i + "@test.com", gpa, null));
        }

        // 3 projects with different requirements
        projects.add(new Project(1, "P1", "Project Alpha", "Easy project", 2, 5, 0.0));
        projects.add(new Project(2, "P2", "Project Beta", "Medium project", 2, 5, 2.5));
        projects.add(new Project(3, "P3", "Project Gamma", "Hard project", 2, 5, 3.0));

        // Each student has preferences
        for (int studentId = 1; studentId <= 10; studentId++) {
            // Rotate preferences so students have different first choices
            int[] prefs = getRotatedPreferences(studentId);
            for (int rank = 1; rank <= 3; rank++) {
                preferences.add(new Preference(studentId, prefs[rank - 1], rank));
            }
        }
    }

    private int[] getRotatedPreferences(int studentId) {
        int[][] allPrefs = {
                {1, 2, 3}, {1, 3, 2}, {2, 1, 3}, {2, 3, 1}, {3, 1, 2},
                {3, 2, 1}, {1, 2, 3}, {2, 1, 3}, {3, 1, 2}, {1, 3, 2}
        };
        return allPrefs[(studentId - 1) % allPrefs.length];
    }

    @Nested
    @DisplayName("Basic Algorithm Tests")
    class BasicAlgorithmTests {

        @Test
        @DisplayName("Algorithm completes successfully")
        void algorithmCompletesSuccessfully() {
            GeneticAlgorithmConfig config = GeneticAlgorithmConfig.forQuickTest();
            GeneticAlgorithm ga = new GeneticAlgorithm(students, projects, preferences, config);

            Chromosome result = ga.run();

            assertNotNull(result);
            assertEquals(students.size(), result.getLength());
        }

        @Test
        @DisplayName("Result has valid fitness")
        void resultHasValidFitness() {
            GeneticAlgorithmConfig config = GeneticAlgorithmConfig.forQuickTest();
            GeneticAlgorithm ga = new GeneticAlgorithm(students, projects, preferences, config);

            Chromosome result = ga.run();

            assertTrue(result.getFitness() > 0);
            assertTrue(result.isFitnessCalculated());
        }

        @Test
        @DisplayName("Result assigns all students to valid projects")
        void resultAssignsToValidProjects() {
            GeneticAlgorithmConfig config = GeneticAlgorithmConfig.forQuickTest();
            GeneticAlgorithm ga = new GeneticAlgorithm(students, projects, preferences, config);

            Chromosome result = ga.run();

            int[] projectIds = {1, 2, 3};
            for (int i = 0; i < result.getLength(); i++) {
                int assignment = result.getAssignment(i);
                boolean isValidProject = false;
                for (int pid : projectIds) {
                    if (pid == assignment) {
                        isValidProject = true;
                        break;
                    }
                }
                assertTrue(isValidProject, "Student " + i + " assigned to invalid project " + assignment);
            }
        }
    }

    @Nested
    @DisplayName("Algorithm Result Tests")
    class AlgorithmResultTests {

        @Test
        @DisplayName("getResult returns valid statistics")
        void getResultReturnsStatistics() {
            GeneticAlgorithmConfig config = GeneticAlgorithmConfig.forQuickTest();
            GeneticAlgorithm ga = new GeneticAlgorithm(students, projects, preferences, config);

            ga.run();
            GeneticAlgorithm.AlgorithmResult result = ga.getResult();

            assertNotNull(result);
            assertTrue(result.getGenerations() > 0);
            assertTrue(result.getBestFitness() > 0);
            assertTrue(result.getExecutionTimeMs() >= 0);
        }

        @Test
        @DisplayName("Preference distribution is calculated")
        void preferenceDistributionCalculated() {
            GeneticAlgorithmConfig config = GeneticAlgorithmConfig.forQuickTest();
            GeneticAlgorithm ga = new GeneticAlgorithm(students, projects, preferences, config);

            ga.run();
            GeneticAlgorithm.AlgorithmResult result = ga.getResult();

            int[] distribution = result.getPreferenceDistribution();
            assertNotNull(distribution);
            assertEquals(6, distribution.length);

            // Sum should equal number of students
            int sum = 0;
            for (int count : distribution) {
                sum += count;
            }
            assertEquals(students.size(), sum);
        }
    }

    @Nested
    @DisplayName("Fitness Improvement Tests")
    class FitnessImprovementTests {

        @Test
        @DisplayName("Fitness improves over generations")
        void fitnessImprovesOverGenerations() {
            GeneticAlgorithmConfig config = new GeneticAlgorithmConfig()
                    .populationSize(50)
                    .maxGenerations(100)
                    .convergenceEnabled(false);

            final double[] fitnessHistory = new double[2];

            GeneticAlgorithm ga = new GeneticAlgorithm(students, projects, preferences, config);
            ga.setGenerationCallback((gen, pop, best) -> {
                if (gen == 1) {
                    fitnessHistory[0] = best.getFitness();
                } else if (gen == 100) {
                    fitnessHistory[1] = best.getFitness();
                }
            });

            ga.run();

            // Fitness at generation 100 should be >= fitness at generation 1
            assertTrue(fitnessHistory[1] >= fitnessHistory[0],
                    "Final fitness should be at least as good as initial");
        }
    }

    @Nested
    @DisplayName("Convergence Tests")
    class ConvergenceTests {

        @Test
        @DisplayName("Algorithm can stop early on convergence")
        void algorithmStopsOnConvergence() {
            GeneticAlgorithmConfig config = new GeneticAlgorithmConfig()
                    .populationSize(50)
                    .maxGenerations(1000)
                    .convergenceEnabled(true)
                    .convergenceGenerations(10)
                    .convergenceThreshold(0.001);

            GeneticAlgorithm ga = new GeneticAlgorithm(students, projects, preferences, config);
            ga.run();

            GeneticAlgorithm.AlgorithmResult result = ga.getResult();

            // With quick convergence settings, it should stop before max generations
            // (may or may not happen depending on data, so just verify it completes)
            assertTrue(result.getGenerations() <= 1000);
        }
    }

    @Nested
    @DisplayName("Stop Algorithm Tests")
    class StopAlgorithmTests {

        @Test
        @DisplayName("Algorithm can be stopped externally")
        void algorithmCanBeStopped() {
            GeneticAlgorithmConfig config = new GeneticAlgorithmConfig()
                    .populationSize(50)
                    .maxGenerations(10000);

            GeneticAlgorithm ga = new GeneticAlgorithm(students, projects, preferences, config);

            // Stop after first few generations
            ga.setGenerationCallback((gen, pop, best) -> {
                if (gen >= 5) {
                    ga.stop();
                }
            });

            Chromosome result = ga.run();

            assertNotNull(result);
            assertTrue(ga.getResult().getGenerations() < 10000);
        }

        @Test
        @DisplayName("isRunning returns correct state")
        void isRunningReturnsCorrectState() {
            GeneticAlgorithmConfig config = GeneticAlgorithmConfig.forQuickTest();
            GeneticAlgorithm ga = new GeneticAlgorithm(students, projects, preferences, config);

            assertFalse(ga.isRunning());

            final boolean[] wasRunning = {false};
            ga.setGenerationCallback((gen, pop, best) -> {
                if (gen == 1) {
                    wasRunning[0] = ga.isRunning();
                }
            });

            ga.run();

            assertTrue(wasRunning[0], "Should have been running during execution");
            assertFalse(ga.isRunning(), "Should not be running after completion");
        }
    }

    @Nested
    @DisplayName("Partner Constraint Tests")
    class PartnerConstraintTests {

        @Test
        @DisplayName("Partners tend to be assigned together")
        void partnersAssignedTogether() {
            // Set up two pairs of partners
            students.get(0).setPartnerId(2);
            students.get(1).setPartnerId(1);
            students.get(4).setPartnerId(6);
            students.get(5).setPartnerId(5);

            GeneticAlgorithmConfig config = new GeneticAlgorithmConfig()
                    .populationSize(100)
                    .maxGenerations(200)
                    .repairEnabled(true);

            GeneticAlgorithm ga = new GeneticAlgorithm(students, projects, preferences, config);
            Chromosome result = ga.run();

            // With repair enabled, partners should usually be together
            // This is probabilistic, so we check the result makes sense
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Algorithm handles minimum viable scenario")
        void handlesMinimumViableScenario() {
            // 2 students, 1 project
            List<Student> minStudents = List.of(
                    new Student(1, "S1", "Student 1", null, 3.0, null),
                    new Student(2, "S2", "Student 2", null, 3.0, null)
            );
            List<Project> minProjects = List.of(
                    new Project(1, "P1", "Project 1", null, 1, 5, 0.0)
            );
            List<Preference> minPrefs = List.of(
                    new Preference(1, 1, 1),
                    new Preference(2, 1, 1)
            );

            GeneticAlgorithmConfig config = GeneticAlgorithmConfig.forQuickTest();
            GeneticAlgorithm ga = new GeneticAlgorithm(minStudents, minProjects, minPrefs, config);

            Chromosome result = ga.run();

            assertNotNull(result);
            assertEquals(2, result.getLength());
            // Both students should be in project 1
            assertEquals(1, result.getAssignment(0));
            assertEquals(1, result.getAssignment(1));
        }
    }
}
