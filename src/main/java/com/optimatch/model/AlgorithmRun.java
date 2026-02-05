package com.optimatch.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a single execution of the genetic algorithm.
 * Stores the parameters used and results achieved.
 */
public class AlgorithmRun {

    private int id;
    private LocalDateTime runTimestamp;
    private int populationSize;
    private int generations;
    private double mutationRate;
    private double crossoverRate;
    private double bestFitness;
    private long executionTimeMs;

    /**
     * Default constructor for AlgorithmRun.
     */
    public AlgorithmRun() {
        this.runTimestamp = LocalDateTime.now();
    }

    /**
     * Creates a new AlgorithmRun with the specified details.
     *
     * @param id              the database ID
     * @param runTimestamp    when the algorithm was executed
     * @param populationSize  the population size used
     * @param generations     the number of generations run
     * @param mutationRate    the mutation rate used
     * @param crossoverRate   the crossover rate used
     * @param bestFitness     the best fitness achieved
     * @param executionTimeMs the execution time in milliseconds
     */
    public AlgorithmRun(int id, LocalDateTime runTimestamp, int populationSize, int generations,
                        double mutationRate, double crossoverRate, double bestFitness, long executionTimeMs) {
        this.id = id;
        this.runTimestamp = runTimestamp;
        this.populationSize = populationSize;
        this.generations = generations;
        this.mutationRate = mutationRate;
        this.crossoverRate = crossoverRate;
        this.bestFitness = bestFitness;
        this.executionTimeMs = executionTimeMs;
    }

    /**
     * Gets the database ID.
     *
     * @return the database ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the database ID.
     *
     * @param id the database ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the run timestamp.
     *
     * @return when the algorithm was executed
     */
    public LocalDateTime getRunTimestamp() {
        return runTimestamp;
    }

    /**
     * Sets the run timestamp.
     *
     * @param runTimestamp when the algorithm was executed
     */
    public void setRunTimestamp(LocalDateTime runTimestamp) {
        this.runTimestamp = runTimestamp;
    }

    /**
     * Gets the population size.
     *
     * @return the population size used
     */
    public int getPopulationSize() {
        return populationSize;
    }

    /**
     * Sets the population size.
     *
     * @param populationSize the population size used
     */
    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    /**
     * Gets the number of generations.
     *
     * @return the number of generations run
     */
    public int getGenerations() {
        return generations;
    }

    /**
     * Sets the number of generations.
     *
     * @param generations the number of generations run
     */
    public void setGenerations(int generations) {
        this.generations = generations;
    }

    /**
     * Gets the mutation rate.
     *
     * @return the mutation rate used (0.0 to 1.0)
     */
    public double getMutationRate() {
        return mutationRate;
    }

    /**
     * Sets the mutation rate.
     *
     * @param mutationRate the mutation rate used (0.0 to 1.0)
     */
    public void setMutationRate(double mutationRate) {
        this.mutationRate = mutationRate;
    }

    /**
     * Gets the crossover rate.
     *
     * @return the crossover rate used (0.0 to 1.0)
     */
    public double getCrossoverRate() {
        return crossoverRate;
    }

    /**
     * Sets the crossover rate.
     *
     * @param crossoverRate the crossover rate used (0.0 to 1.0)
     */
    public void setCrossoverRate(double crossoverRate) {
        this.crossoverRate = crossoverRate;
    }

    /**
     * Gets the best fitness achieved.
     *
     * @return the best fitness score
     */
    public double getBestFitness() {
        return bestFitness;
    }

    /**
     * Sets the best fitness achieved.
     *
     * @param bestFitness the best fitness score
     */
    public void setBestFitness(double bestFitness) {
        this.bestFitness = bestFitness;
    }

    /**
     * Gets the execution time.
     *
     * @return the execution time in milliseconds
     */
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    /**
     * Sets the execution time.
     *
     * @param executionTimeMs the execution time in milliseconds
     */
    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    /**
     * Gets the execution time in seconds.
     *
     * @return the execution time in seconds
     */
    public double getExecutionTimeSeconds() {
        return executionTimeMs / 1000.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AlgorithmRun that = (AlgorithmRun) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AlgorithmRun{" +
                "id=" + id +
                ", runTimestamp=" + runTimestamp +
                ", populationSize=" + populationSize +
                ", generations=" + generations +
                ", bestFitness=" + bestFitness +
                ", executionTimeMs=" + executionTimeMs +
                '}';
    }
}
