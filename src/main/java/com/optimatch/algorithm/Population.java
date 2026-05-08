package com.optimatch.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A population of chromosomes evaluated together each generation.
 * Sorting is performed unconditionally because chromosome fitness can be
 * mutated externally (via {@link Chromosome#setFitness}), making any cached
 * sort flag unreliable.
 */
public class Population {

    private List<Chromosome> chromosomes;

    public Population(int initialCapacity) {
        this.chromosomes = new ArrayList<>(initialCapacity);
    }

    public static Population createRandom(int populationSize, int numStudents,
                                          int[] projectIds, Random random) {
        Population population = new Population(populationSize);
        for (int i = 0; i < populationSize; i++) {
            population.addChromosome(Chromosome.createRandom(numStudents, projectIds, random));
        }
        return population;
    }

    public void addChromosome(Chromosome chromosome) {
        chromosomes.add(chromosome);
    }

    public Chromosome getChromosome(int index) {
        return chromosomes.get(index);
    }

    public int getCurrentSize() {
        return chromosomes.size();
    }

    public List<Chromosome> getChromosomes() {
        return Collections.unmodifiableList(chromosomes);
    }

    public void sortByFitness() {
        Collections.sort(chromosomes);
    }

    public Chromosome getBest() {
        if (chromosomes.isEmpty()) {
            return null;
        }
        sortByFitness();
        return chromosomes.get(0);
    }

    public List<Chromosome> getElite(int n) {
        sortByFitness();
        int eliteCount = Math.min(n, chromosomes.size());
        List<Chromosome> elite = new ArrayList<>(eliteCount);
        for (int i = 0; i < eliteCount; i++) {
            elite.add(chromosomes.get(i).copy());
        }
        return elite;
    }

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

    public double getBestFitness() {
        Chromosome best = getBest();
        return best != null ? best.getFitness() : 0.0;
    }

    public double getWorstFitness() {
        if (chromosomes.isEmpty()) {
            return 0.0;
        }
        sortByFitness();
        return chromosomes.get(chromosomes.size() - 1).getFitness();
    }

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

    public int countValid() {
        int count = 0;
        for (Chromosome chromosome : chromosomes) {
            if (chromosome.isValid()) {
                count++;
            }
        }
        return count;
    }

    public void replaceAll(List<Chromosome> newChromosomes) {
        chromosomes.clear();
        chromosomes.addAll(newChromosomes);
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
