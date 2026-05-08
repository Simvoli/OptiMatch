package com.optimatch.view;

import com.optimatch.model.Project;
import com.optimatch.model.Student;
import com.optimatch.viewmodel.StudentViewModel;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

// FXML controller for the students screen
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

    // FXML init
    @FXML
    public void initialize() {
        viewModel = new StudentViewModel();

        setupTable();
        setupForm();
        setupBindings();
    }

    // configure table columns and selection listener
    private void setupTable() {
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

        studentTable.setItems(viewModel.getFilteredStudents());

        studentTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> viewModel.loadStudentToForm(newVal));
    }

    // configure spinners and combo boxes
    private void setupForm() {
        SpinnerValueFactory<Double> gpaFactory =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 4.0, 0.0, 0.1);
        gpaSpinner.setValueFactory(gpaFactory);

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

        setupProjectCombo(pref1Combo);
        setupProjectCombo(pref2Combo);
        setupProjectCombo(pref3Combo);
        setupProjectCombo(pref4Combo);
        setupProjectCombo(pref5Combo);
    }

    // shared setup for project preference combos
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

    // wire form fields to view model properties
    private void setupBindings() {
        searchField.textProperty().bindBidirectional(viewModel.searchTextProperty());

        studentIdField.textProperty().bindBidirectional(viewModel.studentIdProperty());
        nameField.textProperty().bindBidirectional(viewModel.nameProperty());
        emailField.textProperty().bindBidirectional(viewModel.emailProperty());

        // gpa spinner needs manual two-way binding
        gpaSpinner.getValueFactory().valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                viewModel.gpaProperty().set(newVal);
            }
        });
        viewModel.gpaProperty().addListener((obs, oldVal, newVal) -> {
            gpaSpinner.getValueFactory().setValue(newVal.doubleValue());
        });

        partnerCombo.valueProperty().bindBidirectional(viewModel.selectedPartnerProperty());

        pref1Combo.valueProperty().bindBidirectional(viewModel.preference1Property());
        pref2Combo.valueProperty().bindBidirectional(viewModel.preference2Property());
        pref3Combo.valueProperty().bindBidirectional(viewModel.preference3Property());
        pref4Combo.valueProperty().bindBidirectional(viewModel.preference4Property());
        pref5Combo.valueProperty().bindBidirectional(viewModel.preference5Property());

        statusLabel.textProperty().bind(viewModel.statusMessageProperty());

        // swap labels and buttons when entering/leaving edit mode
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

        viewModel.getFilteredStudents().addListener(
                (javafx.collections.ListChangeListener<Student>) c ->
                        updateCountLabel());
        updateCountLabel();
    }

    // refresh "X of Y students" label
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

    // start a fresh student form
    @FXML
    public void newStudent() {
        viewModel.clearForm();
        studentTable.getSelectionModel().clearSelection();
        studentIdField.requestFocus();
    }

    // reload from db
    @FXML
    public void refresh() {
        viewModel.refresh();
    }

    // clear the search box
    @FXML
    public void clearSearch() {
        searchField.clear();
    }

    // wipe the form
    @FXML
    public void clearForm() {
        viewModel.clearForm();
        studentTable.getSelectionModel().clearSelection();
    }

    // unset the partner combo
    @FXML
    public void clearPartner() {
        partnerCombo.setValue(null);
    }

    // save (create or update)
    @FXML
    public void saveStudent() {
        if (viewModel.save()) {
            studentTable.getSelectionModel().clearSelection();
        }
    }

    // delete the selected student after confirmation
    @FXML
    public void deleteStudent() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Student");
        alert.setHeaderText("Delete " + selected.getName() + "?");
        alert.setContentText("This action cannot be undone. All preferences will also be deleted.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            viewModel.deleteSelected();
        }
    }
}
