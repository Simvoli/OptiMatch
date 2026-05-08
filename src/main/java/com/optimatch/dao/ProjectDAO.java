package com.optimatch.dao;

import com.optimatch.model.Project;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// CRUD for the projects table
public class ProjectDAO {

    private static final String INSERT_SQL =
            "INSERT INTO projects (code, name, description, min_capacity, max_capacity, required_gpa) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL =
            "UPDATE projects SET code = ?, name = ?, description = ?, min_capacity = ?, " +
                    "max_capacity = ?, required_gpa = ? WHERE id = ?";
    private static final String DELETE_SQL =
            "DELETE FROM projects WHERE id = ?";
    private static final String SELECT_BY_CODE_SQL =
            "SELECT id, code, name, description, min_capacity, max_capacity, required_gpa " +
                    "FROM projects WHERE code = ?";
    private static final String SELECT_ALL_SQL =
            "SELECT id, code, name, description, min_capacity, max_capacity, required_gpa " +
                    "FROM projects ORDER BY code";
    private static final String COUNT_SQL =
            "SELECT COUNT(*) FROM projects";

    // insert and assign generated id back to the entity
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

    // update by id, returns true on success
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

    // delete by db id
    public boolean delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // find by short code
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

    // load all projects ordered by code
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

    // total project count
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

    // row -> Project
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
