package com.optimatch.dao;

import com.optimatch.model.GenerationStats;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for GenerationStats entities.
 * Provides CRUD operations for the generation_stats table.
 */
public class GenerationStatsDAO {

    private static final String INSERT_SQL =
            "INSERT INTO generation_stats (run_id, generation, best_fitness, average_fitness, " +
                    "worst_fitness, standard_deviation, valid_count, best_ever_fitness) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String DELETE_BY_RUN_SQL =
            "DELETE FROM generation_stats WHERE run_id = ?";
    private static final String SELECT_BY_RUN_SQL =
            "SELECT id, run_id, generation, best_fitness, average_fitness, worst_fitness, " +
                    "standard_deviation, valid_count, best_ever_fitness " +
                    "FROM generation_stats WHERE run_id = ? ORDER BY generation";
    private static final String COUNT_BY_RUN_SQL =
            "SELECT COUNT(*) FROM generation_stats WHERE run_id = ?";

    /**
     * Inserts a single generation stats record.
     *
     * @param stats the generation stats to insert
     * @return the generated ID
     * @throws SQLException if a database error occurs
     */
    public int insert(GenerationStats stats) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, stats.getRunId());
            stmt.setInt(2, stats.getGeneration());
            stmt.setDouble(3, stats.getBestFitness());
            stmt.setDouble(4, stats.getAverageFitness());
            stmt.setDouble(5, stats.getWorstFitness());
            stmt.setDouble(6, stats.getStandardDeviation());
            stmt.setInt(7, stats.getValidCount());
            stmt.setDouble(8, stats.getBestEverFitness());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    stats.setId(id);
                    return id;
                }
            }
            throw new SQLException("Failed to retrieve generated ID");
        }
    }

    /**
     * Inserts multiple generation stats records in a batch.
     * This is the preferred method for saving generation history as it's much faster.
     *
     * @param statsList the list of generation stats to insert
     * @throws SQLException if a database error occurs
     */
    public void insertBatch(List<GenerationStats> statsList) throws SQLException {
        if (statsList.isEmpty()) {
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            for (GenerationStats stats : statsList) {
                stmt.setInt(1, stats.getRunId());
                stmt.setInt(2, stats.getGeneration());
                stmt.setDouble(3, stats.getBestFitness());
                stmt.setDouble(4, stats.getAverageFitness());
                stmt.setDouble(5, stats.getWorstFitness());
                stmt.setDouble(6, stats.getStandardDeviation());
                stmt.setInt(7, stats.getValidCount());
                stmt.setDouble(8, stats.getBestEverFitness());
                stmt.addBatch();
            }

            stmt.executeBatch();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                int index = 0;
                while (rs.next() && index < statsList.size()) {
                    statsList.get(index).setId(rs.getInt(1));
                    index++;
                }
            }
        }
    }

    /**
     * Finds all generation stats for a specific algorithm run.
     *
     * @param runId the algorithm run ID
     * @return list of generation stats ordered by generation number
     * @throws SQLException if a database error occurs
     */
    public List<GenerationStats> findByRun(int runId) throws SQLException {
        List<GenerationStats> statsList = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_RUN_SQL)) {

            stmt.setInt(1, runId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    statsList.add(mapResultSetToGenerationStats(rs));
                }
            }
        }
        return statsList;
    }

    /**
     * Deletes all generation stats for a specific algorithm run.
     *
     * @param runId the algorithm run ID
     * @return the number of records deleted
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
     * Counts the number of generation stats records for a specific run.
     *
     * @param runId the algorithm run ID
     * @return the count of generation stats
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
     * Maps a ResultSet row to a GenerationStats object.
     *
     * @param rs the ResultSet positioned at a valid row
     * @return a GenerationStats object
     * @throws SQLException if a database error occurs
     */
    private GenerationStats mapResultSetToGenerationStats(ResultSet rs) throws SQLException {
        GenerationStats stats = new GenerationStats();
        stats.setId(rs.getInt("id"));
        stats.setRunId(rs.getInt("run_id"));
        stats.setGeneration(rs.getInt("generation"));
        stats.setBestFitness(rs.getDouble("best_fitness"));
        stats.setAverageFitness(rs.getDouble("average_fitness"));
        stats.setWorstFitness(rs.getDouble("worst_fitness"));
        stats.setStandardDeviation(rs.getDouble("standard_deviation"));
        stats.setValidCount(rs.getInt("valid_count"));
        stats.setBestEverFitness(rs.getDouble("best_ever_fitness"));
        return stats;
    }
}
