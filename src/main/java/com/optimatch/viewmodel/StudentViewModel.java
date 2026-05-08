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

// view model for the students screen: form fields, search, save, delete
public class StudentViewModel {

    private final StudentService studentService;
    private final ProjectService projectService;

    // observable data
    private final ObservableList<Student> students = FXCollections.observableArrayList();
    private final FilteredList<Student> filteredStudents = new FilteredList<>(students, p -> true);
    private final ObservableList<Project> projects = FXCollections.observableArrayList();

    // selected student in the table
    private final ObjectProperty<Student> selectedStudent = new SimpleObjectProperty<>();

    // form fields
    private final StringProperty studentId = new SimpleStringProperty("");
    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty email = new SimpleStringProperty("");
    private final DoubleProperty gpa = new SimpleDoubleProperty(0.0);
    private final ObjectProperty<Student> selectedPartner = new SimpleObjectProperty<>();

    // five ranked preferences
    private final ObjectProperty<Project> preference1 = new SimpleObjectProperty<>();
    private final ObjectProperty<Project> preference2 = new SimpleObjectProperty<>();
    private final ObjectProperty<Project> preference3 = new SimpleObjectProperty<>();
    private final ObjectProperty<Project> preference4 = new SimpleObjectProperty<>();
    private final ObjectProperty<Project> preference5 = new SimpleObjectProperty<>();

    // search box text
    private final StringProperty searchText = new SimpleStringProperty("");

    // status bar message
    private final StringProperty statusMessage = new SimpleStringProperty("");

    // edit vs create flag, plus id of the student being edited
    private final BooleanProperty editMode = new SimpleBooleanProperty(false);
    private int editingStudentId = -1;

    // wires up services and search filter, then loads data
    public StudentViewModel() {
        this.studentService = new StudentService();
        this.projectService = new ProjectService();

        // search filter: name, student id or email
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

        refresh();
    }

    // reload students and projects from db
    public void refresh() {
        try {
            students.setAll(studentService.getAllStudents());
            projects.setAll(projectService.getAllProjects());
            statusMessage.set("Loaded " + students.size() + " students, " + projects.size() + " projects");
        } catch (ServiceException e) {
            statusMessage.set("Error loading data: " + e.getMessage());
        }
    }

    // wipe form and exit edit mode
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

    // populate form from a chosen student
    public void loadStudentToForm(Student student) {
        if (student == null) {
            clearForm();
            return;
        }

        studentId.set(student.getStudentId());
        name.set(student.getName());
        email.set(student.getEmail() != null ? student.getEmail() : "");
        gpa.set(student.getGpa());

        // partner
        if (student.hasPartner()) {
            students.stream()
                    .filter(s -> s.getId() == student.getPartnerId())
                    .findFirst()
                    .ifPresent(selectedPartner::set);
        } else {
            selectedPartner.set(null);
        }

        // preferences from db
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

    // save form (create or update), returns true on success
    public boolean save() {
        try {
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
                student.setId(editingStudentId);

                // RU: если партнёр поменялся - сначала отвязываем старого, иначе двойная связка
                Student oldStudent = studentService.getStudentById(editingStudentId).orElse(null);
                if (oldStudent != null && oldStudent.hasPartner()) {
                    if (selectedPartner.get() == null ||
                        selectedPartner.get().getId() != oldStudent.getPartnerId()) {
                        studentService.unlinkPartners(editingStudentId);
                    }
                }

                studentService.updateStudent(student);

                // привязываем нового партнёра, если он отличается от прежнего
                if (selectedPartner.get() != null &&
                    (oldStudent == null || !oldStudent.hasPartner() ||
                     oldStudent.getPartnerId() != selectedPartner.get().getId())) {
                    studentService.linkPartners(student.getId(), selectedPartner.get().getId());
                }

                statusMessage.set("Student updated successfully");
            } else {
                studentService.createStudent(student);

                if (selectedPartner.get() != null) {
                    studentService.linkPartners(student.getId(), selectedPartner.get().getId());
                }

                statusMessage.set("Student created successfully");
            }

            savePreferences(student.getId());

            refresh();
            clearForm();
            return true;

        } catch (ServiceException e) {
            statusMessage.set("Error: " + e.getMessage());
            return false;
        }
    }

    // collect non-null preferences and pass to service
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

    // delete the currently selected student
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

    // students who can still be picked as partner (no partner yet, not the current one)
    public ObservableList<Student> getAvailablePartners() {
        return students.filtered(s ->
            !s.hasPartner() &&
            (editingStudentId == -1 || s.getId() != editingStudentId)
        );
    }

    // ==================== Property Getters ====================

    // raw student list
    public ObservableList<Student> getStudents() {
        return students;
    }

    // filtered student list (drives the table)
    public FilteredList<Student> getFilteredStudents() {
        return filteredStudents;
    }

    // available projects
    public ObservableList<Project> getProjects() {
        return projects;
    }

    // student id field
    public StringProperty studentIdProperty() {
        return studentId;
    }

    // name field
    public StringProperty nameProperty() {
        return name;
    }

    // email field
    public StringProperty emailProperty() {
        return email;
    }

    // gpa field
    public DoubleProperty gpaProperty() {
        return gpa;
    }

    // chosen partner
    public ObjectProperty<Student> selectedPartnerProperty() {
        return selectedPartner;
    }

    // first preference
    public ObjectProperty<Project> preference1Property() {
        return preference1;
    }

    // second preference
    public ObjectProperty<Project> preference2Property() {
        return preference2;
    }

    // third preference
    public ObjectProperty<Project> preference3Property() {
        return preference3;
    }

    // fourth preference
    public ObjectProperty<Project> preference4Property() {
        return preference4;
    }

    // fifth preference
    public ObjectProperty<Project> preference5Property() {
        return preference5;
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
}
