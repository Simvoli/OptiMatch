package com.optimatch.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

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
        @DisplayName("Crossover produces two offspring of correct length")
        void crossoverProducesTwoOffspring() {
            CrossoverOperator crossover = new CrossoverOperator(random);
            crossover.setCrossoverRate(1.0);

            Chromosome[] offspring = crossover.crossover(parent1, parent2);

            assertEquals(2, offspring.length);
            assertEquals(parent1.getLength(), offspring[0].getLength());
            assertEquals(parent2.getLength(), offspring[1].getLength());
        }

        @Test
        @DisplayName("Offspring contain genes from both parents")
        void offspringContainGenesFromBothParents() {
            CrossoverOperator crossover = new CrossoverOperator(new Random(123));
            crossover.setCrossoverRate(1.0);

            Chromosome[] offspring = crossover.crossover(parent1, parent2);

            boolean hasOne = false, hasTwo = false;
            for (int i = 0; i < offspring[0].getLength(); i++) {
                if (offspring[0].getAssignment(i) == 1) hasOne = true;
                if (offspring[0].getAssignment(i) == 2) hasTwo = true;
            }
            assertTrue(hasOne || hasTwo);
        }
    }

    @Nested
    @DisplayName("Crossover Rate Tests")
    class CrossoverRateTests {

        @Test
        @DisplayName("Crossover rate 0 returns copies of parents")
        void crossoverRateZeroReturnsCopies() {
            CrossoverOperator crossover = new CrossoverOperator(random);
            crossover.setCrossoverRate(0.0);

            Chromosome[] offspring = crossover.crossover(parent1, parent2);

            assertArrayEquals(parent1.getAssignments(), offspring[0].getAssignments());
            assertArrayEquals(parent2.getAssignments(), offspring[1].getAssignments());
        }

        @Test
        @DisplayName("Crossover rate 1 produces visible mixing in most runs")
        void crossoverRateOneAlwaysCrosses() {
            CrossoverOperator crossover = new CrossoverOperator(new Random(456));
            crossover.setCrossoverRate(1.0);

            int crossoverCount = 0;
            for (int i = 0; i < 10; i++) {
                Chromosome[] offspring = crossover.crossover(parent1, parent2);
                if (!offspring[0].equals(parent1) && !offspring[0].equals(parent2)) {
                    crossoverCount++;
                }
            }
            assertTrue(crossoverCount >= 8,
                    "Expected at least 8/10 crossovers to produce different offspring");
        }

        @Test
        @DisplayName("Invalid crossover rate throws exception")
        void invalidCrossoverRateThrows() {
            CrossoverOperator crossover = new CrossoverOperator(random);

            assertThrows(IllegalArgumentException.class, () -> crossover.setCrossoverRate(-0.1));
            assertThrows(IllegalArgumentException.class, () -> crossover.setCrossoverRate(1.1));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Single-gene chromosomes are returned as copies")
        void singleGeneNoCrossover() {
            CrossoverOperator crossover = new CrossoverOperator(random);
            crossover.setCrossoverRate(1.0);

            Chromosome p1 = new Chromosome(new int[]{1});
            Chromosome p2 = new Chromosome(new int[]{2});

            Chromosome[] offspring = crossover.crossover(p1, p2);

            assertArrayEquals(new int[]{1}, offspring[0].getAssignments());
            assertArrayEquals(new int[]{2}, offspring[1].getAssignments());
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("Can get and set crossover rate")
        void crossoverRateGetSet() {
            CrossoverOperator crossover = new CrossoverOperator(random);

            crossover.setCrossoverRate(0.75);

            assertEquals(0.75, crossover.getCrossoverRate());
        }
    }
}
