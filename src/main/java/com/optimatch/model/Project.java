package com.optimatch.model;

import java.util.Objects;

/**
 * Represents a project/course that students can be assigned to.
 * Each project has capacity constraints and may require a minimum GPA.
 */
public class Project {

    private int id;
    private String code;
    private String name;
    private String description;
    private int minCapacity;
    private int maxCapacity;
    private double requiredGpa;

    /**
     * Default constructor for Project.
     */
    public Project() {
        this.minCapacity = 1;
        this.requiredGpa = 0.00;
    }

    /**
     * Creates a new Project with the specified details.
     *
     * @param id          the database ID
     * @param code        the project code
     * @param name        the project name
     * @param description the project description
     * @param minCapacity the minimum number of students required
     * @param maxCapacity the maximum number of students allowed
     * @param requiredGpa the minimum GPA required for this project
     */
    public Project(int id, String code, String name, String description,
                   int minCapacity, int maxCapacity, double requiredGpa) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.minCapacity = minCapacity;
        this.maxCapacity = maxCapacity;
        this.requiredGpa = requiredGpa;
    }

    /**
     * Gets the database ID.
     *
     * @return the database ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the database ID.
     *
     * @param id the database ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the project code.
     *
     * @return the project code
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the project code.
     *
     * @param code the project code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Gets the project name.
     *
     * @return the project name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the project name.
     *
     * @param name the project name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the project description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the project description.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the minimum capacity.
     *
     * @return the minimum number of students required
     */
    public int getMinCapacity() {
        return minCapacity;
    }

    /**
     * Sets the minimum capacity.
     *
     * @param minCapacity the minimum number of students required
     */
    public void setMinCapacity(int minCapacity) {
        this.minCapacity = minCapacity;
    }

    /**
     * Gets the maximum capacity.
     *
     * @return the maximum number of students allowed
     */
    public int getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * Sets the maximum capacity.
     *
     * @param maxCapacity the maximum number of students allowed
     */
    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    /**
     * Gets the required GPA.
     *
     * @return the minimum GPA required
     */
    public double getRequiredGpa() {
        return requiredGpa;
    }

    /**
     * Sets the required GPA.
     *
     * @param requiredGpa the minimum GPA required
     */
    public void setRequiredGpa(double requiredGpa) {
        this.requiredGpa = requiredGpa;
    }

    /**
     * Checks if the given count of students is within the valid capacity range.
     *
     * @param count the number of students
     * @return true if count is between minCapacity and maxCapacity (inclusive)
     */
    public boolean isWithinCapacity(int count) {
        return count >= minCapacity && count <= maxCapacity;
    }

    /**
     * Checks if a student's GPA meets the project requirement.
     *
     * @param studentGpa the student's GPA
     * @return true if the GPA meets or exceeds the requirement
     */
    public boolean meetsGpaRequirement(double studentGpa) {
        return studentGpa >= requiredGpa;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Project project = (Project) o;
        return id == project.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", capacity=" + minCapacity + "-" + maxCapacity +
                '}';
    }
}
