package com.optimatch.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Chromosome class.
 */
@DisplayName("Chromosome Tests")
class ChromosomeTest {

    private Chromosome chromosome;

    @BeforeEach
    void setUp() {
        chromosome = new Chromosome(5);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor with size creates chromosome with correct length")
        void constructorWithSize() {
            Chromosome c = new Chromosome(10);
            assertEquals(10, c.getLength());
        }

        @Test
        @DisplayName("Constructor with array copies assignments correctly")
        void constructorWithArray() {
            int[] assignments = {1, 2, 3, 4, 5};
            Chromosome c = new Chromosome(assignments);

            assertEquals(5, c.getLength());
            assertEquals(1, c.getAssignment(0));
            assertEquals(5, c.getAssignment(4));
        }

        @Test
        @DisplayName("Copy constructor creates independent copy")
        void copyConstructor() {
            int[] assignments = {1, 2, 3};
            Chromosome original = new Chromosome(assignments);
            original.setFitness(100.0);
            original.setValid(true);

            Chromosome copy = new Chromosome(original);

            assertEquals(original.getFitness(), copy.getFitness());
            assertEquals(original.isValid(), copy.isValid());
            assertArrayEquals(original.getAssignments(), copy.getAssignments());

            // Modify copy - should not affect original
            copy.setAssignment(0, 99);
            assertEquals(1, original.getAssignment(0));
        }

        @Test
        @DisplayName("createRandom creates valid random chromosome")
        void createRandom() {
            int[] projectIds = {10, 20, 30};
            Random random = new Random(42);

            Chromosome c = Chromosome.createRandom(5, projectIds, random);

            assertEquals(5, c.getLength());
            for (int i = 0; i < c.getLength(); i++) {
                int assignment = c.getAssignment(i);
                assertTrue(assignment == 10 || assignment == 20 || assignment == 30);
            }
        }
    }

    @Nested
    @DisplayName("Assignment Tests")
    class AssignmentTests {

        @Test
        @DisplayName("setAssignment and getAssignment work correctly")
        void setAndGetAssignment() {
            chromosome.setAssignment(0, 10);
            chromosome.setAssignment(4, 50);

            assertEquals(10, chromosome.getAssignment(0));
            assertEquals(50, chromosome.getAssignment(4));
        }

        @Test
        @DisplayName("getAssignments returns copy of assignments")
        void getAssignmentsCopiesArray() {
            int[] assignments = {1, 2, 3, 4, 5};
            Chromosome c = new Chromosome(assignments);

            int[] retrieved = c.getAssignments();
            retrieved[0] = 99;

            assertEquals(1, c.getAssignment(0));
        }

        @Test
        @DisplayName("swapAssignments swaps two assignments")
        void swapAssignments() {
            int[] assignments = {1, 2, 3, 4, 5};
            Chromosome c = new Chromosome(assignments);

            c.swapAssignments(0, 4);

            assertEquals(5, c.getAssignment(0));
            assertEquals(1, c.getAssignment(4));
        }
    }

    @Nested
    @DisplayName("Fitness Tests")
    class FitnessTests {

        @Test
        @DisplayName("Initial fitness is not calculated")
        void initialFitnessNotCalculated() {
            assertFalse(chromosome.isFitnessCalculated());
            assertEquals(0.0, chromosome.getFitness());
        }

        @Test
        @DisplayName("setFitness sets value and marks as calculated")
        void setFitness() {
            chromosome.setFitness(150.5);

            assertTrue(chromosome.isFitnessCalculated());
            assertEquals(150.5, chromosome.getFitness());
        }

        @Test
        @DisplayName("invalidateFitness resets fitness")
        void invalidateFitness() {
            chromosome.setFitness(100.0);
            chromosome.invalidateFitness();

            assertFalse(chromosome.isFitnessCalculated());
            assertEquals(0.0, chromosome.getFitness());
        }

        @Test
        @DisplayName("setAssignment invalidates fitness")
        void setAssignmentInvalidatesFitness() {
            chromosome.setFitness(100.0);
            chromosome.setAssignment(0, 10);

            assertFalse(chromosome.isFitnessCalculated());
        }

        @Test
        @DisplayName("swapAssignments invalidates fitness")
        void swapInvalidatesFitness() {
            int[] assignments = {1, 2, 3};
            Chromosome c = new Chromosome(assignments);
            c.setFitness(100.0);

            c.swapAssignments(0, 2);

            assertFalse(c.isFitnessCalculated());
        }
    }

    @Nested
    @DisplayName("Project Counting Tests")
    class ProjectCountingTests {

        @Test
        @DisplayName("countStudentsInProject counts correctly")
        void countStudentsInProject() {
            int[] assignments = {1, 2, 1, 1, 2};
            Chromosome c = new Chromosome(assignments);

            assertEquals(3, c.countStudentsInProject(1));
            assertEquals(2, c.countStudentsInProject(2));
            assertEquals(0, c.countStudentsInProject(3));
        }

        @Test
        @DisplayName("getStudentsInProject returns correct indices")
        void getStudentsInProject() {
            int[] assignments = {1, 2, 1, 1, 2};
            Chromosome c = new Chromosome(assignments);

            int[] studentsIn1 = c.getStudentsInProject(1);
            assertArrayEquals(new int[]{0, 2, 3}, studentsIn1);

            int[] studentsIn2 = c.getStudentsInProject(2);
            assertArrayEquals(new int[]{1, 4}, studentsIn2);
        }
    }

    @Nested
    @DisplayName("Comparison Tests")
    class ComparisonTests {

        @Test
        @DisplayName("compareTo returns correct ordering by fitness (descending)")
        void compareTo() {
            Chromosome c1 = new Chromosome(3);
            c1.setFitness(100.0);

            Chromosome c2 = new Chromosome(3);
            c2.setFitness(50.0);

            Chromosome c3 = new Chromosome(3);
            c3.setFitness(100.0);

            assertTrue(c1.compareTo(c2) < 0); // c1 is better (higher fitness)
            assertTrue(c2.compareTo(c1) > 0); // c2 is worse
            assertEquals(0, c1.compareTo(c3)); // Equal fitness
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Chromosomes with same assignments are equal")
        void equalsWithSameAssignments() {
            int[] assignments = {1, 2, 3};
            Chromosome c1 = new Chromosome(assignments);
            Chromosome c2 = new Chromosome(assignments);

            assertEquals(c1, c2);
            assertEquals(c1.hashCode(), c2.hashCode());
        }

        @Test
        @DisplayName("Chromosomes with different assignments are not equal")
        void notEqualsWithDifferentAssignments() {
            Chromosome c1 = new Chromosome(new int[]{1, 2, 3});
            Chromosome c2 = new Chromosome(new int[]{1, 2, 4});

            assertNotEquals(c1, c2);
        }

        @Test
        @DisplayName("Chromosome is equal to itself")
        void equalsSelf() {
            assertEquals(chromosome, chromosome);
        }

        @Test
        @DisplayName("Chromosome is not equal to null")
        void notEqualsNull() {
            assertNotEquals(null, chromosome);
        }
    }

    @Nested
    @DisplayName("Copy Tests")
    class CopyTests {

        @Test
        @DisplayName("copy creates independent copy")
        void copy() {
            int[] assignments = {1, 2, 3};
            Chromosome original = new Chromosome(assignments);
            original.setFitness(100.0);

            Chromosome copy = original.copy();

            assertEquals(original, copy);

            copy.setAssignment(0, 99);
            assertNotEquals(original.getAssignment(0), copy.getAssignment(0));
        }
    }

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString contains fitness and assignments")
        void toStringContainsInfo() {
            int[] assignments = {1, 2, 3};
            Chromosome c = new Chromosome(assignments);
            c.setFitness(100.5);

            String str = c.toString();
            // Use locale-agnostic checks (some locales use comma as decimal separator)
            assertTrue(str.contains("fitness"));
            assertTrue(str.contains("[1, 2, 3]"));
        }

        @Test
        @DisplayName("toShortString contains fitness and validity")
        void toShortString() {
            chromosome.setFitness(50.0);
            chromosome.setValid(true);

            String str = chromosome.toShortString();
            // Use locale-agnostic checks
            assertTrue(str.contains("fitness"));
            assertTrue(str.contains("true"));
        }
    }
}
