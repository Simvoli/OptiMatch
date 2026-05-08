package com.optimatch.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MutationOperator Tests")
class MutationOperatorTest {

    private Chromosome chromosome;
    private Random random;

    @BeforeEach
    void setUp() {
        chromosome = new Chromosome(new int[]{1, 2, 3, 4, 5});
        random = new Random(42);
    }

    @Nested
    @DisplayName("Per-gene Swap Mutation Tests")
    class SwapMutationTests {

        @Test
        @DisplayName("Mutation rate 1.0 mutates and preserves multiset of values")
        void mutationRateOnePreservesMultiset() {
            MutationOperator mutation = new MutationOperator(random);
            mutation.setMutationRate(1.0);

            int[] before = chromosome.getAssignments();
            boolean mutated = mutation.mutate(chromosome);

            assertTrue(mutated);
            int[] sortedBefore = before.clone();
            int[] sortedAfter = chromosome.getAssignments();
            Arrays.sort(sortedBefore);
            Arrays.sort(sortedAfter);
            assertArrayEquals(sortedBefore, sortedAfter,
                    "Swap mutation must preserve the multiset of gene values");
        }

        @Test
        @DisplayName("Mutation rate 0.0 leaves the chromosome unchanged")
        void mutationRateZeroNoChange() {
            MutationOperator mutation = new MutationOperator(random);
            mutation.setMutationRate(0.0);

            int[] before = chromosome.getAssignments();
            boolean mutated = mutation.mutate(chromosome);

            assertFalse(mutated);
            assertArrayEquals(before, chromosome.getAssignments());
        }

        @Test
        @DisplayName("Single-gene chromosome cannot be mutated")
        void singleGeneNotMutated() {
            MutationOperator mutation = new MutationOperator(random);
            mutation.setMutationRate(1.0);

            Chromosome single = new Chromosome(new int[]{42});
            boolean mutated = mutation.mutate(single);

            assertFalse(mutated);
            assertArrayEquals(new int[]{42}, single.getAssignments());
        }
    }

    @Nested
    @DisplayName("Mutation Rate Tests")
    class MutationRateTests {

        @Test
        @DisplayName("Default mutation rate is 0.02")
        void defaultMutationRate() {
            MutationOperator mutation = new MutationOperator(random);

            assertEquals(0.02, mutation.getMutationRate());
        }

        @Test
        @DisplayName("Can get and set mutation rate")
        void mutationRateGetSet() {
            MutationOperator mutation = new MutationOperator(random);

            mutation.setMutationRate(0.05);

            assertEquals(0.05, mutation.getMutationRate());
        }

        @Test
        @DisplayName("Invalid mutation rate throws exception")
        void invalidMutationRateThrows() {
            MutationOperator mutation = new MutationOperator(random);

            assertThrows(IllegalArgumentException.class, () -> mutation.setMutationRate(-0.1));
            assertThrows(IllegalArgumentException.class, () -> mutation.setMutationRate(1.1));
        }
    }
}
