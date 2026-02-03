package com.optimatch.model;

import java.util.Objects;

/**
 * Represents a final assignment of a student to a project.
 * Assignments are the result of running the genetic algorithm.
 */
public class Assignment {

    private int id;
    private int runId;
    private int studentId;
    private int projectId;
    private Integer preferenceRank;

    /**
     * Default constructor for Assignment.
     */
    public Assignment() {
    }

    /**
     * Creates a new Assignment with the specified details.
     *
     * @param id             the database ID
     * @param runId          the algorithm run ID
     * @param studentId      the student's database ID
     * @param projectId      the project's database ID
     * @param preferenceRank the rank of this project in student's preferences, or null if not in preferences
     */
    public Assignment(int id, int runId, int studentId, int projectId, Integer preferenceRank) {
        this.id = id;
        this.runId = runId;
        this.studentId = studentId;
        this.projectId = projectId;
        this.preferenceRank = preferenceRank;
    }

    /**
     * Creates a new Assignment without a database ID.
     *
     * @param runId          the algorithm run ID
     * @param studentId      the student's database ID
     * @param projectId      the project's database ID
     * @param preferenceRank the rank of this project in student's preferences, or null if not in preferences
     */
    public Assignment(int runId, int studentId, int projectId, Integer preferenceRank) {
        this.runId = runId;
        this.studentId = studentId;
        this.projectId = projectId;
        this.preferenceRank = preferenceRank;
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
     * Gets the algorithm run ID.
     *
     * @return the run ID
     */
    public int getRunId() {
        return runId;
    }

    /**
     * Sets the algorithm run ID.
     *
     * @param runId the run ID
     */
    public void setRunId(int runId) {
        this.runId = runId;
    }

    /**
     * Gets the student's database ID.
     *
     * @return the student ID
     */
    public int getStudentId() {
        return studentId;
    }

    /**
     * Sets the student's database ID.
     *
     * @param studentId the student ID
     */
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    /**
     * Gets the project's database ID.
     *
     * @return the project ID
     */
    public int getProjectId() {
        return projectId;
    }

    /**
     * Sets the project's database ID.
     *
     * @param projectId the project ID
     */
    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    /**
     * Gets the preference rank.
     *
     * @return the rank if this project was in student's preferences, null otherwise
     */
    public Integer getPreferenceRank() {
        return preferenceRank;
    }

    /**
     * Sets the preference rank.
     *
     * @param preferenceRank the rank, or null if not in preferences
     */
    public void setPreferenceRank(Integer preferenceRank) {
        this.preferenceRank = preferenceRank;
    }

    /**
     * Checks if this assignment was in the student's preferences.
     *
     * @return true if the assigned project was in the student's preference list
     */
    public boolean wasInPreferences() {
        return preferenceRank != null;
    }

    /**
     * Gets the satisfaction score for this assignment.
     *
     * @return the weight value based on preference rank
     */
    public int getSatisfactionScore() {
        if (preferenceRank == null) {
            return Preference.WEIGHT_NO_PREFERENCE;
        }
        return Preference.getWeightForRank(preferenceRank);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Assignment that = (Assignment) o;
        return runId == that.runId && studentId == that.studentId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(runId, studentId);
    }

    @Override
    public String toString() {
        return "Assignment{" +
                "runId=" + runId +
                ", studentId=" + studentId +
                ", projectId=" + projectId +
                ", preferenceRank=" + preferenceRank +
                '}';
    }
}
