package com.optimatch.dao;

import com.optimatch.model.AlgorithmRun;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for AlgorithmRun entities.
 * Provides CRUD operations for the algorithm_runs table.
 */
public class AlgorithmRunDAO {

    private static final String INSERT_SQL =
            "INSERT INTO algorithm_runs (run_timestamp, population_size, generations, " +
                    "mutation_rate, crossover_rate, best_fitness, execution_time_ms) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL =
            "UPDATE algorithm_runs SET run_timestamp = ?, population_size = ?, generations = ?, " +
                    "mutation_rate = ?, crossover_rate = ?, best_fitness = ?, execution_time_ms = ? WHERE id = ?";
    private static final String DELETE_SQL =
            "DELETE FROM algorithm_runs WHERE id = ?";
    private static final String SELECT_BY_ID_SQL =
            "SELECT id, run_timestamp, population_size, generations, mutation_rate, " +
                    "crossover_rate, best_fitness, execution_time_ms FROM algorithm_runs WHERE id = ?";
    private static final String SELECT_ALL_SQL =
            "SELECT id, run_timestamp, population_size, generations, mutation_rate, " +
                    "crossover_rate, best_fitness, execution_time_ms FROM algorithm_runs ORDER BY run_timestamp DESC";
    private static final String SELECT_LATEST_SQL =
            "SELECT id, run_timestamp, population_size, generations, mutation_rate, " +
                    "crossover_rate, best_fitness, execution_time_ms FROM algorithm_runs " +
                    "ORDER BY run_timestamp DESC LIMIT 1";
    private static final String COUNT_SQL =
            "SELECT COUNT(*) FROM algorithm_runs";

    /**
     * Inserts a new algorithm run into the database.
     *
     * @param run the algorithm run to insert
     * @return the generated ID for the new run
     * @throws SQLException if a database error occurs
     */
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

    /**
     * Updates an existing algorithm run in the database.
     *
     * @param run the algorithm run to update
     * @return true if the update was successful
     * @throws SQLException if a database error occurs
     */
    public boolean update(AlgorithmRun run) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            stmt.setTimestamp(1, Timestamp.valueOf(run.getRunTimestamp()));
            stmt.setInt(2, run.getPopulationSize());
            stmt.setInt(3, run.getGenerations());
            stmt.setDouble(4, run.getMutationRate());
            stmt.setDouble(5, run.getCrossoverRate());
            stmt.setDouble(6, run.getBestFitness());
            stmt.setLong(7, run.getExecutionTimeMs());
            stmt.setInt(8, run.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes an algorithm run from the database.
     *
     * @param id the ID of the run to delete
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
     * Finds an algorithm run by its database ID.
     *
     * @param id the database ID
     * @return an Optional containing the run if found
     * @throws SQLException if a database error occurs
     */
    public Optional<AlgorithmRun> findById(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAlgorithmRun(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves all algorithm runs from the database, ordered by timestamp (newest first).
     *
     * @return a list of all algorithm runs
     * @throws SQLException if a database error occurs
     */
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

    /**
     * Retrieves the most recent algorithm run.
     *
     * @return an Optional containing the latest run if any exist
     * @throws SQLException if a database error occurs
     */
    public Optional<AlgorithmRun> findLatest() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_LATEST_SQL);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return Optional.of(mapResultSetToAlgorithmRun(rs));
            }
        }
        return Optional.empty();
    }

    /**
     * Counts the total number of algorithm runs in the database.
     *
     * @return the count of runs
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
     * Maps a ResultSet row to an AlgorithmRun object.
     *
     * @param rs the ResultSet positioned at a valid row
     * @return an AlgorithmRun object
     * @throws SQLException if a database error occurs
     */
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
