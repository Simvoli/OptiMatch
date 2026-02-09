package com.optimatch.dao;

import com.optimatch.model.Project;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Project entities.
 * Provides CRUD operations for the projects table.
 */
public class ProjectDAO {

    private static final String INSERT_SQL =
            "INSERT INTO projects (code, name, description, min_capacity, max_capacity, required_gpa) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL =
            "UPDATE projects SET code = ?, name = ?, description = ?, min_capacity = ?, " +
                    "max_capacity = ?, required_gpa = ? WHERE id = ?";
    private static final String DELETE_SQL =
            "DELETE FROM projects WHERE id = ?";
    private static final String SELECT_BY_ID_SQL =
            "SELECT id, code, name, description, min_capacity, max_capacity, required_gpa " +
                    "FROM projects WHERE id = ?";
    private static final String SELECT_BY_CODE_SQL =
            "SELECT id, code, name, description, min_capacity, max_capacity, required_gpa " +
                    "FROM projects WHERE code = ?";
    private static final String SELECT_ALL_SQL =
            "SELECT id, code, name, description, min_capacity, max_capacity, required_gpa " +
                    "FROM projects ORDER BY code";
    private static final String COUNT_SQL =
            "SELECT COUNT(*) FROM projects";

    /**
     * Inserts a new project into the database.
     *
     * @param project the project to insert
     * @return the generated ID for the new project
     * @throws SQLException if a database error occurs
     */
    public int insert(Project project) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, project.getCode());
            stmt.setString(2, project.getName());
            stmt.setString(3, project.getDescription());
            stmt.setInt(4, project.getMinCapacity());
            stmt.setInt(5, project.getMaxCapacity());
            stmt.setDouble(6, project.getRequiredGpa());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    project.setId(id);
                    return id;
                }
            }
            throw new SQLException("Failed to retrieve generated ID");
        }
    }

    /**
     * Updates an existing project in the database.
     *
     * @param project the project to update
     * @return true if the update was successful
     * @throws SQLException if a database error occurs
     */
    public boolean update(Project project) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            stmt.setString(1, project.getCode());
            stmt.setString(2, project.getName());
            stmt.setString(3, project.getDescription());
            stmt.setInt(4, project.getMinCapacity());
            stmt.setInt(5, project.getMaxCapacity());
            stmt.setDouble(6, project.getRequiredGpa());
            stmt.setInt(7, project.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a project from the database.
     *
     * @param id the ID of the project to delete
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
     * Finds a project by its database ID.
     *
     * @param id the database ID
     * @return an Optional containing the project if found
     * @throws SQLException if a database error occurs
     */
    public Optional<Project> findById(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProject(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds a project by its code.
     *
     * @param code the project code
     * @return an Optional containing the project if found
     * @throws SQLException if a database error occurs
     */
    public Optional<Project> findByCode(String code) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_CODE_SQL)) {

            stmt.setString(1, code);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProject(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves all projects from the database.
     *
     * @return a list of all projects
     * @throws SQLException if a database error occurs
     */
    public List<Project> findAll() throws SQLException {
        List<Project> projects = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                projects.add(mapResultSetToProject(rs));
            }
        }
        return projects;
    }

    /**
     * Counts the total number of projects in the database.
     *
     * @return the count of projects
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
     * Maps a ResultSet row to a Project object.
     *
     * @param rs the ResultSet positioned at a valid row
     * @return a Project object
     * @throws SQLException if a database error occurs
     */
    private Project mapResultSetToProject(ResultSet rs) throws SQLException {
        Project project = new Project();
        project.setId(rs.getInt("id"));
        project.setCode(rs.getString("code"));
        project.setName(rs.getString("name"));
        project.setDescription(rs.getString("description"));
        project.setMinCapacity(rs.getInt("min_capacity"));
        project.setMaxCapacity(rs.getInt("max_capacity"));
        project.setRequiredGpa(rs.getDouble("required_gpa"));
        return project;
    }
}
