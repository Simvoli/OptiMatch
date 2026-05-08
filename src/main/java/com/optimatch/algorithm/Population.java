package com.optimatch.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

// pool of chromosomes for one generation
public class Population {

    private List<Chromosome> chromosomes;

    // empty population with reserved capacity
    public Population(int initialCapacity) {
        this.chromosomes = new ArrayList<>(initialCapacity);
    }

    // build a random starting population
    public static Population createRandom(int populationSize, int numStudents,
                                          int[] projectIds, Random random) {
        Population population = new Population(populationSize);
        for (int i = 0; i < populationSize; i++) {
            population.addChromosome(Chromosome.createRandom(numStudents, projectIds, random));
        }
        return population;
    }

    // add a chromosome to the pool
    public void addChromosome(Chromosome chromosome) {
        chromosomes.add(chromosome);
    }

    // chromosome at the given index
    public Chromosome getChromosome(int index) {
        return chromosomes.get(index);
    }

    // current number of chromosomes
    public int getCurrentSize() {
        return chromosomes.size();
    }

    // read-only view of the full list
    public List<Chromosome> getChromosomes() {
        return Collections.unmodifiableList(chromosomes);
    }

    // RU: сортируем безусловно: fitness может быть обновлён извне через setFitness,
    // поэтому любой кеш "уже отсортировано" будет ненадёжен
    public void sortByFitness() {
        Collections.sort(chromosomes);
    }

    // best chromosome (or null if empty)
    public Chromosome getBest() {
        if (chromosomes.isEmpty()) {
            return null;
        }
        sortByFitness();
        return chromosomes.get(0);
    }

    // top n chromosomes as deep copies
    public List<Chromosome> getElite(int n) {
        sortByFitness();
        int eliteCount = Math.min(n, chromosomes.size());
        List<Chromosome> elite = new ArrayList<>(eliteCount);
        for (int i = 0; i < eliteCount; i++) {
            elite.add(chromosomes.get(i).copy());
        }
        return elite;
    }

    // mean fitness across the pool
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

    // best fitness value (0 if empty)
    public double getBestFitness() {
        Chromosome best = getBest();
        return best != null ? best.getFitness() : 0.0;
    }

    // worst fitness value (0 if empty)
    public double getWorstFitness() {
        if (chromosomes.isEmpty()) {
            return 0.0;
        }
        sortByFitness();
        return chromosomes.get(chromosomes.size() - 1).getFitness();
    }

    // population spread, useful for diversity tracking
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

    // number of fully valid chromosomes in the pool
    public int countValid() {
        int count = 0;
        for (Chromosome chromosome : chromosomes) {
            if (chromosome.isValid()) {
                count++;
            }
        }
        return count;
    }

    // wholesale replacement of the pool, used between generations
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
