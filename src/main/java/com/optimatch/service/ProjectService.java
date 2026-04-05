package com.optimatch.service;

import com.optimatch.dao.ProjectDAO;
import com.optimatch.model.Project;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for project management.
 * Handles business logic for project operations including capacity validation.
 */
public class ProjectService {

    private final ProjectDAO projectDAO;

    /**
     * Creates a ProjectService with default DAO.
     */
    public ProjectService() {
        this.projectDAO = new ProjectDAO();
    }

    /**
     * Creates a ProjectService with the specified DAO.
     *
     * @param projectDAO the project DAO
     */
    public ProjectService(ProjectDAO projectDAO) {
        this.projectDAO = projectDAO;
    }

    /**
     * Creates a new project.
     *
     * @param project the project to create
     * @return the created project with generated ID
     * @throws ServiceException if validation fails or database error occurs
     */
    public Project createProject(Project project) throws ServiceException {
        validateProject(project);

        try {
            // Check for duplicate code
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

    /**
     * Updates an existing project.
     *
     * @param project the project to update
     * @return true if update was successful
     * @throws ServiceException if validation fails or database error occurs
     */
    public boolean updateProject(Project project) throws ServiceException {
        validateProject(project);

        try {
            return projectDAO.update(project);
        } catch (SQLException e) {
            throw new ServiceException("Failed to update project: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a project.
     *
     * @param projectId the database ID of the project to delete
     * @return true if deletion was successful
     * @throws ServiceException if database error occurs
     */
    public boolean deleteProject(int projectId) throws ServiceException {
        try {
            return projectDAO.delete(projectId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to delete project: " + e.getMessage(), e);
        }
    }

    /**
     * Gets a project by database ID.
     *
     * @param id the database ID
     * @return the project if found
     * @throws ServiceException if database error occurs
     */
    public Optional<Project> getProjectById(int id) throws ServiceException {
        try {
            return projectDAO.findById(id);
        } catch (SQLException e) {
            throw new ServiceException("Failed to get project: " + e.getMessage(), e);
        }
    }

    /**
     * Gets a project by code.
     *
     * @param code the project code
     * @return the project if found
     * @throws ServiceException if database error occurs
     */
    public Optional<Project> getProjectByCode(String code) throws ServiceException {
        try {
            return projectDAO.findByCode(code);
        } catch (SQLException e) {
            throw new ServiceException("Failed to get project: " + e.getMessage(), e);
        }
    }

    /**
     * Gets all projects.
     *
     * @return list of all projects
     * @throws ServiceException if database error occurs
     */
    public List<Project> getAllProjects() throws ServiceException {
        try {
            return projectDAO.findAll();
        } catch (SQLException e) {
            throw new ServiceException("Failed to get projects: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the total number of projects.
     *
     * @return the count of projects
     * @throws ServiceException if database error occurs
     */
    public int getProjectCount() throws ServiceException {
        try {
            return projectDAO.count();
        } catch (SQLException e) {
            throw new ServiceException("Failed to count projects: " + e.getMessage(), e);
        }
    }

    /**
     * Calculates the total capacity across all projects.
     *
     * @return the sum of all project max capacities
     * @throws ServiceException if database error occurs
     */
    public int getTotalCapacity() throws ServiceException {
        List<Project> projects = getAllProjects();
        return projects.stream()
                .mapToInt(Project::getMaxCapacity)
                .sum();
    }

    /**
     * Calculates the minimum required students to fill all projects.
     *
     * @return the sum of all project min capacities
     * @throws ServiceException if database error occurs
     */
    public int getMinimumRequiredStudents() throws ServiceException {
        List<Project> projects = getAllProjects();
        return projects.stream()
                .mapToInt(Project::getMinCapacity)
                .sum();
    }

    /**
     * Checks if the number of students can be accommodated by the projects.
     *
     * @param studentCount the number of students
     * @return true if student count is within total capacity range
     * @throws ServiceException if database error occurs
     */
    public boolean canAccommodateStudents(int studentCount) throws ServiceException {
        int minRequired = getMinimumRequiredStudents();
        int maxCapacity = getTotalCapacity();
        return studentCount >= minRequired && studentCount <= maxCapacity;
    }

    /**
     * Gets projects that a student qualifies for based on GPA.
     *
     * @param studentGpa the student's GPA
     * @return list of projects the student can be assigned to
     * @throws ServiceException if database error occurs
     */
    public List<Project> getEligibleProjects(double studentGpa) throws ServiceException {
        List<Project> allProjects = getAllProjects();
        return allProjects.stream()
                .filter(p -> p.meetsGpaRequirement(studentGpa))
                .toList();
    }

    /**
     * Validates a project object.
     *
     * @param project the project to validate
     * @throws ServiceException if validation fails
     */
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
