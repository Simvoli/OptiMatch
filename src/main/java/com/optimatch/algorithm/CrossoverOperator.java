package com.optimatch.algorithm;

import java.util.Random;

/**
 * Uniform crossover operator: each gene is independently inherited from
 * one of the two parents with equal probability.
 */
public class CrossoverOperator {

    private final Random random;
    private double crossoverRate;

    public CrossoverOperator(Random random) {
        this.random = random;
        this.crossoverRate = 0.8;
    }

    /**
     * Performs crossover between two parents and returns two offspring.
     * If the crossover roll fails (or chromosomes have fewer than 2 genes),
     * returns copies of the parents unchanged.
     */
    public Chromosome[] crossover(Chromosome parent1, Chromosome parent2) {
        if (random.nextDouble() > crossoverRate || parent1.getLength() < 2) {
            return new Chromosome[]{parent1.copy(), parent2.copy()};
        }

        int length = parent1.getLength();
        Chromosome offspring1 = new Chromosome(length);
        Chromosome offspring2 = new Chromosome(length);

        for (int i = 0; i < length; i++) {
            if (random.nextBoolean()) {
                offspring1.setAssignment(i, parent1.getAssignment(i));
                offspring2.setAssignment(i, parent2.getAssignment(i));
            } else {
                offspring1.setAssignment(i, parent2.getAssignment(i));
                offspring2.setAssignment(i, parent1.getAssignment(i));
            }
        }
        return new Chromosome[]{offspring1, offspring2};
    }

    public double getCrossoverRate() {
        return crossoverRate;
    }

    public void setCrossoverRate(double crossoverRate) {
        if (crossoverRate < 0.0 || crossoverRate > 1.0) {
            throw new IllegalArgumentException("Crossover rate must be between 0.0 and 1.0");
        }
        this.crossoverRate = crossoverRate;
    }
}
