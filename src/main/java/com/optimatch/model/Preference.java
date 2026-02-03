package com.optimatch.model;

import java.util.Objects;

/**
 * Represents a student's preference for a project.
 * Each preference has a rank (1 = first choice, 2 = second choice, etc.)
 */
public class Preference {

    /**
     * Weight constants for preference ranks used in fitness calculation.
     */
    public static final int WEIGHT_FIRST_CHOICE = 100;
    public static final int WEIGHT_SECOND_CHOICE = 80;
    public static final int WEIGHT_THIRD_CHOICE = 60;
    public static final int WEIGHT_FOURTH_CHOICE = 40;
    public static final int WEIGHT_FIFTH_CHOICE = 20;
    public static final int WEIGHT_NO_PREFERENCE = 0;

    private int id;
    private int studentId;
    private int projectId;
    private int rank;

    /**
     * Default constructor for Preference.
     */
    public Preference() {
    }

    /**
     * Creates a new Preference with the specified details.
     *
     * @param id        the database ID
     * @param studentId the student's database ID
     * @param projectId the project's database ID
     * @param rank      the preference rank (1 = first choice)
     */
    public Preference(int id, int studentId, int projectId, int rank) {
        this.id = id;
        this.studentId = studentId;
        this.projectId = projectId;
        this.rank = rank;
    }

    /**
     * Creates a new Preference without a database ID.
     *
     * @param studentId the student's database ID
     * @param projectId the project's database ID
     * @param rank      the preference rank (1 = first choice)
     */
    public Preference(int studentId, int projectId, int rank) {
        this.studentId = studentId;
        this.projectId = projectId;
        this.rank = rank;
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
     * @return the rank (1 = first choice)
     */
    public int getRank() {
        return rank;
    }

    /**
     * Sets the preference rank.
     *
     * @param rank the rank (1 = first choice)
     */
    public void setRank(int rank) {
        this.rank = rank;
    }

    /**
     * Gets the weight for this preference based on its rank.
     * Used in fitness calculation.
     *
     * @return the weight value for this preference rank
     */
    public int getWeight() {
        return getWeightForRank(this.rank);
    }

    /**
     * Gets the weight for a given preference rank.
     *
     * @param rank the preference rank
     * @return the weight value
     */
    public static int getWeightForRank(int rank) {
        switch (rank) {
            case 1:
                return WEIGHT_FIRST_CHOICE;
            case 2:
                return WEIGHT_SECOND_CHOICE;
            case 3:
                return WEIGHT_THIRD_CHOICE;
            case 4:
                return WEIGHT_FOURTH_CHOICE;
            case 5:
                return WEIGHT_FIFTH_CHOICE;
            default:
                return WEIGHT_NO_PREFERENCE;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Preference that = (Preference) o;
        return studentId == that.studentId && projectId == that.projectId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, projectId);
    }

    @Override
    public String toString() {
        return "Preference{" +
                "studentId=" + studentId +
                ", projectId=" + projectId +
                ", rank=" + rank +
                '}';
    }
}
