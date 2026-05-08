package com.optimatch.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the MutationOperator class.
 */
@DisplayName("MutationOperator Tests")
class MutationOperatorTest {

    private Chromosome chromosome;
    private Random random;
    private int[] projectIds;

    @BeforeEach
    void setUp() {
        chromosome = new Chromosome(new int[]{1, 2, 3, 4, 5});
        random = new Random(42);
        projectIds = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    }

    @Nested
    @DisplayName("Swap Mutation Tests")
    class SwapMutationTests {

        @Test
        @DisplayName("Swap mutation exchanges gene values when applied per-gene")
        void swapMutationSwapsGenes() {
            // mutationRate=1.0 means every gene is considered for a swap.
            // Resulting state must differ from the original (multiset is preserved).
            MutationOperator mutation = MutationOperator.swap(random, 1.0);

            int[] originalAssignments = chromosome.getAssignments().clone();
            boolean mutated = mutation.mutate(chromosome);

            assertTrue(mutated, "mutate should return true when all genes are eligible for a swap");

            int[] sortedOriginal = originalAssignments.clone();
            int[] sortedAfter = chromosome.getAssignments();
            java.util.Arrays.sort(sortedOriginal);
            java.util.Arrays.sort(sortedAfter);
            assertArrayEquals(sortedOriginal, sortedAfter,
                    "Swap mutation must preserve the multiset of gene values");
        }

        @Test
        @DisplayName("Swap mutation at specific indices")
        void swapMutationAtIndices() {
            MutationOperator mutation = new MutationOperator(random);

            mutation.swapMutate(chromosome, 0, 4);

            assertEquals(5, chromosome.getAssignment(0)); // Was 1, now 5
            assertEquals(1, chromosome.getAssignment(4)); // Was 5, now 1
        }

        @Test
        @DisplayName("Multiple swaps perform correctly")
        void multiSwapMutate() {
            MutationOperator mutation = MutationOperator.swap(random, 1.0);

            int[] original = chromosome.getAssignments().clone();
            mutation.multiSwapMutate(chromosome, 3);

            // After multiple swaps, values should differ
            int changedCount = 0;
            for (int i = 0; i < chromosome.getLength(); i++) {
                if (chromosome.getAssignment(i) != original[i]) {
                    changedCount++;
                }
            }
            assertTrue(changedCount > 0);
        }
    }

    @Nested
    @DisplayName("Random Reset Mutation Tests")
    class RandomResetMutationTests {

        @Test
        @DisplayName("Random reset replaces every gene at rate 1.0")
        void randomResetChangesOneGene() {
            // With mutationRate=1.0 every gene is reset to a value from the
            // available project IDs, applied per gene.
            MutationOperator mutation = MutationOperator.randomReset(random, 1.0, projectIds);

            boolean mutated = mutation.mutate(chromosome);
            assertTrue(mutated);

            for (int i = 0; i < chromosome.getLength(); i++) {
                int value = chromosome.getAssignment(i);
                boolean valid = false;
                for (int pid : projectIds) {
                    if (pid == value) {
                        valid = true;
                        break;
                    }
                }
                assertTrue(valid, "Gene " + i + " must hold a valid project id after reset, got " + value);
            }
        }

        @Test
        @DisplayName("Random reset at specific index")
        void randomResetAtIndex() {
            MutationOperator mutation = MutationOperator.randomReset(random, 1.0, projectIds);

            mutation.randomResetMutate(chromosome, 2);

            // Value at index 2 should be from projectIds
            int newValue = chromosome.getAssignment(2);
            boolean isValidProject = false;
            for (int pid : projectIds) {
                if (pid == newValue) {
                    isValidProject = true;
                    break;
                }
            }
            assertTrue(isValidProject);
        }

        @Test
        @DisplayName("Random reset requires project IDs")
        void randomResetRequiresProjectIds() {
            MutationOperator mutation = new MutationOperator(random,
                    MutationOperator.MutationMethod.RANDOM_RESET, 1.0);

            assertThrows(IllegalStateException.class, () ->
                    mutation.mutate(chromosome));
        }

        @Test
        @DisplayName("Reset mutate to specific project")
        void resetMutateToSpecificProject() {
            MutationOperator mutation = new MutationOperator(random);

            mutation.resetMutate(chromosome, 2, 99);

            assertEquals(99, chromosome.getAssignment(2));
        }
    }

    @Nested
    @DisplayName("Scramble Mutation Tests")
    class ScrambleMutationTests {

        @Test
        @DisplayName("Scramble mutation shuffles a segment")
        void scrambleMutationShufflesSegment() {
            chromosome = new Chromosome(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
            MutationOperator mutation = MutationOperator.scramble(random, 1.0);

            int[] original = chromosome.getAssignments().clone();
            mutation.mutate(chromosome);

            // Check that values are preserved (just rearranged)
            int[] current = chromosome.getAssignments();
            java.util.Arrays.sort(original);
            java.util.Arrays.sort(current);
            assertArrayEquals(original, current);
        }

        @Test
        @DisplayName("Scramble mutation at specific segment")
        void scrambleMutateAtSegment() {
            chromosome = new Chromosome(new int[]{1, 2, 3, 4, 5});
            MutationOperator mutation = new MutationOperator(random);

            mutation.scrambleMutate(chromosome, 1, 3);

            // Indices 1-3 should be scrambled, 0 and 4 unchanged
            assertEquals(1, chromosome.getAssignment(0));
            assertEquals(5, chromosome.getAssignment(4));
        }
    }

    @Nested
    @DisplayName("Inversion Mutation Tests")
    class InversionMutationTests {

        @Test
        @DisplayName("Inversion mutation reverses a segment")
        void inversionMutationReversesSegment() {
            chromosome = new Chromosome(new int[]{1, 2, 3, 4, 5});
            MutationOperator mutation = new MutationOperator(random);

            mutation.inversionMutate(chromosome, 1, 3);

            // [1, 2, 3, 4, 5] with indices 1-3 inverted -> [1, 4, 3, 2, 5]
            assertEquals(1, chromosome.getAssignment(0));
            assertEquals(4, chromosome.getAssignment(1));
            assertEquals(3, chromosome.getAssignment(2));
            assertEquals(2, chromosome.getAssignment(3));
            assertEquals(5, chromosome.getAssignment(4));
        }

        @Test
        @DisplayName("Inversion mutation handles swapped indices")
        void inversionHandlesSwappedIndices() {
            chromosome = new Chromosome(new int[]{1, 2, 3, 4, 5});
            MutationOperator mutation = new MutationOperator(random);

            // Pass indices in wrong order
            mutation.inversionMutate(chromosome, 3, 1);

            // Should still work
            assertEquals(5, chromosome.getLength());
        }
    }

    @Nested
    @DisplayName("Mutation Rate Tests")
    class MutationRateTests {

        @Test
        @DisplayName("Mutation rate 0 does not mutate")
        void mutationRateZeroNoMutation() {
            MutationOperator mutation = MutationOperator.swap(random, 0.0);

            int[] original = chromosome.getAssignments().clone();
            boolean mutated = mutation.mutate(chromosome);

            assertFalse(mutated);
            assertArrayEquals(original, chromosome.getAssignments());
        }

        @Test
        @DisplayName("Mutation rate 1 always mutates")
        void mutationRateOneAlwaysMutates() {
            MutationOperator mutation = MutationOperator.swap(new Random(123), 1.0);

            int mutationCount = 0;
            for (int i = 0; i < 10; i++) {
                Chromosome c = new Chromosome(new int[]{1, 2, 3, 4, 5});
                if (mutation.mutate(c)) {
                    mutationCount++;
                }
            }

            assertEquals(10, mutationCount);
        }

        @Test
        @DisplayName("Invalid mutation rate throws exception")
        void invalidMutationRateThrows() {
            MutationOperator mutation = new MutationOperator(random);

            assertThrows(IllegalArgumentException.class, () ->
                    mutation.setMutationRate(-0.1));
            assertThrows(IllegalArgumentException.class, () ->
                    mutation.setMutationRate(1.1));
        }
    }

    @Nested
    @DisplayName("mutateAndCopy Tests")
    class MutateAndCopyTests {

        @Test
        @DisplayName("mutateAndCopy returns new chromosome")
        void mutateAndCopyReturnsNew() {
            MutationOperator mutation = MutationOperator.swap(random, 1.0);

            int[] original = chromosome.getAssignments().clone();
            Chromosome mutated = mutation.mutateAndCopy(chromosome);

            // Original should be unchanged
            assertArrayEquals(original, chromosome.getAssignments());
            // Mutated should be different
            assertNotEquals(chromosome, mutated);
        }
    }

    @Nested
    @DisplayName("Per-Gene Mutation Tests")
    class PerGeneMutationTests {

        @Test
        @DisplayName("mutatePerGene can mutate multiple genes")
        void mutatePerGeneMultipleGenes() {
            MutationOperator mutation = new MutationOperator(random);
            mutation.setAvailableProjectIds(projectIds);

            int mutatedCount = mutation.mutatePerGene(chromosome, 1.0);

            // With rate 1.0, all genes should be mutated
            assertEquals(chromosome.getLength(), mutatedCount);
        }

        @Test
        @DisplayName("mutatePerGene requires project IDs")
        void mutatePerGeneRequiresProjectIds() {
            MutationOperator mutation = new MutationOperator(random);

            assertThrows(IllegalStateException.class, () ->
                    mutation.mutatePerGene(chromosome, 0.5));
        }
    }

    @Nested
    @DisplayName("Adaptive Mutation Tests")
    class AdaptiveMutationTests {

        @Test
        @DisplayName("Adaptive mutation adjusts rate based on fitness")
        void adaptiveMutationAdjustsRate() {
            MutationOperator mutation = MutationOperator.swap(new Random(789), 0.5);

            // Low fitness should have higher mutation chance
            int lowFitnessMutations = 0;
            int highFitnessMutations = 0;

            for (int i = 0; i < 100; i++) {
                Chromosome c1 = new Chromosome(new int[]{1, 2, 3, 4, 5});
                if (mutation.adaptiveMutate(c1, 10, 100, 0.01, 0.5)) {
                    lowFitnessMutations++;
                }

                Chromosome c2 = new Chromosome(new int[]{1, 2, 3, 4, 5});
                if (mutation.adaptiveMutate(c2, 90, 100, 0.01, 0.5)) {
                    highFitnessMutations++;
                }
            }

            // Low fitness should mutate more often
            assertTrue(lowFitnessMutations > highFitnessMutations);
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("Default method is swap")
        void defaultMethodIsSwap() {
            MutationOperator mutation = new MutationOperator(random);

            assertEquals(MutationOperator.MutationMethod.SWAP, mutation.getMethod());
        }

        @Test
        @DisplayName("Can change mutation method")
        void canChangeMethod() {
            MutationOperator mutation = new MutationOperator(random);

            mutation.setMethod(MutationOperator.MutationMethod.SCRAMBLE);

            assertEquals(MutationOperator.MutationMethod.SCRAMBLE, mutation.getMethod());
        }

        @Test
        @DisplayName("Can get and set mutation rate")
        void mutationRateGetSet() {
            MutationOperator mutation = new MutationOperator(random);

            mutation.setMutationRate(0.03);

            assertEquals(0.03, mutation.getMutationRate());
        }
    }
}
