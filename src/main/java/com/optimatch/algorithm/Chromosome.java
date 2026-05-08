package com.optimatch.algorithm;

import java.util.Arrays;
import java.util.Random;

// one candidate solution: index = student index, value = project id assigned to that student
public class Chromosome implements Comparable<Chromosome> {

    private int[] assignments;
    private double fitness;
    private boolean fitnessCalculated;
    private boolean isValid;

    // empty chromosome of given size
    public Chromosome(int numStudents) {
        this.assignments = new int[numStudents];
        this.fitness = 0.0;
        this.fitnessCalculated = false;
        this.isValid = false;
    }

    // chromosome built from a ready array
    public Chromosome(int[] assignments) {
        this.assignments = Arrays.copyOf(assignments, assignments.length);
        this.fitness = 0.0;
        this.fitnessCalculated = false;
        this.isValid = false;
    }

    // deep copy
    public Chromosome(Chromosome other) {
        this.assignments = Arrays.copyOf(other.assignments, other.assignments.length);
        this.fitness = other.fitness;
        this.fitnessCalculated = other.fitnessCalculated;
        this.isValid = other.isValid;
    }

    // random assignment for each student
    public static Chromosome createRandom(int numStudents, int[] projectIds, Random random) {
        Chromosome chromosome = new Chromosome(numStudents);
        for (int i = 0; i < numStudents; i++) {
            int randomIndex = random.nextInt(projectIds.length);
            chromosome.assignments[i] = projectIds[randomIndex];
        }
        return chromosome;
    }

    // number of students
    public int getLength() {
        return assignments.length;
    }

    // project assigned to a single student
    public int getAssignment(int studentIndex) {
        return assignments[studentIndex];
    }

    // change a student's assignment, fitness becomes stale
    public void setAssignment(int studentIndex, int projectId) {
        assignments[studentIndex] = projectId;
        invalidateFitness();
    }

    // copy of the full assignment array
    public int[] getAssignments() {
        return Arrays.copyOf(assignments, assignments.length);
    }

    // current fitness value (0 if not yet calculated)
    public double getFitness() {
        return fitness;
    }

    // store calculated fitness
    public void setFitness(double fitness) {
        this.fitness = fitness;
        this.fitnessCalculated = true;
    }

    // true if fitness is up to date
    public boolean isFitnessCalculated() {
        return fitnessCalculated;
    }

    // mark fitness as stale after edit
    public void invalidateFitness() {
        this.fitnessCalculated = false;
        this.fitness = 0.0;
    }

    // true if all constraints pass
    public boolean isValid() {
        return isValid;
    }

    // store validity flag
    public void setValid(boolean valid) {
        this.isValid = valid;
    }

    // swap projects of two students (used by mutation)
    public void swapAssignments(int studentIndex1, int studentIndex2) {
        int temp = assignments[studentIndex1];
        assignments[studentIndex1] = assignments[studentIndex2];
        assignments[studentIndex2] = temp;
        invalidateFitness();
    }

    // how many students sit on this project
    public int countStudentsInProject(int projectId) {
        int count = 0;
        for (int assignment : assignments) {
            if (assignment == projectId) {
                count++;
            }
        }
        return count;
    }

    // student indices currently on this project
    public int[] getStudentsInProject(int projectId) {
        int count = countStudentsInProject(projectId);
        int[] students = new int[count];
        int index = 0;
        for (int i = 0; i < assignments.length; i++) {
            if (assignments[i] == projectId) {
                students[index++] = i;
            }
        }
        return students;
    }

    // higher fitness wins, so we reverse the natural order
    @Override
    public int compareTo(Chromosome other) {
        return Double.compare(other.fitness, this.fitness);
    }

    // deep copy via copy constructor
    public Chromosome copy() {
        return new Chromosome(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Chromosome that = (Chromosome) o;
        return Arrays.equals(assignments, that.assignments);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(assignments);
    }

    @Override
    public String toString() {
        return "Chromosome{" +
                "fitness=" + String.format("%.2f", fitness) +
                ", valid=" + isValid +
                ", assignments=" + Arrays.toString(assignments) +
                '}';
    }
}
