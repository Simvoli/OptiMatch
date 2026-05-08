package com.optimatch.algorithm;

import java.util.Random;

/**
 * Tournament selection operator: randomly samples {@code tournamentSize}
 * chromosomes from the population and returns the fittest. Ties are broken
 * randomly to avoid systematic bias toward earlier indices.
 */
public class SelectionOperator {

    private final Random random;
    private int tournamentSize;

    public SelectionOperator(Random random) {
        this.random = random;
        this.tournamentSize = 3;
    }

    public Chromosome select(Population population) {
        int popSize = population.getCurrentSize();
        Chromosome best = null;

        for (int i = 0; i < tournamentSize; i++) {
            Chromosome competitor = population.getChromosome(random.nextInt(popSize));
            if (best == null || competitor.getFitness() > best.getFitness()) {
                best = competitor;
            } else if (competitor.getFitness() == best.getFitness() && random.nextBoolean()) {
                best = competitor;
            }
        }
        return best;
    }

    public int getTournamentSize() {
        return tournamentSize;
    }

    public void setTournamentSize(int tournamentSize) {
        if (tournamentSize < 2) {
            throw new IllegalArgumentException("Tournament size must be at least 2");
        }
        this.tournamentSize = tournamentSize;
    }
}
