package com.optimatch.model;

import java.util.Objects;

// final student-to-project assignment, the output of one GA run
public class Assignment {

    private int id;
    private int runId;
    private int studentId;
    private int projectId;
    private Integer preferenceRank;

    // empty assignment
    public Assignment() {
    }

    // full assignment
    public Assignment(int id, int runId, int studentId, int projectId, Integer preferenceRank) {
        this.id = id;
        this.runId = runId;
        this.studentId = studentId;
        this.projectId = projectId;
        this.preferenceRank = preferenceRank;
    }

    // assignment without db id
    public Assignment(int runId, int studentId, int projectId, Integer preferenceRank) {
        this.runId = runId;
        this.studentId = studentId;
        this.projectId = projectId;
        this.preferenceRank = preferenceRank;
    }

    // db id
    public int getId() {
        return id;
    }

    // set db id
    public void setId(int id) {
        this.id = id;
    }

    // owning run id
    public int getRunId() {
        return runId;
    }

    // set owning run id
    public void setRunId(int runId) {
        this.runId = runId;
    }

    // student id
    public int getStudentId() {
        return studentId;
    }

    // set student id
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    // assigned project id
    public int getProjectId() {
        return projectId;
    }

    // set assigned project id
    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    // rank of the assigned project in the student's preferences (null if not in list)
    public Integer getPreferenceRank() {
        return preferenceRank;
    }

    // set preference rank
    public void setPreferenceRank(Integer preferenceRank) {
        this.preferenceRank = preferenceRank;
    }

    // true if the assigned project was on the student's preference list
    public boolean wasInPreferences() {
        return preferenceRank != null;
    }

    // satisfaction score for this assignment (uses Preference weights)
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
