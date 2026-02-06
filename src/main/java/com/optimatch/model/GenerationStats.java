package com.optimatch.model;

/**
 * Represents statistics for a single generation of the genetic algorithm.
 * Used for persisting and displaying per-generation data.
 */
public class GenerationStats {

    private int id;
    private int runId;
    private int generation;
    private double bestFitness;
    private double averageFitness;
    private double worstFitness;
    private double standardDeviation;
    private int validCount;
    private double bestEverFitness;

    /**
     * Default constructor.
     */
    public GenerationStats() {
    }

    /**
     * Creates a GenerationStats with all fields except id.
     *
     * @param runId             the algorithm run ID
     * @param generation        the generation number (0-based)
     * @param bestFitness       best fitness in this generation
     * @param averageFitness    average fitness in this generation
     * @param worstFitness      worst fitness in this generation
     * @param standardDeviation fitness standard deviation
     * @param validCount        count of valid chromosomes
     * @param bestEverFitness   best fitness found so far (across all generations)
     */
    public GenerationStats(int runId, int generation, double bestFitness, double averageFitness,
                           double worstFitness, double standardDeviation, int validCount,
                           double bestEverFitness) {
        this.runId = runId;
        this.generation = generation;
        this.bestFitness = bestFitness;
        this.averageFitness = averageFitness;
        this.worstFitness = worstFitness;
        this.standardDeviation = standardDeviation;
        this.validCount = validCount;
        this.bestEverFitness = bestEverFitness;
    }

    // ==================== Getters and Setters ====================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRunId() {
        return runId;
    }

    public void setRunId(int runId) {
        this.runId = runId;
    }

    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    public double getBestFitness() {
        return bestFitness;
    }

    public void setBestFitness(double bestFitness) {
        this.bestFitness = bestFitness;
    }

    public double getAverageFitness() {
        return averageFitness;
    }

    public void setAverageFitness(double averageFitness) {
        this.averageFitness = averageFitness;
    }

    public double getWorstFitness() {
        return worstFitness;
    }

    public void setWorstFitness(double worstFitness) {
        this.worstFitness = worstFitness;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public int getValidCount() {
        return validCount;
    }

    public void setValidCount(int validCount) {
        this.validCount = validCount;
    }

    public double getBestEverFitness() {
        return bestEverFitness;
    }

    public void setBestEverFitness(double bestEverFitness) {
        this.bestEverFitness = bestEverFitness;
    }

    @Override
    public String toString() {
        return String.format("GenerationStats{gen=%d, best=%.2f, avg=%.2f, worst=%.2f, valid=%d, bestEver=%.2f}",
                generation, bestFitness, averageFitness, worstFitness, validCount, bestEverFitness);
    }
}
