package com.optimatch.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Represents a population of chromosomes in the genetic algorithm.
 * Manages creation, sorting, and selection of chromosomes.
 */
public class Population {

    private List<Chromosome> chromosomes;
    private int size;
    private boolean isSorted;

    /**
     * Creates an empty population with the specified capacity.
     *
     * @param size the maximum population size
     */
    public Population(int size) {
        this.size = size;
        this.chromosomes = new ArrayList<>(size);
        this.isSorted = false;
    }

    /**
     * Creates a population from an existing list of chromosomes.
     *
     * @param chromosomes the list of chromosomes
     */
    public Population(List<Chromosome> chromosomes) {
        this.chromosomes = new ArrayList<>(chromosomes);
        this.size = chromosomes.size();
        this.isSorted = false;
    }

    /**
     * Creates a random initial population.
     *
     * @param populationSize the number of chromosomes to create
     * @param numStudents    the number of students (chromosome length)
     * @param projectIds     array of available project IDs
     * @param random         random number generator
     * @return a new population with random chromosomes
     */
    public static Population createRandom(int populationSize, int numStudents,
                                          int[] projectIds, Random random) {
        Population population = new Population(populationSize);
        for (int i = 0; i < populationSize; i++) {
            Chromosome chromosome = Chromosome.createRandom(numStudents, projectIds, random);
            population.addChromosome(chromosome);
        }
        return population;
    }

    /**
     * Adds a chromosome to the population.
     *
     * @param chromosome the chromosome to add
     */
    public void addChromosome(Chromosome chromosome) {
        chromosomes.add(chromosome);
        isSorted = false;
    }

    /**
     * Gets a chromosome by index.
     *
     * @param index the index
     * @return the chromosome at the given index
     */
    public Chromosome getChromosome(int index) {
        return chromosomes.get(index);
    }

    /**
     * Sets a chromosome at the specified index.
     *
     * @param index      the index
     * @param chromosome the chromosome to set
     */
    public void setChromosome(int index, Chromosome chromosome) {
        chromosomes.set(index, chromosome);
        isSorted = false;
    }

    /**
     * Gets the current population size.
     *
     * @return the number of chromosomes
     */
    public int getCurrentSize() {
        return chromosomes.size();
    }

    /**
     * Gets the target population size.
     *
     * @return the target size
     */
    public int getTargetSize() {
        return size;
    }

    /**
     * Gets all chromosomes.
     *
     * @return unmodifiable list of chromosomes
     */
    public List<Chromosome> getChromosomes() {
        return Collections.unmodifiableList(chromosomes);
    }

    /**
     * Sorts the population by fitness (best first).
     * Higher fitness chromosomes come first.
     */
    public void sortByFitness() {
        if (!isSorted) {
            Collections.sort(chromosomes);
            isSorted = true;
        }
    }

    /**
     * Gets the best chromosome (highest fitness).
     * Sorts the population if not already sorted.
     *
     * @return the best chromosome, or null if population is empty
     */
    public Chromosome getBest() {
        if (chromosomes.isEmpty()) {
            return null;
        }
        sortByFitness();
        return chromosomes.get(0);
    }

    /**
     * Gets the worst chromosome (lowest fitness).
     * Sorts the population if not already sorted.
     *
     * @return the worst chromosome, or null if population is empty
     */
    public Chromosome getWorst() {
        if (chromosomes.isEmpty()) {
            return null;
        }
        sortByFitness();
        return chromosomes.get(chromosomes.size() - 1);
    }

    /**
     * Gets the top N chromosomes (elite).
     *
     * @param n the number of elite chromosomes to get
     * @return list of the top N chromosomes
     */
    public List<Chromosome> getElite(int n) {
        sortByFitness();
        int eliteCount = Math.min(n, chromosomes.size());
        List<Chromosome> elite = new ArrayList<>(eliteCount);
        for (int i = 0; i < eliteCount; i++) {
            elite.add(chromosomes.get(i).copy());
        }
        return elite;
    }

    /**
     * Gets the average fitness of the population.
     *
     * @return the average fitness
     */
    public double getAverageFitness() {
        if (chromosomes.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (Chromosome chromosome : chromosomes) {
            sum += chromosome.getFitness();
        }
        return sum / chromosomes.size();
    }

    /**
     * Gets the best fitness in the population.
     *
     * @return the highest fitness value
     */
    public double getBestFitness() {
        Chromosome best = getBest();
        return best != null ? best.getFitness() : 0.0;
    }

    /**
     * Gets the worst fitness in the population.
     *
     * @return the lowest fitness value
     */
    public double getWorstFitness() {
        Chromosome worst = getWorst();
        return worst != null ? worst.getFitness() : 0.0;
    }

    /**
     * Gets the fitness standard deviation.
     *
     * @return the standard deviation of fitness values
     */
    public double getFitnessStdDev() {
        if (chromosomes.size() < 2) {
            return 0.0;
        }
        double avg = getAverageFitness();
        double sumSquaredDiff = 0.0;
        for (Chromosome chromosome : chromosomes) {
            double diff = chromosome.getFitness() - avg;
            sumSquaredDiff += diff * diff;
        }
        return Math.sqrt(sumSquaredDiff / chromosomes.size());
    }

    /**
     * Counts valid chromosomes in the population.
     *
     * @return the number of valid chromosomes
     */
    public int countValid() {
        int count = 0;
        for (Chromosome chromosome : chromosomes) {
            if (chromosome.isValid()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Clears all chromosomes from the population.
     */
    public void clear() {
        chromosomes.clear();
        isSorted = false;
    }

    /**
     * Replaces the population with a new set of chromosomes.
     *
     * @param newChromosomes the new chromosomes
     */
    public void replaceAll(List<Chromosome> newChromosomes) {
        chromosomes.clear();
        chromosomes.addAll(newChromosomes);
        isSorted = false;
    }

    /**
     * Trims the population to the target size, keeping the best chromosomes.
     */
    public void trimToSize() {
        if (chromosomes.size() > size) {
            sortByFitness();
            chromosomes = new ArrayList<>(chromosomes.subList(0, size));
        }
    }

    /**
     * Checks if the population contains a chromosome with identical assignments.
     *
     * @param chromosome the chromosome to check
     * @return true if a duplicate exists
     */
    public boolean containsDuplicate(Chromosome chromosome) {
        for (Chromosome existing : chromosomes) {
            if (existing.equals(chromosome)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("Population[size=%d, best=%.2f, avg=%.2f, worst=%.2f, valid=%d]",
                chromosomes.size(),
                getBestFitness(),
                getAverageFitness(),
                getWorstFitness(),
                countValid());
    }
}
