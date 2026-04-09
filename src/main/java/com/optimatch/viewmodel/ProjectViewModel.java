package com.optimatch.viewmodel;

import com.optimatch.model.Project;
import com.optimatch.service.ProjectService;
import com.optimatch.service.ServiceException;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

/**
 * ViewModel for the Project management screen.
 * Handles data binding and business logic for the UI.
 */
public class ProjectViewModel {

    private final ProjectService projectService;

    // Observable collections
    private final ObservableList<Project> projects = FXCollections.observableArrayList();
    private final FilteredList<Project> filteredProjects = new FilteredList<>(projects, p -> true);

    // Selected project
    private final ObjectProperty<Project> selectedProject = new SimpleObjectProperty<>();

    // Form fields
    private final StringProperty code = new SimpleStringProperty("");
    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty description = new SimpleStringProperty("");
    private final IntegerProperty minCapacity = new SimpleIntegerProperty(1);
    private final IntegerProperty maxCapacity = new SimpleIntegerProperty(10);
    private final DoubleProperty requiredGpa = new SimpleDoubleProperty(0.0);

    // Search/filter
    private final StringProperty searchText = new SimpleStringProperty("");

    // Status message
    private final StringProperty statusMessage = new SimpleStringProperty("");

    // Edit mode
    private final BooleanProperty editMode = new SimpleBooleanProperty(false);
    private int editingProjectId = -1;

    // Statistics
    private final IntegerProperty totalCapacity = new SimpleIntegerProperty(0);
    private final IntegerProperty totalMinRequired = new SimpleIntegerProperty(0);

    /**
     * Creates a ProjectViewModel with default service.
     */
    public ProjectViewModel() {
        this(new ProjectService());
    }

    /**
     * Creates a ProjectViewModel with the specified service.
     *
     * @param projectService the project service
     */
    public ProjectViewModel(ProjectService projectService) {
        this.projectService = projectService;

        // Set up search filter
        searchText.addListener((obs, oldVal, newVal) -> {
            filteredProjects.setPredicate(project -> {
                if (newVal == null || newVal.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newVal.toLowerCase();
                return project.getName().toLowerCase().contains(lowerCaseFilter) ||
                       project.getCode().toLowerCase().contains(lowerCaseFilter) ||
                       (project.getDescription() != null &&
                        project.getDescription().toLowerCase().contains(lowerCaseFilter));
            });
        });

        // Load initial data
        refresh();
    }

    /**
     * Refreshes data from the database.
     */
    public void refresh() {
        try {
            projects.setAll(projectService.getAllProjects());
            updateStatistics();
            statusMessage.set("Loaded " + projects.size() + " projects");
        } catch (ServiceException e) {
            statusMessage.set("Error loading data: " + e.getMessage());
        }
    }

    /**
     * Updates capacity statistics.
     */
    private void updateStatistics() {
        int totalMax = projects.stream().mapToInt(Project::getMaxCapacity).sum();
        int totalMin = projects.stream().mapToInt(Project::getMinCapacity).sum();
        totalCapacity.set(totalMax);
        totalMinRequired.set(totalMin);
    }

    /**
     * Clears the form fields.
     */
    public void clearForm() {
        code.set("");
        name.set("");
        description.set("");
        minCapacity.set(1);
        maxCapacity.set(10);
        requiredGpa.set(0.0);
        editMode.set(false);
        editingProjectId = -1;
        selectedProject.set(null);
    }

    /**
     * Loads the selected project's data into the form.
     *
     * @param project the project to edit
     */
    public void loadProjectToForm(Project project) {
        if (project == null) {
            clearForm();
            return;
        }

        code.set(project.getCode());
        name.set(project.getName());
        description.set(project.getDescription() != null ? project.getDescription() : "");
        minCapacity.set(project.getMinCapacity());
        maxCapacity.set(project.getMaxCapacity());
        requiredGpa.set(project.getRequiredGpa());

        editMode.set(true);
        editingProjectId = project.getId();
        selectedProject.set(project);
    }

    /**
     * Saves the current form data (create or update).
     *
     * @return true if save was successful
     */
    public boolean save() {
        try {
            // Validate
            if (code.get().trim().isEmpty()) {
                statusMessage.set("Project code is required");
                return false;
            }
            if (name.get().trim().isEmpty()) {
                statusMessage.set("Project name is required");
                return false;
            }
            if (minCapacity.get() < 0) {
                statusMessage.set("Minimum capacity cannot be negative");
                return false;
            }
            if (maxCapacity.get() < 1) {
                statusMessage.set("Maximum capacity must be at least 1");
                return false;
            }
            if (minCapacity.get() > maxCapacity.get()) {
                statusMessage.set("Minimum capacity cannot exceed maximum capacity");
                return false;
            }
            if (requiredGpa.get() < 0 || requiredGpa.get() > 4.0) {
                statusMessage.set("Required GPA must be between 0.00 and 4.00");
                return false;
            }

            Project project = new Project();
            project.setCode(code.get().trim());
            project.setName(name.get().trim());
            project.setDescription(description.get().trim().isEmpty() ? null : description.get().trim());
            project.setMinCapacity(minCapacity.get());
            project.setMaxCapacity(maxCapacity.get());
            project.setRequiredGpa(requiredGpa.get());

            if (editMode.get()) {
                // Update existing
                project.setId(editingProjectId);
                projectService.updateProject(project);
                statusMessage.set("Project updated successfully");
            } else {
                // Create new
                projectService.createProject(project);
                statusMessage.set("Project created successfully");
            }

            refresh();
            clearForm();
            return true;

        } catch (ServiceException e) {
            statusMessage.set("Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes the currently selected project.
     *
     * @return true if deletion was successful
     */
    public boolean deleteSelected() {
        if (selectedProject.get() == null) {
            statusMessage.set("No project selected");
            return false;
        }

        try {
            projectService.deleteProject(selectedProject.get().getId());
            statusMessage.set("Project deleted successfully");
            refresh();
            clearForm();
            return true;
        } catch (ServiceException e) {
            statusMessage.set("Error: " + e.getMessage());
            return false;
        }
    }

    // ==================== Property Getters ====================

    public ObservableList<Project> getProjects() {
        return projects;
    }

    public FilteredList<Project> getFilteredProjects() {
        return filteredProjects;
    }

    public ObjectProperty<Project> selectedProjectProperty() {
        return selectedProject;
    }

    public StringProperty codeProperty() {
        return code;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public IntegerProperty minCapacityProperty() {
        return minCapacity;
    }

    public IntegerProperty maxCapacityProperty() {
        return maxCapacity;
    }

    public DoubleProperty requiredGpaProperty() {
        return requiredGpa;
    }

    public StringProperty searchTextProperty() {
        return searchText;
    }

    public StringProperty statusMessageProperty() {
        return statusMessage;
    }

    public BooleanProperty editModeProperty() {
        return editMode;
    }

    public IntegerProperty totalCapacityProperty() {
        return totalCapacity;
    }

    public IntegerProperty totalMinRequiredProperty() {
        return totalMinRequired;
    }
}
