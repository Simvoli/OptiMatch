package com.optimatch.algorithm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// unit tests for GeneticAlgorithmConfig
@DisplayName("GeneticAlgorithmConfig Tests")
class GeneticAlgorithmConfigTest {

    @Nested
    @DisplayName("Default Configuration Tests")
    class DefaultConfigTests {

        @Test
        @DisplayName("Default config has reasonable defaults")
        void defaultConfigHasDefaults() {
            GeneticAlgorithmConfig config = new GeneticAlgorithmConfig();

            assertTrue(config.getPopulationSize() >= 100);
            assertTrue(config.getMaxGenerations() >= 100);
            assertTrue(config.getMutationRate() >= 0.01 && config.getMutationRate() <= 0.1);
            assertTrue(config.getCrossoverRate() >= 0.5 && config.getCrossoverRate() <= 1.0);
            assertTrue(config.getElitePercentage() >= 0.01 && config.getElitePercentage() <= 0.2);
            assertTrue(config.getTournamentSize() >= 2);
        }
    }

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderPatternTests {

        @Test
        @DisplayName("Builder pattern sets all values")
        void builderSetsValues() {
            GeneticAlgorithmConfig config = new GeneticAlgorithmConfig()
                    .populationSize(250)
                    .maxGenerations(1500)
                    .mutationRate(0.03)
                    .crossoverRate(0.85)
                    .elitePercentage(0.08)
                    .tournamentSize(4);

            assertEquals(250, config.getPopulationSize());
            assertEquals(1500, config.getMaxGenerations());
            assertEquals(0.03, config.getMutationRate());
            assertEquals(0.85, config.getCrossoverRate());
            assertEquals(0.08, config.getElitePercentage());
            assertEquals(4, config.getTournamentSize());
        }

        @Test
        @DisplayName("Builder returns same instance for chaining")
        void builderReturnsInstance() {
            GeneticAlgorithmConfig config = new GeneticAlgorithmConfig();

            GeneticAlgorithmConfig result = config.populationSize(100);

            assertSame(config, result);
        }
    }

    @Nested
    @DisplayName("Convergence Configuration Tests")
    class ConvergenceConfigTests {

        @Test
        @DisplayName("Convergence settings can be configured")
        void convergenceSettings() {
            GeneticAlgorithmConfig config = new GeneticAlgorithmConfig()
                    .convergenceEnabled(true)
                    .convergenceGenerations(75)
                    .convergenceThreshold(0.001);

            assertTrue(config.isConvergenceEnabled());
            assertEquals(75, config.getConvergenceGenerations());
            assertEquals(0.001, config.getConvergenceThreshold());
        }

        @Test
        @DisplayName("Convergence can be disabled")
        void convergenceDisabled() {
            GeneticAlgorithmConfig config = new GeneticAlgorithmConfig()
                    .convergenceEnabled(false);

            assertFalse(config.isConvergenceEnabled());
        }
    }

    @Nested
    @DisplayName("Repair Configuration Tests")
    class RepairConfigTests {

        @Test
        @DisplayName("Repair setting can be configured")
        void repairSettings() {
            GeneticAlgorithmConfig config = new GeneticAlgorithmConfig()
                    .repairEnabled(true);

            assertTrue(config.isRepairEnabled());
        }

        @Test
        @DisplayName("Repair can be disabled")
        void repairDisabled() {
            GeneticAlgorithmConfig config = new GeneticAlgorithmConfig()
                    .repairEnabled(false);

            assertFalse(config.isRepairEnabled());
        }
    }

    @Nested
    @DisplayName("Preset Configuration Tests")
    class PresetConfigTests {

        @Test
        @DisplayName("Small dataset preset has smaller parameters")
        void smallDatasetPreset() {
            GeneticAlgorithmConfig config = GeneticAlgorithmConfig.forSmallDataset();

            assertTrue(config.getPopulationSize() <= 150);
            assertTrue(config.getMaxGenerations() <= 500);
        }

        @Test
        @DisplayName("Medium dataset preset has medium parameters")
        void mediumDatasetPreset() {
            GeneticAlgorithmConfig config = GeneticAlgorithmConfig.forMediumDataset();

            assertTrue(config.getPopulationSize() >= 100 && config.getPopulationSize() <= 300);
            assertTrue(config.getMaxGenerations() >= 500 && config.getMaxGenerations() <= 1500);
        }

        @Test
        @DisplayName("Large dataset preset has larger parameters")
        void largeDatasetPreset() {
            GeneticAlgorithmConfig config = GeneticAlgorithmConfig.forLargeDataset();

            assertTrue(config.getPopulationSize() >= 200);
            assertTrue(config.getMaxGenerations() >= 1000);
        }

        @Test
        @DisplayName("Quick test preset has minimal parameters")
        void quickTestPreset() {
            GeneticAlgorithmConfig config = GeneticAlgorithmConfig.forQuickTest();

            assertTrue(config.getPopulationSize() <= 50);
            assertTrue(config.getMaxGenerations() <= 100);
        }
    }

    @Nested
    @DisplayName("Elite Percentage Tests")
    class ElitePercentageTests {

        @Test
        @DisplayName("Elite count can be calculated from percentage")
        void eliteCountCalculation() {
            GeneticAlgorithmConfig config = new GeneticAlgorithmConfig()
                    .populationSize(100)
                    .elitePercentage(0.1);

            int eliteCount = (int) (config.getPopulationSize() * config.getElitePercentage());

            assertEquals(10, eliteCount);
        }

        @Test
        @DisplayName("Elite percentage is stored correctly")
        void elitePercentageStored() {
            GeneticAlgorithmConfig config = new GeneticAlgorithmConfig()
                    .populationSize(10)
                    .elitePercentage(0.05);

            assertEquals(0.05, config.getElitePercentage());
        }
    }

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString contains config information")
        void toStringContainsInfo() {
            GeneticAlgorithmConfig config = new GeneticAlgorithmConfig()
                    .populationSize(200)
                    .maxGenerations(1000);

            String str = config.toString();

            assertTrue(str.contains("200") || str.contains("population"));
            assertTrue(str.contains("1000") || str.contains("generation"));
        }
    }
}
