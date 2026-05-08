package com.optimatch.algorithm;

import java.util.Random;

// uniform crossover: each gene goes to either offspring with 50/50 chance
public class CrossoverOperator {

    private final Random random;
    private double crossoverRate;

    // default crossover rate of 0.8
    public CrossoverOperator(Random random) {
        this.random = random;
        this.crossoverRate = 0.8;
    }

    // produce two offspring from two parents, or copies if the roll fails
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

    // current crossover rate
    public double getCrossoverRate() {
        return crossoverRate;
    }

    // update crossover rate, must be in [0, 1]
    public void setCrossoverRate(double crossoverRate) {
        if (crossoverRate < 0.0 || crossoverRate > 1.0) {
            throw new IllegalArgumentException("Crossover rate must be between 0.0 and 1.0");
        }
        this.crossoverRate = crossoverRate;
    }
}
