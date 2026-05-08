package com.optimatch.dao;

import com.optimatch.model.GenerationStats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// CRUD for the generation_stats table
public class GenerationStatsDAO {

    private static final String INSERT_SQL =
            "INSERT INTO generation_stats (run_id, generation, best_fitness, average_fitness, " +
                    "worst_fitness, standard_deviation, valid_count, best_ever_fitness) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_RUN_SQL =
            "SELECT id, run_id, generation, best_fitness, average_fitness, worst_fitness, " +
                    "standard_deviation, valid_count, best_ever_fitness " +
                    "FROM generation_stats WHERE run_id = ? ORDER BY generation";

    // batch insert, generated ids written back into the list
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

    // load all stats rows for a given run, ordered by generation
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

    // row -> GenerationStats
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
