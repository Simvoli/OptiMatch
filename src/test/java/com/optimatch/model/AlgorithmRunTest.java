package com.optimatch.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the AlgorithmRun model class.
 */
@DisplayName("AlgorithmRun Model Tests")
class AlgorithmRunTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor sets current timestamp")
        void defaultConstructorSetsTimestamp() {
            AlgorithmRun run = new AlgorithmRun();

            assertNotNull(run.getRunTimestamp());
            // Timestamp should be very recent (within last second)
            assertTrue(run.getRunTimestamp().isAfter(LocalDateTime.now().minusSeconds(1)));
        }

        @Test
        @DisplayName("Full constructor sets all fields")
        void fullConstructor() {
            LocalDateTime timestamp = LocalDateTime.of(2026, 1, 28, 10, 30, 0);
            AlgorithmRun run = new AlgorithmRun(1, timestamp, 200, 1000, 0.02, 0.8, 450.5, 5000);

            assertEquals(1, run.getId());
            assertEquals(timestamp, run.getRunTimestamp());
            assertEquals(200, run.getPopulationSize());
            assertEquals(1000, run.getGenerations());
            assertEquals(0.02, run.getMutationRate());
            assertEquals(0.8, run.getCrossoverRate());
            assertEquals(450.5, run.getBestFitness());
            assertEquals(5000, run.getExecutionTimeMs());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Set and get all fields")
        void setAndGetAllFields() {
            AlgorithmRun run = new AlgorithmRun();
            LocalDateTime timestamp = LocalDateTime.now();

            run.setId(5);
            run.setRunTimestamp(timestamp);
            run.setPopulationSize(300);
            run.setGenerations(1500);
            run.setMutationRate(0.03);
            run.setCrossoverRate(0.85);
            run.setBestFitness(500.0);
            run.setExecutionTimeMs(10000);

            assertEquals(5, run.getId());
            assertEquals(timestamp, run.getRunTimestamp());
            assertEquals(300, run.getPopulationSize());
            assertEquals(1500, run.getGenerations());
            assertEquals(0.03, run.getMutationRate());
            assertEquals(0.85, run.getCrossoverRate());
            assertEquals(500.0, run.getBestFitness());
            assertEquals(10000, run.getExecutionTimeMs());
        }
    }

    @Nested
    @DisplayName("Execution Time Tests")
    class ExecutionTimeTests {

        @Test
        @DisplayName("getExecutionTimeSeconds converts correctly")
        void executionTimeSeconds() {
            AlgorithmRun run = new AlgorithmRun();
            run.setExecutionTimeMs(5500);

            assertEquals(5.5, run.getExecutionTimeSeconds(), 0.001);
        }

        @Test
        @DisplayName("getExecutionTimeSeconds handles zero")
        void executionTimeSecondsZero() {
            AlgorithmRun run = new AlgorithmRun();
            run.setExecutionTimeMs(0);

            assertEquals(0.0, run.getExecutionTimeSeconds());
        }

        @Test
        @DisplayName("getExecutionTimeSeconds handles large values")
        void executionTimeSecondsLarge() {
            AlgorithmRun run = new AlgorithmRun();
            run.setExecutionTimeMs(120000); // 2 minutes

            assertEquals(120.0, run.getExecutionTimeSeconds(), 0.001);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Runs with same ID are equal")
        void equalsWithSameId() {
            AlgorithmRun r1 = new AlgorithmRun();
            r1.setId(1);
            r1.setPopulationSize(100);

            AlgorithmRun r2 = new AlgorithmRun();
            r2.setId(1);
            r2.setPopulationSize(200);

            assertEquals(r1, r2);
            assertEquals(r1.hashCode(), r2.hashCode());
        }

        @Test
        @DisplayName("Runs with different IDs are not equal")
        void notEqualsWithDifferentId() {
            AlgorithmRun r1 = new AlgorithmRun();
            r1.setId(1);

            AlgorithmRun r2 = new AlgorithmRun();
            r2.setId(2);

            assertNotEquals(r1, r2);
        }

        @Test
        @DisplayName("Run is equal to itself")
        void equalsSelf() {
            AlgorithmRun run = new AlgorithmRun();
            run.setId(1);

            assertEquals(run, run);
        }

        @Test
        @DisplayName("Run is not equal to null")
        void notEqualsNull() {
            AlgorithmRun run = new AlgorithmRun();
            run.setId(1);

            assertNotEquals(null, run);
        }
    }

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString contains run information")
        void toStringContainsInfo() {
            AlgorithmRun run = new AlgorithmRun();
            run.setId(1);
            run.setPopulationSize(200);
            run.setGenerations(1000);
            run.setBestFitness(450.5);

            String str = run.toString();

            assertTrue(str.contains("id=1"));
            assertTrue(str.contains("populationSize=200"));
            assertTrue(str.contains("generations=1000"));
            assertTrue(str.contains("bestFitness=450.5"));
        }
    }
}
