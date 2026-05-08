package com.optimatch.model;

// fitness stats for one generation, persisted alongside the run
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

    // empty stats
    public GenerationStats() {
    }

    // stats without run id (set later when persisting)
    public GenerationStats(int generation, double bestFitness, double averageFitness,
                           double worstFitness, double standardDeviation, int validCount,
                           double bestEverFitness) {
        this(0, generation, bestFitness, averageFitness, worstFitness,
                standardDeviation, validCount, bestEverFitness);
    }

    // full stats
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

    // db id
    public int getId() {
        return id;
    }

    // set db id
    public void setId(int id) {
        this.id = id;
    }

    // owning run id
    public int getRunId() {
        return runId;
    }

    // set owning run id
    public void setRunId(int runId) {
        this.runId = runId;
    }

    // generation number (0-based)
    public int getGeneration() {
        return generation;
    }

    // set generation number
    public void setGeneration(int generation) {
        this.generation = generation;
    }

    // best fitness this generation
    public double getBestFitness() {
        return bestFitness;
    }

    // set best fitness this generation
    public void setBestFitness(double bestFitness) {
        this.bestFitness = bestFitness;
    }

    // average fitness this generation
    public double getAverageFitness() {
        return averageFitness;
    }

    // set average fitness
    public void setAverageFitness(double averageFitness) {
        this.averageFitness = averageFitness;
    }

    // worst fitness this generation
    public double getWorstFitness() {
        return worstFitness;
    }

    // set worst fitness
    public void setWorstFitness(double worstFitness) {
        this.worstFitness = worstFitness;
    }

    // population std dev this generation
    public double getStandardDeviation() {
        return standardDeviation;
    }

    // set std dev
    public void setStandardDeviation(double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    // number of valid chromosomes this generation
    public int getValidCount() {
        return validCount;
    }

    // set valid count
    public void setValidCount(int validCount) {
        this.validCount = validCount;
    }

    // best fitness ever (cumulative across generations)
    public double getBestEverFitness() {
        return bestEverFitness;
    }

    // set best ever fitness
    public void setBestEverFitness(double bestEverFitness) {
        this.bestEverFitness = bestEverFitness;
    }

    @Override
    public String toString() {
        return String.format("GenerationStats{gen=%d, best=%.2f, avg=%.2f, worst=%.2f, valid=%d, bestEver=%.2f}",
                generation, bestFitness, averageFitness, worstFitness, validCount, bestEverFitness);
    }
}
