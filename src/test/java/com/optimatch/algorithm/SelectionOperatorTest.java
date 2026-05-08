package com.optimatch.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SelectionOperator Tests")
class SelectionOperatorTest {

    private Population population;
    private Random random;

    @BeforeEach
    void setUp() {
        random = new Random(42);
        population = new Population(10);
        for (int i = 0; i < 10; i++) {
            Chromosome c = new Chromosome(5);
            c.setFitness(i * 10.0); // 0, 10, 20, ..., 90
            population.addChromosome(c);
        }
    }

    @Nested
    @DisplayName("Tournament Selection Tests")
    class TournamentSelectionTests {

        @Test
        @DisplayName("Tournament selection returns a chromosome from the population")
        void tournamentSelectReturnsChromosome() {
            SelectionOperator selector = new SelectionOperator(random);

            Chromosome selected = selector.select(population);

            assertNotNull(selected);
            assertTrue(population.getChromosomes().contains(selected));
        }

        @Test
        @DisplayName("Larger tournament size biases towards fitter individuals")
        void largerTournamentBiasesTowardsFitter() {
            SelectionOperator selector = new SelectionOperator(new Random(123));
            selector.setTournamentSize(5);

            double totalFitness = 0;
            int selections = 100;
            for (int i = 0; i < selections; i++) {
                totalFitness += selector.select(population).getFitness();
            }
            double avgFitness = totalFitness / selections;

            // Population avg is 45 — tournament size 5 should consistently exceed it
            assertTrue(avgFitness > 50);
        }

        @Test
        @DisplayName("Tournament size cannot be less than 2")
        void tournamentSizeValidation() {
            SelectionOperator selector = new SelectionOperator(random);

            assertThrows(IllegalArgumentException.class, () -> selector.setTournamentSize(1));
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("Default tournament size is 3")
        void defaultTournamentSize() {
            SelectionOperator selector = new SelectionOperator(random);

            assertEquals(3, selector.getTournamentSize());
        }

        @Test
        @DisplayName("Can get and set tournament size")
        void tournamentSizeGetSet() {
            SelectionOperator selector = new SelectionOperator(random);

            selector.setTournamentSize(5);

            assertEquals(5, selector.getTournamentSize());
        }
    }
}
