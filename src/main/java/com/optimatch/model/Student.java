package com.optimatch.model;

import java.util.Objects;

/**
 * Represents a student in the OptiMatch system.
 * Each student has preferences for projects and may have a partner.
 */
public class Student {

    private int id;
    private String studentId;
    private String name;
    private String email;
    private double gpa;
    private Integer partnerId;

    /**
     * Default constructor for Student.
     */
    public Student() {
    }

    /**
     * Creates a new Student with the specified details.
     *
     * @param id        the database ID
     * @param studentId the student's institutional ID
     * @param name      the student's full name
     * @param email     the student's email address
     * @param gpa       the student's GPA (0.00 to 4.00)
     * @param partnerId the ID of the student's partner, or null if none
     */
    public Student(int id, String studentId, String name, String email, double gpa, Integer partnerId) {
        this.id = id;
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.gpa = gpa;
        this.partnerId = partnerId;
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
     * Gets the student's institutional ID.
     *
     * @return the student ID
     */
    public String getStudentId() {
        return studentId;
    }

    /**
     * Sets the student's institutional ID.
     *
     * @param studentId the student ID
     */
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    /**
     * Gets the student's name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the student's name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the student's email address.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the student's email address.
     *
     * @param email the email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the student's GPA.
     *
     * @return the GPA
     */
    public double getGpa() {
        return gpa;
    }

    /**
     * Sets the student's GPA.
     *
     * @param gpa the GPA (should be between 0.00 and 4.00)
     */
    public void setGpa(double gpa) {
        this.gpa = gpa;
    }

    /**
     * Gets the partner's database ID.
     *
     * @return the partner ID, or null if no partner
     */
    public Integer getPartnerId() {
        return partnerId;
    }

    /**
     * Sets the partner's database ID.
     *
     * @param partnerId the partner ID, or null if no partner
     */
    public void setPartnerId(Integer partnerId) {
        this.partnerId = partnerId;
    }

    /**
     * Checks if the student has a partner.
     *
     * @return true if the student has a partner
     */
    public boolean hasPartner() {
        return partnerId != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Student student = (Student) o;
        return id == student.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", studentId='" + studentId + '\'' +
                ", name='" + name + '\'' +
                ", gpa=" + gpa +
                '}';
    }
}
