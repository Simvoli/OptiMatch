package com.optimatch.algorithm;

import java.util.Random;

/**
 * Per-gene swap mutation operator.
 *
 * Each gene independently has probability {@code mutationRate} of being swapped
 * with another random gene. This is the standard interpretation of mutation rate
 * in GA literature and produces a number of mutations proportional to chromosome
 * length.
 */
public class MutationOperator {

    private final Random random;
    private double mutationRate;

    public MutationOperator(Random random) {
        this.random = random;
        this.mutationRate = 0.02;
    }

    public boolean mutate(Chromosome chromosome) {
        int length = chromosome.getLength();
        if (length < 2) {
            return false;
        }
        boolean mutated = false;
        for (int i = 0; i < length; i++) {
            if (random.nextDouble() < mutationRate) {
                int j = random.nextInt(length);
                if (j == i) {
                    j = (j + 1) % length;
                }
                chromosome.swapAssignments(i, j);
                mutated = true;
            }
        }
        return mutated;
    }

    public double getMutationRate() {
        return mutationRate;
    }

    public void setMutationRate(double mutationRate) {
        if (mutationRate < 0.0 || mutationRate > 1.0) {
            throw new IllegalArgumentException("Mutation rate must be between 0.0 and 1.0");
        }
        this.mutationRate = mutationRate;
    }
}
