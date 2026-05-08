package com.optimatch.algorithm;

// settings for one GA run, with fluent builder methods and a few presets
public class GeneticAlgorithmConfig {

    // population
    private int populationSize = 200;
    private int maxGenerations = 1000;

    // operators
    private double mutationRate = 0.02;
    private double crossoverRate = 0.8;

    // elite share
    private double elitePercentage = 0.05;

    // tournament size for selection
    private int tournamentSize = 3;

    // early stopping
    private boolean convergenceEnabled = true;
    private int convergenceGenerations = 50;
    private double convergenceThreshold = 0.001;

    // run constraint repair after operators
    private boolean repairEnabled = true;

    // null means non-deterministic
    private Long seed = null;

    // default config (medium dataset)
    public GeneticAlgorithmConfig() {
    }

    // ==================== Builder Methods ====================

    // set population size
    public GeneticAlgorithmConfig populationSize(int populationSize) {
        this.populationSize = populationSize;
        return this;
    }

    // set max generations
    public GeneticAlgorithmConfig maxGenerations(int maxGenerations) {
        this.maxGenerations = maxGenerations;
        return this;
    }

    // set mutation rate
    public GeneticAlgorithmConfig mutationRate(double mutationRate) {
        this.mutationRate = mutationRate;
        return this;
    }

    // set crossover rate
    public GeneticAlgorithmConfig crossoverRate(double crossoverRate) {
        this.crossoverRate = crossoverRate;
        return this;
    }

    // set elite percentage
    public GeneticAlgorithmConfig elitePercentage(double elitePercentage) {
        this.elitePercentage = elitePercentage;
        return this;
    }

    // set tournament size
    public GeneticAlgorithmConfig tournamentSize(int tournamentSize) {
        this.tournamentSize = tournamentSize;
        return this;
    }

    // toggle early stopping
    public GeneticAlgorithmConfig convergenceEnabled(boolean enabled) {
        this.convergenceEnabled = enabled;
        return this;
    }

    // set how many stagnant generations trigger early stop
    public GeneticAlgorithmConfig convergenceGenerations(int generations) {
        this.convergenceGenerations = generations;
        return this;
    }

    // set minimum improvement that still counts as progress
    public GeneticAlgorithmConfig convergenceThreshold(double threshold) {
        this.convergenceThreshold = threshold;
        return this;
    }

    // toggle constraint repair
    public GeneticAlgorithmConfig repairEnabled(boolean enabled) {
        this.repairEnabled = enabled;
        return this;
    }

    // set rng seed (null for non-deterministic)
    public GeneticAlgorithmConfig seed(Long seed) {
        this.seed = seed;
        return this;
    }

    // ==================== Getters ====================

    // population size
    public int getPopulationSize() {
        return populationSize;
    }

    // max generations
    public int getMaxGenerations() {
        return maxGenerations;
    }

    // mutation rate
    public double getMutationRate() {
        return mutationRate;
    }

    // crossover rate
    public double getCrossoverRate() {
        return crossoverRate;
    }

    // elite percentage
    public double getElitePercentage() {
        return elitePercentage;
    }

    // tournament size
    public int getTournamentSize() {
        return tournamentSize;
    }

    // is early stopping on
    public boolean isConvergenceEnabled() {
        return convergenceEnabled;
    }

    // stagnation window size
    public int getConvergenceGenerations() {
        return convergenceGenerations;
    }

    // stagnation threshold
    public double getConvergenceThreshold() {
        return convergenceThreshold;
    }

    // is repair on
    public boolean isRepairEnabled() {
        return repairEnabled;
    }

    // rng seed (null = random)
    public Long getSeed() {
        return seed;
    }

    // ==================== Presets ====================

    // tuned for fewer than 50 students
    public static GeneticAlgorithmConfig forSmallDataset() {
        return new GeneticAlgorithmConfig()
                .populationSize(100)
                .maxGenerations(500)
                .mutationRate(0.03)
                .crossoverRate(0.8)
                .elitePercentage(0.10)
                .tournamentSize(3);
    }

    // tuned for 50 to 200 students
    public static GeneticAlgorithmConfig forMediumDataset() {
        return new GeneticAlgorithmConfig()
                .populationSize(200)
                .maxGenerations(1000)
                .mutationRate(0.02)
                .crossoverRate(0.8)
                .elitePercentage(0.05)
                .tournamentSize(4);
    }

    // tuned for more than 200 students
    public static GeneticAlgorithmConfig forLargeDataset() {
        return new GeneticAlgorithmConfig()
                .populationSize(500)
                .maxGenerations(2000)
                .mutationRate(0.01)
                .crossoverRate(0.85)
                .elitePercentage(0.05)
                .tournamentSize(5);
    }

    // small and fast, for smoke tests
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

    // best results, slowest run
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

    // throws if any parameter is out of range
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
