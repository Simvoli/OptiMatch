package com.optimatch.algorithm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Elitism operator for the genetic algorithm.
 * Preserves the best solutions from one generation to the next,
 * ensuring that the best fitness never decreases.
 *
 * Recommended: Keep top 5-10% unchanged (from CLAUDE.md specification).
 */
public class ElitismOperator {

    private static final double DEFAULT_ELITE_PERCENTAGE = 0.05; // 5%
    private static final int DEFAULT_MIN_ELITE = 1;
    private static final int DEFAULT_MAX_ELITE = 50;

    private double elitePercentage;
    private int minEliteCount;
    private int maxEliteCount;
    private boolean preserveUnique;

    /**
     * Creates an ElitismOperator with default settings (5% elite).
     */
    public ElitismOperator() {
        this.elitePercentage = DEFAULT_ELITE_PERCENTAGE;
        this.minEliteCount = DEFAULT_MIN_ELITE;
        this.maxEliteCount = DEFAULT_MAX_ELITE;
        this.preserveUnique = true;
    }

    /**
     * Creates an ElitismOperator with specified elite percentage.
     *
     * @param elitePercentage percentage of population to preserve (0.0 to 1.0)
     */
    public ElitismOperator(double elitePercentage) {
        this.elitePercentage = elitePercentage;
        this.minEliteCount = DEFAULT_MIN_ELITE;
        this.maxEliteCount = DEFAULT_MAX_ELITE;
        this.preserveUnique = true;
    }

    /**
     * Creates an ElitismOperator with specified settings.
     *
     * @param elitePercentage percentage of population to preserve (0.0 to 1.0)
     * @param minEliteCount   minimum number of elite individuals
     * @param maxEliteCount   maximum number of elite individuals
     */
    public ElitismOperator(double elitePercentage, int minEliteCount, int maxEliteCount) {
        this.elitePercentage = elitePercentage;
        this.minEliteCount = minEliteCount;
        this.maxEliteCount = maxEliteCount;
        this.preserveUnique = true;
    }

    /**
     * Calculates the number of elite individuals to preserve.
     *
     * @param populationSize the total population size
     * @return the number of elite individuals
     */
    public int calculateEliteCount(int populationSize) {
        int count = (int) Math.round(populationSize * elitePercentage);
        count = Math.max(count, minEliteCount);
        count = Math.min(count, maxEliteCount);
        count = Math.min(count, populationSize); // Can't exceed population
        return count;
    }

    /**
     * Selects the elite individuals from a population.
     * Returns deep copies to prevent modification.
     *
     * @param population the population to select from
     * @return list of elite chromosomes (copies)
     */
    public List<Chromosome> selectElite(Population population) {
        int eliteCount = calculateEliteCount(population.getCurrentSize());
        return population.getElite(eliteCount);
    }

    /**
     * Selects a specific number of elite individuals.
     *
     * @param population the population to select from
     * @param count      the number of elite to select
     * @return list of elite chromosomes (copies)
     */
    public List<Chromosome> selectElite(Population population, int count) {
        return population.getElite(count);
    }

    /**
     * Selects unique elite individuals (no duplicate assignments).
     * Useful when diversity is important.
     *
     * @param population the population to select from
     * @return list of unique elite chromosomes (copies)
     */
    public List<Chromosome> selectUniqueElite(Population population) {
        population.sortByFitness();
        List<Chromosome> chromosomes = population.getChromosomes();
        int targetCount = calculateEliteCount(population.getCurrentSize());

        List<Chromosome> elite = new ArrayList<>();
        Set<Integer> seenHashes = new HashSet<>();

        for (Chromosome chromosome : chromosomes) {
            if (elite.size() >= targetCount) {
                break;
            }

            int hash = chromosome.hashCode();
            if (!seenHashes.contains(hash)) {
                elite.add(chromosome.copy());
                seenHashes.add(hash);
            }
        }

        return elite;
    }

    /**
     * Applies elitism by injecting elite into a new population.
     * Replaces the worst individuals in the new population with elite.
     *
     * @param elite         the elite chromosomes to preserve
     * @param newPopulation the new population to inject elite into
     */
    public void applyElitism(List<Chromosome> elite, Population newPopulation) {
        if (elite.isEmpty()) {
            return;
        }

        newPopulation.sortByFitness();
        int popSize = newPopulation.getCurrentSize();

        // Replace worst individuals with elite
        for (int i = 0; i < elite.size() && i < popSize; i++) {
            int replaceIndex = popSize - 1 - i; // Start from worst
            newPopulation.setChromosome(replaceIndex, elite.get(i).copy());
        }
    }

    /**
     * Applies elitism by adding elite to a population list.
     * Used when building a new population from scratch.
     *
     * @param elite             the elite chromosomes to preserve
     * @param newChromosomes    the list of new chromosomes being built
     */
    public void addEliteToList(List<Chromosome> elite, List<Chromosome> newChromosomes) {
        for (Chromosome eliteChromosome : elite) {
            newChromosomes.add(eliteChromosome.copy());
        }
    }

    /**
     * Creates a new population with elite preserved and remaining slots
     * available for new offspring.
     *
     * @param oldPopulation the previous generation's population
     * @param targetSize    the target size for the new population
     * @return a new population with elite already added
     */
    public Population createPopulationWithElite(Population oldPopulation, int targetSize) {
        List<Chromosome> elite = preserveUnique
                ? selectUniqueElite(oldPopulation)
                : selectElite(oldPopulation);

        Population newPopulation = new Population(targetSize);
        for (Chromosome eliteChromosome : elite) {
            newPopulation.addChromosome(eliteChromosome);
        }

        return newPopulation;
    }

    /**
     * Gets the number of remaining slots after elite are preserved.
     *
     * @param populationSize the total population size
     * @return the number of slots for new offspring
     */
    public int getRemainingSlots(int populationSize) {
        return populationSize - calculateEliteCount(populationSize);
    }

    /**
     * Checks if the best solution was preserved correctly.
     *
     * @param oldBest the best from previous generation
     * @param newPopulation the new generation
     * @return true if the best was preserved
     */
    public boolean verifyElitePreserved(Chromosome oldBest, Population newPopulation) {
        Chromosome newBest = newPopulation.getBest();
        return newBest != null && newBest.getFitness() >= oldBest.getFitness();
    }

    /**
     * Gets statistics about the elite.
     *
     * @param elite the list of elite chromosomes
     * @return EliteStats with summary information
     */
    public EliteStats getEliteStats(List<Chromosome> elite) {
        if (elite.isEmpty()) {
            return new EliteStats(0, 0, 0, 0);
        }

        double bestFitness = elite.get(0).getFitness();
        double worstFitness = elite.get(elite.size() - 1).getFitness();
        double totalFitness = 0;

        for (Chromosome c : elite) {
            totalFitness += c.getFitness();
        }

        double avgFitness = totalFitness / elite.size();

        return new EliteStats(elite.size(), bestFitness, worstFitness, avgFitness);
    }

    // ==================== Configuration ====================

    /**
     * Gets the elite percentage.
     *
     * @return the elite percentage (0.0 to 1.0)
     */
    public double getElitePercentage() {
        return elitePercentage;
    }

    /**
     * Sets the elite percentage.
     * Recommended values: 0.05-0.10 (5-10%) from CLAUDE.md specification.
     *
     * @param elitePercentage the percentage (0.0 to 1.0)
     * @throws IllegalArgumentException if not in valid range
     */
    public void setElitePercentage(double elitePercentage) {
        if (elitePercentage < 0.0 || elitePercentage > 1.0) {
            throw new IllegalArgumentException("Elite percentage must be between 0.0 and 1.0");
        }
        this.elitePercentage = elitePercentage;
    }

    /**
     * Gets the minimum elite count.
     *
     * @return the minimum number of elite individuals
     */
    public int getMinEliteCount() {
        return minEliteCount;
    }

    /**
     * Sets the minimum elite count.
     *
     * @param minEliteCount the minimum count (>= 0)
     */
    public void setMinEliteCount(int minEliteCount) {
        if (minEliteCount < 0) {
            throw new IllegalArgumentException("Minimum elite count cannot be negative");
        }
        this.minEliteCount = minEliteCount;
    }

    /**
     * Gets the maximum elite count.
     *
     * @return the maximum number of elite individuals
     */
    public int getMaxEliteCount() {
        return maxEliteCount;
    }

    /**
     * Sets the maximum elite count.
     *
     * @param maxEliteCount the maximum count
     */
    public void setMaxEliteCount(int maxEliteCount) {
        if (maxEliteCount < minEliteCount) {
            throw new IllegalArgumentException("Maximum elite count cannot be less than minimum");
        }
        this.maxEliteCount = maxEliteCount;
    }

    /**
     * Gets whether unique preservation is enabled.
     *
     * @return true if only unique chromosomes are preserved
     */
    public boolean isPreserveUnique() {
        return preserveUnique;
    }

    /**
     * Sets whether to preserve only unique chromosomes.
     *
     * @param preserveUnique true to avoid duplicate elite
     */
    public void setPreserveUnique(boolean preserveUnique) {
        this.preserveUnique = preserveUnique;
    }

    // ==================== Factory Methods ====================

    /**
     * Creates an elitism operator with 5% preservation.
     *
     * @return configured ElitismOperator
     */
    public static ElitismOperator fivePercent() {
        return new ElitismOperator(0.05);
    }

    /**
     * Creates an elitism operator with 10% preservation.
     *
     * @return configured ElitismOperator
     */
    public static ElitismOperator tenPercent() {
        return new ElitismOperator(0.10);
    }

    /**
     * Creates an elitism operator that preserves a fixed count.
     *
     * @param count the exact number of elite to preserve
     * @return configured ElitismOperator
     */
    public static ElitismOperator fixedCount(int count) {
        ElitismOperator operator = new ElitismOperator(1.0); // 100% but capped
        operator.setMinEliteCount(count);
        operator.setMaxEliteCount(count);
        return operator;
    }

    /**
     * Creates an elitism operator with custom range.
     *
     * @param percentage percentage of population (0.0 to 1.0)
     * @param min        minimum elite count
     * @param max        maximum elite count
     * @return configured ElitismOperator
     */
    public static ElitismOperator withRange(double percentage, int min, int max) {
        return new ElitismOperator(percentage, min, max);
    }

    /**
     * Holds statistics about the elite individuals.
     */
    public static class EliteStats {
        private final int count;
        private final double bestFitness;
        private final double worstFitness;
        private final double averageFitness;

        public EliteStats(int count, double bestFitness, double worstFitness, double averageFitness) {
            this.count = count;
            this.bestFitness = bestFitness;
            this.worstFitness = worstFitness;
            this.averageFitness = averageFitness;
        }

        public int getCount() {
            return count;
        }

        public double getBestFitness() {
            return bestFitness;
        }

        public double getWorstFitness() {
            return worstFitness;
        }

        public double getAverageFitness() {
            return averageFitness;
        }

        public double getFitnessRange() {
            return bestFitness - worstFitness;
        }

        @Override
        public String toString() {
            return String.format("EliteStats{count=%d, best=%.2f, worst=%.2f, avg=%.2f}",
                    count, bestFitness, worstFitness, averageFitness);
        }
    }
}
