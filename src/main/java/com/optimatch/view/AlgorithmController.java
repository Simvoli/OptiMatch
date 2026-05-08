package com.optimatch.view;

import com.optimatch.model.AlgorithmRun;
import com.optimatch.viewmodel.AlgorithmViewModel;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.format.DateTimeFormatter;

// FXML controller for the algorithm screen
public class AlgorithmController {

    // data summary labels
    @FXML private Label studentCountLabel;
    @FXML private Label projectCountLabel;
    @FXML private Label capacityLabel;
    @FXML private Label dataStatusLabel;

    // configuration controls
    @FXML private Spinner<Integer> populationSpinner;
    @FXML private Spinner<Integer> generationsSpinner;
    @FXML private Spinner<Double> mutationSpinner;
    @FXML private Spinner<Double> crossoverSpinner;
    @FXML private Spinner<Double> eliteSpinner;
    @FXML private Spinner<Integer> tournamentSpinner;
    @FXML private CheckBox convergenceCheckbox;
    @FXML private Spinner<Integer> convergenceGenSpinner;

    // execution controls
    @FXML private Button runButton;
    @FXML private Button stopButton;
    @FXML private ProgressBar progressBar;
    @FXML private Label generationLabel;
    @FXML private Label fitnessLabel;
    @FXML private Label avgFitnessLabel;
    @FXML private Label timeLabel;

    // history table
    @FXML private TableView<AlgorithmRun> historyTable;
    @FXML private TableColumn<AlgorithmRun, String> colDate;
    @FXML private TableColumn<AlgorithmRun, Integer> colGenerations;
    @FXML private TableColumn<AlgorithmRun, Double> colFitness;
    @FXML private TableColumn<AlgorithmRun, String> colTime;

    // status bar
    @FXML private Label statusLabel;

    private AlgorithmViewModel viewModel;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // FXML init
    @FXML
    public void initialize() {
        viewModel = new AlgorithmViewModel();

        setupSpinners();
        setupTable();
        setupBindings();
    }

    // configure all spinner ranges and steps
    private void setupSpinners() {
        // population (10 to 1000, step 50)
        populationSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 1000, 200, 50));

        // generations (10 to 5000, step 100)
        generationsSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 5000, 1000, 100));

        // mutation rate (0.001 to 0.5)
        mutationSpinner.setValueFactory(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0.001, 0.5, 0.02, 0.01));

        // crossover rate (0.1 to 1.0)
        crossoverSpinner.setValueFactory(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 1.0, 0.8, 0.05));

        // elite percentage (0.01 to 0.5)
        eliteSpinner.setValueFactory(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0.01, 0.5, 0.05, 0.01));

        // tournament size (2 to 10)
        tournamentSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 10, 3, 1));

        // convergence window (10 to 200)
        convergenceGenSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 200, 50, 10));
    }

    // configure history table columns
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

    // wire labels, buttons and progress bar to view model properties
    private void setupBindings() {
        // data summary
        viewModel.studentCountProperty().addListener((obs, oldVal, newVal) ->
                studentCountLabel.setText(newVal.intValue() + " students"));
        viewModel.projectCountProperty().addListener((obs, oldVal, newVal) ->
                projectCountLabel.setText(newVal.intValue() + " projects"));
        viewModel.totalCapacityProperty().addListener((obs, oldVal, newVal) ->
                capacityLabel.setText("Capacity: " + viewModel.minRequiredProperty().get() +
                        "-" + newVal.intValue() + " slots"));

        // recompute data status whenever the inputs change
        updateDataStatus();
        viewModel.studentCountProperty().addListener((obs, oldVal, newVal) -> updateDataStatus());
        viewModel.projectCountProperty().addListener((obs, oldVal, newVal) -> updateDataStatus());
        viewModel.totalCapacityProperty().addListener((obs, oldVal, newVal) -> updateDataStatus());

        // configuration spinners
        bindSpinnerToProperty(populationSpinner, viewModel.populationSizeProperty());
        bindSpinnerToProperty(generationsSpinner, viewModel.maxGenerationsProperty());
        bindDoubleSpinnerToProperty(mutationSpinner, viewModel.mutationRateProperty());
        bindDoubleSpinnerToProperty(crossoverSpinner, viewModel.crossoverRateProperty());
        bindDoubleSpinnerToProperty(eliteSpinner, viewModel.elitePercentageProperty());
        bindSpinnerToProperty(tournamentSpinner, viewModel.tournamentSizeProperty());
        bindSpinnerToProperty(convergenceGenSpinner, viewModel.convergenceGenerationsProperty());

        convergenceCheckbox.selectedProperty().bindBidirectional(viewModel.convergenceEnabledProperty());

        // disable convergence spinner when a run is active or the checkbox is off
        convergenceGenSpinner.disableProperty().bind(
                viewModel.runningProperty().or(convergenceCheckbox.selectedProperty().not()));

        // run/stop buttons
        runButton.disableProperty().bind(viewModel.runningProperty());
        stopButton.disableProperty().bind(viewModel.runningProperty().not());

        // disable config controls during a run
        populationSpinner.disableProperty().bind(viewModel.runningProperty());
        generationsSpinner.disableProperty().bind(viewModel.runningProperty());
        mutationSpinner.disableProperty().bind(viewModel.runningProperty());
        crossoverSpinner.disableProperty().bind(viewModel.runningProperty());
        eliteSpinner.disableProperty().bind(viewModel.runningProperty());
        tournamentSpinner.disableProperty().bind(viewModel.runningProperty());
        convergenceCheckbox.disableProperty().bind(viewModel.runningProperty());

        // live progress
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

        statusLabel.textProperty().bind(viewModel.statusMessageProperty());

        // initial label values
        studentCountLabel.setText(viewModel.studentCountProperty().get() + " students");
        projectCountLabel.setText(viewModel.projectCountProperty().get() + " projects");
        capacityLabel.setText("Capacity: " + viewModel.minRequiredProperty().get() +
                "-" + viewModel.totalCapacityProperty().get() + " slots");
        generationLabel.setText("Generation: 0 / " + viewModel.maxGenerationsProperty().get());
        fitnessLabel.setText("Best Fitness: 0.00");
        avgFitnessLabel.setText("Avg Fitness: 0.00");
        timeLabel.setText("Time: 0.0s");
    }

    // colour-coded ready/not-ready label
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

    // two-way binding for an integer spinner
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

    // two-way binding for a double spinner
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

    // start a GA run
    @FXML
    public void runAlgorithm() {
        viewModel.runAlgorithm();
    }

    // stop the running GA
    @FXML
    public void stopAlgorithm() {
        viewModel.stopAlgorithm();
    }

    // apply small dataset preset
    @FXML
    public void applySmallPreset() {
        viewModel.applyPreset("small");
    }

    // apply medium dataset preset
    @FXML
    public void applyMediumPreset() {
        viewModel.applyPreset("medium");
    }

    // apply large dataset preset
    @FXML
    public void applyLargePreset() {
        viewModel.applyPreset("large");
    }

    // apply quick test preset
    @FXML
    public void applyQuickPreset() {
        viewModel.applyPreset("quick");
    }

    // apply high quality preset
    @FXML
    public void applyHighQualityPreset() {
        viewModel.applyPreset("highquality");
    }

    // reload data and history
    @FXML
    public void refresh() {
        viewModel.refresh();
    }

    // delete the selected past run after confirmation
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
