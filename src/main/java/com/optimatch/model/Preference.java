package com.optimatch.model;

import java.util.Objects;

// one (student, project, rank) tuple, where rank 1 = top choice
public class Preference {

    // weight per rank used by the fitness function
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

    // empty preference
    public Preference() {
    }

    // preference loaded from db
    public Preference(int id, int studentId, int projectId, int rank) {
        this.id = id;
        this.studentId = studentId;
        this.projectId = projectId;
        this.rank = rank;
    }

    // new preference, no db id yet
    public Preference(int studentId, int projectId, int rank) {
        this.studentId = studentId;
        this.projectId = projectId;
        this.rank = rank;
    }

    // db id
    public int getId() {
        return id;
    }

    // set db id
    public void setId(int id) {
        this.id = id;
    }

    // student id
    public int getStudentId() {
        return studentId;
    }

    // set student id
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    // project id
    public int getProjectId() {
        return projectId;
    }

    // set project id
    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    // rank (1 is best)
    public int getRank() {
        return rank;
    }

    // set rank
    public void setRank(int rank) {
        this.rank = rank;
    }

    // weight of this preference
    public int getWeight() {
        return getWeightForRank(this.rank);
    }

    // weight for any rank
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
