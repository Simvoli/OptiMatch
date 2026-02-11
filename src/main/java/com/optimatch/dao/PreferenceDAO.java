package com.optimatch.dao;

import com.optimatch.model.Preference;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Preference entities.
 * Provides CRUD operations for the preferences table.
 */
public class PreferenceDAO {

    private static final String INSERT_SQL =
            "INSERT INTO preferences (student_id, project_id, `rank`) VALUES (?, ?, ?)";
    private static final String UPDATE_SQL =
            "UPDATE preferences SET student_id = ?, project_id = ?, `rank` = ? WHERE id = ?";
    private static final String DELETE_SQL =
            "DELETE FROM preferences WHERE id = ?";
    private static final String DELETE_BY_STUDENT_SQL =
            "DELETE FROM preferences WHERE student_id = ?";
    private static final String SELECT_BY_ID_SQL =
            "SELECT id, student_id, project_id, `rank` FROM preferences WHERE id = ?";
    private static final String SELECT_BY_STUDENT_SQL =
            "SELECT id, student_id, project_id, `rank` FROM preferences WHERE student_id = ? ORDER BY `rank`";
    private static final String SELECT_BY_STUDENT_AND_PROJECT_SQL =
            "SELECT id, student_id, project_id, `rank` FROM preferences WHERE student_id = ? AND project_id = ?";
    private static final String SELECT_ALL_SQL =
            "SELECT id, student_id, project_id, `rank` FROM preferences ORDER BY student_id, `rank`";
    private static final String COUNT_SQL =
            "SELECT COUNT(*) FROM preferences";

    /**
     * Inserts a new preference into the database.
     *
     * @param preference the preference to insert
     * @return the generated ID for the new preference
     * @throws SQLException if a database error occurs
     */
    public int insert(Preference preference) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, preference.getStudentId());
            stmt.setInt(2, preference.getProjectId());
            stmt.setInt(3, preference.getRank());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    preference.setId(id);
                    return id;
                }
            }
            throw new SQLException("Failed to retrieve generated ID");
        }
    }

    /**
     * Inserts multiple preferences for a student in a batch.
     *
     * @param preferences the list of preferences to insert
     * @throws SQLException if a database error occurs
     */
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

    /**
     * Updates an existing preference in the database.
     *
     * @param preference the preference to update
     * @return true if the update was successful
     * @throws SQLException if a database error occurs
     */
    public boolean update(Preference preference) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            stmt.setInt(1, preference.getStudentId());
            stmt.setInt(2, preference.getProjectId());
            stmt.setInt(3, preference.getRank());
            stmt.setInt(4, preference.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a preference from the database.
     *
     * @param id the ID of the preference to delete
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
     * Deletes all preferences for a student.
     *
     * @param studentId the database ID of the student
     * @return the number of preferences deleted
     * @throws SQLException if a database error occurs
     */
    public int deleteByStudent(int studentId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_BY_STUDENT_SQL)) {

            stmt.setInt(1, studentId);
            return stmt.executeUpdate();
        }
    }

    /**
     * Finds a preference by its database ID.
     *
     * @param id the database ID
     * @return an Optional containing the preference if found
     * @throws SQLException if a database error occurs
     */
    public Optional<Preference> findById(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPreference(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds all preferences for a student, ordered by rank.
     *
     * @param studentId the database ID of the student
     * @return a list of preferences for the student
     * @throws SQLException if a database error occurs
     */
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

    /**
     * Finds a preference for a specific student-project combination.
     *
     * @param studentId the database ID of the student
     * @param projectId the database ID of the project
     * @return an Optional containing the preference if found
     * @throws SQLException if a database error occurs
     */
    public Optional<Preference> findByStudentAndProject(int studentId, int projectId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_STUDENT_AND_PROJECT_SQL)) {

            stmt.setInt(1, studentId);
            stmt.setInt(2, projectId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPreference(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves all preferences from the database.
     *
     * @return a list of all preferences
     * @throws SQLException if a database error occurs
     */
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

    /**
     * Counts the total number of preferences in the database.
     *
     * @return the count of preferences
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
     * Maps a ResultSet row to a Preference object.
     *
     * @param rs the ResultSet positioned at a valid row
     * @return a Preference object
     * @throws SQLException if a database error occurs
     */
    private Preference mapResultSetToPreference(ResultSet rs) throws SQLException {
        Preference preference = new Preference();
        preference.setId(rs.getInt("id"));
        preference.setStudentId(rs.getInt("student_id"));
        preference.setProjectId(rs.getInt("project_id"));
        preference.setRank(rs.getInt("rank"));
        return preference;
    }
}
