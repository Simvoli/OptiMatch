package com.optimatch.dao;

import com.optimatch.model.AlgorithmRun;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the {@code algorithm_runs} table.
 */
public class AlgorithmRunDAO {

    private static final String INSERT_SQL =
            "INSERT INTO algorithm_runs (run_timestamp, population_size, generations, " +
                    "mutation_rate, crossover_rate, best_fitness, execution_time_ms) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String DELETE_SQL =
            "DELETE FROM algorithm_runs WHERE id = ?";
    private static final String SELECT_ALL_SQL =
            "SELECT id, run_timestamp, population_size, generations, mutation_rate, " +
                    "crossover_rate, best_fitness, execution_time_ms FROM algorithm_runs ORDER BY run_timestamp DESC";

    public int insert(AlgorithmRun run) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setTimestamp(1, Timestamp.valueOf(run.getRunTimestamp()));
            stmt.setInt(2, run.getPopulationSize());
            stmt.setInt(3, run.getGenerations());
            stmt.setDouble(4, run.getMutationRate());
            stmt.setDouble(5, run.getCrossoverRate());
            stmt.setDouble(6, run.getBestFitness());
            stmt.setLong(7, run.getExecutionTimeMs());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    run.setId(id);
                    return id;
                }
            }
            throw new SQLException("Failed to retrieve generated ID");
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<AlgorithmRun> findAll() throws SQLException {
        List<AlgorithmRun> runs = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                runs.add(mapResultSetToAlgorithmRun(rs));
            }
        }
        return runs;
    }

    private AlgorithmRun mapResultSetToAlgorithmRun(ResultSet rs) throws SQLException {
        AlgorithmRun run = new AlgorithmRun();
        run.setId(rs.getInt("id"));

        Timestamp timestamp = rs.getTimestamp("run_timestamp");
        if (timestamp != null) {
            run.setRunTimestamp(timestamp.toLocalDateTime());
        }

        run.setPopulationSize(rs.getInt("population_size"));
        run.setGenerations(rs.getInt("generations"));
        run.setMutationRate(rs.getDouble("mutation_rate"));
        run.setCrossoverRate(rs.getDouble("crossover_rate"));
        run.setBestFitness(rs.getDouble("best_fitness"));
        run.setExecutionTimeMs(rs.getLong("execution_time_ms"));
        return run;
    }
}
