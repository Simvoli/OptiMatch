package com.optimatch.service;

import com.optimatch.dao.PreferenceDAO;
import com.optimatch.dao.StudentDAO;
import com.optimatch.model.Preference;
import com.optimatch.model.Student;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for student management.
 * Handles business logic for student operations including partner linking and validation.
 */
public class StudentService {

    private final StudentDAO studentDAO;
    private final PreferenceDAO preferenceDAO;

    /**
     * Creates a StudentService with default DAOs.
     */
    public StudentService() {
        this.studentDAO = new StudentDAO();
        this.preferenceDAO = new PreferenceDAO();
    }

    /**
     * Creates a StudentService with the specified DAOs.
     *
     * @param studentDAO    the student DAO
     * @param preferenceDAO the preference DAO
     */
    public StudentService(StudentDAO studentDAO, PreferenceDAO preferenceDAO) {
        this.studentDAO = studentDAO;
        this.preferenceDAO = preferenceDAO;
    }

    /**
     * Creates a new student.
     *
     * @param student the student to create
     * @return the created student with generated ID
     * @throws ServiceException if validation fails or database error occurs
     */
    public Student createStudent(Student student) throws ServiceException {
        validateStudent(student);

        try {
            // Check for duplicate student ID
            Optional<Student> existing = studentDAO.findByStudentId(student.getStudentId());
            if (existing.isPresent()) {
                throw new ServiceException("Student with ID " + student.getStudentId() + " already exists");
            }

            studentDAO.insert(student);
            return student;
        } catch (SQLException e) {
            throw new ServiceException("Failed to create student: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing student.
     *
     * @param student the student to update
     * @return true if update was successful
     * @throws ServiceException if validation fails or database error occurs
     */
    public boolean updateStudent(Student student) throws ServiceException {
        validateStudent(student);

        try {
            return studentDAO.update(student);
        } catch (SQLException e) {
            throw new ServiceException("Failed to update student: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a student and their preferences.
     *
     * @param studentId the database ID of the student to delete
     * @return true if deletion was successful
     * @throws ServiceException if database error occurs
     */
    public boolean deleteStudent(int studentId) throws ServiceException {
        try {
            // Remove partner references first
            Optional<Student> student = studentDAO.findById(studentId);
            if (student.isPresent() && student.get().hasPartner()) {
                unlinkPartners(studentId);
            }

            // Preferences are deleted by CASCADE, but we can do it explicitly
            preferenceDAO.deleteByStudent(studentId);

            return studentDAO.delete(studentId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to delete student: " + e.getMessage(), e);
        }
    }

    /**
     * Gets a student by database ID.
     *
     * @param id the database ID
     * @return the student if found
     * @throws ServiceException if database error occurs
     */
    public Optional<Student> getStudentById(int id) throws ServiceException {
        try {
            return studentDAO.findById(id);
        } catch (SQLException e) {
            throw new ServiceException("Failed to get student: " + e.getMessage(), e);
        }
    }

    /**
     * Gets a student by institutional student ID.
     *
     * @param studentId the institutional student ID
     * @return the student if found
     * @throws ServiceException if database error occurs
     */
    public Optional<Student> getStudentByStudentId(String studentId) throws ServiceException {
        try {
            return studentDAO.findByStudentId(studentId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to get student: " + e.getMessage(), e);
        }
    }

    /**
     * Gets all students.
     *
     * @return list of all students
     * @throws ServiceException if database error occurs
     */
    public List<Student> getAllStudents() throws ServiceException {
        try {
            return studentDAO.findAll();
        } catch (SQLException e) {
            throw new ServiceException("Failed to get students: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the total number of students.
     *
     * @return the count of students
     * @throws ServiceException if database error occurs
     */
    public int getStudentCount() throws ServiceException {
        try {
            return studentDAO.count();
        } catch (SQLException e) {
            throw new ServiceException("Failed to count students: " + e.getMessage(), e);
        }
    }

    /**
     * Links two students as partners.
     * Partners must be assigned to the same project.
     *
     * @param studentId1 the first student's database ID
     * @param studentId2 the second student's database ID
     * @throws ServiceException if students not found or already have partners
     */
    public void linkPartners(int studentId1, int studentId2) throws ServiceException {
        if (studentId1 == studentId2) {
            throw new ServiceException("A student cannot be their own partner");
        }

        try {
            Optional<Student> student1Opt = studentDAO.findById(studentId1);
            Optional<Student> student2Opt = studentDAO.findById(studentId2);

            if (student1Opt.isEmpty()) {
                throw new ServiceException("Student with ID " + studentId1 + " not found");
            }
            if (student2Opt.isEmpty()) {
                throw new ServiceException("Student with ID " + studentId2 + " not found");
            }

            Student student1 = student1Opt.get();
            Student student2 = student2Opt.get();

            if (student1.hasPartner()) {
                throw new ServiceException("Student " + student1.getName() + " already has a partner");
            }
            if (student2.hasPartner()) {
                throw new ServiceException("Student " + student2.getName() + " already has a partner");
            }

            // Link both directions
            student1.setPartnerId(studentId2);
            student2.setPartnerId(studentId1);

            studentDAO.update(student1);
            studentDAO.update(student2);

        } catch (SQLException e) {
            throw new ServiceException("Failed to link partners: " + e.getMessage(), e);
        }
    }

    /**
     * Unlinks a student from their partner.
     *
     * @param studentId the database ID of either partner
     * @throws ServiceException if student not found or has no partner
     */
    public void unlinkPartners(int studentId) throws ServiceException {
        try {
            Optional<Student> studentOpt = studentDAO.findById(studentId);
            if (studentOpt.isEmpty()) {
                throw new ServiceException("Student with ID " + studentId + " not found");
            }

            Student student = studentOpt.get();
            if (!student.hasPartner()) {
                throw new ServiceException("Student " + student.getName() + " has no partner");
            }

            int partnerId = student.getPartnerId();
            Optional<Student> partnerOpt = studentDAO.findById(partnerId);

            // Unlink both
            student.setPartnerId(null);
            studentDAO.update(student);

            if (partnerOpt.isPresent()) {
                Student partner = partnerOpt.get();
                partner.setPartnerId(null);
                studentDAO.update(partner);
            }

        } catch (SQLException e) {
            throw new ServiceException("Failed to unlink partners: " + e.getMessage(), e);
        }
    }

    /**
     * Sets the preferences for a student.
     * Replaces any existing preferences.
     *
     * @param studentId   the student's database ID
     * @param preferences list of preferences (must have correct studentId set)
     * @throws ServiceException if database error occurs
     */
    public void setPreferences(int studentId, List<Preference> preferences) throws ServiceException {
        try {
            // Validate preferences
            for (Preference pref : preferences) {
                if (pref.getStudentId() != studentId) {
                    throw new ServiceException("Preference student ID does not match");
                }
                if (pref.getRank() < 1 || pref.getRank() > 5) {
                    throw new ServiceException("Preference rank must be between 1 and 5");
                }
            }

            // Delete existing preferences
            preferenceDAO.deleteByStudent(studentId);

            // Insert new preferences
            if (!preferences.isEmpty()) {
                preferenceDAO.insertBatch(preferences);
            }

        } catch (SQLException e) {
            throw new ServiceException("Failed to set preferences: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the preferences for a student.
     *
     * @param studentId the student's database ID
     * @return list of preferences ordered by rank
     * @throws ServiceException if database error occurs
     */
    public List<Preference> getPreferences(int studentId) throws ServiceException {
        try {
            return preferenceDAO.findByStudent(studentId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to get preferences: " + e.getMessage(), e);
        }
    }

    /**
     * Validates a student object.
     *
     * @param student the student to validate
     * @throws ServiceException if validation fails
     */
    private void validateStudent(Student student) throws ServiceException {
        if (student == null) {
            throw new ServiceException("Student cannot be null");
        }
        if (student.getStudentId() == null || student.getStudentId().trim().isEmpty()) {
            throw new ServiceException("Student ID is required");
        }
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            throw new ServiceException("Student name is required");
        }
        if (student.getGpa() < 0 || student.getGpa() > 4.0) {
            throw new ServiceException("GPA must be between 0.00 and 4.00");
        }
    }
}
