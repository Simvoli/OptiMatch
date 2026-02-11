package com.optimatch.dao;

import com.optimatch.model.Assignment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Assignment entities.
 * Provides CRUD operations for the assignments table.
 */
public class AssignmentDAO {

    private static final String INSERT_SQL =
            "INSERT INTO assignments (run_id, student_id, project_id, preference_rank) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_SQL =
            "UPDATE assignments SET run_id = ?, student_id = ?, project_id = ?, preference_rank = ? WHERE id = ?";
    private static final String DELETE_SQL =
            "DELETE FROM assignments WHERE id = ?";
    private static final String DELETE_BY_RUN_SQL =
            "DELETE FROM assignments WHERE run_id = ?";
    private static final String SELECT_BY_ID_SQL =
            "SELECT id, run_id, student_id, project_id, preference_rank FROM assignments WHERE id = ?";
    private static final String SELECT_BY_RUN_SQL =
            "SELECT id, run_id, student_id, project_id, preference_rank FROM assignments WHERE run_id = ?";
    private static final String SELECT_BY_RUN_AND_STUDENT_SQL =
            "SELECT id, run_id, student_id, project_id, preference_rank FROM assignments " +
                    "WHERE run_id = ? AND student_id = ?";
    private static final String SELECT_BY_RUN_AND_PROJECT_SQL =
            "SELECT id, run_id, student_id, project_id, preference_rank FROM assignments " +
                    "WHERE run_id = ? AND project_id = ?";
    private static final String SELECT_ALL_SQL =
            "SELECT id, run_id, student_id, project_id, preference_rank FROM assignments";
    private static final String COUNT_SQL =
            "SELECT COUNT(*) FROM assignments";
    private static final String COUNT_BY_RUN_SQL =
            "SELECT COUNT(*) FROM assignments WHERE run_id = ?";

    /**
     * Inserts a new assignment into the database.
     *
     * @param assignment the assignment to insert
     * @return the generated ID for the new assignment
     * @throws SQLException if a database error occurs
     */
    public int insert(Assignment assignment) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, assignment.getRunId());
            stmt.setInt(2, assignment.getStudentId());
            stmt.setInt(3, assignment.getProjectId());
            if (assignment.getPreferenceRank() != null) {
                stmt.setInt(4, assignment.getPreferenceRank());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    assignment.setId(id);
                    return id;
                }
            }
            throw new SQLException("Failed to retrieve generated ID");
        }
    }

    /**
     * Inserts multiple assignments in a batch.
     *
     * @param assignments the list of assignments to insert
     * @throws SQLException if a database error occurs
     */
    public void insertBatch(List<Assignment> assignments) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            for (Assignment assignment : assignments) {
                stmt.setInt(1, assignment.getRunId());
                stmt.setInt(2, assignment.getStudentId());
                stmt.setInt(3, assignment.getProjectId());
                if (assignment.getPreferenceRank() != null) {
                    stmt.setInt(4, assignment.getPreferenceRank());
                } else {
                    stmt.setNull(4, Types.INTEGER);
                }
                stmt.addBatch();
            }

            stmt.executeBatch();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                int index = 0;
                while (rs.next() && index < assignments.size()) {
                    assignments.get(index).setId(rs.getInt(1));
                    index++;
                }
            }
        }
    }

    /**
     * Updates an existing assignment in the database.
     *
     * @param assignment the assignment to update
     * @return true if the update was successful
     * @throws SQLException if a database error occurs
     */
    public boolean update(Assignment assignment) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            stmt.setInt(1, assignment.getRunId());
            stmt.setInt(2, assignment.getStudentId());
            stmt.setInt(3, assignment.getProjectId());
            if (assignment.getPreferenceRank() != null) {
                stmt.setInt(4, assignment.getPreferenceRank());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            stmt.setInt(5, assignment.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes an assignment from the database.
     *
     * @param id the ID of the assignment to delete
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
     * Deletes all assignments for a specific algorithm run.
     *
     * @param runId the ID of the algorithm run
     * @return the number of assignments deleted
     * @throws SQLException if a database error occurs
     */
    public int deleteByRun(int runId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_BY_RUN_SQL)) {

            stmt.setInt(1, runId);
            return stmt.executeUpdate();
        }
    }

    /**
     * Finds an assignment by its database ID.
     *
     * @param id the database ID
     * @return an Optional containing the assignment if found
     * @throws SQLException if a database error occurs
     */
    public Optional<Assignment> findById(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAssignment(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds all assignments for a specific algorithm run.
     *
     * @param runId the ID of the algorithm run
     * @return a list of assignments for the run
     * @throws SQLException if a database error occurs
     */
    public List<Assignment> findByRun(int runId) throws SQLException {
        List<Assignment> assignments = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_RUN_SQL)) {

            stmt.setInt(1, runId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    assignments.add(mapResultSetToAssignment(rs));
                }
            }
        }
        return assignments;
    }

    /**
     * Finds the assignment for a specific student in a specific run.
     *
     * @param runId     the ID of the algorithm run
     * @param studentId the ID of the student
     * @return an Optional containing the assignment if found
     * @throws SQLException if a database error occurs
     */
    public Optional<Assignment> findByRunAndStudent(int runId, int studentId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_RUN_AND_STUDENT_SQL)) {

            stmt.setInt(1, runId);
            stmt.setInt(2, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAssignment(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds all assignments for a specific project in a specific run.
     *
     * @param runId     the ID of the algorithm run
     * @param projectId the ID of the project
     * @return a list of assignments for the project
     * @throws SQLException if a database error occurs
     */
    public List<Assignment> findByRunAndProject(int runId, int projectId) throws SQLException {
        List<Assignment> assignments = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_RUN_AND_PROJECT_SQL)) {

            stmt.setInt(1, runId);
            stmt.setInt(2, projectId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    assignments.add(mapResultSetToAssignment(rs));
                }
            }
        }
        return assignments;
    }

    /**
     * Retrieves all assignments from the database.
     *
     * @return a list of all assignments
     * @throws SQLException if a database error occurs
     */
    public List<Assignment> findAll() throws SQLException {
        List<Assignment> assignments = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                assignments.add(mapResultSetToAssignment(rs));
            }
        }
        return assignments;
    }

    /**
     * Counts the total number of assignments in the database.
     *
     * @return the count of assignments
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
     * Counts the number of assignments for a specific algorithm run.
     *
     * @param runId the ID of the algorithm run
     * @return the count of assignments for the run
     * @throws SQLException if a database error occurs
     */
    public int countByRun(int runId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_BY_RUN_SQL)) {

            stmt.setInt(1, runId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Maps a ResultSet row to an Assignment object.
     *
     * @param rs the ResultSet positioned at a valid row
     * @return an Assignment object
     * @throws SQLException if a database error occurs
     */
    private Assignment mapResultSetToAssignment(ResultSet rs) throws SQLException {
        Assignment assignment = new Assignment();
        assignment.setId(rs.getInt("id"));
        assignment.setRunId(rs.getInt("run_id"));
        assignment.setStudentId(rs.getInt("student_id"));
        assignment.setProjectId(rs.getInt("project_id"));

        int preferenceRank = rs.getInt("preference_rank");
        if (!rs.wasNull()) {
            assignment.setPreferenceRank(preferenceRank);
        }

        return assignment;
    }
}
