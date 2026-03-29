package com.optimatch.algorithm;

import com.optimatch.model.Preference;
import com.optimatch.model.Project;
import com.optimatch.model.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * Algorithm flow:
 * 1. Initialize random population
 * 2. Evaluate fitness of all chromosomes
 * 3. Select elite individuals for preservation
 * 4. While not converged and generations < max:
 *    a. Select parents using tournament selection
 *    b. Apply crossover to create offspring
 *    c. Apply mutation to offspring
 *    d. Repair constraint violations
 *    e. Evaluate fitness of new chromosomes
 *    f. Create new population with elite + offspring
 * 5. Return best solution
 *
 * Configuration defaults:
 * - Population size: 100-500
 * - Generations: 500-2000
 * - Mutation rate: 0.01-0.05
 * - Crossover rate: 0.7-0.9
 * - Elitism: 5-10%
 * - Tournament size: 3-5
 */
public class GeneticAlgorithm {

    // Algorithm components
    private final FitnessCalculator fitnessCalculator;
    private final SelectionOperator selectionOperator;
    private final CrossoverOperator crossoverOperator;
    private final MutationOperator mutationOperator;
    private final ElitismOperator elitismOperator;
    private final ConstraintChecker constraintChecker;
    private final Random random;

    // Configuration
    private final GeneticAlgorithmConfig config;

    // State
    private Population population;
    private Chromosome bestEver;
    private int currentGeneration;
    private boolean isRunning;
    private boolean isStopped;

    // Statistics
    private final List<GenerationStats> generationHistory;
    private long startTimeMs;
    private long endTimeMs;

    // Callbacks
    private GenerationCallback generationCallback;

    /**
     * Creates a GeneticAlgorithm with the given data and default configuration.
     *
     * @param students    list of all students
     * @param projects    list of all projects
     * @param preferences list of all preferences
     */
    public GeneticAlgorithm(List<Student> students, List<Project> projects, List<Preference> preferences) {
        this(students, projects, preferences, new GeneticAlgorithmConfig());
    }

    /**
     * Creates a GeneticAlgorithm with the given data and configuration.
     *
     * @param students    list of all students
     * @param projects    list of all projects
     * @param preferences list of all preferences
     * @param config      algorithm configuration
     */
    public GeneticAlgorithm(List<Student> students, List<Project> projects,
                            List<Preference> preferences, GeneticAlgorithmConfig config) {
        this.config = config;
        this.random = config.getSeed() != null ? new Random(config.getSeed()) : new Random();

        // Initialize components
        this.fitnessCalculator = new FitnessCalculator(students, projects, preferences);
        this.constraintChecker = new ConstraintChecker(students, projects, random);

        this.selectionOperator = new SelectionOperator(random);
        this.selectionOperator.setTournamentSize(config.getTournamentSize());

        this.crossoverOperator = new CrossoverOperator(random);
        this.crossoverOperator.setCrossoverRate(config.getCrossoverRate());

        this.mutationOperator = new MutationOperator(random);
        this.mutationOperator.setMutationRate(config.getMutationRate());
        this.mutationOperator.setAvailableProjectIds(constraintChecker.getProjectIds());

        this.elitismOperator = new ElitismOperator(config.getElitePercentage());

        // Initialize state
        this.generationHistory = new ArrayList<>();
        this.currentGeneration = 0;
        this.isRunning = false;
        this.isStopped = false;
    }

    /**
     * Runs the genetic algorithm and returns the best solution.
     *
     * @return the best chromosome found
     */
    public Chromosome run() {
        isRunning = true;
        isStopped = false;
        startTimeMs = System.currentTimeMillis();

        try {
            // Step 1: Initialize population
            initializePopulation();

            // Step 2: Evaluate initial population
            evaluatePopulation(population);

            // Track best ever
            bestEver = population.getBest().copy();

            // Record initial generation stats
            recordGenerationStats();

            // Step 3: Evolution loop
            while (!shouldTerminate()) {
                evolveOneGeneration();
                currentGeneration++;

                // Update best ever
                Chromosome currentBest = population.getBest();
                if (currentBest.getFitness() > bestEver.getFitness()) {
                    bestEver = currentBest.copy();
                }

                // Record stats
                recordGenerationStats();

                // Callback
                if (generationCallback != null) {
                    generationCallback.onGeneration(currentGeneration, population, bestEver);
                }
            }

        } finally {
            endTimeMs = System.currentTimeMillis();
            isRunning = false;
        }

        return bestEver;
    }

    /**
     * Initializes the population with random chromosomes.
     */
    private void initializePopulation() {
        int numStudents = fitnessCalculator.getTheoreticalMaxFitness() > 0
                ? (int) (fitnessCalculator.getTheoreticalMaxFitness() / Preference.WEIGHT_FIRST_CHOICE)
                : 0;

        int[] projectIds = constraintChecker.getProjectIds();

        population = Population.createRandom(
                config.getPopulationSize(),
                numStudents,
                projectIds,
                random
        );

        // Optionally repair initial population
        if (config.isRepairEnabled()) {
            for (int i = 0; i < population.getCurrentSize(); i++) {
                Chromosome chromosome = population.getChromosome(i);
                constraintChecker.repair(chromosome);
            }
        }
    }

    /**
     * Evolves the population by one generation.
     */
    private void evolveOneGeneration() {
        // Step 1: Select elite
        List<Chromosome> elite = elitismOperator.selectElite(population);

        // Step 2: Create new population starting with elite
        List<Chromosome> newChromosomes = new ArrayList<>(config.getPopulationSize());
        for (Chromosome eliteChromosome : elite) {
            newChromosomes.add(eliteChromosome);
        }

        // Step 3: Fill remaining slots with offspring
        int remainingSlots = config.getPopulationSize() - elite.size();

        while (newChromosomes.size() < config.getPopulationSize()) {
            // Select parents
            Chromosome parent1 = selectionOperator.select(population);
            Chromosome parent2 = selectionOperator.select(population);

            // Crossover
            Chromosome[] offspring = crossoverOperator.crossover(parent1, parent2);

            // Mutate
            mutationOperator.mutate(offspring[0]);
            mutationOperator.mutate(offspring[1]);

            // Repair if enabled
            if (config.isRepairEnabled()) {
                constraintChecker.repair(offspring[0]);
                constraintChecker.repair(offspring[1]);
            }

            // Add offspring (check we don't exceed population size)
            newChromosomes.add(offspring[0]);
            if (newChromosomes.size() < config.getPopulationSize()) {
                newChromosomes.add(offspring[1]);
            }
        }

        // Step 4: Replace population
        population.replaceAll(newChromosomes);

        // Step 5: Evaluate new population
        evaluatePopulation(population);
    }

    /**
     * Evaluates fitness for all chromosomes in the population.
     *
     * @param pop the population to evaluate
     */
    private void evaluatePopulation(Population pop) {
        for (int i = 0; i < pop.getCurrentSize(); i++) {
            Chromosome chromosome = pop.getChromosome(i);
            if (!chromosome.isFitnessCalculated()) {
                fitnessCalculator.calculateFitness(chromosome);
                constraintChecker.checkAll(chromosome);
            }
        }
    }

    /**
     * Checks if the algorithm should terminate.
     *
     * @return true if termination criteria met
     */
    private boolean shouldTerminate() {
        // Check if manually stopped
        if (isStopped) {
            return true;
        }

        // Check max generations
        if (currentGeneration >= config.getMaxGenerations()) {
            return true;
        }

        // Check convergence
        if (config.isConvergenceEnabled() && isConverged()) {
            return true;
        }

        // Check target fitness
        if (config.getTargetFitness() != null && bestEver.getFitness() >= config.getTargetFitness()) {
            return true;
        }

        return false;
    }

    /**
     * Checks if the algorithm has converged (no improvement for N generations).
     *
     * @return true if converged
     */
    private boolean isConverged() {
        int windowSize = config.getConvergenceGenerations();
        if (generationHistory.size() < windowSize) {
            return false;
        }

        // Check if best fitness hasn't improved in the window
        double currentBest = bestEver.getFitness();
        int startIndex = generationHistory.size() - windowSize;
        double oldBest = generationHistory.get(startIndex).getBestFitness();

        double improvement = currentBest - oldBest;
        return improvement < config.getConvergenceThreshold();
    }

    /**
     * Records statistics for the current generation.
     */
    private void recordGenerationStats() {
        GenerationStats stats = new GenerationStats(
                currentGeneration,
                population.getBestFitness(),
                population.getAverageFitness(),
                population.getWorstFitness(),
                population.getFitnessStdDev(),
                population.countValid(),
                bestEver.getFitness()
        );
        generationHistory.add(stats);
    }

    /**
     * Stops the algorithm gracefully.
     */
    public void stop() {
        isStopped = true;
    }

    // ==================== Getters ====================

    /**
     * Gets the best chromosome ever found.
     *
     * @return the best chromosome
     */
    public Chromosome getBestSolution() {
        return bestEver;
    }

    /**
     * Gets the current population.
     *
     * @return the population
     */
    public Population getPopulation() {
        return population;
    }

    /**
     * Gets the current generation number.
     *
     * @return the generation number
     */
    public int getCurrentGeneration() {
        return currentGeneration;
    }

    /**
     * Checks if the algorithm is currently running.
     *
     * @return true if running
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Gets the generation history.
     *
     * @return list of generation statistics
     */
    public List<GenerationStats> getGenerationHistory() {
        return new ArrayList<>(generationHistory);
    }

    /**
     * Gets the total execution time in milliseconds.
     *
     * @return execution time
     */
    public long getExecutionTimeMs() {
        if (isRunning) {
            return System.currentTimeMillis() - startTimeMs;
        }
        return endTimeMs - startTimeMs;
    }

    /**
     * Gets the fitness calculator.
     *
     * @return the fitness calculator
     */
    public FitnessCalculator getFitnessCalculator() {
        return fitnessCalculator;
    }

    /**
     * Gets the constraint checker.
     *
     * @return the constraint checker
     */
    public ConstraintChecker getConstraintChecker() {
        return constraintChecker;
    }

    /**
     * Gets the configuration.
     *
     * @return the configuration
     */
    public GeneticAlgorithmConfig getConfig() {
        return config;
    }

    /**
     * Sets the generation callback.
     *
     * @param callback the callback to invoke after each generation
     */
    public void setGenerationCallback(GenerationCallback callback) {
        this.generationCallback = callback;
    }

    /**
     * Gets a summary of the algorithm run.
     *
     * @return AlgorithmResult with all statistics
     */
    public AlgorithmResult getResult() {
        return new AlgorithmResult(
                bestEver,
                currentGeneration,
                getExecutionTimeMs(),
                generationHistory,
                fitnessCalculator.getBreakdown(bestEver),
                fitnessCalculator.countPreferenceDistribution(bestEver)
        );
    }

    // ==================== Inner Classes ====================

    /**
     * Callback interface for generation events.
     */
    public interface GenerationCallback {
        void onGeneration(int generation, Population population, Chromosome bestEver);
    }

    /**
     * Statistics for a single generation.
     */
    public static class GenerationStats {
        private final int generation;
        private final double bestFitness;
        private final double averageFitness;
        private final double worstFitness;
        private final double standardDeviation;
        private final int validCount;
        private final double bestEverFitness;

        public GenerationStats(int generation, double bestFitness, double averageFitness,
                               double worstFitness, double standardDeviation,
                               int validCount, double bestEverFitness) {
            this.generation = generation;
            this.bestFitness = bestFitness;
            this.averageFitness = averageFitness;
            this.worstFitness = worstFitness;
            this.standardDeviation = standardDeviation;
            this.validCount = validCount;
            this.bestEverFitness = bestEverFitness;
        }

        public int getGeneration() {
            return generation;
        }

        public double getBestFitness() {
            return bestFitness;
        }

        public double getAverageFitness() {
            return averageFitness;
        }

        public double getWorstFitness() {
            return worstFitness;
        }

        public double getStandardDeviation() {
            return standardDeviation;
        }

        public int getValidCount() {
            return validCount;
        }

        public double getBestEverFitness() {
            return bestEverFitness;
        }

        @Override
        public String toString() {
            return String.format("Gen %d: best=%.2f, avg=%.2f, worst=%.2f, valid=%d",
                    generation, bestFitness, averageFitness, worstFitness, validCount);
        }
    }

    /**
     * Complete result of an algorithm run.
     */
    public static class AlgorithmResult {
        private final Chromosome bestSolution;
        private final int generations;
        private final long executionTimeMs;
        private final List<GenerationStats> history;
        private final FitnessCalculator.FitnessBreakdown fitnessBreakdown;
        private final int[] preferenceDistribution;

        public AlgorithmResult(Chromosome bestSolution, int generations, long executionTimeMs,
                               List<GenerationStats> history,
                               FitnessCalculator.FitnessBreakdown fitnessBreakdown,
                               int[] preferenceDistribution) {
            this.bestSolution = bestSolution;
            this.generations = generations;
            this.executionTimeMs = executionTimeMs;
            this.history = history;
            this.fitnessBreakdown = fitnessBreakdown;
            this.preferenceDistribution = preferenceDistribution;
        }

        public Chromosome getBestSolution() {
            return bestSolution;
        }

        public int getGenerations() {
            return generations;
        }

        public long getExecutionTimeMs() {
            return executionTimeMs;
        }

        public double getExecutionTimeSeconds() {
            return executionTimeMs / 1000.0;
        }

        public List<GenerationStats> getHistory() {
            return history;
        }

        public FitnessCalculator.FitnessBreakdown getFitnessBreakdown() {
            return fitnessBreakdown;
        }

        public int[] getPreferenceDistribution() {
            return preferenceDistribution;
        }

        public double getBestFitness() {
            return bestSolution.getFitness();
        }

        public boolean isSolutionValid() {
            return bestSolution.isValid();
        }

        @Override
        public String toString() {
            return String.format(
                    "AlgorithmResult{fitness=%.2f, valid=%s, generations=%d, time=%.2fs, " +
                            "prefs=[none=%d, 1st=%d, 2nd=%d, 3rd=%d, 4th=%d, 5th=%d]}",
                    bestSolution.getFitness(),
                    bestSolution.isValid(),
                    generations,
                    getExecutionTimeSeconds(),
                    preferenceDistribution[0],
                    preferenceDistribution[1],
                    preferenceDistribution[2],
                    preferenceDistribution[3],
                    preferenceDistribution[4],
                    preferenceDistribution[5]
            );
        }
    }
}
