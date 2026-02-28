package com.optimatch.algorithm;

import java.util.Random;

/**
 * Mutation operators for the genetic algorithm.
 * Responsible for introducing random variations in chromosomes.
 */
public class MutationOperator {

    private final Random random;
    private MutationMethod method;
    private double mutationRate;
    private int[] availableProjectIds;

    /**
     * Available mutation methods.
     */
    public enum MutationMethod {
        /** Swap mutation - swap assignments of two random students */
        SWAP,
        /** Random reset - change one gene to a random valid value */
        RANDOM_RESET,
        /** Scramble - shuffle a random segment */
        SCRAMBLE,
        /** Inversion - reverse a random segment */
        INVERSION
    }

    /**
     * Creates a MutationOperator with swap mutation (default).
     *
     * @param random random number generator
     */
    public MutationOperator(Random random) {
        this.random = random;
        this.method = MutationMethod.SWAP;
        this.mutationRate = 0.02; // Default from CLAUDE.md range (0.01-0.05)
    }

    /**
     * Creates a MutationOperator with the specified method and rate.
     *
     * @param random       random number generator
     * @param method       the mutation method to use
     * @param mutationRate probability of mutation occurring (0.0 to 1.0)
     */
    public MutationOperator(Random random, MutationMethod method, double mutationRate) {
        this.random = random;
        this.method = method;
        this.mutationRate = mutationRate;
    }

    /**
     * Sets the available project IDs for random reset mutation.
     *
     * @param projectIds array of valid project IDs
     */
    public void setAvailableProjectIds(int[] projectIds) {
        this.availableProjectIds = projectIds;
    }

    /**
     * Mutates a chromosome based on the mutation rate.
     * The chromosome is modified in place.
     *
     * @param chromosome the chromosome to mutate
     * @return true if mutation occurred
     */
    public boolean mutate(Chromosome chromosome) {
        if (random.nextDouble() > mutationRate) {
            return false;
        }

        switch (method) {
            case SWAP:
                swapMutate(chromosome);
                break;
            case RANDOM_RESET:
                randomResetMutate(chromosome);
                break;
            case SCRAMBLE:
                scrambleMutate(chromosome);
                break;
            case INVERSION:
                inversionMutate(chromosome);
                break;
        }

        return true;
    }

    /**
     * Mutates a chromosome and returns a new mutated copy.
     * Original chromosome is not modified.
     *
     * @param chromosome the chromosome to mutate
     * @return a new mutated chromosome
     */
    public Chromosome mutateAndCopy(Chromosome chromosome) {
        Chromosome copy = chromosome.copy();
        mutate(copy);
        return copy;
    }

    /**
     * Applies mutation to each gene independently based on per-gene rate.
     * Different from standard mutation which applies once per chromosome.
     *
     * @param chromosome  the chromosome to mutate
     * @param perGeneRate probability of each gene being mutated
     * @return number of genes mutated
     */
    public int mutatePerGene(Chromosome chromosome, double perGeneRate) {
        if (availableProjectIds == null || availableProjectIds.length == 0) {
            throw new IllegalStateException("Available project IDs must be set for per-gene mutation");
        }

        int mutationCount = 0;
        for (int i = 0; i < chromosome.getLength(); i++) {
            if (random.nextDouble() < perGeneRate) {
                int newProject = availableProjectIds[random.nextInt(availableProjectIds.length)];
                chromosome.setAssignment(i, newProject);
                mutationCount++;
            }
        }
        return mutationCount;
    }

    // ==================== Swap Mutation ====================

    /**
     * Swap mutation: swap the assignments of two random students.
     * This is the recommended method per CLAUDE.md specification.
     *
     * Before: [A, B, C, D, E]  (swap indices 1 and 3)
     * After:  [A, D, C, B, E]
     *
     * @param chromosome the chromosome to mutate
     */
    private void swapMutate(Chromosome chromosome) {
        int length = chromosome.getLength();
        if (length < 2) {
            return;
        }

        int index1 = random.nextInt(length);
        int index2 = random.nextInt(length);

        // Ensure different indices
        while (index2 == index1) {
            index2 = random.nextInt(length);
        }

        chromosome.swapAssignments(index1, index2);
    }

    /**
     * Swap mutation with specified indices.
     *
     * @param chromosome the chromosome to mutate
     * @param index1     first student index
     * @param index2     second student index
     */
    public void swapMutate(Chromosome chromosome, int index1, int index2) {
        chromosome.swapAssignments(index1, index2);
    }

    /**
     * Multiple swap mutation: perform multiple swaps.
     *
     * @param chromosome the chromosome to mutate
     * @param swapCount  number of swaps to perform
     */
    public void multiSwapMutate(Chromosome chromosome, int swapCount) {
        for (int i = 0; i < swapCount; i++) {
            swapMutate(chromosome);
        }
    }

    // ==================== Random Reset Mutation ====================

    /**
     * Random reset mutation: change one random gene to a random valid project.
     *
     * @param chromosome the chromosome to mutate
     */
    private void randomResetMutate(Chromosome chromosome) {
        if (availableProjectIds == null || availableProjectIds.length == 0) {
            throw new IllegalStateException("Available project IDs must be set for random reset mutation");
        }

        int index = random.nextInt(chromosome.getLength());
        int newProject = availableProjectIds[random.nextInt(availableProjectIds.length)];
        chromosome.setAssignment(index, newProject);
    }

    /**
     * Random reset mutation at a specific index.
     *
     * @param chromosome the chromosome to mutate
     * @param index      the gene index to reset
     */
    public void randomResetMutate(Chromosome chromosome, int index) {
        if (availableProjectIds == null || availableProjectIds.length == 0) {
            throw new IllegalStateException("Available project IDs must be set for random reset mutation");
        }

        int newProject = availableProjectIds[random.nextInt(availableProjectIds.length)];
        chromosome.setAssignment(index, newProject);
    }

    /**
     * Random reset mutation to a specific project.
     *
     * @param chromosome the chromosome to mutate
     * @param index      the gene index to reset
     * @param projectId  the new project ID
     */
    public void resetMutate(Chromosome chromosome, int index, int projectId) {
        chromosome.setAssignment(index, projectId);
    }

    // ==================== Scramble Mutation ====================

    /**
     * Scramble mutation: shuffle a random segment of the chromosome.
     *
     * Before: [A, B, C, D, E, F]  (scramble indices 1-4)
     * After:  [A, D, B, E, C, F]  (segment shuffled randomly)
     *
     * @param chromosome the chromosome to mutate
     */
    private void scrambleMutate(Chromosome chromosome) {
        int length = chromosome.getLength();
        if (length < 2) {
            return;
        }

        // Select random segment
        int start = random.nextInt(length);
        int end = random.nextInt(length);
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        // Fisher-Yates shuffle on segment
        for (int i = end; i > start; i--) {
            int j = start + random.nextInt(i - start + 1);
            chromosome.swapAssignments(i, j);
        }
    }

    /**
     * Scramble mutation on a specific segment.
     *
     * @param chromosome the chromosome to mutate
     * @param start      start index (inclusive)
     * @param end        end index (inclusive)
     */
    public void scrambleMutate(Chromosome chromosome, int start, int end) {
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        for (int i = end; i > start; i--) {
            int j = start + random.nextInt(i - start + 1);
            chromosome.swapAssignments(i, j);
        }
    }

    // ==================== Inversion Mutation ====================

    /**
     * Inversion mutation: reverse a random segment of the chromosome.
     *
     * Before: [A, B, C, D, E, F]  (invert indices 1-4)
     * After:  [A, E, D, C, B, F]
     *
     * @param chromosome the chromosome to mutate
     */
    private void inversionMutate(Chromosome chromosome) {
        int length = chromosome.getLength();
        if (length < 2) {
            return;
        }

        // Select random segment
        int start = random.nextInt(length);
        int end = random.nextInt(length);
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        // Reverse segment
        while (start < end) {
            chromosome.swapAssignments(start, end);
            start++;
            end--;
        }
    }

    /**
     * Inversion mutation on a specific segment.
     *
     * @param chromosome the chromosome to mutate
     * @param start      start index (inclusive)
     * @param end        end index (inclusive)
     */
    public void inversionMutate(Chromosome chromosome, int start, int end) {
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        while (start < end) {
            chromosome.swapAssignments(start, end);
            start++;
            end--;
        }
    }

    // ==================== Adaptive Mutation ====================

    /**
     * Applies adaptive mutation based on fitness.
     * Lower fitness = higher mutation rate to explore more.
     *
     * @param chromosome    the chromosome to mutate
     * @param currentFitness the chromosome's fitness
     * @param maxFitness     the best fitness in population
     * @param minRate        minimum mutation rate
     * @param maxRate        maximum mutation rate
     * @return true if mutation occurred
     */
    public boolean adaptiveMutate(Chromosome chromosome, double currentFitness,
                                   double maxFitness, double minRate, double maxRate) {
        // Calculate adaptive rate: worse fitness = higher rate
        double fitnessRatio = maxFitness > 0 ? currentFitness / maxFitness : 0;
        double adaptiveRate = maxRate - (fitnessRatio * (maxRate - minRate));

        if (random.nextDouble() < adaptiveRate) {
            swapMutate(chromosome);
            return true;
        }
        return false;
    }

    // ==================== Configuration ====================

    /**
     * Gets the current mutation method.
     *
     * @return the mutation method
     */
    public MutationMethod getMethod() {
        return method;
    }

    /**
     * Sets the mutation method.
     *
     * @param method the mutation method
     */
    public void setMethod(MutationMethod method) {
        this.method = method;
    }

    /**
     * Gets the mutation rate.
     *
     * @return the mutation rate (0.0 to 1.0)
     */
    public double getMutationRate() {
        return mutationRate;
    }

    /**
     * Sets the mutation rate.
     * Recommended values: 0.01-0.05 (from CLAUDE.md specification)
     *
     * @param mutationRate the probability of mutation (0.0 to 1.0)
     * @throws IllegalArgumentException if rate is not in [0.0, 1.0]
     */
    public void setMutationRate(double mutationRate) {
        if (mutationRate < 0.0 || mutationRate > 1.0) {
            throw new IllegalArgumentException("Mutation rate must be between 0.0 and 1.0");
        }
        this.mutationRate = mutationRate;
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a swap mutation operator (recommended).
     *
     * @param random       random number generator
     * @param mutationRate probability of mutation (0.01-0.05 recommended)
     * @return configured MutationOperator
     */
    public static MutationOperator swap(Random random, double mutationRate) {
        return new MutationOperator(random, MutationMethod.SWAP, mutationRate);
    }

    /**
     * Creates a random reset mutation operator.
     *
     * @param random       random number generator
     * @param mutationRate probability of mutation
     * @param projectIds   array of valid project IDs
     * @return configured MutationOperator
     */
    public static MutationOperator randomReset(Random random, double mutationRate, int[] projectIds) {
        MutationOperator operator = new MutationOperator(random, MutationMethod.RANDOM_RESET, mutationRate);
        operator.setAvailableProjectIds(projectIds);
        return operator;
    }

    /**
     * Creates a scramble mutation operator.
     *
     * @param random       random number generator
     * @param mutationRate probability of mutation
     * @return configured MutationOperator
     */
    public static MutationOperator scramble(Random random, double mutationRate) {
        return new MutationOperator(random, MutationMethod.SCRAMBLE, mutationRate);
    }

    /**
     * Creates an inversion mutation operator.
     *
     * @param random       random number generator
     * @param mutationRate probability of mutation
     * @return configured MutationOperator
     */
    public static MutationOperator inversion(Random random, double mutationRate) {
        return new MutationOperator(random, MutationMethod.INVERSION, mutationRate);
    }
}
