package com.optimatch.viewmodel;

import com.optimatch.model.Preference;
import com.optimatch.model.Project;
import com.optimatch.model.Student;
import com.optimatch.service.ProjectService;
import com.optimatch.service.ServiceException;
import com.optimatch.service.StudentService;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ViewModel for the Student management screen.
 * Handles data binding and business logic for the UI.
 */
public class StudentViewModel {

    private final StudentService studentService;
    private final ProjectService projectService;

    // Observable collections
    private final ObservableList<Student> students = FXCollections.observableArrayList();
    private final FilteredList<Student> filteredStudents = new FilteredList<>(students, p -> true);
    private final ObservableList<Project> projects = FXCollections.observableArrayList();

    // Selected student
    private final ObjectProperty<Student> selectedStudent = new SimpleObjectProperty<>();

    // Form fields
    private final StringProperty studentId = new SimpleStringProperty("");
    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty email = new SimpleStringProperty("");
    private final DoubleProperty gpa = new SimpleDoubleProperty(0.0);
    private final ObjectProperty<Student> selectedPartner = new SimpleObjectProperty<>();

    // Preferences (up to 5)
    private final ObjectProperty<Project> preference1 = new SimpleObjectProperty<>();
    private final ObjectProperty<Project> preference2 = new SimpleObjectProperty<>();
    private final ObjectProperty<Project> preference3 = new SimpleObjectProperty<>();
    private final ObjectProperty<Project> preference4 = new SimpleObjectProperty<>();
    private final ObjectProperty<Project> preference5 = new SimpleObjectProperty<>();

    // Search/filter
    private final StringProperty searchText = new SimpleStringProperty("");

    // Status message
    private final StringProperty statusMessage = new SimpleStringProperty("");

    // Edit mode
    private final BooleanProperty editMode = new SimpleBooleanProperty(false);
    private int editingStudentId = -1;

    /**
     * Creates a StudentViewModel with default services.
     */
    public StudentViewModel() {
        this(new StudentService(), new ProjectService());
    }

    /**
     * Creates a StudentViewModel with the specified services.
     *
     * @param studentService the student service
     * @param projectService the project service
     */
    public StudentViewModel(StudentService studentService, ProjectService projectService) {
        this.studentService = studentService;
        this.projectService = projectService;

        // Set up search filter
        searchText.addListener((obs, oldVal, newVal) -> {
            filteredStudents.setPredicate(student -> {
                if (newVal == null || newVal.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newVal.toLowerCase();
                return student.getName().toLowerCase().contains(lowerCaseFilter) ||
                       student.getStudentId().toLowerCase().contains(lowerCaseFilter) ||
                       (student.getEmail() != null && student.getEmail().toLowerCase().contains(lowerCaseFilter));
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
            students.setAll(studentService.getAllStudents());
            projects.setAll(projectService.getAllProjects());
            statusMessage.set("Loaded " + students.size() + " students, " + projects.size() + " projects");
        } catch (ServiceException e) {
            statusMessage.set("Error loading data: " + e.getMessage());
        }
    }

    /**
     * Clears the form fields.
     */
    public void clearForm() {
        studentId.set("");
        name.set("");
        email.set("");
        gpa.set(0.0);
        selectedPartner.set(null);
        preference1.set(null);
        preference2.set(null);
        preference3.set(null);
        preference4.set(null);
        preference5.set(null);
        editMode.set(false);
        editingStudentId = -1;
        selectedStudent.set(null);
    }

    /**
     * Loads the selected student's data into the form.
     *
     * @param student the student to edit
     */
    public void loadStudentToForm(Student student) {
        if (student == null) {
            clearForm();
            return;
        }

        studentId.set(student.getStudentId());
        name.set(student.getName());
        email.set(student.getEmail() != null ? student.getEmail() : "");
        gpa.set(student.getGpa());

        // Load partner
        if (student.hasPartner()) {
            students.stream()
                    .filter(s -> s.getId() == student.getPartnerId())
                    .findFirst()
                    .ifPresent(selectedPartner::set);
        } else {
            selectedPartner.set(null);
        }

        // Load preferences
        try {
            List<Preference> prefs = studentService.getPreferences(student.getId());
            preference1.set(null);
            preference2.set(null);
            preference3.set(null);
            preference4.set(null);
            preference5.set(null);

            for (Preference pref : prefs) {
                Project project = projects.stream()
                        .filter(p -> p.getId() == pref.getProjectId())
                        .findFirst()
                        .orElse(null);

                switch (pref.getRank()) {
                    case 1 -> preference1.set(project);
                    case 2 -> preference2.set(project);
                    case 3 -> preference3.set(project);
                    case 4 -> preference4.set(project);
                    case 5 -> preference5.set(project);
                }
            }
        } catch (ServiceException e) {
            statusMessage.set("Error loading preferences: " + e.getMessage());
        }

        editMode.set(true);
        editingStudentId = student.getId();
        selectedStudent.set(student);
    }

    /**
     * Saves the current form data (create or update).
     *
     * @return true if save was successful
     */
    public boolean save() {
        try {
            // Validate
            if (studentId.get().trim().isEmpty()) {
                statusMessage.set("Student ID is required");
                return false;
            }
            if (name.get().trim().isEmpty()) {
                statusMessage.set("Name is required");
                return false;
            }
            if (gpa.get() < 0 || gpa.get() > 4.0) {
                statusMessage.set("GPA must be between 0.00 and 4.00");
                return false;
            }

            Student student = new Student();
            student.setStudentId(studentId.get().trim());
            student.setName(name.get().trim());
            student.setEmail(email.get().trim().isEmpty() ? null : email.get().trim());
            student.setGpa(gpa.get());

            if (editMode.get()) {
                // Update existing
                student.setId(editingStudentId);

                // Handle partner changes
                Student oldStudent = studentService.getStudentById(editingStudentId).orElse(null);
                if (oldStudent != null && oldStudent.hasPartner()) {
                    // Unlink old partner if different
                    if (selectedPartner.get() == null ||
                        selectedPartner.get().getId() != oldStudent.getPartnerId()) {
                        studentService.unlinkPartners(editingStudentId);
                    }
                }

                studentService.updateStudent(student);

                // Link new partner if selected
                if (selectedPartner.get() != null &&
                    (oldStudent == null || !oldStudent.hasPartner() ||
                     oldStudent.getPartnerId() != selectedPartner.get().getId())) {
                    studentService.linkPartners(student.getId(), selectedPartner.get().getId());
                }

                statusMessage.set("Student updated successfully");
            } else {
                // Create new
                studentService.createStudent(student);

                // Link partner if selected
                if (selectedPartner.get() != null) {
                    studentService.linkPartners(student.getId(), selectedPartner.get().getId());
                }

                statusMessage.set("Student created successfully");
            }

            // Save preferences
            savePreferences(student.getId());

            refresh();
            clearForm();
            return true;

        } catch (ServiceException e) {
            statusMessage.set("Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Saves preferences for a student.
     */
    private void savePreferences(int studentId) throws ServiceException {
        List<Preference> preferences = new ArrayList<>();

        if (preference1.get() != null) {
            preferences.add(new Preference(studentId, preference1.get().getId(), 1));
        }
        if (preference2.get() != null) {
            preferences.add(new Preference(studentId, preference2.get().getId(), 2));
        }
        if (preference3.get() != null) {
            preferences.add(new Preference(studentId, preference3.get().getId(), 3));
        }
        if (preference4.get() != null) {
            preferences.add(new Preference(studentId, preference4.get().getId(), 4));
        }
        if (preference5.get() != null) {
            preferences.add(new Preference(studentId, preference5.get().getId(), 5));
        }

        studentService.setPreferences(studentId, preferences);
    }

    /**
     * Deletes the currently selected student.
     *
     * @return true if deletion was successful
     */
    public boolean deleteSelected() {
        if (selectedStudent.get() == null) {
            statusMessage.set("No student selected");
            return false;
        }

        try {
            studentService.deleteStudent(selectedStudent.get().getId());
            statusMessage.set("Student deleted successfully");
            refresh();
            clearForm();
            return true;
        } catch (ServiceException e) {
            statusMessage.set("Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets students available as partners (excluding the current student being edited).
     *
     * @return list of potential partners
     */
    public ObservableList<Student> getAvailablePartners() {
        return students.filtered(s ->
            !s.hasPartner() &&
            (editingStudentId == -1 || s.getId() != editingStudentId)
        );
    }

    // ==================== Property Getters ====================

    public ObservableList<Student> getStudents() {
        return students;
    }

    public FilteredList<Student> getFilteredStudents() {
        return filteredStudents;
    }

    public ObservableList<Project> getProjects() {
        return projects;
    }

    public ObjectProperty<Student> selectedStudentProperty() {
        return selectedStudent;
    }

    public StringProperty studentIdProperty() {
        return studentId;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty emailProperty() {
        return email;
    }

    public DoubleProperty gpaProperty() {
        return gpa;
    }

    public ObjectProperty<Student> selectedPartnerProperty() {
        return selectedPartner;
    }

    public ObjectProperty<Project> preference1Property() {
        return preference1;
    }

    public ObjectProperty<Project> preference2Property() {
        return preference2;
    }

    public ObjectProperty<Project> preference3Property() {
        return preference3;
    }

    public ObjectProperty<Project> preference4Property() {
        return preference4;
    }

    public ObjectProperty<Project> preference5Property() {
        return preference5;
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
}
