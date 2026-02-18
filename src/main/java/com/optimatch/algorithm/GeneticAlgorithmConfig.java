package com.optimatch.algorithm;

/**
 * Configuration for the Genetic Algorithm.
 *
 * Recommended ranges:
 * - Population size: 100-500
 * - Max generations: 500-2000
 * - Mutation rate: 0.01-0.05
 * - Crossover rate: 0.7-0.9
 * - Elite percentage: 0.05-0.10 (5-10%)
 * - Tournament size: 3-5
 */
public class GeneticAlgorithmConfig {

    // Population settings
    private int populationSize = 200;
    private int maxGenerations = 1000;

    // Operator rates
    private double mutationRate = 0.02;
    private double crossoverRate = 0.8;

    // Elitism
    private double elitePercentage = 0.05;

    // Selection
    private int tournamentSize = 3;

    // Convergence
    private boolean convergenceEnabled = true;
    private int convergenceGenerations = 50;
    private double convergenceThreshold = 0.001;

    // Target fitness (optional early stopping)
    private Double targetFitness = null;

    // Repair
    private boolean repairEnabled = true;

    // Random seed (null = random)
    private Long seed = null;

    /**
     * Creates a default configuration.
     */
    public GeneticAlgorithmConfig() {
    }

    // ==================== Builder Methods ====================

    /**
     * Sets the population size.
     *
     * @param populationSize number of chromosomes (100-500 recommended)
     * @return this config for chaining
     */
    public GeneticAlgorithmConfig populationSize(int populationSize) {
        this.populationSize = populationSize;
        return this;
    }

    /**
     * Sets the maximum number of generations.
     *
     * @param maxGenerations maximum generations (500-2000 recommended)
     * @return this config for chaining
     */
    public GeneticAlgorithmConfig maxGenerations(int maxGenerations) {
        this.maxGenerations = maxGenerations;
        return this;
    }

    /**
     * Sets the mutation rate.
     *
     * @param mutationRate probability of mutation (0.01-0.05 recommended)
     * @return this config for chaining
     */
    public GeneticAlgorithmConfig mutationRate(double mutationRate) {
        this.mutationRate = mutationRate;
        return this;
    }

    /**
     * Sets the crossover rate.
     *
     * @param crossoverRate probability of crossover (0.7-0.9 recommended)
     * @return this config for chaining
     */
    public GeneticAlgorithmConfig crossoverRate(double crossoverRate) {
        this.crossoverRate = crossoverRate;
        return this;
    }

    /**
     * Sets the elite percentage.
     *
     * @param elitePercentage percentage to preserve (0.05-0.10 recommended)
     * @return this config for chaining
     */
    public GeneticAlgorithmConfig elitePercentage(double elitePercentage) {
        this.elitePercentage = elitePercentage;
        return this;
    }

    /**
     * Sets the tournament size.
     *
     * @param tournamentSize number of competitors (3-5 recommended)
     * @return this config for chaining
     */
    public GeneticAlgorithmConfig tournamentSize(int tournamentSize) {
        this.tournamentSize = tournamentSize;
        return this;
    }

    /**
     * Enables or disables convergence detection.
     *
     * @param enabled true to enable early stopping on convergence
     * @return this config for chaining
     */
    public GeneticAlgorithmConfig convergenceEnabled(boolean enabled) {
        this.convergenceEnabled = enabled;
        return this;
    }

    /**
     * Sets the number of generations to check for convergence.
     *
     * @param generations generations without improvement to trigger convergence
     * @return this config for chaining
     */
    public GeneticAlgorithmConfig convergenceGenerations(int generations) {
        this.convergenceGenerations = generations;
        return this;
    }

    /**
     * Sets the convergence threshold.
     *
     * @param threshold minimum improvement required
     * @return this config for chaining
     */
    public GeneticAlgorithmConfig convergenceThreshold(double threshold) {
        this.convergenceThreshold = threshold;
        return this;
    }

    /**
     * Sets a target fitness for early stopping.
     *
     * @param targetFitness fitness value to stop at (null to disable)
     * @return this config for chaining
     */
    public GeneticAlgorithmConfig targetFitness(Double targetFitness) {
        this.targetFitness = targetFitness;
        return this;
    }

    /**
     * Enables or disables constraint repair.
     *
     * @param enabled true to repair chromosomes after genetic operations
     * @return this config for chaining
     */
    public GeneticAlgorithmConfig repairEnabled(boolean enabled) {
        this.repairEnabled = enabled;
        return this;
    }

    /**
     * Sets the random seed for reproducibility.
     *
     * @param seed random seed (null for random)
     * @return this config for chaining
     */
    public GeneticAlgorithmConfig seed(Long seed) {
        this.seed = seed;
        return this;
    }

    // ==================== Getters ====================

    public int getPopulationSize() {
        return populationSize;
    }

    public int getMaxGenerations() {
        return maxGenerations;
    }

    public double getMutationRate() {
        return mutationRate;
    }

    public double getCrossoverRate() {
        return crossoverRate;
    }

    public double getElitePercentage() {
        return elitePercentage;
    }

    public int getTournamentSize() {
        return tournamentSize;
    }

    public boolean isConvergenceEnabled() {
        return convergenceEnabled;
    }

    public int getConvergenceGenerations() {
        return convergenceGenerations;
    }

    public double getConvergenceThreshold() {
        return convergenceThreshold;
    }

    public Double getTargetFitness() {
        return targetFitness;
    }

    public boolean isRepairEnabled() {
        return repairEnabled;
    }

    public Long getSeed() {
        return seed;
    }

    // ==================== Setters ====================

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public void setMaxGenerations(int maxGenerations) {
        this.maxGenerations = maxGenerations;
    }

    public void setMutationRate(double mutationRate) {
        this.mutationRate = mutationRate;
    }

    public void setCrossoverRate(double crossoverRate) {
        this.crossoverRate = crossoverRate;
    }

    public void setElitePercentage(double elitePercentage) {
        this.elitePercentage = elitePercentage;
    }

    public void setTournamentSize(int tournamentSize) {
        this.tournamentSize = tournamentSize;
    }

    public void setConvergenceEnabled(boolean convergenceEnabled) {
        this.convergenceEnabled = convergenceEnabled;
    }

    public void setConvergenceGenerations(int convergenceGenerations) {
        this.convergenceGenerations = convergenceGenerations;
    }

    public void setConvergenceThreshold(double convergenceThreshold) {
        this.convergenceThreshold = convergenceThreshold;
    }

    public void setTargetFitness(Double targetFitness) {
        this.targetFitness = targetFitness;
    }

    public void setRepairEnabled(boolean repairEnabled) {
        this.repairEnabled = repairEnabled;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }

    // ==================== Presets ====================

    /**
     * Creates a configuration optimized for small datasets (< 50 students).
     *
     * @return configured instance
     */
    public static GeneticAlgorithmConfig forSmallDataset() {
        return new GeneticAlgorithmConfig()
                .populationSize(100)
                .maxGenerations(500)
                .mutationRate(0.03)
                .crossoverRate(0.8)
                .elitePercentage(0.10)
                .tournamentSize(3);
    }

    /**
     * Creates a configuration optimized for medium datasets (50-200 students).
     *
     * @return configured instance
     */
    public static GeneticAlgorithmConfig forMediumDataset() {
        return new GeneticAlgorithmConfig()
                .populationSize(200)
                .maxGenerations(1000)
                .mutationRate(0.02)
                .crossoverRate(0.8)
                .elitePercentage(0.05)
                .tournamentSize(4);
    }

    /**
     * Creates a configuration optimized for large datasets (> 200 students).
     *
     * @return configured instance
     */
    public static GeneticAlgorithmConfig forLargeDataset() {
        return new GeneticAlgorithmConfig()
                .populationSize(500)
                .maxGenerations(2000)
                .mutationRate(0.01)
                .crossoverRate(0.85)
                .elitePercentage(0.05)
                .tournamentSize(5);
    }

    /**
     * Creates a fast configuration for quick testing.
     *
     * @return configured instance
     */
    public static GeneticAlgorithmConfig forQuickTest() {
        return new GeneticAlgorithmConfig()
                .populationSize(50)
                .maxGenerations(100)
                .mutationRate(0.05)
                .crossoverRate(0.9)
                .elitePercentage(0.10)
                .tournamentSize(3)
                .convergenceGenerations(20);
    }

    /**
     * Creates a high quality configuration for maximum optimization.
     * Trades execution time for better results.
     *
     * @return configured instance
     */
    public static GeneticAlgorithmConfig forHighQuality() {
        return new GeneticAlgorithmConfig()
                .populationSize(750)
                .maxGenerations(3000)
                .mutationRate(0.025)
                .crossoverRate(0.85)
                .elitePercentage(0.10)
                .tournamentSize(5)
                .convergenceGenerations(100)
                .convergenceThreshold(0.0005);
    }

    // ==================== Validation ====================

    /**
     * Validates the configuration parameters.
     *
     * @throws IllegalStateException if any parameter is invalid
     */
    public void validate() {
        if (populationSize < 10) {
            throw new IllegalStateException("Population size must be at least 10");
        }
        if (maxGenerations < 1) {
            throw new IllegalStateException("Max generations must be at least 1");
        }
        if (mutationRate < 0 || mutationRate > 1) {
            throw new IllegalStateException("Mutation rate must be between 0 and 1");
        }
        if (crossoverRate < 0 || crossoverRate > 1) {
            throw new IllegalStateException("Crossover rate must be between 0 and 1");
        }
        if (elitePercentage < 0 || elitePercentage > 1) {
            throw new IllegalStateException("Elite percentage must be between 0 and 1");
        }
        if (tournamentSize < 2) {
            throw new IllegalStateException("Tournament size must be at least 2");
        }
        if (convergenceGenerations < 1) {
            throw new IllegalStateException("Convergence generations must be at least 1");
        }
    }

    @Override
    public String toString() {
        return "GeneticAlgorithmConfig{" +
                "populationSize=" + populationSize +
                ", maxGenerations=" + maxGenerations +
                ", mutationRate=" + mutationRate +
                ", crossoverRate=" + crossoverRate +
                ", elitePercentage=" + elitePercentage +
                ", tournamentSize=" + tournamentSize +
                ", convergenceEnabled=" + convergenceEnabled +
                ", repairEnabled=" + repairEnabled +
                '}';
    }
}
