package com.optimatch.view;

import com.optimatch.model.Project;
import com.optimatch.viewmodel.ProjectViewModel;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;

// FXML controller for the projects screen
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

    // FXML init
    @FXML
    public void initialize() {
        viewModel = new ProjectViewModel();

        setupTable();
        setupForm();
        setupBindings();
    }

    // configure table columns and selection listener
    private void setupTable() {
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

        projectTable.setItems(viewModel.getFilteredProjects());

        projectTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> viewModel.loadProjectToForm(newVal));
    }

    // configure spinners
    private void setupForm() {
        SpinnerValueFactory<Integer> minFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 1, 1);
        minCapacitySpinner.setValueFactory(minFactory);

        SpinnerValueFactory<Integer> maxFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 10, 1);
        maxCapacitySpinner.setValueFactory(maxFactory);

        SpinnerValueFactory<Double> gpaFactory =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 4.0, 0.0, 0.1);
        requiredGpaSpinner.setValueFactory(gpaFactory);
    }

    // wire form fields to view model properties
    private void setupBindings() {
        searchField.textProperty().bindBidirectional(viewModel.searchTextProperty());

        codeField.textProperty().bindBidirectional(viewModel.codeProperty());
        nameField.textProperty().bindBidirectional(viewModel.nameProperty());
        descriptionArea.textProperty().bindBidirectional(viewModel.descriptionProperty());

        // min capacity manual two-way binding
        minCapacitySpinner.getValueFactory().valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                viewModel.minCapacityProperty().set(newVal);
            }
        });
        viewModel.minCapacityProperty().addListener((obs, oldVal, newVal) -> {
            minCapacitySpinner.getValueFactory().setValue(newVal.intValue());
        });

        // max capacity manual two-way binding
        maxCapacitySpinner.getValueFactory().valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                viewModel.maxCapacityProperty().set(newVal);
            }
        });
        viewModel.maxCapacityProperty().addListener((obs, oldVal, newVal) -> {
            maxCapacitySpinner.getValueFactory().setValue(newVal.intValue());
        });

        // required gpa manual two-way binding
        requiredGpaSpinner.getValueFactory().valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                viewModel.requiredGpaProperty().set(newVal);
            }
        });
        viewModel.requiredGpaProperty().addListener((obs, oldVal, newVal) -> {
            requiredGpaSpinner.getValueFactory().setValue(newVal.doubleValue());
        });

        statusLabel.textProperty().bind(viewModel.statusMessageProperty());

        // swap labels and buttons when entering/leaving edit mode
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

        viewModel.getFilteredProjects().addListener(
                (javafx.collections.ListChangeListener<Project>) c -> updateLabels());
        viewModel.totalCapacityProperty().addListener((obs, oldVal, newVal) -> updateLabels());
        updateLabels();
    }

    // refresh count and capacity labels
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

    // start a fresh project form
    @FXML
    public void newProject() {
        viewModel.clearForm();
        projectTable.getSelectionModel().clearSelection();
        codeField.requestFocus();
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
        projectTable.getSelectionModel().clearSelection();
    }

    // save (create or update)
    @FXML
    public void saveProject() {
        if (viewModel.save()) {
            projectTable.getSelectionModel().clearSelection();
        }
    }

    // delete the selected project after confirmation
    @FXML
    public void deleteProject() {
        Project selected = projectTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Project");
        alert.setHeaderText("Delete " + selected.getName() + "?");
        alert.setContentText("This action cannot be undone. All student preferences for this project will also be affected.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            viewModel.deleteSelected();
        }
    }
}
