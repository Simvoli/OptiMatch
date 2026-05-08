package com.optimatch.algorithm;

import java.util.List;

// keeps the best chromosomes alive across generations so fitness never drops
public class ElitismOperator {

    private static final int MIN_ELITE = 1;

    private final double elitePercentage;

    // store the elite percentage, must be in [0, 1]
    public ElitismOperator(double elitePercentage) {
        if (elitePercentage < 0.0 || elitePercentage > 1.0) {
            throw new IllegalArgumentException("Elite percentage must be between 0.0 and 1.0");
        }
        this.elitePercentage = elitePercentage;
    }

    // pick the top chunk and return deep copies
    public List<Chromosome> selectElite(Population population) {
        int populationSize = population.getCurrentSize();
        int count = (int) Math.round(populationSize * elitePercentage);
        count = Math.max(count, MIN_ELITE);
        count = Math.min(count, populationSize);
        return population.getElite(count);
    }
}
