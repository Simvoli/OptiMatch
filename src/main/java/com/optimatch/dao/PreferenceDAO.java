package com.optimatch.dao;

import com.optimatch.model.Preference;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the {@code preferences} table.
 */
public class PreferenceDAO {

    private static final String INSERT_SQL =
            "INSERT INTO preferences (student_id, project_id, `rank`) VALUES (?, ?, ?)";
    private static final String DELETE_BY_STUDENT_SQL =
            "DELETE FROM preferences WHERE student_id = ?";
    private static final String SELECT_BY_STUDENT_SQL =
            "SELECT id, student_id, project_id, `rank` FROM preferences WHERE student_id = ? ORDER BY `rank`";
    private static final String SELECT_ALL_SQL =
            "SELECT id, student_id, project_id, `rank` FROM preferences ORDER BY student_id, `rank`";

    public void insertBatch(List<Preference> preferences) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            for (Preference preference : preferences) {
                stmt.setInt(1, preference.getStudentId());
                stmt.setInt(2, preference.getProjectId());
                stmt.setInt(3, preference.getRank());
                stmt.addBatch();
            }

            stmt.executeBatch();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                int index = 0;
                while (rs.next() && index < preferences.size()) {
                    preferences.get(index).setId(rs.getInt(1));
                    index++;
                }
            }
        }
    }

    public int deleteByStudent(int studentId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_BY_STUDENT_SQL)) {
            stmt.setInt(1, studentId);
            return stmt.executeUpdate();
        }
    }

    public List<Preference> findByStudent(int studentId) throws SQLException {
        List<Preference> preferences = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_STUDENT_SQL)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    preferences.add(mapResultSetToPreference(rs));
                }
            }
        }
        return preferences;
    }

    public List<Preference> findAll() throws SQLException {
        List<Preference> preferences = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                preferences.add(mapResultSetToPreference(rs));
            }
        }
        return preferences;
    }

    private Preference mapResultSetToPreference(ResultSet rs) throws SQLException {
        Preference preference = new Preference();
        preference.setId(rs.getInt("id"));
        preference.setStudentId(rs.getInt("student_id"));
        preference.setProjectId(rs.getInt("project_id"));
        preference.setRank(rs.getInt("rank"));
        return preference;
    }
}
