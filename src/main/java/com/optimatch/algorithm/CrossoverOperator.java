package com.optimatch.algorithm;

import java.util.Random;

/**
 * Crossover operators for the genetic algorithm.
 * Responsible for combining two parent chromosomes to create offspring.
 */
public class CrossoverOperator {

    private final Random random;
    private CrossoverMethod method;
    private double crossoverRate;
    private double uniformBias;

    /**
     * Available crossover methods.
     */
    public enum CrossoverMethod {
        /** Uniform crossover - each gene randomly from either parent */
        UNIFORM,
        /** Single-point crossover - split at one random point */
        SINGLE_POINT,
        /** Two-point crossover - swap middle segment */
        TWO_POINT
    }

    /**
     * Creates a CrossoverOperator with uniform crossover (default).
     *
     * @param random random number generator
     */
    public CrossoverOperator(Random random) {
        this.random = random;
        this.method = CrossoverMethod.UNIFORM;
        this.crossoverRate = 0.8; // Default from CLAUDE.md range (0.7-0.9)
        this.uniformBias = 0.5;   // 50% chance from each parent
    }

    /**
     * Creates a CrossoverOperator with the specified method.
     *
     * @param random        random number generator
     * @param method        the crossover method to use
     * @param crossoverRate probability of crossover occurring (0.0 to 1.0)
     */
    public CrossoverOperator(Random random, CrossoverMethod method, double crossoverRate) {
        this.random = random;
        this.method = method;
        this.crossoverRate = crossoverRate;
        this.uniformBias = 0.5;
    }

    /**
     * Performs crossover between two parents to produce two offspring.
     * If crossover doesn't occur (based on rate), returns copies of parents.
     *
     * @param parent1 the first parent
     * @param parent2 the second parent
     * @return array of two offspring chromosomes
     */
    public Chromosome[] crossover(Chromosome parent1, Chromosome parent2) {
        // Check if crossover should occur
        if (random.nextDouble() > crossoverRate) {
            // No crossover - return copies of parents
            return new Chromosome[]{parent1.copy(), parent2.copy()};
        }

        switch (method) {
            case UNIFORM:
                return uniformCrossover(parent1, parent2);
            case SINGLE_POINT:
                return singlePointCrossover(parent1, parent2);
            case TWO_POINT:
                return twoPointCrossover(parent1, parent2);
            default:
                return uniformCrossover(parent1, parent2);
        }
    }

    /**
     * Performs crossover and returns a single offspring.
     * Randomly selects one of the two offspring produced.
     *
     * @param parent1 the first parent
     * @param parent2 the second parent
     * @return single offspring chromosome
     */
    public Chromosome crossoverSingle(Chromosome parent1, Chromosome parent2) {
        Chromosome[] offspring = crossover(parent1, parent2);
        return random.nextBoolean() ? offspring[0] : offspring[1];
    }

    // ==================== Uniform Crossover ====================

    /**
     * Uniform crossover: each gene is randomly selected from either parent.
     * This is the recommended method per CLAUDE.md specification.
     *
     * @param parent1 the first parent
     * @param parent2 the second parent
     * @return array of two offspring
     */
    private Chromosome[] uniformCrossover(Chromosome parent1, Chromosome parent2) {
        int length = parent1.getLength();
        Chromosome offspring1 = new Chromosome(length);
        Chromosome offspring2 = new Chromosome(length);

        for (int i = 0; i < length; i++) {
            if (random.nextDouble() < uniformBias) {
                // Offspring1 gets gene from parent1, offspring2 from parent2
                offspring1.setAssignment(i, parent1.getAssignment(i));
                offspring2.setAssignment(i, parent2.getAssignment(i));
            } else {
                // Swap: offspring1 gets from parent2, offspring2 from parent1
                offspring1.setAssignment(i, parent2.getAssignment(i));
                offspring2.setAssignment(i, parent1.getAssignment(i));
            }
        }

        return new Chromosome[]{offspring1, offspring2};
    }

    /**
     * Uniform crossover with mask: uses a binary mask to determine gene source.
     *
     * @param parent1 the first parent
     * @param parent2 the second parent
     * @param mask    boolean array where true = take from parent1
     * @return array of two offspring
     */
    public Chromosome[] uniformCrossoverWithMask(Chromosome parent1, Chromosome parent2, boolean[] mask) {
        int length = parent1.getLength();
        Chromosome offspring1 = new Chromosome(length);
        Chromosome offspring2 = new Chromosome(length);

        for (int i = 0; i < length; i++) {
            if (mask[i]) {
                offspring1.setAssignment(i, parent1.getAssignment(i));
                offspring2.setAssignment(i, parent2.getAssignment(i));
            } else {
                offspring1.setAssignment(i, parent2.getAssignment(i));
                offspring2.setAssignment(i, parent1.getAssignment(i));
            }
        }

        return new Chromosome[]{offspring1, offspring2};
    }

    // ==================== Single-Point Crossover ====================

    /**
     * Single-point crossover: split chromosomes at one random point and swap tails.
     *
     * Parent1: [A A A A | B B B]
     * Parent2: [C C C C | D D D]
     * Result1: [A A A A | D D D]
     * Result2: [C C C C | B B B]
     *
     * @param parent1 the first parent
     * @param parent2 the second parent
     * @return array of two offspring
     */
    private Chromosome[] singlePointCrossover(Chromosome parent1, Chromosome parent2) {
        int length = parent1.getLength();
        Chromosome offspring1 = new Chromosome(length);
        Chromosome offspring2 = new Chromosome(length);

        // Random crossover point (1 to length-1)
        int crossPoint = 1 + random.nextInt(length - 1);

        for (int i = 0; i < length; i++) {
            if (i < crossPoint) {
                offspring1.setAssignment(i, parent1.getAssignment(i));
                offspring2.setAssignment(i, parent2.getAssignment(i));
            } else {
                offspring1.setAssignment(i, parent2.getAssignment(i));
                offspring2.setAssignment(i, parent1.getAssignment(i));
            }
        }

        return new Chromosome[]{offspring1, offspring2};
    }

    /**
     * Single-point crossover with specified crossover point.
     *
     * @param parent1    the first parent
     * @param parent2    the second parent
     * @param crossPoint the index where crossover occurs
     * @return array of two offspring
     */
    public Chromosome[] singlePointCrossover(Chromosome parent1, Chromosome parent2, int crossPoint) {
        int length = parent1.getLength();
        Chromosome offspring1 = new Chromosome(length);
        Chromosome offspring2 = new Chromosome(length);

        for (int i = 0; i < length; i++) {
            if (i < crossPoint) {
                offspring1.setAssignment(i, parent1.getAssignment(i));
                offspring2.setAssignment(i, parent2.getAssignment(i));
            } else {
                offspring1.setAssignment(i, parent2.getAssignment(i));
                offspring2.setAssignment(i, parent1.getAssignment(i));
            }
        }

        return new Chromosome[]{offspring1, offspring2};
    }

    // ==================== Two-Point Crossover ====================

    /**
     * Two-point crossover: swap the segment between two random points.
     *
     * Parent1: [A A | B B B | C C]
     * Parent2: [D D | E E E | F F]
     * Result1: [A A | E E E | C C]
     * Result2: [D D | B B B | F F]
     *
     * @param parent1 the first parent
     * @param parent2 the second parent
     * @return array of two offspring
     */
    private Chromosome[] twoPointCrossover(Chromosome parent1, Chromosome parent2) {
        int length = parent1.getLength();
        Chromosome offspring1 = new Chromosome(length);
        Chromosome offspring2 = new Chromosome(length);

        // Two random crossover points
        int point1 = random.nextInt(length);
        int point2 = random.nextInt(length);

        // Ensure point1 < point2
        if (point1 > point2) {
            int temp = point1;
            point1 = point2;
            point2 = temp;
        }

        for (int i = 0; i < length; i++) {
            if (i >= point1 && i < point2) {
                // Middle segment - swap
                offspring1.setAssignment(i, parent2.getAssignment(i));
                offspring2.setAssignment(i, parent1.getAssignment(i));
            } else {
                // Outside segment - keep original
                offspring1.setAssignment(i, parent1.getAssignment(i));
                offspring2.setAssignment(i, parent2.getAssignment(i));
            }
        }

        return new Chromosome[]{offspring1, offspring2};
    }

    /**
     * Two-point crossover with specified crossover points.
     *
     * @param parent1 the first parent
     * @param parent2 the second parent
     * @param point1  the first crossover point
     * @param point2  the second crossover point
     * @return array of two offspring
     */
    public Chromosome[] twoPointCrossover(Chromosome parent1, Chromosome parent2, int point1, int point2) {
        int length = parent1.getLength();
        Chromosome offspring1 = new Chromosome(length);
        Chromosome offspring2 = new Chromosome(length);

        // Ensure point1 < point2
        if (point1 > point2) {
            int temp = point1;
            point1 = point2;
            point2 = temp;
        }

        for (int i = 0; i < length; i++) {
            if (i >= point1 && i < point2) {
                offspring1.setAssignment(i, parent2.getAssignment(i));
                offspring2.setAssignment(i, parent1.getAssignment(i));
            } else {
                offspring1.setAssignment(i, parent1.getAssignment(i));
                offspring2.setAssignment(i, parent2.getAssignment(i));
            }
        }

        return new Chromosome[]{offspring1, offspring2};
    }

    // ==================== Configuration ====================

    /**
     * Gets the current crossover method.
     *
     * @return the crossover method
     */
    public CrossoverMethod getMethod() {
        return method;
    }

    /**
     * Sets the crossover method.
     *
     * @param method the crossover method
     */
    public void setMethod(CrossoverMethod method) {
        this.method = method;
    }

    /**
     * Gets the crossover rate.
     *
     * @return the crossover rate (0.0 to 1.0)
     */
    public double getCrossoverRate() {
        return crossoverRate;
    }

    /**
     * Sets the crossover rate.
     * Recommended values: 0.7-0.9 (from CLAUDE.md specification)
     *
     * @param crossoverRate the probability of crossover (0.0 to 1.0)
     * @throws IllegalArgumentException if rate is not in [0.0, 1.0]
     */
    public void setCrossoverRate(double crossoverRate) {
        if (crossoverRate < 0.0 || crossoverRate > 1.0) {
            throw new IllegalArgumentException("Crossover rate must be between 0.0 and 1.0");
        }
        this.crossoverRate = crossoverRate;
    }

    /**
     * Gets the uniform crossover bias.
     *
     * @return the bias (probability of taking from parent1)
     */
    public double getUniformBias() {
        return uniformBias;
    }

    /**
     * Sets the uniform crossover bias.
     *
     * @param uniformBias probability of taking gene from parent1 (0.0 to 1.0)
     */
    public void setUniformBias(double uniformBias) {
        if (uniformBias < 0.0 || uniformBias > 1.0) {
            throw new IllegalArgumentException("Uniform bias must be between 0.0 and 1.0");
        }
        this.uniformBias = uniformBias;
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a uniform crossover operator (recommended).
     *
     * @param random        random number generator
     * @param crossoverRate probability of crossover (0.7-0.9 recommended)
     * @return configured CrossoverOperator
     */
    public static CrossoverOperator uniform(Random random, double crossoverRate) {
        return new CrossoverOperator(random, CrossoverMethod.UNIFORM, crossoverRate);
    }

    /**
     * Creates a single-point crossover operator.
     *
     * @param random        random number generator
     * @param crossoverRate probability of crossover
     * @return configured CrossoverOperator
     */
    public static CrossoverOperator singlePoint(Random random, double crossoverRate) {
        return new CrossoverOperator(random, CrossoverMethod.SINGLE_POINT, crossoverRate);
    }

    /**
     * Creates a two-point crossover operator.
     *
     * @param random        random number generator
     * @param crossoverRate probability of crossover
     * @return configured CrossoverOperator
     */
    public static CrossoverOperator twoPoint(Random random, double crossoverRate) {
        return new CrossoverOperator(random, CrossoverMethod.TWO_POINT, crossoverRate);
    }
}
