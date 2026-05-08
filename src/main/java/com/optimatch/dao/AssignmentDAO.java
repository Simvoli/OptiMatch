package com.optimatch.dao;

import com.optimatch.model.Assignment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

// CRUD for the assignments table
public class AssignmentDAO {

    private static final String INSERT_SQL =
            "INSERT INTO assignments (run_id, student_id, project_id, preference_rank) VALUES (?, ?, ?, ?)";
    private static final String SELECT_BY_RUN_SQL =
            "SELECT id, run_id, student_id, project_id, preference_rank FROM assignments WHERE run_id = ?";

    // batch insert, generated ids written back into the list
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

    // load all assignments for a given run
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

    // row -> Assignment
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
