package com.optimatch.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Selection operators for the genetic algorithm.
 * Responsible for selecting parent chromosomes for reproduction.
 */
public class SelectionOperator {

    private final Random random;
    private SelectionMethod method;
    private int tournamentSize;

    /**
     * Available selection methods.
     */
    public enum SelectionMethod {
        /** Tournament selection - compete random individuals, best wins */
        TOURNAMENT,
        /** Roulette wheel selection - probability proportional to fitness */
        ROULETTE_WHEEL,
        /** Rank-based selection - probability based on rank, not raw fitness */
        RANK
    }

    /**
     * Creates a SelectionOperator with tournament selection (default).
     *
     * @param random random number generator
     */
    public SelectionOperator(Random random) {
        this.random = random;
        this.method = SelectionMethod.TOURNAMENT;
        this.tournamentSize = 3; // Default tournament size
    }

    /**
     * Creates a SelectionOperator with the specified method.
     *
     * @param random random number generator
     * @param method the selection method to use
     */
    public SelectionOperator(Random random, SelectionMethod method) {
        this.random = random;
        this.method = method;
        this.tournamentSize = 3;
    }

    /**
     * Selects a single parent from the population.
     *
     * @param population the population to select from
     * @return the selected chromosome
     */
    public Chromosome select(Population population) {
        switch (method) {
            case TOURNAMENT:
                return tournamentSelect(population);
            case ROULETTE_WHEEL:
                return rouletteWheelSelect(population);
            case RANK:
                return rankSelect(population);
            default:
                return tournamentSelect(population);
        }
    }

    /**
     * Selects multiple parents from the population.
     *
     * @param population the population to select from
     * @param count      the number of parents to select
     * @return list of selected chromosomes
     */
    public List<Chromosome> selectMultiple(Population population, int count) {
        List<Chromosome> selected = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            selected.add(select(population));
        }
        return selected;
    }

    /**
     * Selects a pair of parents for crossover.
     *
     * @param population the population to select from
     * @return array of two parent chromosomes
     */
    public Chromosome[] selectParents(Population population) {
        Chromosome parent1 = select(population);
        Chromosome parent2 = select(population);

        // Ensure parents are different (optional, can be disabled for performance)
        int attempts = 0;
        while (parent2 == parent1 && attempts < 10) {
            parent2 = select(population);
            attempts++;
        }

        return new Chromosome[]{parent1, parent2};
    }

    // ==================== Tournament Selection ====================

    /**
     * Tournament selection: randomly select tournamentSize individuals,
     * return the one with the highest fitness.
     *
     * @param population the population to select from
     * @return the winner of the tournament
     */
    private Chromosome tournamentSelect(Population population) {
        int popSize = population.getCurrentSize();
        Chromosome best = null;

        for (int i = 0; i < tournamentSize; i++) {
            int randomIndex = random.nextInt(popSize);
            Chromosome competitor = population.getChromosome(randomIndex);

            if (best == null || competitor.getFitness() > best.getFitness()) {
                best = competitor;
            }
        }

        return best;
    }

    /**
     * Tournament selection with specified size.
     *
     * @param population     the population to select from
     * @param tournamentSize the number of competitors
     * @return the winner of the tournament
     */
    public Chromosome tournamentSelect(Population population, int tournamentSize) {
        int popSize = population.getCurrentSize();
        Chromosome best = null;

        for (int i = 0; i < tournamentSize; i++) {
            int randomIndex = random.nextInt(popSize);
            Chromosome competitor = population.getChromosome(randomIndex);

            if (best == null || competitor.getFitness() > best.getFitness()) {
                best = competitor;
            }
        }

        return best;
    }

    // ==================== Roulette Wheel Selection ====================

    /**
     * Roulette wheel selection: probability of selection is proportional to fitness.
     * Note: Requires all fitness values to be positive.
     *
     * @param population the population to select from
     * @return the selected chromosome
     */
    private Chromosome rouletteWheelSelect(Population population) {
        List<Chromosome> chromosomes = population.getChromosomes();

        // Calculate total fitness (shift if negative values exist)
        double minFitness = Double.MAX_VALUE;
        for (Chromosome c : chromosomes) {
            if (c.getFitness() < minFitness) {
                minFitness = c.getFitness();
            }
        }

        // Shift to make all values positive
        double shift = minFitness < 0 ? -minFitness + 1 : 0;

        double totalFitness = 0;
        for (Chromosome c : chromosomes) {
            totalFitness += c.getFitness() + shift;
        }

        // Handle edge case where total fitness is 0
        if (totalFitness == 0) {
            return chromosomes.get(random.nextInt(chromosomes.size()));
        }

        // Spin the wheel
        double randomValue = random.nextDouble() * totalFitness;
        double cumulativeFitness = 0;

        for (Chromosome c : chromosomes) {
            cumulativeFitness += c.getFitness() + shift;
            if (cumulativeFitness >= randomValue) {
                return c;
            }
        }

        // Fallback (shouldn't reach here)
        return chromosomes.get(chromosomes.size() - 1);
    }

    // ==================== Rank-Based Selection ====================

    /**
     * Rank-based selection: probability based on rank rather than raw fitness.
     * Best individual has highest probability, worst has lowest.
     * More stable than roulette wheel when fitness values vary widely.
     *
     * @param population the population to select from
     * @return the selected chromosome
     */
    private Chromosome rankSelect(Population population) {
        population.sortByFitness();
        List<Chromosome> chromosomes = population.getChromosomes();
        int n = chromosomes.size();

        // Calculate total rank sum: n + (n-1) + ... + 1 = n*(n+1)/2
        int totalRank = n * (n + 1) / 2;

        // Random value in range [0, totalRank)
        int randomValue = random.nextInt(totalRank);
        int cumulativeRank = 0;

        // Best chromosome (index 0) has rank n, worst has rank 1
        for (int i = 0; i < n; i++) {
            cumulativeRank += (n - i); // Rank decreases with index
            if (cumulativeRank > randomValue) {
                return chromosomes.get(i);
            }
        }

        // Fallback
        return chromosomes.get(0);
    }

    // ==================== Configuration ====================

    /**
     * Gets the current selection method.
     *
     * @return the selection method
     */
    public SelectionMethod getMethod() {
        return method;
    }

    /**
     * Sets the selection method.
     *
     * @param method the selection method
     */
    public void setMethod(SelectionMethod method) {
        this.method = method;
    }

    /**
     * Gets the tournament size.
     *
     * @return the tournament size
     */
    public int getTournamentSize() {
        return tournamentSize;
    }

    /**
     * Sets the tournament size.
     * Recommended values: 3-5 (from CLAUDE.md specification)
     *
     * @param tournamentSize the tournament size (must be >= 2)
     * @throws IllegalArgumentException if tournamentSize < 2
     */
    public void setTournamentSize(int tournamentSize) {
        if (tournamentSize < 2) {
            throw new IllegalArgumentException("Tournament size must be at least 2");
        }
        this.tournamentSize = tournamentSize;
    }

    /**
     * Creates a tournament selection operator.
     *
     * @param random         random number generator
     * @param tournamentSize the tournament size (3-5 recommended)
     * @return configured SelectionOperator
     */
    public static SelectionOperator tournament(Random random, int tournamentSize) {
        SelectionOperator operator = new SelectionOperator(random, SelectionMethod.TOURNAMENT);
        operator.setTournamentSize(tournamentSize);
        return operator;
    }

    /**
     * Creates a roulette wheel selection operator.
     *
     * @param random random number generator
     * @return configured SelectionOperator
     */
    public static SelectionOperator rouletteWheel(Random random) {
        return new SelectionOperator(random, SelectionMethod.ROULETTE_WHEEL);
    }

    /**
     * Creates a rank-based selection operator.
     *
     * @param random random number generator
     * @return configured SelectionOperator
     */
    public static SelectionOperator rank(Random random) {
        return new SelectionOperator(random, SelectionMethod.RANK);
    }
}
