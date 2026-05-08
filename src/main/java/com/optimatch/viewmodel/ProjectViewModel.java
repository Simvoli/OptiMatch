package com.optimatch.viewmodel;

import com.optimatch.model.Project;
import com.optimatch.service.ProjectService;
import com.optimatch.service.ServiceException;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

// view model for the projects screen: form fields, search, save, delete
public class ProjectViewModel {

    private final ProjectService projectService;

    // observable data
    private final ObservableList<Project> projects = FXCollections.observableArrayList();
    private final FilteredList<Project> filteredProjects = new FilteredList<>(projects, p -> true);

    // selected project in the table
    private final ObjectProperty<Project> selectedProject = new SimpleObjectProperty<>();

    // form fields
    private final StringProperty code = new SimpleStringProperty("");
    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty description = new SimpleStringProperty("");
    private final IntegerProperty minCapacity = new SimpleIntegerProperty(1);
    private final IntegerProperty maxCapacity = new SimpleIntegerProperty(10);
    private final DoubleProperty requiredGpa = new SimpleDoubleProperty(0.0);

    // search box text
    private final StringProperty searchText = new SimpleStringProperty("");

    // status bar message
    private final StringProperty statusMessage = new SimpleStringProperty("");

    // edit vs create flag, plus id of the project being edited
    private final BooleanProperty editMode = new SimpleBooleanProperty(false);
    private int editingProjectId = -1;

    // running totals for capacity labels
    private final IntegerProperty totalCapacity = new SimpleIntegerProperty(0);
    private final IntegerProperty totalMinRequired = new SimpleIntegerProperty(0);

    // wires up service and search filter, then loads data
    public ProjectViewModel() {
        this.projectService = new ProjectService();

        // search filter: code, name or description
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

        refresh();
    }

    // reload projects from db and update totals
    public void refresh() {
        try {
            projects.setAll(projectService.getAllProjects());
            updateStatistics();
            statusMessage.set("Loaded " + projects.size() + " projects");
        } catch (ServiceException e) {
            statusMessage.set("Error loading data: " + e.getMessage());
        }
    }

    // recompute total min/max capacity across all projects
    private void updateStatistics() {
        int totalMax = projects.stream().mapToInt(Project::getMaxCapacity).sum();
        int totalMin = projects.stream().mapToInt(Project::getMinCapacity).sum();
        totalCapacity.set(totalMax);
        totalMinRequired.set(totalMin);
    }

    // wipe form and exit edit mode
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

    // populate form from a chosen project
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

    // save form (create or update), returns true on success
    public boolean save() {
        try {
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
                project.setId(editingProjectId);
                projectService.updateProject(project);
                statusMessage.set("Project updated successfully");
            } else {
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

    // delete the currently selected project
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

    // raw project list
    public ObservableList<Project> getProjects() {
        return projects;
    }

    // filtered project list (drives the table)
    public FilteredList<Project> getFilteredProjects() {
        return filteredProjects;
    }

    // code field
    public StringProperty codeProperty() {
        return code;
    }

    // name field
    public StringProperty nameProperty() {
        return name;
    }

    // description field
    public StringProperty descriptionProperty() {
        return description;
    }

    // min capacity field
    public IntegerProperty minCapacityProperty() {
        return minCapacity;
    }

    // max capacity field
    public IntegerProperty maxCapacityProperty() {
        return maxCapacity;
    }

    // required gpa field
    public DoubleProperty requiredGpaProperty() {
        return requiredGpa;
    }

    // search text
    public StringProperty searchTextProperty() {
        return searchText;
    }

    // status message
    public StringProperty statusMessageProperty() {
        return statusMessage;
    }

    // edit vs create flag
    public BooleanProperty editModeProperty() {
        return editMode;
    }

    // total of max capacities across all projects
    public IntegerProperty totalCapacityProperty() {
        return totalCapacity;
    }

    // total of min capacities across all projects
    public IntegerProperty totalMinRequiredProperty() {
        return totalMinRequired;
    }
}
