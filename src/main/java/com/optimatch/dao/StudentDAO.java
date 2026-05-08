package com.optimatch.dao;

import com.optimatch.model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// CRUD for the students table
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

    // insert and assign generated id back to the entity
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

    // update by id, returns true on success
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

    // delete by db id
    public boolean delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // find by db id
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

    // find by institutional student id
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

    // load all students ordered by name
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

    // total student count
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

    // row -> Student
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
