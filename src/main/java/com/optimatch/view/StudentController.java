package com.optimatch.view;

import com.optimatch.model.Project;
import com.optimatch.model.Student;
import com.optimatch.viewmodel.StudentViewModel;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

/**
 * Controller for the Student management screen.
 */
public class StudentController {

    @FXML private TextField searchField;
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> colStudentId;
    @FXML private TableColumn<Student, String> colName;
    @FXML private TableColumn<Student, Double> colGpa;
    @FXML private TableColumn<Student, String> colPartner;
    @FXML private Label countLabel;

    @FXML private Label formTitle;
    @FXML private Button deleteButton;
    @FXML private Button saveButton;

    @FXML private TextField studentIdField;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private Spinner<Double> gpaSpinner;

    @FXML private ComboBox<Student> partnerCombo;

    @FXML private ComboBox<Project> pref1Combo;
    @FXML private ComboBox<Project> pref2Combo;
    @FXML private ComboBox<Project> pref3Combo;
    @FXML private ComboBox<Project> pref4Combo;
    @FXML private ComboBox<Project> pref5Combo;

    @FXML private Label statusLabel;

    private StudentViewModel viewModel;

    @FXML
    public void initialize() {
        viewModel = new StudentViewModel();

        setupTable();
        setupForm();
        setupBindings();
    }

    /**
     * Sets up the student table columns and data.
     */
    private void setupTable() {
        // Configure columns
        colStudentId.setCellValueFactory(cellData ->
                Bindings.createStringBinding(() -> cellData.getValue().getStudentId()));

        colName.setCellValueFactory(cellData ->
                Bindings.createStringBinding(() -> cellData.getValue().getName()));

        colGpa.setCellValueFactory(cellData ->
                Bindings.createObjectBinding(() -> cellData.getValue().getGpa()));
        colGpa.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double gpa, boolean empty) {
                super.updateItem(gpa, empty);
                if (empty || gpa == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", gpa));
                }
            }
        });

        colPartner.setCellValueFactory(cellData ->
                Bindings.createStringBinding(() ->
                        cellData.getValue().hasPartner() ? "Yes" : ""));

        // Set data
        studentTable.setItems(viewModel.getFilteredStudents());

        // Selection listener
        studentTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> viewModel.loadStudentToForm(newVal));
    }

    /**
     * Sets up the form controls.
     */
    private void setupForm() {
        // GPA Spinner
        SpinnerValueFactory<Double> gpaFactory =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 4.0, 0.0, 0.1);
        gpaSpinner.setValueFactory(gpaFactory);

        // Partner ComboBox
        partnerCombo.setItems(viewModel.getAvailablePartners());
        partnerCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Student student) {
                return student == null ? "" : student.getName() + " (" + student.getStudentId() + ")";
            }

            @Override
            public Student fromString(String string) {
                return null;
            }
        });

        // Project ComboBoxes
        setupProjectCombo(pref1Combo);
        setupProjectCombo(pref2Combo);
        setupProjectCombo(pref3Combo);
        setupProjectCombo(pref4Combo);
        setupProjectCombo(pref5Combo);
    }

    /**
     * Sets up a project ComboBox.
     */
    private void setupProjectCombo(ComboBox<Project> combo) {
        combo.setItems(viewModel.getProjects());
        combo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Project project) {
                return project == null ? "" : project.getCode() + " - " + project.getName();
            }

            @Override
            public Project fromString(String string) {
                return null;
            }
        });
    }

    /**
     * Sets up data bindings between view and view model.
     */
    private void setupBindings() {
        // Search field
        searchField.textProperty().bindBidirectional(viewModel.searchTextProperty());

        // Form fields
        studentIdField.textProperty().bindBidirectional(viewModel.studentIdProperty());
        nameField.textProperty().bindBidirectional(viewModel.nameProperty());
        emailField.textProperty().bindBidirectional(viewModel.emailProperty());

        // GPA binding
        gpaSpinner.getValueFactory().valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                viewModel.gpaProperty().set(newVal);
            }
        });
        viewModel.gpaProperty().addListener((obs, oldVal, newVal) -> {
            gpaSpinner.getValueFactory().setValue(newVal.doubleValue());
        });

        // Partner binding
        partnerCombo.valueProperty().bindBidirectional(viewModel.selectedPartnerProperty());

        // Preference bindings
        pref1Combo.valueProperty().bindBidirectional(viewModel.preference1Property());
        pref2Combo.valueProperty().bindBidirectional(viewModel.preference2Property());
        pref3Combo.valueProperty().bindBidirectional(viewModel.preference3Property());
        pref4Combo.valueProperty().bindBidirectional(viewModel.preference4Property());
        pref5Combo.valueProperty().bindBidirectional(viewModel.preference5Property());

        // Status message
        statusLabel.textProperty().bind(viewModel.statusMessageProperty());

        // Edit mode bindings
        viewModel.editModeProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                formTitle.setText("Edit Student");
                saveButton.setText("Update Student");
                deleteButton.setVisible(true);
            } else {
                formTitle.setText("New Student");
                saveButton.setText("Save Student");
                deleteButton.setVisible(false);
            }
        });

        // Count label
        viewModel.getFilteredStudents().addListener(
                (javafx.collections.ListChangeListener<Student>) c ->
                        updateCountLabel());
        updateCountLabel();
    }

    /**
     * Updates the student count label.
     */
    private void updateCountLabel() {
        int filtered = viewModel.getFilteredStudents().size();
        int total = viewModel.getStudents().size();
        if (filtered == total) {
            countLabel.setText(total + " students");
        } else {
            countLabel.setText(filtered + " of " + total + " students");
        }
    }

    // ==================== Action Handlers ====================

    @FXML
    public void newStudent() {
        viewModel.clearForm();
        studentTable.getSelectionModel().clearSelection();
        studentIdField.requestFocus();
    }

    @FXML
    public void refresh() {
        viewModel.refresh();
    }

    @FXML
    public void clearSearch() {
        searchField.clear();
    }

    @FXML
    public void clearForm() {
        viewModel.clearForm();
        studentTable.getSelectionModel().clearSelection();
    }

    @FXML
    public void clearPartner() {
        partnerCombo.setValue(null);
    }

    @FXML
    public void saveStudent() {
        if (viewModel.save()) {
            studentTable.getSelectionModel().clearSelection();
        }
    }

    @FXML
    public void deleteStudent() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        // Confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Student");
        alert.setHeaderText("Delete " + selected.getName() + "?");
        alert.setContentText("This action cannot be undone. All preferences will also be deleted.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            viewModel.deleteSelected();
        }
    }
}
