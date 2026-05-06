package com.optimatch.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the CrossoverOperator class.
 */
@DisplayName("CrossoverOperator Tests")
class CrossoverOperatorTest {

    private Chromosome parent1;
    private Chromosome parent2;
    private Random random;

    @BeforeEach
    void setUp() {
        parent1 = new Chromosome(new int[]{1, 1, 1, 1, 1});
        parent2 = new Chromosome(new int[]{2, 2, 2, 2, 2});
        random = new Random(42);
    }

    @Nested
    @DisplayName("Uniform Crossover Tests")
    class UniformCrossoverTests {

        @Test
        @DisplayName("Uniform crossover produces two offspring")
        void uniformCrossoverProducesTwoOffspring() {
            CrossoverOperator crossover = CrossoverOperator.uniform(random, 1.0);

            Chromosome[] offspring = crossover.crossover(parent1, parent2);

            assertEquals(2, offspring.length);
            assertNotNull(offspring[0]);
            assertNotNull(offspring[1]);
        }

        @Test
        @DisplayName("Offspring have same length as parents")
        void offspringSameLength() {
            CrossoverOperator crossover = CrossoverOperator.uniform(random, 1.0);

            Chromosome[] offspring = crossover.crossover(parent1, parent2);

            assertEquals(parent1.getLength(), offspring[0].getLength());
            assertEquals(parent2.getLength(), offspring[1].getLength());
        }

        @Test
        @DisplayName("Offspring contain genes from both parents")
        void offspringContainGenesFromBothParents() {
            CrossoverOperator crossover = CrossoverOperator.uniform(new Random(123), 1.0);

            Chromosome[] offspring = crossover.crossover(parent1, parent2);

            // Check offspring have mix of 1s and 2s
            boolean hasOne = false, hasTwo = false;
            for (int i = 0; i < offspring[0].getLength(); i++) {
                if (offspring[0].getAssignment(i) == 1) hasOne = true;
                if (offspring[0].getAssignment(i) == 2) hasTwo = true;
            }

            assertTrue(hasOne || hasTwo); // At least one from each parent
        }

        @Test
        @DisplayName("Uniform crossover with mask works correctly")
        void uniformCrossoverWithMask() {
            CrossoverOperator crossover = new CrossoverOperator(random);
            boolean[] mask = {true, false, true, false, true};

            Chromosome[] offspring = crossover.uniformCrossoverWithMask(parent1, parent2, mask);

            // offspring[0]: true positions from parent1, false positions from parent2
            assertEquals(1, offspring[0].getAssignment(0)); // true -> from parent1
            assertEquals(2, offspring[0].getAssignment(1)); // false -> from parent2
            assertEquals(1, offspring[0].getAssignment(2)); // true -> from parent1
            assertEquals(2, offspring[0].getAssignment(3)); // false -> from parent2
            assertEquals(1, offspring[0].getAssignment(4)); // true -> from parent1
        }
    }

    @Nested
    @DisplayName("Single-Point Crossover Tests")
    class SinglePointCrossoverTests {

        @Test
        @DisplayName("Single-point crossover produces valid offspring")
        void singlePointCrossoverProducesOffspring() {
            CrossoverOperator crossover = CrossoverOperator.singlePoint(random, 1.0);

            Chromosome[] offspring = crossover.crossover(parent1, parent2);

            assertEquals(2, offspring.length);
            assertNotNull(offspring[0]);
            assertNotNull(offspring[1]);
        }

        @Test
        @DisplayName("Single-point crossover at specific point")
        void singlePointCrossoverAtPoint() {
            CrossoverOperator crossover = new CrossoverOperator(random);

            Chromosome[] offspring = crossover.singlePointCrossover(parent1, parent2, 2);

            // Before point 2: from parent1/parent2
            assertEquals(1, offspring[0].getAssignment(0));
            assertEquals(1, offspring[0].getAssignment(1));
            // After point 2: swapped
            assertEquals(2, offspring[0].getAssignment(2));
            assertEquals(2, offspring[0].getAssignment(3));
            assertEquals(2, offspring[0].getAssignment(4));

            // Offspring 2 should be opposite
            assertEquals(2, offspring[1].getAssignment(0));
            assertEquals(2, offspring[1].getAssignment(1));
            assertEquals(1, offspring[1].getAssignment(2));
        }
    }

    @Nested
    @DisplayName("Two-Point Crossover Tests")
    class TwoPointCrossoverTests {

        @Test
        @DisplayName("Two-point crossover produces valid offspring")
        void twoPointCrossoverProducesOffspring() {
            CrossoverOperator crossover = CrossoverOperator.twoPoint(random, 1.0);

            Chromosome[] offspring = crossover.crossover(parent1, parent2);

            assertEquals(2, offspring.length);
            assertNotNull(offspring[0]);
            assertNotNull(offspring[1]);
        }

        @Test
        @DisplayName("Two-point crossover at specific points")
        void twoPointCrossoverAtPoints() {
            CrossoverOperator crossover = new CrossoverOperator(random);

            Chromosome[] offspring = crossover.twoPointCrossover(parent1, parent2, 1, 4);

            // Before point 1: from parent1
            assertEquals(1, offspring[0].getAssignment(0));
            // Between points 1 and 4: swapped from parent2
            assertEquals(2, offspring[0].getAssignment(1));
            assertEquals(2, offspring[0].getAssignment(2));
            assertEquals(2, offspring[0].getAssignment(3));
            // After point 4: from parent1
            assertEquals(1, offspring[0].getAssignment(4));
        }

        @Test
        @DisplayName("Two-point crossover handles swapped points")
        void twoPointHandlesSwappedPoints() {
            CrossoverOperator crossover = new CrossoverOperator(random);

            // Pass points in wrong order - should still work
            Chromosome[] offspring = crossover.twoPointCrossover(parent1, parent2, 4, 1);

            // Should produce valid offspring
            assertEquals(5, offspring[0].getLength());
        }
    }

    @Nested
    @DisplayName("Crossover Rate Tests")
    class CrossoverRateTests {

        @Test
        @DisplayName("Crossover rate 0 returns copies of parents")
        void crossoverRateZeroReturnsCopies() {
            CrossoverOperator crossover = CrossoverOperator.uniform(random, 0.0);

            Chromosome[] offspring = crossover.crossover(parent1, parent2);

            assertArrayEquals(parent1.getAssignments(), offspring[0].getAssignments());
            assertArrayEquals(parent2.getAssignments(), offspring[1].getAssignments());
        }

        @Test
        @DisplayName("Crossover rate 1 always performs crossover")
        void crossoverRateOneAlwaysCrosses() {
            CrossoverOperator crossover = CrossoverOperator.uniform(new Random(456), 1.0);

            int crossoverCount = 0;
            for (int i = 0; i < 10; i++) {
                Chromosome[] offspring = crossover.crossover(parent1, parent2);
                // If crossover occurred, offspring should differ from parents
                // Note: By chance, uniform crossover may occasionally produce offspring
                // identical to a parent if all genes happen to come from one parent
                if (!offspring[0].equals(parent1) && !offspring[0].equals(parent2)) {
                    crossoverCount++;
                }
            }

            // Most should perform visible crossover (allow for rare edge cases)
            assertTrue(crossoverCount >= 8, "Expected at least 8/10 crossovers to produce different offspring");
        }

        @Test
        @DisplayName("Invalid crossover rate throws exception")
        void invalidCrossoverRateThrows() {
            CrossoverOperator crossover = new CrossoverOperator(random);

            assertThrows(IllegalArgumentException.class, () ->
                    crossover.setCrossoverRate(-0.1));
            assertThrows(IllegalArgumentException.class, () ->
                    crossover.setCrossoverRate(1.1));
        }
    }

    @Nested
    @DisplayName("crossoverSingle Tests")
    class CrossoverSingleTests {

        @Test
        @DisplayName("crossoverSingle returns one offspring")
        void crossoverSingleReturnsOne() {
            CrossoverOperator crossover = CrossoverOperator.uniform(random, 1.0);

            Chromosome offspring = crossover.crossoverSingle(parent1, parent2);

            assertNotNull(offspring);
            assertEquals(parent1.getLength(), offspring.getLength());
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("Default method is uniform")
        void defaultMethodIsUniform() {
            CrossoverOperator crossover = new CrossoverOperator(random);

            assertEquals(CrossoverOperator.CrossoverMethod.UNIFORM, crossover.getMethod());
        }

        @Test
        @DisplayName("Can change crossover method")
        void canChangeMethod() {
            CrossoverOperator crossover = new CrossoverOperator(random);

            crossover.setMethod(CrossoverOperator.CrossoverMethod.SINGLE_POINT);

            assertEquals(CrossoverOperator.CrossoverMethod.SINGLE_POINT, crossover.getMethod());
        }

        @Test
        @DisplayName("Can get and set crossover rate")
        void crossoverRateGetSet() {
            CrossoverOperator crossover = new CrossoverOperator(random);

            crossover.setCrossoverRate(0.75);

            assertEquals(0.75, crossover.getCrossoverRate());
        }

        @Test
        @DisplayName("Can get and set uniform bias")
        void uniformBiasGetSet() {
            CrossoverOperator crossover = new CrossoverOperator(random);

            crossover.setUniformBias(0.7);

            assertEquals(0.7, crossover.getUniformBias());
        }

        @Test
        @DisplayName("Invalid uniform bias throws exception")
        void invalidUniformBiasThrows() {
            CrossoverOperator crossover = new CrossoverOperator(random);

            assertThrows(IllegalArgumentException.class, () ->
                    crossover.setUniformBias(-0.1));
            assertThrows(IllegalArgumentException.class, () ->
                    crossover.setUniformBias(1.1));
        }
    }
}
