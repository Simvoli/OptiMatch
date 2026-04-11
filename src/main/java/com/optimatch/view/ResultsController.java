package com.optimatch.view;

import com.optimatch.model.AlgorithmRun;
import com.optimatch.model.GenerationStats;
import com.optimatch.service.ReportService.ProjectAssignmentDetail;
import com.optimatch.service.ReportService.StudentAssignmentDetail;
import com.optimatch.viewmodel.ResultsViewModel;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;

/**
 * Controller for the Results display screen.
 */
public class ResultsController {

    // Run selector
    @FXML private ComboBox<AlgorithmRun> runSelector;
    @FXML private Button deleteRunButton;

    // Summary labels
    @FXML private Label totalStudentsLabel;
    @FXML private Label totalProjectsLabel;
    @FXML private Label bestFitnessLabel;
    @FXML private Label satisfactionLabel;

    // Distribution labels
    @FXML private Label firstChoiceLabel;
    @FXML private Label secondChoiceLabel;
    @FXML private Label thirdChoiceLabel;
    @FXML private Label fourthChoiceLabel;
    @FXML private Label fifthChoiceLabel;
    @FXML private Label unmatchedLabel;

    // Student assignments table
    @FXML private TableView<StudentAssignmentDetail> studentTable;
    @FXML private TableColumn<StudentAssignmentDetail, String> colStudentId;
    @FXML private TableColumn<StudentAssignmentDetail, String> colStudentName;
    @FXML private TableColumn<StudentAssignmentDetail, String> colAssignedProject;
    @FXML private TableColumn<StudentAssignmentDetail, String> colPreferenceRank;
    @FXML private TableColumn<StudentAssignmentDetail, Integer> colSatisfaction;

    // Project assignments table
    @FXML private TableView<ProjectAssignmentDetail> projectTable;
    @FXML private TableColumn<ProjectAssignmentDetail, String> colProjectCode;
    @FXML private TableColumn<ProjectAssignmentDetail, String> colProjectName;
    @FXML private TableColumn<ProjectAssignmentDetail, String> colCapacity;
    @FXML private TableColumn<ProjectAssignmentDetail, Integer> colAssignedCount;
    @FXML private TableColumn<ProjectAssignmentDetail, String> colStatus;

    // Generations table
    @FXML private TableView<GenerationStats> generationsTable;
    @FXML private TableColumn<GenerationStats, Integer> colGeneration;
    @FXML private TableColumn<GenerationStats, Double> colGenBestFitness;
    @FXML private TableColumn<GenerationStats, Double> colGenAvgFitness;
    @FXML private TableColumn<GenerationStats, Double> colGenWorstFitness;
    @FXML private TableColumn<GenerationStats, Double> colGenStdDev;
    @FXML private TableColumn<GenerationStats, Integer> colGenValidCount;
    @FXML private TableColumn<GenerationStats, Double> colGenBestEver;

    // Status
    @FXML private Label statusLabel;

    private ResultsViewModel viewModel;

    @FXML
    public void initialize() {
        viewModel = new ResultsViewModel();

        setupRunSelector();
        setupStudentTable();
        setupProjectTable();
        setupGenerationsTable();
        setupBindings();
    }

    /**
     * Sets up the run selector combo box.
     */
    private void setupRunSelector() {
        runSelector.setItems(viewModel.getAvailableRuns());
        runSelector.setConverter(new StringConverter<>() {
            @Override
            public String toString(AlgorithmRun run) {
                return ResultsViewModel.formatRunForDisplay(run);
            }

            @Override
            public AlgorithmRun fromString(String string) {
                return null;
            }
        });

        runSelector.valueProperty().bindBidirectional(viewModel.selectedRunProperty());
    }

    /**
     * Sets up the student assignments table.
     */
    private void setupStudentTable() {
        colStudentId.setCellValueFactory(cellData ->
                Bindings.createStringBinding(() -> cellData.getValue().getStudent().getStudentId()));

        colStudentName.setCellValueFactory(cellData ->
                Bindings.createStringBinding(() -> cellData.getValue().getStudent().getName()));

        colAssignedProject.setCellValueFactory(cellData ->
                Bindings.createStringBinding(() ->
                        cellData.getValue().getAssignedProject().getCode() + " - " +
                                cellData.getValue().getAssignedProject().getName()));

        colPreferenceRank.setCellValueFactory(cellData ->
                Bindings.createStringBinding(() -> {
                    Integer rank = cellData.getValue().getPreferenceRank();
                    return rank != null ? "#" + rank : "N/A";
                }));
        colPreferenceRank.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("N/A".equals(item)) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else if ("#1".equals(item)) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        colSatisfaction.setCellValueFactory(cellData ->
                Bindings.createObjectBinding(() -> cellData.getValue().getSatisfactionScore()));

        studentTable.setItems(viewModel.getStudentAssignments());
    }

    /**
     * Sets up the project assignments table.
     */
    private void setupProjectTable() {
        colProjectCode.setCellValueFactory(cellData ->
                Bindings.createStringBinding(() -> cellData.getValue().getProject().getCode()));

        colProjectName.setCellValueFactory(cellData ->
                Bindings.createStringBinding(() -> cellData.getValue().getProject().getName()));

        colCapacity.setCellValueFactory(cellData ->
                Bindings.createStringBinding(() ->
                        cellData.getValue().getProject().getMinCapacity() + "-" +
                                cellData.getValue().getProject().getMaxCapacity()));

        colAssignedCount.setCellValueFactory(cellData ->
                Bindings.createObjectBinding(() -> cellData.getValue().getAssignedCount()));

        colStatus.setCellValueFactory(cellData ->
                Bindings.createStringBinding(() -> {
                    ProjectAssignmentDetail detail = cellData.getValue();
                    if (detail.isValid()) {
                        return "OK";
                    } else if (!detail.isMeetsMinCapacity()) {
                        return "Under";
                    } else {
                        return "Over";
                    }
                }));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "OK" -> setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        case "Under" -> setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        case "Over" -> setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                        default -> setStyle("");
                    }
                }
            }
        });

        projectTable.setItems(viewModel.getProjectAssignments());
    }

    /**
     * Sets up the generations statistics table.
     */
    private void setupGenerationsTable() {
        colGeneration.setCellValueFactory(cellData ->
                Bindings.createObjectBinding(() -> cellData.getValue().getGeneration()));

        colGenBestFitness.setCellValueFactory(cellData ->
                Bindings.createObjectBinding(() -> cellData.getValue().getBestFitness()));
        colGenBestFitness.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });

        colGenAvgFitness.setCellValueFactory(cellData ->
                Bindings.createObjectBinding(() -> cellData.getValue().getAverageFitness()));
        colGenAvgFitness.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });

        colGenWorstFitness.setCellValueFactory(cellData ->
                Bindings.createObjectBinding(() -> cellData.getValue().getWorstFitness()));
        colGenWorstFitness.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });

        colGenStdDev.setCellValueFactory(cellData ->
                Bindings.createObjectBinding(() -> cellData.getValue().getStandardDeviation()));
        colGenStdDev.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.4f", item));
                }
            }
        });

        colGenValidCount.setCellValueFactory(cellData ->
                Bindings.createObjectBinding(() -> cellData.getValue().getValidCount()));

        colGenBestEver.setCellValueFactory(cellData ->
                Bindings.createObjectBinding(() -> cellData.getValue().getBestEverFitness()));
        colGenBestEver.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f", item));
                    setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                }
            }
        });

        generationsTable.setItems(viewModel.getGenerationStats());
    }

    /**
     * Sets up data bindings.
     */
    private void setupBindings() {
        // Summary labels
        viewModel.totalStudentsProperty().addListener((obs, oldVal, newVal) ->
                totalStudentsLabel.setText(newVal.intValue() + " students"));
        viewModel.totalProjectsProperty().addListener((obs, oldVal, newVal) ->
                totalProjectsLabel.setText(newVal.intValue() + " projects"));
        viewModel.bestFitnessProperty().addListener((obs, oldVal, newVal) ->
                bestFitnessLabel.setText(String.format("%.2f", newVal.doubleValue())));
        viewModel.satisfactionRateProperty().addListener((obs, oldVal, newVal) ->
                satisfactionLabel.setText(String.format("%.1f%%", newVal.doubleValue())));

        // Distribution labels
        viewModel.firstChoiceCountProperty().addListener((obs, oldVal, newVal) ->
                firstChoiceLabel.setText(newVal.toString()));
        viewModel.secondChoiceCountProperty().addListener((obs, oldVal, newVal) ->
                secondChoiceLabel.setText(newVal.toString()));
        viewModel.thirdChoiceCountProperty().addListener((obs, oldVal, newVal) ->
                thirdChoiceLabel.setText(newVal.toString()));
        viewModel.fourthChoiceCountProperty().addListener((obs, oldVal, newVal) ->
                fourthChoiceLabel.setText(newVal.toString()));
        viewModel.fifthChoiceCountProperty().addListener((obs, oldVal, newVal) ->
                fifthChoiceLabel.setText(newVal.toString()));
        viewModel.unmatchedCountProperty().addListener((obs, oldVal, newVal) ->
                unmatchedLabel.setText(newVal.toString()));

        // Status
        statusLabel.textProperty().bind(viewModel.statusMessageProperty());

        // Initialize labels
        totalStudentsLabel.setText(viewModel.totalStudentsProperty().get() + " students");
        totalProjectsLabel.setText(viewModel.totalProjectsProperty().get() + " projects");
        bestFitnessLabel.setText(String.format("%.2f", viewModel.bestFitnessProperty().get()));
        satisfactionLabel.setText(String.format("%.1f%%", viewModel.satisfactionRateProperty().get()));
        firstChoiceLabel.setText(String.valueOf(viewModel.firstChoiceCountProperty().get()));
        secondChoiceLabel.setText(String.valueOf(viewModel.secondChoiceCountProperty().get()));
        thirdChoiceLabel.setText(String.valueOf(viewModel.thirdChoiceCountProperty().get()));
        fourthChoiceLabel.setText(String.valueOf(viewModel.fourthChoiceCountProperty().get()));
        fifthChoiceLabel.setText(String.valueOf(viewModel.fifthChoiceCountProperty().get()));
        unmatchedLabel.setText(String.valueOf(viewModel.unmatchedCountProperty().get()));
    }

    // ==================== Action Handlers ====================

    @FXML
    public void refresh() {
        viewModel.refresh();
    }

    @FXML
    public void deleteRun() {
        if (viewModel.selectedRunProperty().get() == null) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Run");
        alert.setHeaderText("Delete this algorithm run?");
        alert.setContentText("This will permanently delete the run and all its assignments.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            viewModel.deleteSelectedRun();
        }
    }

    @FXML
    public void exportStudentsCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Student Assignments");
        fileChooser.setInitialFileName("student_assignments.csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showSaveDialog(studentTable.getScene().getWindow());
        if (file != null) {
            viewModel.exportStudentsCsv(file);
        }
    }

    @FXML
    public void exportProjectsCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Project Assignments");
        fileChooser.setInitialFileName("project_assignments.csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showSaveDialog(projectTable.getScene().getWindow());
        if (file != null) {
            viewModel.exportProjectsCsv(file);
        }
    }

    @FXML
    public void exportFullReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Full Report");
        fileChooser.setInitialFileName("matching_report.txt");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        File file = fileChooser.showSaveDialog(studentTable.getScene().getWindow());
        if (file != null) {
            viewModel.exportFullReport(file);
        }
    }

    @FXML
    public void exportGenerationsCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Generation Statistics");
        fileChooser.setInitialFileName("generation_stats.csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showSaveDialog(generationsTable.getScene().getWindow());
        if (file != null) {
            viewModel.exportGenerationsCsv(file);
        }
    }
}
