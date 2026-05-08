package com.optimatch.algorithm;

import java.util.Random;

// per-gene swap mutation
// RU: вероятность mutationRate проверяется на КАЖДЫЙ ген, не на всю хромосому,
// поэтому ожидаемое число мутаций пропорционально длине хромосомы
public class MutationOperator {

    private final Random random;
    private double mutationRate;

    // default mutation rate of 0.02 per gene
    public MutationOperator(Random random) {
        this.random = random;
        this.mutationRate = 0.02;
    }

    // try mutating each gene, returns true if at least one swap happened
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

    // current mutation rate
    public double getMutationRate() {
        return mutationRate;
    }

    // update mutation rate, must be in [0, 1]
    public void setMutationRate(double mutationRate) {
        if (mutationRate < 0.0 || mutationRate > 1.0) {
            throw new IllegalArgumentException("Mutation rate must be between 0.0 and 1.0");
        }
        this.mutationRate = mutationRate;
    }
}
