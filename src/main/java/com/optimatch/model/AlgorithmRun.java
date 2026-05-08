package com.optimatch.model;

import java.time.LocalDateTime;
import java.util.Objects;

// metadata for one GA run: parameters, fitness, runtime
public class AlgorithmRun {

    private int id;
    private LocalDateTime runTimestamp;
    private int populationSize;
    private int generations;
    private double mutationRate;
    private double crossoverRate;
    private double bestFitness;
    private long executionTimeMs;

    // empty run with current timestamp
    public AlgorithmRun() {
        this.runTimestamp = LocalDateTime.now();
    }

    // full run
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

    // db id
    public int getId() {
        return id;
    }

    // set db id
    public void setId(int id) {
        this.id = id;
    }

    // when the run started
    public LocalDateTime getRunTimestamp() {
        return runTimestamp;
    }

    // set start timestamp
    public void setRunTimestamp(LocalDateTime runTimestamp) {
        this.runTimestamp = runTimestamp;
    }

    // population size used
    public int getPopulationSize() {
        return populationSize;
    }

    // set population size
    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    // generations actually run
    public int getGenerations() {
        return generations;
    }

    // set generations count
    public void setGenerations(int generations) {
        this.generations = generations;
    }

    // mutation rate used
    public double getMutationRate() {
        return mutationRate;
    }

    // set mutation rate
    public void setMutationRate(double mutationRate) {
        this.mutationRate = mutationRate;
    }

    // crossover rate used
    public double getCrossoverRate() {
        return crossoverRate;
    }

    // set crossover rate
    public void setCrossoverRate(double crossoverRate) {
        this.crossoverRate = crossoverRate;
    }

    // best fitness reached
    public double getBestFitness() {
        return bestFitness;
    }

    // set best fitness
    public void setBestFitness(double bestFitness) {
        this.bestFitness = bestFitness;
    }

    // wall clock time in ms
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    // set wall clock time
    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    // wall clock time in seconds
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
