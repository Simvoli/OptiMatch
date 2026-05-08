package com.optimatch.model;

import java.util.Objects;

// student record: id, name, email, gpa, optional partner
public class Student {

    private int id;
    private String studentId;
    private String name;
    private String email;
    private double gpa;
    private Integer partnerId;

    // empty student, fields filled by setters
    public Student() {
    }

    // full student
    public Student(int id, String studentId, String name, String email, double gpa, Integer partnerId) {
        this.id = id;
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        setGpa(gpa);
        this.partnerId = partnerId;
    }

    // db id
    public int getId() {
        return id;
    }

    // set db id
    public void setId(int id) {
        this.id = id;
    }

    // institutional student id
    public String getStudentId() {
        return studentId;
    }

    // set institutional student id
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    // full name
    public String getName() {
        return name;
    }

    // set full name
    public void setName(String name) {
        this.name = name;
    }

    // email or null
    public String getEmail() {
        return email;
    }

    // set email
    public void setEmail(String email) {
        this.email = email;
    }

    // current gpa
    public double getGpa() {
        return gpa;
    }

    // set gpa, must be in [0, 4]
    public void setGpa(double gpa) {
        if (gpa < 0.0 || gpa > 4.0) {
            throw new IllegalArgumentException("GPA must be between 0.0 and 4.0: " + gpa);
        }
        this.gpa = gpa;
    }

    // partner id or null
    public Integer getPartnerId() {
        return partnerId;
    }

    // set partner id (null clears it)
    public void setPartnerId(Integer partnerId) {
        this.partnerId = partnerId;
    }

    // true if this student has a partner
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
