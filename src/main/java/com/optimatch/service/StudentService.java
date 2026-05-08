package com.optimatch.service;

import com.optimatch.dao.PreferenceDAO;
import com.optimatch.dao.StudentDAO;
import com.optimatch.model.Preference;
import com.optimatch.model.Student;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

// student business logic: validation, partner linking, preferences
public class StudentService {

    private final StudentDAO studentDAO;
    private final PreferenceDAO preferenceDAO;

    // wires up default DAOs
    public StudentService() {
        this.studentDAO = new StudentDAO();
        this.preferenceDAO = new PreferenceDAO();
    }

    // create a new student (rejects duplicate institutional id)
    public Student createStudent(Student student) throws ServiceException {
        validateStudent(student);

        try {
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

    // update an existing student
    public boolean updateStudent(Student student) throws ServiceException {
        validateStudent(student);

        try {
            return studentDAO.update(student);
        } catch (SQLException e) {
            throw new ServiceException("Failed to update student: " + e.getMessage(), e);
        }
    }

    // delete a student and clean up partner links and preferences
    public boolean deleteStudent(int studentId) throws ServiceException {
        try {
            // unlink the partner first
            Optional<Student> student = studentDAO.findById(studentId);
            if (student.isPresent() && student.get().hasPartner()) {
                unlinkPartners(studentId);
            }

            // RU: внешний ключ стоит ON DELETE CASCADE, но удаляем явно для предсказуемости
            preferenceDAO.deleteByStudent(studentId);

            return studentDAO.delete(studentId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to delete student: " + e.getMessage(), e);
        }
    }

    // find student by db id
    public Optional<Student> getStudentById(int id) throws ServiceException {
        try {
            return studentDAO.findById(id);
        } catch (SQLException e) {
            throw new ServiceException("Failed to get student: " + e.getMessage(), e);
        }
    }

    // load all students
    public List<Student> getAllStudents() throws ServiceException {
        try {
            return studentDAO.findAll();
        } catch (SQLException e) {
            throw new ServiceException("Failed to get students: " + e.getMessage(), e);
        }
    }

    // total student count
    public int getStudentCount() throws ServiceException {
        try {
            return studentDAO.count();
        } catch (SQLException e) {
            throw new ServiceException("Failed to count students: " + e.getMessage(), e);
        }
    }

    // link two students as partners (both directions written to db)
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

            student1.setPartnerId(studentId2);
            student2.setPartnerId(studentId1);

            studentDAO.update(student1);
            studentDAO.update(student2);

        } catch (SQLException e) {
            throw new ServiceException("Failed to link partners: " + e.getMessage(), e);
        }
    }

    // remove partner link from a student and the partner
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

    // replace all preferences for a student
    public void setPreferences(int studentId, List<Preference> preferences) throws ServiceException {
        try {
            for (Preference pref : preferences) {
                if (pref.getStudentId() != studentId) {
                    throw new ServiceException("Preference student ID does not match");
                }
                if (pref.getRank() < 1 || pref.getRank() > 5) {
                    throw new ServiceException("Preference rank must be between 1 and 5");
                }
            }

            preferenceDAO.deleteByStudent(studentId);

            if (!preferences.isEmpty()) {
                preferenceDAO.insertBatch(preferences);
            }

        } catch (SQLException e) {
            throw new ServiceException("Failed to set preferences: " + e.getMessage(), e);
        }
    }

    // load preferences for one student
    public List<Preference> getPreferences(int studentId) throws ServiceException {
        try {
            return preferenceDAO.findByStudent(studentId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to get preferences: " + e.getMessage(), e);
        }
    }

    // basic field-level validation
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
