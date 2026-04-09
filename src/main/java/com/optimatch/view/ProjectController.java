package com.optimatch.view;

import com.optimatch.model.Project;
import com.optimatch.viewmodel.ProjectViewModel;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Controller for the Project management screen.
 */
public class ProjectController {

    @FXML private TextField searchField;
    @FXML private TableView<Project> projectTable;
    @FXML private TableColumn<Project, String> colCode;
    @FXML private TableColumn<Project, String> colName;
    @FXML private TableColumn<Project, String> colCapacity;
    @FXML private TableColumn<Project, Double> colGpa;
    @FXML private Label countLabel;
    @FXML private Label capacityLabel;

    @FXML private Label formTitle;
    @FXML private Button deleteButton;
    @FXML private Button saveButton;

    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private Spinner<Integer> minCapacitySpinner;
    @FXML private Spinner<Integer> maxCapacitySpinner;
    @FXML private Spinner<Double> requiredGpaSpinner;

    @FXML private Label statusLabel;

    private ProjectViewModel viewModel;

    @FXML
    public void initialize() {
        viewModel = new ProjectViewModel();

        setupTable();
        setupForm();
        setupBindings();
    }

    /**
     * Sets up the project table columns and data.
     */
    private void setupTable() {
        // Configure columns
        colCode.setCellValueFactory(cellData ->
                Bindings.createStringBinding(() -> cellData.getValue().getCode()));

        colName.setCellValueFactory(cellData ->
                Bindings.createStringBinding(() -> cellData.getValue().getName()));

        colCapacity.setCellValueFactory(cellData ->
                Bindings.createStringBinding(() ->
                    cellData.getValue().getMinCapacity() + "-" + cellData.getValue().getMaxCapacity()));

        colGpa.setCellValueFactory(cellData ->
                Bindings.createObjectBinding(() -> cellData.getValue().getRequiredGpa()));
        colGpa.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double gpa, boolean empty) {
                super.updateItem(gpa, empty);
                if (empty || gpa == null) {
                    setText(null);
                } else if (gpa == 0.0) {
                    setText("-");
                } else {
                    setText(String.format("%.2f", gpa));
                }
            }
        });

        // Set data
        projectTable.setItems(viewModel.getFilteredProjects());

        // Selection listener
        projectTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> viewModel.loadProjectToForm(newVal));
    }

    /**
     * Sets up the form controls.
     */
    private void setupForm() {
        // Min Capacity Spinner (1-100)
        SpinnerValueFactory<Integer> minFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 1, 1);
        minCapacitySpinner.setValueFactory(minFactory);

        // Max Capacity Spinner (1-100)
        SpinnerValueFactory<Integer> maxFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 10, 1);
        maxCapacitySpinner.setValueFactory(maxFactory);

        // Required GPA Spinner (0.0-4.0)
        SpinnerValueFactory<Double> gpaFactory =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 4.0, 0.0, 0.1);
        requiredGpaSpinner.setValueFactory(gpaFactory);
    }

    /**
     * Sets up data bindings between view and view model.
     */
    private void setupBindings() {
        // Search field
        searchField.textProperty().bindBidirectional(viewModel.searchTextProperty());

        // Form fields
        codeField.textProperty().bindBidirectional(viewModel.codeProperty());
        nameField.textProperty().bindBidirectional(viewModel.nameProperty());
        descriptionArea.textProperty().bindBidirectional(viewModel.descriptionProperty());

        // Min Capacity binding
        minCapacitySpinner.getValueFactory().valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                viewModel.minCapacityProperty().set(newVal);
            }
        });
        viewModel.minCapacityProperty().addListener((obs, oldVal, newVal) -> {
            minCapacitySpinner.getValueFactory().setValue(newVal.intValue());
        });

        // Max Capacity binding
        maxCapacitySpinner.getValueFactory().valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                viewModel.maxCapacityProperty().set(newVal);
            }
        });
        viewModel.maxCapacityProperty().addListener((obs, oldVal, newVal) -> {
            maxCapacitySpinner.getValueFactory().setValue(newVal.intValue());
        });

        // Required GPA binding
        requiredGpaSpinner.getValueFactory().valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                viewModel.requiredGpaProperty().set(newVal);
            }
        });
        viewModel.requiredGpaProperty().addListener((obs, oldVal, newVal) -> {
            requiredGpaSpinner.getValueFactory().setValue(newVal.doubleValue());
        });

        // Status message
        statusLabel.textProperty().bind(viewModel.statusMessageProperty());

        // Edit mode bindings
        viewModel.editModeProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                formTitle.setText("Edit Project");
                saveButton.setText("Update Project");
                deleteButton.setVisible(true);
                codeField.setEditable(false);
                codeField.setStyle("-fx-background-color: #f0f0f0;");
            } else {
                formTitle.setText("New Project");
                saveButton.setText("Save Project");
                deleteButton.setVisible(false);
                codeField.setEditable(true);
                codeField.setStyle("");
            }
        });

        // Count and capacity labels
        viewModel.getFilteredProjects().addListener(
                (javafx.collections.ListChangeListener<Project>) c -> updateLabels());
        viewModel.totalCapacityProperty().addListener((obs, oldVal, newVal) -> updateLabels());
        updateLabels();
    }

    /**
     * Updates the project count and capacity labels.
     */
    private void updateLabels() {
        int filtered = viewModel.getFilteredProjects().size();
        int total = viewModel.getProjects().size();
        if (filtered == total) {
            countLabel.setText(total + " projects");
        } else {
            countLabel.setText(filtered + " of " + total + " projects");
        }

        int totalCap = viewModel.totalCapacityProperty().get();
        int minReq = viewModel.totalMinRequiredProperty().get();
        capacityLabel.setText("Total capacity: " + minReq + "-" + totalCap + " students");
    }

    // ==================== Action Handlers ====================

    @FXML
    public void newProject() {
        viewModel.clearForm();
        projectTable.getSelectionModel().clearSelection();
        codeField.requestFocus();
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
        projectTable.getSelectionModel().clearSelection();
    }

    @FXML
    public void saveProject() {
        if (viewModel.save()) {
            projectTable.getSelectionModel().clearSelection();
        }
    }

    @FXML
    public void deleteProject() {
        Project selected = projectTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        // Confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Project");
        alert.setHeaderText("Delete " + selected.getName() + "?");
        alert.setContentText("This action cannot be undone. All student preferences for this project will also be affected.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            viewModel.deleteSelected();
        }
    }
}
