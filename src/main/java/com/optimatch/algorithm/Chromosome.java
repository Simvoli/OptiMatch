package com.optimatch.algorithm;

import java.util.Arrays;
import java.util.Random;

/**
 * Represents a chromosome in the genetic algorithm.
 * A chromosome encodes a complete assignment solution where:
 * - Index = student index (0 to numStudents-1)
 * - Value = project ID assigned to that student
 *
 * Example: assignments[3] = 5 means student at index 3 is assigned to project ID 5
 */
public class Chromosome implements Comparable<Chromosome> {

    private int[] assignments;
    private double fitness;
    private boolean fitnessCalculated;
    private boolean isValid;

    /**
     * Creates an empty chromosome with the specified size.
     *
     * @param numStudents the number of students (chromosome length)
     */
    public Chromosome(int numStudents) {
        this.assignments = new int[numStudents];
        this.fitness = 0.0;
        this.fitnessCalculated = false;
        this.isValid = false;
    }

    /**
     * Creates a chromosome with the given assignments.
     *
     * @param assignments array where index = student index, value = project ID
     */
    public Chromosome(int[] assignments) {
        this.assignments = Arrays.copyOf(assignments, assignments.length);
        this.fitness = 0.0;
        this.fitnessCalculated = false;
        this.isValid = false;
    }

    /**
     * Copy constructor - creates a deep copy of another chromosome.
     *
     * @param other the chromosome to copy
     */
    public Chromosome(Chromosome other) {
        this.assignments = Arrays.copyOf(other.assignments, other.assignments.length);
        this.fitness = other.fitness;
        this.fitnessCalculated = other.fitnessCalculated;
        this.isValid = other.isValid;
    }

    /**
     * Creates a random chromosome by assigning each student to a random project.
     *
     * @param numStudents the number of students
     * @param projectIds  array of available project IDs
     * @param random      random number generator
     * @return a new randomly initialized chromosome
     */
    public static Chromosome createRandom(int numStudents, int[] projectIds, Random random) {
        Chromosome chromosome = new Chromosome(numStudents);
        for (int i = 0; i < numStudents; i++) {
            int randomIndex = random.nextInt(projectIds.length);
            chromosome.assignments[i] = projectIds[randomIndex];
        }
        return chromosome;
    }

    /**
     * Gets the number of students (chromosome length).
     *
     * @return the number of students
     */
    public int getLength() {
        return assignments.length;
    }

    /**
     * Gets the project ID assigned to a student.
     *
     * @param studentIndex the student's index
     * @return the project ID assigned to the student
     */
    public int getAssignment(int studentIndex) {
        return assignments[studentIndex];
    }

    /**
     * Sets the project ID for a student.
     *
     * @param studentIndex the student's index
     * @param projectId    the project ID to assign
     */
    public void setAssignment(int studentIndex, int projectId) {
        assignments[studentIndex] = projectId;
        invalidateFitness();
    }

    /**
     * Gets the entire assignments array.
     *
     * @return a copy of the assignments array
     */
    public int[] getAssignments() {
        return Arrays.copyOf(assignments, assignments.length);
    }

    /**
     * Gets the fitness value.
     *
     * @return the fitness value
     */
    public double getFitness() {
        return fitness;
    }

    /**
     * Sets the fitness value.
     *
     * @param fitness the fitness value
     */
    public void setFitness(double fitness) {
        this.fitness = fitness;
        this.fitnessCalculated = true;
    }

    /**
     * Checks if fitness has been calculated.
     *
     * @return true if fitness is up to date
     */
    public boolean isFitnessCalculated() {
        return fitnessCalculated;
    }

    /**
     * Invalidates the cached fitness value.
     * Should be called after any modification to assignments.
     */
    public void invalidateFitness() {
        this.fitnessCalculated = false;
        this.fitness = 0.0;
    }

    /**
     * Checks if the chromosome represents a valid solution.
     *
     * @return true if all constraints are satisfied
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * Sets the validity status.
     *
     * @param valid true if all constraints are satisfied
     */
    public void setValid(boolean valid) {
        this.isValid = valid;
    }

    /**
     * Swaps the assignments of two students.
     * Used in mutation operations.
     *
     * @param studentIndex1 first student's index
     * @param studentIndex2 second student's index
     */
    public void swapAssignments(int studentIndex1, int studentIndex2) {
        int temp = assignments[studentIndex1];
        assignments[studentIndex1] = assignments[studentIndex2];
        assignments[studentIndex2] = temp;
        invalidateFitness();
    }

    /**
     * Counts how many students are assigned to a specific project.
     *
     * @param projectId the project ID
     * @return the number of students assigned to this project
     */
    public int countStudentsInProject(int projectId) {
        int count = 0;
        for (int assignment : assignments) {
            if (assignment == projectId) {
                count++;
            }
        }
        return count;
    }

    /**
     * Gets all student indices assigned to a specific project.
     *
     * @param projectId the project ID
     * @return array of student indices assigned to this project
     */
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

    /**
     * Compares chromosomes by fitness (higher fitness = better).
     * Used for sorting populations.
     *
     * @param other the chromosome to compare to
     * @return negative if this is better, positive if other is better
     */
    @Override
    public int compareTo(Chromosome other) {
        // Higher fitness is better, so reverse the comparison
        return Double.compare(other.fitness, this.fitness);
    }

    /**
     * Creates a deep copy of this chromosome.
     *
     * @return a new chromosome with the same assignments and fitness
     */
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

    /**
     * Returns a compact string representation showing only fitness and validity.
     *
     * @return compact string
     */
    public String toShortString() {
        return String.format("Chromosome[fitness=%.2f, valid=%s]", fitness, isValid);
    }
}
