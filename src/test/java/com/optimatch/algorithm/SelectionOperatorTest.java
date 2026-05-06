package com.optimatch.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the SelectionOperator class.
 */
@DisplayName("SelectionOperator Tests")
class SelectionOperatorTest {

    private Population population;
    private Random random;

    @BeforeEach
    void setUp() {
        random = new Random(42); // Fixed seed for reproducibility

        // Create a population with different fitness values
        population = new Population(10);
        for (int i = 0; i < 10; i++) {
            Chromosome c = new Chromosome(5);
            c.setFitness(i * 10.0); // Fitness: 0, 10, 20, ..., 90
            population.addChromosome(c);
        }
    }

    @Nested
    @DisplayName("Tournament Selection Tests")
    class TournamentSelectionTests {

        @Test
        @DisplayName("Tournament selection returns a chromosome")
        void tournamentSelectReturnsChromosome() {
            SelectionOperator selector = SelectionOperator.tournament(random, 3);

            Chromosome selected = selector.select(population);

            assertNotNull(selected);
        }

        @Test
        @DisplayName("Tournament selection with size 2 works")
        void tournamentSize2() {
            SelectionOperator selector = SelectionOperator.tournament(random, 2);

            Chromosome selected = selector.select(population);

            assertNotNull(selected);
            assertTrue(selected.getFitness() >= 0);
        }

        @Test
        @DisplayName("Larger tournament size biases towards fitter individuals")
        void largerTournamentBiasTowardsFitter() {
            SelectionOperator selector = SelectionOperator.tournament(new Random(123), 5);

            // Select many times and check average fitness
            double totalFitness = 0;
            int selections = 100;
            for (int i = 0; i < selections; i++) {
                Chromosome selected = selector.select(population);
                totalFitness += selected.getFitness();
            }
            double avgFitness = totalFitness / selections;

            // With tournament size 5, average should be high (biased towards best)
            // Population avg is 45, tournament selection should give higher
            assertTrue(avgFitness > 50);
        }

        @Test
        @DisplayName("Tournament size cannot be less than 2")
        void tournamentSizeValidation() {
            SelectionOperator selector = new SelectionOperator(random);

            assertThrows(IllegalArgumentException.class, () ->
                    selector.setTournamentSize(1));
        }
    }

    @Nested
    @DisplayName("Roulette Wheel Selection Tests")
    class RouletteWheelTests {

        @Test
        @DisplayName("Roulette wheel selection returns a chromosome")
        void rouletteSelectReturnsChromosome() {
            SelectionOperator selector = SelectionOperator.rouletteWheel(random);

            Chromosome selected = selector.select(population);

            assertNotNull(selected);
        }

        @Test
        @DisplayName("Roulette wheel handles population with zero total fitness")
        void rouletteHandlesZeroFitness() {
            Population zeroPop = new Population(5);
            for (int i = 0; i < 5; i++) {
                Chromosome c = new Chromosome(3);
                c.setFitness(0.0);
                zeroPop.addChromosome(c);
            }

            SelectionOperator selector = SelectionOperator.rouletteWheel(random);

            Chromosome selected = selector.select(zeroPop);

            assertNotNull(selected);
        }

        @Test
        @DisplayName("Roulette wheel handles negative fitness values")
        void rouletteHandlesNegativeFitness() {
            Population negativePop = new Population(5);
            for (int i = 0; i < 5; i++) {
                Chromosome c = new Chromosome(3);
                c.setFitness(-50.0 + i * 20); // -50, -30, -10, 10, 30
                negativePop.addChromosome(c);
            }

            SelectionOperator selector = SelectionOperator.rouletteWheel(random);

            Chromosome selected = selector.select(negativePop);

            assertNotNull(selected);
        }
    }

    @Nested
    @DisplayName("Rank Selection Tests")
    class RankSelectionTests {

        @Test
        @DisplayName("Rank selection returns a chromosome")
        void rankSelectReturnsChromosome() {
            SelectionOperator selector = SelectionOperator.rank(random);

            Chromosome selected = selector.select(population);

            assertNotNull(selected);
        }

        @Test
        @DisplayName("Rank selection biases towards higher ranked individuals")
        void rankBiasTowardsHigherRank() {
            SelectionOperator selector = SelectionOperator.rank(new Random(456));

            double totalFitness = 0;
            int selections = 100;
            for (int i = 0; i < selections; i++) {
                Chromosome selected = selector.select(population);
                totalFitness += selected.getFitness();
            }
            double avgFitness = totalFitness / selections;

            // Rank selection should bias towards fitter individuals
            assertTrue(avgFitness > 40);
        }
    }

    @Nested
    @DisplayName("Select Multiple Tests")
    class SelectMultipleTests {

        @Test
        @DisplayName("selectMultiple returns correct number of chromosomes")
        void selectMultipleReturnsCorrectCount() {
            SelectionOperator selector = new SelectionOperator(random);

            List<Chromosome> selected = selector.selectMultiple(population, 5);

            assertEquals(5, selected.size());
        }

        @Test
        @DisplayName("selectMultiple can select more than population size")
        void selectMultipleCanExceedPopulation() {
            SelectionOperator selector = new SelectionOperator(random);

            List<Chromosome> selected = selector.selectMultiple(population, 20);

            assertEquals(20, selected.size());
        }
    }

    @Nested
    @DisplayName("Select Parents Tests")
    class SelectParentsTests {

        @Test
        @DisplayName("selectParents returns two chromosomes")
        void selectParentsReturnsPair() {
            SelectionOperator selector = new SelectionOperator(random);

            Chromosome[] parents = selector.selectParents(population);

            assertEquals(2, parents.length);
            assertNotNull(parents[0]);
            assertNotNull(parents[1]);
        }

        @Test
        @DisplayName("selectParents tries to select different parents")
        void selectParentsTrysToSelectDifferent() {
            // With population of 10 different fitness values,
            // should usually get different parents
            SelectionOperator selector = new SelectionOperator(new Random(789));

            int differentCount = 0;
            for (int i = 0; i < 50; i++) {
                Chromosome[] parents = selector.selectParents(population);
                if (parents[0] != parents[1]) {
                    differentCount++;
                }
            }

            // Most selections should be different
            assertTrue(differentCount > 30);
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("Default method is tournament")
        void defaultMethodIsTournament() {
            SelectionOperator selector = new SelectionOperator(random);

            assertEquals(SelectionOperator.SelectionMethod.TOURNAMENT, selector.getMethod());
        }

        @Test
        @DisplayName("Can change selection method")
        void canChangeMethod() {
            SelectionOperator selector = new SelectionOperator(random);

            selector.setMethod(SelectionOperator.SelectionMethod.ROULETTE_WHEEL);

            assertEquals(SelectionOperator.SelectionMethod.ROULETTE_WHEEL, selector.getMethod());
        }

        @Test
        @DisplayName("Can get and set tournament size")
        void tournamentSizeGetSet() {
            SelectionOperator selector = new SelectionOperator(random);

            selector.setTournamentSize(5);

            assertEquals(5, selector.getTournamentSize());
        }
    }

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("tournament factory creates tournament selector")
        void tournamentFactory() {
            SelectionOperator selector = SelectionOperator.tournament(random, 4);

            assertEquals(SelectionOperator.SelectionMethod.TOURNAMENT, selector.getMethod());
            assertEquals(4, selector.getTournamentSize());
        }

        @Test
        @DisplayName("rouletteWheel factory creates roulette selector")
        void rouletteFactory() {
            SelectionOperator selector = SelectionOperator.rouletteWheel(random);

            assertEquals(SelectionOperator.SelectionMethod.ROULETTE_WHEEL, selector.getMethod());
        }

        @Test
        @DisplayName("rank factory creates rank selector")
        void rankFactory() {
            SelectionOperator selector = SelectionOperator.rank(random);

            assertEquals(SelectionOperator.SelectionMethod.RANK, selector.getMethod());
        }
    }
}
