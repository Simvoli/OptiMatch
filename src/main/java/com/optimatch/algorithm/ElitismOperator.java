package com.optimatch.algorithm;

import java.util.List;

/**
 * Elitism operator: preserves the top {@code elitePercentage} of the population
 * unchanged across generations, ensuring the best fitness never decreases.
 * Caller receives deep copies, so subsequent mutations do not affect them.
 */
public class ElitismOperator {

    private static final int MIN_ELITE = 1;

    private final double elitePercentage;

    public ElitismOperator(double elitePercentage) {
        if (elitePercentage < 0.0 || elitePercentage > 1.0) {
            throw new IllegalArgumentException("Elite percentage must be between 0.0 and 1.0");
        }
        this.elitePercentage = elitePercentage;
    }

    public List<Chromosome> selectElite(Population population) {
        int populationSize = population.getCurrentSize();
        int count = (int) Math.round(populationSize * elitePercentage);
        count = Math.max(count, MIN_ELITE);
        count = Math.min(count, populationSize);
        return population.getElite(count);
    }

    public double getElitePercentage() {
        return elitePercentage;
    }
}
