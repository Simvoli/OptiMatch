package com.optimatch.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Population class.
 */
@DisplayName("Population Tests")
class PopulationTest {

    private Population population;

    @BeforeEach
    void setUp() {
        population = new Population(10);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor creates empty population with correct capacity")
        void constructorCreatesEmptyPopulation() {
            Population pop = new Population(50);

            assertEquals(0, pop.getCurrentSize());
            assertEquals(50, pop.getTargetSize());
        }
    }

    @Nested
    @DisplayName("Add Chromosome Tests")
    class AddChromosomeTests {

        @Test
        @DisplayName("addChromosome increases size")
        void addChromosomeIncreasesSize() {
            population.addChromosome(new Chromosome(5));
            population.addChromosome(new Chromosome(5));

            assertEquals(2, population.getCurrentSize());
        }

        @Test
        @DisplayName("getChromosome retrieves correct chromosome")
        void getChromosomeRetrievesCorrect() {
            Chromosome c1 = new Chromosome(new int[]{1, 2, 3});
            Chromosome c2 = new Chromosome(new int[]{4, 5, 6});

            population.addChromosome(c1);
            population.addChromosome(c2);

            assertEquals(c1, population.getChromosome(0));
            assertEquals(c2, population.getChromosome(1));
        }
    }

    @Nested
    @DisplayName("Best/Worst Fitness Tests")
    class BestWorstFitnessTests {

        @BeforeEach
        void populateWithFitness() {
            for (int i = 0; i < 5; i++) {
                Chromosome c = new Chromosome(3);
                c.setFitness((i + 1) * 10.0); // 10, 20, 30, 40, 50
                population.addChromosome(c);
            }
        }

        @Test
        @DisplayName("getBest returns highest fitness")
        void getBestChromosome() {
            Chromosome best = population.getBest();

            assertEquals(50.0, best.getFitness());
        }

        @Test
        @DisplayName("getWorst returns lowest fitness")
        void getWorstChromosome() {
            Chromosome worst = population.getWorst();

            assertEquals(10.0, worst.getFitness());
        }

        @Test
        @DisplayName("getBestFitness returns best fitness value")
        void getBestFitness() {
            assertEquals(50.0, population.getBestFitness());
        }

        @Test
        @DisplayName("getWorstFitness returns worst fitness value")
        void getWorstFitness() {
            assertEquals(10.0, population.getWorstFitness());
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @BeforeEach
        void populateWithFitness() {
            double[] fitnesses = {10.0, 20.0, 30.0, 40.0, 50.0};
            for (double fitness : fitnesses) {
                Chromosome c = new Chromosome(3);
                c.setFitness(fitness);
                population.addChromosome(c);
            }
        }

        @Test
        @DisplayName("getAverageFitness calculates correct average")
        void getAverageFitness() {
            double avg = population.getAverageFitness();

            // (10 + 20 + 30 + 40 + 50) / 5 = 30
            assertEquals(30.0, avg, 0.001);
        }

        @Test
        @DisplayName("getFitnessStdDev calculates standard deviation")
        void getFitnessStdDev() {
            double stdDev = population.getFitnessStdDev();

            // Standard deviation of 10, 20, 30, 40, 50 with mean 30
            // Variance = ((10-30)^2 + (20-30)^2 + (30-30)^2 + (40-30)^2 + (50-30)^2) / 5
            // = (400 + 100 + 0 + 100 + 400) / 5 = 200
            // StdDev = sqrt(200) ≈ 14.14
            assertTrue(stdDev > 14 && stdDev < 15);
        }
    }

    @Nested
    @DisplayName("Sort Tests")
    class SortTests {

        @Test
        @DisplayName("sortByFitness orders chromosomes descending")
        void sortByFitness() {
            // Add in random order
            Chromosome c1 = new Chromosome(3);
            c1.setFitness(30.0);
            Chromosome c2 = new Chromosome(3);
            c2.setFitness(50.0);
            Chromosome c3 = new Chromosome(3);
            c3.setFitness(10.0);

            population.addChromosome(c1);
            population.addChromosome(c2);
            population.addChromosome(c3);

            population.sortByFitness();

            assertEquals(50.0, population.getChromosome(0).getFitness());
            assertEquals(30.0, population.getChromosome(1).getFitness());
            assertEquals(10.0, population.getChromosome(2).getFitness());
        }
    }

    @Nested
    @DisplayName("Get Chromosomes Tests")
    class GetChromosomesTests {

        @Test
        @DisplayName("getChromosomes returns list of all chromosomes")
        void getChromosomes() {
            Chromosome c1 = new Chromosome(3);
            Chromosome c2 = new Chromosome(3);

            population.addChromosome(c1);
            population.addChromosome(c2);

            List<Chromosome> chromosomes = population.getChromosomes();

            assertEquals(2, chromosomes.size());
            assertTrue(chromosomes.contains(c1));
            assertTrue(chromosomes.contains(c2));
        }
    }

    @Nested
    @DisplayName("Clear Tests")
    class ClearTests {

        @Test
        @DisplayName("clear removes all chromosomes")
        void clearRemovesAll() {
            population.addChromosome(new Chromosome(3));
            population.addChromosome(new Chromosome(3));

            population.clear();

            assertEquals(0, population.getCurrentSize());
        }
    }

    @Nested
    @DisplayName("Full Population Tests")
    class FullPopulationTests {

        @Test
        @DisplayName("Population at capacity has currentSize equal to targetSize")
        void isFullWhenAtCapacity() {
            Population smallPop = new Population(3);
            smallPop.addChromosome(new Chromosome(2));
            smallPop.addChromosome(new Chromosome(2));
            smallPop.addChromosome(new Chromosome(2));

            assertEquals(smallPop.getTargetSize(), smallPop.getCurrentSize());
        }

        @Test
        @DisplayName("Population not at capacity has currentSize less than targetSize")
        void isFullWhenNotAtCapacity() {
            Population smallPop = new Population(3);
            smallPop.addChromosome(new Chromosome(2));
            smallPop.addChromosome(new Chromosome(2));

            assertTrue(smallPop.getCurrentSize() < smallPop.getTargetSize());
        }
    }

    @Nested
    @DisplayName("Empty Population Tests")
    class EmptyPopulationTests {

        @Test
        @DisplayName("getBest returns null for empty population")
        void getBestChromosomeEmpty() {
            assertNull(population.getBest());
        }

        @Test
        @DisplayName("getAverageFitness returns 0 for empty population")
        void getAverageFitnessEmpty() {
            assertEquals(0.0, population.getAverageFitness());
        }
    }

    @Nested
    @DisplayName("Replace Chromosome Tests")
    class ReplaceChromosomeTests {

        @Test
        @DisplayName("setChromosome replaces at index")
        void setChromosomeReplaces() {
            Chromosome original = new Chromosome(new int[]{1, 2, 3});
            Chromosome replacement = new Chromosome(new int[]{4, 5, 6});

            population.addChromosome(original);
            population.setChromosome(0, replacement);

            assertEquals(replacement, population.getChromosome(0));
        }
    }

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString contains population info")
        void toStringContainsInfo() {
            population.addChromosome(new Chromosome(3));
            population.addChromosome(new Chromosome(3));

            String str = population.toString();

            assertTrue(str.contains("2") || str.contains("size"));
        }
    }
}
