package com.optimatch.service;

import com.optimatch.dao.ProjectDAO;
import com.optimatch.model.Project;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

// project business logic: validation, totals
public class ProjectService {

    private final ProjectDAO projectDAO;

    // wires up default DAO
    public ProjectService() {
        this.projectDAO = new ProjectDAO();
    }

    // create a new project (rejects duplicate code)
    public Project createProject(Project project) throws ServiceException {
        validateProject(project);

        try {
            Optional<Project> existing = projectDAO.findByCode(project.getCode());
            if (existing.isPresent()) {
                throw new ServiceException("Project with code " + project.getCode() + " already exists");
            }

            projectDAO.insert(project);
            return project;
        } catch (SQLException e) {
            throw new ServiceException("Failed to create project: " + e.getMessage(), e);
        }
    }

    // update an existing project
    public boolean updateProject(Project project) throws ServiceException {
        validateProject(project);

        try {
            return projectDAO.update(project);
        } catch (SQLException e) {
            throw new ServiceException("Failed to update project: " + e.getMessage(), e);
        }
    }

    // delete a project
    public boolean deleteProject(int projectId) throws ServiceException {
        try {
            return projectDAO.delete(projectId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to delete project: " + e.getMessage(), e);
        }
    }

    // load all projects
    public List<Project> getAllProjects() throws ServiceException {
        try {
            return projectDAO.findAll();
        } catch (SQLException e) {
            throw new ServiceException("Failed to get projects: " + e.getMessage(), e);
        }
    }

    // total project count
    public int getProjectCount() throws ServiceException {
        try {
            return projectDAO.count();
        } catch (SQLException e) {
            throw new ServiceException("Failed to count projects: " + e.getMessage(), e);
        }
    }

    // sum of max capacities across all projects
    public int getTotalCapacity() throws ServiceException {
        List<Project> projects = getAllProjects();
        return projects.stream()
                .mapToInt(Project::getMaxCapacity)
                .sum();
    }

    // sum of min capacities across all projects
    public int getMinimumRequiredStudents() throws ServiceException {
        List<Project> projects = getAllProjects();
        return projects.stream()
                .mapToInt(Project::getMinCapacity)
                .sum();
    }

    // basic field-level validation
    private void validateProject(Project project) throws ServiceException {
        if (project == null) {
            throw new ServiceException("Project cannot be null");
        }
        if (project.getCode() == null || project.getCode().trim().isEmpty()) {
            throw new ServiceException("Project code is required");
        }
        if (project.getName() == null || project.getName().trim().isEmpty()) {
            throw new ServiceException("Project name is required");
        }
        if (project.getMinCapacity() < 0) {
            throw new ServiceException("Minimum capacity cannot be negative");
        }
        if (project.getMaxCapacity() < 1) {
            throw new ServiceException("Maximum capacity must be at least 1");
        }
        if (project.getMinCapacity() > project.getMaxCapacity()) {
            throw new ServiceException("Minimum capacity cannot exceed maximum capacity");
        }
        if (project.getRequiredGpa() < 0 || project.getRequiredGpa() > 4.0) {
            throw new ServiceException("Required GPA must be between 0.00 and 4.00");
        }
    }
}
