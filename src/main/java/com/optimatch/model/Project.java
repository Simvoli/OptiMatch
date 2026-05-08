package com.optimatch.model;

import java.util.Objects;

// project record: capacity range and optional gpa requirement
public class Project {

    private int id;
    private String code;
    private String name;
    private String description;
    private int minCapacity;
    private int maxCapacity;
    private double requiredGpa;

    // empty project with sane defaults
    public Project() {
        this.minCapacity = 1;
        this.requiredGpa = 0.00;
    }

    // full project
    public Project(int id, String code, String name, String description,
                   int minCapacity, int maxCapacity, double requiredGpa) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        setMinCapacity(minCapacity);
        setMaxCapacity(maxCapacity);
        setRequiredGpa(requiredGpa);
    }

    // db id
    public int getId() {
        return id;
    }

    // set db id
    public void setId(int id) {
        this.id = id;
    }

    // short project code
    public String getCode() {
        return code;
    }

    // set short project code
    public void setCode(String code) {
        this.code = code;
    }

    // display name
    public String getName() {
        return name;
    }

    // set display name
    public void setName(String name) {
        this.name = name;
    }

    // long description (may be null)
    public String getDescription() {
        return description;
    }

    // set description
    public void setDescription(String description) {
        this.description = description;
    }

    // minimum students
    public int getMinCapacity() {
        return minCapacity;
    }

    // set minimum students, must be >= 0
    public void setMinCapacity(int minCapacity) {
        if (minCapacity < 0) {
            throw new IllegalArgumentException("Minimum capacity cannot be negative: " + minCapacity);
        }
        this.minCapacity = minCapacity;
    }

    // maximum students
    public int getMaxCapacity() {
        return maxCapacity;
    }

    // set maximum students, must be >= 1
    public void setMaxCapacity(int maxCapacity) {
        if (maxCapacity < 1) {
            throw new IllegalArgumentException("Maximum capacity must be at least 1: " + maxCapacity);
        }
        this.maxCapacity = maxCapacity;
    }

    // required gpa (0 means no requirement)
    public double getRequiredGpa() {
        return requiredGpa;
    }

    // set required gpa, must be in [0, 4]
    public void setRequiredGpa(double requiredGpa) {
        if (requiredGpa < 0.0 || requiredGpa > 4.0) {
            throw new IllegalArgumentException("Required GPA must be between 0.0 and 4.0: " + requiredGpa);
        }
        this.requiredGpa = requiredGpa;
    }

    // is the count between min and max inclusive
    public boolean isWithinCapacity(int count) {
        return count >= minCapacity && count <= maxCapacity;
    }

    // does the student's gpa meet the requirement
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
