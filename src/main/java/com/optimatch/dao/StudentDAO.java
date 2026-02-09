package com.optimatch.dao;

import com.optimatch.model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Student entities.
 * Provides CRUD operations for the students table.
 */
public class StudentDAO {

    private static final String INSERT_SQL =
            "INSERT INTO students (student_id, name, email, gpa, partner_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL =
            "UPDATE students SET student_id = ?, name = ?, email = ?, gpa = ?, partner_id = ? WHERE id = ?";
    private static final String DELETE_SQL =
            "DELETE FROM students WHERE id = ?";
    private static final String SELECT_BY_ID_SQL =
            "SELECT id, student_id, name, email, gpa, partner_id FROM students WHERE id = ?";
    private static final String SELECT_BY_STUDENT_ID_SQL =
            "SELECT id, student_id, name, email, gpa, partner_id FROM students WHERE student_id = ?";
    private static final String SELECT_ALL_SQL =
            "SELECT id, student_id, name, email, gpa, partner_id FROM students ORDER BY name";
    private static final String COUNT_SQL =
            "SELECT COUNT(*) FROM students";

    /**
     * Inserts a new student into the database.
     *
     * @param student the student to insert
     * @return the generated ID for the new student
     * @throws SQLException if a database error occurs
     */
    public int insert(Student student) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, student.getStudentId());
            stmt.setString(2, student.getName());
            stmt.setString(3, student.getEmail());
            stmt.setDouble(4, student.getGpa());
            if (student.getPartnerId() != null) {
                stmt.setInt(5, student.getPartnerId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    student.setId(id);
                    return id;
                }
            }
            throw new SQLException("Failed to retrieve generated ID");
        }
    }

    /**
     * Updates an existing student in the database.
     *
     * @param student the student to update
     * @return true if the update was successful
     * @throws SQLException if a database error occurs
     */
    public boolean update(Student student) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            stmt.setString(1, student.getStudentId());
            stmt.setString(2, student.getName());
            stmt.setString(3, student.getEmail());
            stmt.setDouble(4, student.getGpa());
            if (student.getPartnerId() != null) {
                stmt.setInt(5, student.getPartnerId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            stmt.setInt(6, student.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a student from the database.
     *
     * @param id the ID of the student to delete
     * @return true if the deletion was successful
     * @throws SQLException if a database error occurs
     */
    public boolean delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Finds a student by their database ID.
     *
     * @param id the database ID
     * @return an Optional containing the student if found
     * @throws SQLException if a database error occurs
     */
    public Optional<Student> findById(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToStudent(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds a student by their institutional student ID.
     *
     * @param studentId the institutional student ID
     * @return an Optional containing the student if found
     * @throws SQLException if a database error occurs
     */
    public Optional<Student> findByStudentId(String studentId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_STUDENT_ID_SQL)) {

            stmt.setString(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToStudent(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves all students from the database.
     *
     * @return a list of all students
     * @throws SQLException if a database error occurs
     */
    public List<Student> findAll() throws SQLException {
        List<Student> students = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        }
        return students;
    }

    /**
     * Counts the total number of students in the database.
     *
     * @return the count of students
     * @throws SQLException if a database error occurs
     */
    public int count() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_SQL);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Maps a ResultSet row to a Student object.
     *
     * @param rs the ResultSet positioned at a valid row
     * @return a Student object
     * @throws SQLException if a database error occurs
     */
    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setId(rs.getInt("id"));
        student.setStudentId(rs.getString("student_id"));
        student.setName(rs.getString("name"));
        student.setEmail(rs.getString("email"));
        student.setGpa(rs.getDouble("gpa"));

        int partnerId = rs.getInt("partner_id");
        if (!rs.wasNull()) {
            student.setPartnerId(partnerId);
        }

        return student;
    }
}
