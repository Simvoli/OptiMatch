package com.optimatch.algorithm;

import java.util.Random;

// tournament selection: pick k random chromosomes, return the fittest
public class SelectionOperator {

    private final Random random;
    private int tournamentSize;

    // default tournament size of 3
    public SelectionOperator(Random random) {
        this.random = random;
        this.tournamentSize = 3;
    }

    // run one tournament and return the winner
    public Chromosome select(Population population) {
        int popSize = population.getCurrentSize();
        Chromosome best = null;

        for (int i = 0; i < tournamentSize; i++) {
            Chromosome competitor = population.getChromosome(random.nextInt(popSize));
            if (best == null || competitor.getFitness() > best.getFitness()) {
                best = competitor;
            } else if (competitor.getFitness() == best.getFitness() && random.nextBoolean()) {
                // RU: на равных подкидываем монетку, чтобы не было перекоса в сторону ранних индексов
                best = competitor;
            }
        }
        return best;
    }

    // current tournament size
    public int getTournamentSize() {
        return tournamentSize;
    }

    // change tournament size, must be at least 2
    public void setTournamentSize(int tournamentSize) {
        if (tournamentSize < 2) {
            throw new IllegalArgumentException("Tournament size must be at least 2");
        }
        this.tournamentSize = tournamentSize;
    }
}
