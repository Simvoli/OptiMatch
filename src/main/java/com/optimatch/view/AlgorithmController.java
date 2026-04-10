package com.optimatch.view;

import com.optimatch.model.AlgorithmRun;
import com.optimatch.viewmodel.AlgorithmViewModel;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.format.DateTimeFormatter;

/**
 * Controller for the Algorithm configuration and execution screen.
 */
public class AlgorithmController {

    // Data summary labels
    @FXML private Label studentCountLabel;
    @FXML private Label projectCountLabel;
    @FXML private Label capacityLabel;
    @FXML private Label dataStatusLabel;

    // Configuration controls
    @FXML private Spinner<Integer> populationSpinner;
    @FXML private Spinner<Integer> generationsSpinner;
    @FXML private Spinner<Double> mutationSpinner;
    @FXML private Spinner<Double> crossoverSpinner;
    @FXML private Spinner<Double> eliteSpinner;
    @FXML private Spinner<Integer> tournamentSpinner;
    @FXML private CheckBox convergenceCheckbox;
    @FXML private Spinner<Integer> convergenceGenSpinner;

    // Execution controls
    @FXML private Button runButton;
    @FXML private Button stopButton;
    @FXML private ProgressBar progressBar;
    @FXML private Label generationLabel;
    @FXML private Label fitnessLabel;
    @FXML private Label avgFitnessLabel;
    @FXML private Label timeLabel;

    // History table
    @FXML private TableView<AlgorithmRun> historyTable;
    @FXML private TableColumn<AlgorithmRun, String> colDate;
    @FXML private TableColumn<AlgorithmRun, Integer> colGenerations;
    @FXML private TableColumn<AlgorithmRun, Double> colFitness;
    @FXML private TableColumn<AlgorithmRun, String> colTime;

    // Status
    @FXML private Label statusLabel;

    private AlgorithmViewModel viewModel;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        viewModel = new AlgorithmViewModel();

        setupSpinners();
        setupTable();
        setupBindings();
    }

    /**
     * Sets up the spinner controls.
     */
    private void setupSpinners() {
        // Population size (10-1000)
        populationSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 1000, 200, 50));

        // Max generations (10-5000)
        generationsSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 5000, 1000, 100));

        // Mutation rate (0.001-0.5)
        mutationSpinner.setValueFactory(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0.001, 0.5, 0.02, 0.01));

        // Crossover rate (0.1-1.0)
        crossoverSpinner.setValueFactory(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 1.0, 0.8, 0.05));

        // Elite percentage (0.01-0.5)
        eliteSpinner.setValueFactory(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0.01, 0.5, 0.05, 0.01));

        // Tournament size (2-10)
        tournamentSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 10, 3, 1));

        // Convergence generations (10-200)
        convergenceGenSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 200, 50, 10));
    }

    /**
     * Sets up the history table.
     */
    private void setupTable() {
        colDate.setCellValueFactory(cellData ->
                Bindings.createStringBinding(() ->
                        cellData.getValue().getRunTimestamp().format(DATE_FORMAT)));

        colGenerations.setCellValueFactory(cellData ->
                Bindings.createObjectBinding(() -> cellData.getValue().getGenerations()));

        colFitness.setCellValueFactory(cellData ->
                Bindings.createObjectBinding(() -> cellData.getValue().getBestFitness()));
        colFitness.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double fitness, boolean empty) {
                super.updateItem(fitness, empty);
                if (empty || fitness == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", fitness));
                }
            }
        });

        colTime.setCellValueFactory(cellData ->
                Bindings.createStringBinding(() ->
                        AlgorithmViewModel.formatElapsedTime(cellData.getValue().getExecutionTimeMs())));

        historyTable.setItems(viewModel.getRunHistory());
    }

    /**
     * Sets up data bindings.
     */
    private void setupBindings() {
        // Data summary
        viewModel.studentCountProperty().addListener((obs, oldVal, newVal) ->
                studentCountLabel.setText(newVal.intValue() + " students"));
        viewModel.projectCountProperty().addListener((obs, oldVal, newVal) ->
                projectCountLabel.setText(newVal.intValue() + " projects"));
        viewModel.totalCapacityProperty().addListener((obs, oldVal, newVal) ->
                capacityLabel.setText("Capacity: " + viewModel.minRequiredProperty().get() +
                        "-" + newVal.intValue() + " slots"));

        // Update data status
        updateDataStatus();
        viewModel.studentCountProperty().addListener((obs, oldVal, newVal) -> updateDataStatus());
        viewModel.projectCountProperty().addListener((obs, oldVal, newVal) -> updateDataStatus());
        viewModel.totalCapacityProperty().addListener((obs, oldVal, newVal) -> updateDataStatus());

        // Configuration spinners
        bindSpinnerToProperty(populationSpinner, viewModel.populationSizeProperty());
        bindSpinnerToProperty(generationsSpinner, viewModel.maxGenerationsProperty());
        bindDoubleSpinnerToProperty(mutationSpinner, viewModel.mutationRateProperty());
        bindDoubleSpinnerToProperty(crossoverSpinner, viewModel.crossoverRateProperty());
        bindDoubleSpinnerToProperty(eliteSpinner, viewModel.elitePercentageProperty());
        bindSpinnerToProperty(tournamentSpinner, viewModel.tournamentSizeProperty());
        bindSpinnerToProperty(convergenceGenSpinner, viewModel.convergenceGenerationsProperty());

        convergenceCheckbox.selectedProperty().bindBidirectional(viewModel.convergenceEnabledProperty());

        // Bind convergenceGenSpinner disable to: running OR checkbox not selected
        convergenceGenSpinner.disableProperty().bind(
                viewModel.runningProperty().or(convergenceCheckbox.selectedProperty().not()));

        // Execution state - bind buttons and config controls to running property
        runButton.disableProperty().bind(viewModel.runningProperty());
        stopButton.disableProperty().bind(viewModel.runningProperty().not());

        // Bind all config controls to running property
        populationSpinner.disableProperty().bind(viewModel.runningProperty());
        generationsSpinner.disableProperty().bind(viewModel.runningProperty());
        mutationSpinner.disableProperty().bind(viewModel.runningProperty());
        crossoverSpinner.disableProperty().bind(viewModel.runningProperty());
        eliteSpinner.disableProperty().bind(viewModel.runningProperty());
        tournamentSpinner.disableProperty().bind(viewModel.runningProperty());
        convergenceCheckbox.disableProperty().bind(viewModel.runningProperty());

        // Progress
        progressBar.progressProperty().bind(viewModel.progressProperty());
        viewModel.currentGenerationProperty().addListener((obs, oldVal, newVal) ->
                generationLabel.setText("Generation: " + newVal.intValue() + " / " +
                        viewModel.maxGenerationsProperty().get()));
        viewModel.bestFitnessProperty().addListener((obs, oldVal, newVal) ->
                fitnessLabel.setText(String.format("Best Fitness: %.2f", newVal.doubleValue())));
        viewModel.averageFitnessProperty().addListener((obs, oldVal, newVal) ->
                avgFitnessLabel.setText(String.format("Avg Fitness: %.2f", newVal.doubleValue())));
        viewModel.elapsedTimeMsProperty().addListener((obs, oldVal, newVal) ->
                timeLabel.setText("Time: " + AlgorithmViewModel.formatElapsedTime(newVal.longValue())));

        // Status
        statusLabel.textProperty().bind(viewModel.statusMessageProperty());

        // Initialize labels
        studentCountLabel.setText(viewModel.studentCountProperty().get() + " students");
        projectCountLabel.setText(viewModel.projectCountProperty().get() + " projects");
        capacityLabel.setText("Capacity: " + viewModel.minRequiredProperty().get() +
                "-" + viewModel.totalCapacityProperty().get() + " slots");
        generationLabel.setText("Generation: 0 / " + viewModel.maxGenerationsProperty().get());
        fitnessLabel.setText("Best Fitness: 0.00");
        avgFitnessLabel.setText("Avg Fitness: 0.00");
        timeLabel.setText("Time: 0.0s");
    }

    /**
     * Updates the data status indicator.
     */
    private void updateDataStatus() {
        int students = viewModel.studentCountProperty().get();
        int projects = viewModel.projectCountProperty().get();
        int capacity = viewModel.totalCapacityProperty().get();

        if (students == 0 || projects == 0) {
            dataStatusLabel.setText("Add students and projects first");
            dataStatusLabel.setStyle("-fx-text-fill: #e74c3c;");
        } else if (students > capacity) {
            dataStatusLabel.setText("Not enough capacity!");
            dataStatusLabel.setStyle("-fx-text-fill: #e74c3c;");
        } else {
            dataStatusLabel.setText("Ready to run");
            dataStatusLabel.setStyle("-fx-text-fill: #27ae60;");
        }
    }

    /**
     * Binds an integer spinner to an integer property.
     */
    private void bindSpinnerToProperty(Spinner<Integer> spinner, javafx.beans.property.IntegerProperty property) {
        spinner.getValueFactory().valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                property.set(newVal);
            }
        });
        property.addListener((obs, oldVal, newVal) ->
                spinner.getValueFactory().setValue(newVal.intValue()));
        spinner.getValueFactory().setValue(property.get());
    }

    /**
     * Binds a double spinner to a double property.
     */
    private void bindDoubleSpinnerToProperty(Spinner<Double> spinner, javafx.beans.property.DoubleProperty property) {
        spinner.getValueFactory().valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                property.set(newVal);
            }
        });
        property.addListener((obs, oldVal, newVal) ->
                spinner.getValueFactory().setValue(newVal.doubleValue()));
        spinner.getValueFactory().setValue(property.get());
    }

    // ==================== Action Handlers ====================

    @FXML
    public void runAlgorithm() {
        viewModel.runAlgorithm();
    }

    @FXML
    public void stopAlgorithm() {
        viewModel.stopAlgorithm();
    }

    @FXML
    public void applySmallPreset() {
        viewModel.applyPreset("small");
    }

    @FXML
    public void applyMediumPreset() {
        viewModel.applyPreset("medium");
    }

    @FXML
    public void applyLargePreset() {
        viewModel.applyPreset("large");
    }

    @FXML
    public void applyQuickPreset() {
        viewModel.applyPreset("quick");
    }

    @FXML
    public void applyHighQualityPreset() {
        viewModel.applyPreset("highquality");
    }

    @FXML
    public void refresh() {
        viewModel.refresh();
    }

    @FXML
    public void deleteSelectedRun() {
        AlgorithmRun selected = historyTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Run");
        alert.setHeaderText("Delete this algorithm run?");
        alert.setContentText("This will also delete all associated assignments.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            viewModel.deleteRun(selected);
        }
    }
}
