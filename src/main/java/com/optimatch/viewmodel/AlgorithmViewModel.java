package com.optimatch.viewmodel;

import com.optimatch.algorithm.Chromosome;
import com.optimatch.algorithm.GeneticAlgorithm;
import com.optimatch.algorithm.GeneticAlgorithmConfig;
import com.optimatch.algorithm.Population;
import com.optimatch.model.AlgorithmRun;
import com.optimatch.service.MatchingService;
import com.optimatch.service.ProjectService;
import com.optimatch.service.ServiceException;
import com.optimatch.service.StudentService;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel for the Algorithm configuration and execution screen.
 * Handles GA parameter configuration, execution, and progress tracking.
 */
public class AlgorithmViewModel {

    private final MatchingService matchingService;
    private final StudentService studentService;
    private final ProjectService projectService;

    // Use daemon thread so it doesn't prevent app from closing
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "GA-Executor");
        t.setDaemon(true);
        return t;
    });

    // Data summary
    private final IntegerProperty studentCount = new SimpleIntegerProperty(0);
    private final IntegerProperty projectCount = new SimpleIntegerProperty(0);
    private final IntegerProperty totalCapacity = new SimpleIntegerProperty(0);
    private final IntegerProperty minRequired = new SimpleIntegerProperty(0);

    // Configuration parameters
    private final IntegerProperty populationSize = new SimpleIntegerProperty(200);
    private final IntegerProperty maxGenerations = new SimpleIntegerProperty(1000);
    private final DoubleProperty mutationRate = new SimpleDoubleProperty(0.02);
    private final DoubleProperty crossoverRate = new SimpleDoubleProperty(0.80);
    private final DoubleProperty elitePercentage = new SimpleDoubleProperty(0.05);
    private final IntegerProperty tournamentSize = new SimpleIntegerProperty(3);
    private final BooleanProperty convergenceEnabled = new SimpleBooleanProperty(true);
    private final IntegerProperty convergenceGenerations = new SimpleIntegerProperty(50);

    // Execution state
    private final BooleanProperty running = new SimpleBooleanProperty(false);
    private final IntegerProperty currentGeneration = new SimpleIntegerProperty(0);
    private final DoubleProperty bestFitness = new SimpleDoubleProperty(0);
    private final DoubleProperty averageFitness = new SimpleDoubleProperty(0);
    private final DoubleProperty progress = new SimpleDoubleProperty(0);
    private final LongProperty elapsedTimeMs = new SimpleLongProperty(0);

    // Run history
    private final ObservableList<AlgorithmRun> runHistory = FXCollections.observableArrayList();

    // Status message
    private final StringProperty statusMessage = new SimpleStringProperty("");

    // Latest result run ID (for navigation to results)
    private final IntegerProperty latestRunId = new SimpleIntegerProperty(-1);

    private long startTime;

    /**
     * Creates an AlgorithmViewModel with default services.
     */
    public AlgorithmViewModel() {
        this(new MatchingService(), new StudentService(), new ProjectService());
    }

    /**
     * Creates an AlgorithmViewModel with the specified services.
     *
     * @param matchingService the matching service
     * @param studentService  the student service
     * @param projectService  the project service
     */
    public AlgorithmViewModel(MatchingService matchingService,
                              StudentService studentService,
                              ProjectService projectService) {
        this.matchingService = matchingService;
        this.studentService = studentService;
        this.projectService = projectService;

        refresh();
    }

    /**
     * Refreshes data summary and run history.
     */
    public void refresh() {
        try {
            studentCount.set(studentService.getStudentCount());
            projectCount.set(projectService.getProjectCount());
            totalCapacity.set(projectService.getTotalCapacity());
            minRequired.set(projectService.getMinimumRequiredStudents());

            runHistory.setAll(matchingService.getAllRuns());

            statusMessage.set("Data loaded: " + studentCount.get() + " students, " +
                    projectCount.get() + " projects");
        } catch (ServiceException e) {
            statusMessage.set("Error loading data: " + e.getMessage());
        }
    }

    /**
     * Applies a preset configuration.
     *
     * @param preset the preset name ("small", "medium", "large", "quick", "highquality")
     */
    public void applyPreset(String preset) {
        GeneticAlgorithmConfig config = switch (preset.toLowerCase()) {
            case "small" -> GeneticAlgorithmConfig.forSmallDataset();
            case "medium" -> GeneticAlgorithmConfig.forMediumDataset();
            case "large" -> GeneticAlgorithmConfig.forLargeDataset();
            case "quick" -> GeneticAlgorithmConfig.forQuickTest();
            case "highquality" -> GeneticAlgorithmConfig.forHighQuality();
            default -> new GeneticAlgorithmConfig();
        };

        populationSize.set(config.getPopulationSize());
        maxGenerations.set(config.getMaxGenerations());
        mutationRate.set(config.getMutationRate());
        crossoverRate.set(config.getCrossoverRate());
        elitePercentage.set(config.getElitePercentage());
        tournamentSize.set(config.getTournamentSize());
        convergenceEnabled.set(config.isConvergenceEnabled());
        convergenceGenerations.set(config.getConvergenceGenerations());

        statusMessage.set("Applied " + preset + " preset");
    }

    /**
     * Builds a configuration from the current UI values.
     *
     * @return the configuration
     */
    public GeneticAlgorithmConfig buildConfig() {
        return new GeneticAlgorithmConfig()
                .populationSize(populationSize.get())
                .maxGenerations(maxGenerations.get())
                .mutationRate(mutationRate.get())
                .crossoverRate(crossoverRate.get())
                .elitePercentage(elitePercentage.get())
                .tournamentSize(tournamentSize.get())
                .convergenceEnabled(convergenceEnabled.get())
                .convergenceGenerations(convergenceGenerations.get());
    }

    /**
     * Starts the algorithm execution.
     */
    public void runAlgorithm() {
        if (running.get()) {
            return;
        }

        // Validate data
        if (studentCount.get() == 0) {
            statusMessage.set("Error: No students found. Add students first.");
            return;
        }
        if (projectCount.get() == 0) {
            statusMessage.set("Error: No projects found. Add projects first.");
            return;
        }
        if (studentCount.get() > totalCapacity.get()) {
            statusMessage.set("Error: Not enough capacity (" + studentCount.get() +
                    " students, " + totalCapacity.get() + " slots)");
            return;
        }

        // Reset state
        running.set(true);
        currentGeneration.set(0);
        bestFitness.set(0);
        averageFitness.set(0);
        progress.set(0);
        elapsedTimeMs.set(0);
        latestRunId.set(-1);
        startTime = System.currentTimeMillis();

        statusMessage.set("Running algorithm...");

        // Set progress callback
        matchingService.setProgressCallback(this::onGenerationComplete);

        // Run in background thread
        executor.submit(() -> {
            try {
                GeneticAlgorithmConfig config = buildConfig();
                MatchingService.MatchingResult result = matchingService.runMatching(config);

                Platform.runLater(() -> {
                    running.set(false);
                    latestRunId.set(result.getRun().getId());
                    refresh();
                    statusMessage.set(String.format(
                            "Completed! Fitness: %.2f, Generations: %d, Time: %.2fs",
                            result.getBestFitness(),
                            result.getGenerations(),
                            result.getExecutionTimeMs() / 1000.0
                    ));
                });

            } catch (ServiceException e) {
                Platform.runLater(() -> {
                    running.set(false);
                    statusMessage.set("Error: " + e.getMessage());
                });
            }
        });
    }

    /**
     * Stops the currently running algorithm.
     */
    public void stopAlgorithm() {
        if (running.get()) {
            matchingService.stopMatching();
            statusMessage.set("Stopping algorithm...");
        }
    }

    /**
     * Callback for generation completion.
     */
    private void onGenerationComplete(int generation, Population population, Chromosome bestEver) {
        // Capture values in the GA thread before passing to UI thread
        double avgFitness = population.getAverageFitness();
        double best = bestEver.getFitness();
        int maxGen = maxGenerations.get();
        long elapsed = System.currentTimeMillis() - startTime;

        Platform.runLater(() -> {
            currentGeneration.set(generation);
            bestFitness.set(best);
            averageFitness.set(avgFitness);
            progress.set((double) generation / maxGen);
            elapsedTimeMs.set(elapsed);
        });
    }

    /**
     * Deletes a run from history.
     *
     * @param run the run to delete
     * @return true if successful
     */
    public boolean deleteRun(AlgorithmRun run) {
        if (run == null) {
            return false;
        }
        try {
            matchingService.deleteRun(run.getId());
            runHistory.remove(run);
            statusMessage.set("Run deleted");
            return true;
        } catch (ServiceException e) {
            statusMessage.set("Error deleting run: " + e.getMessage());
            return false;
        }
    }

    /**
     * Formats elapsed time for display.
     *
     * @param ms milliseconds
     * @return formatted string
     */
    public static String formatElapsedTime(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        if (minutes > 0) {
            return String.format("%d:%02d", minutes, seconds);
        }
        return String.format("%.1fs", ms / 1000.0);
    }

    /**
     * Shuts down the executor service.
     */
    public void shutdown() {
        executor.shutdownNow();
    }

    // ==================== Property Getters ====================

    public IntegerProperty studentCountProperty() {
        return studentCount;
    }

    public IntegerProperty projectCountProperty() {
        return projectCount;
    }

    public IntegerProperty totalCapacityProperty() {
        return totalCapacity;
    }

    public IntegerProperty minRequiredProperty() {
        return minRequired;
    }

    public IntegerProperty populationSizeProperty() {
        return populationSize;
    }

    public IntegerProperty maxGenerationsProperty() {
        return maxGenerations;
    }

    public DoubleProperty mutationRateProperty() {
        return mutationRate;
    }

    public DoubleProperty crossoverRateProperty() {
        return crossoverRate;
    }

    public DoubleProperty elitePercentageProperty() {
        return elitePercentage;
    }

    public IntegerProperty tournamentSizeProperty() {
        return tournamentSize;
    }

    public BooleanProperty convergenceEnabledProperty() {
        return convergenceEnabled;
    }

    public IntegerProperty convergenceGenerationsProperty() {
        return convergenceGenerations;
    }

    public BooleanProperty runningProperty() {
        return running;
    }

    public IntegerProperty currentGenerationProperty() {
        return currentGeneration;
    }

    public DoubleProperty bestFitnessProperty() {
        return bestFitness;
    }

    public DoubleProperty averageFitnessProperty() {
        return averageFitness;
    }

    public DoubleProperty progressProperty() {
        return progress;
    }

    public LongProperty elapsedTimeMsProperty() {
        return elapsedTimeMs;
    }

    public ObservableList<AlgorithmRun> getRunHistory() {
        return runHistory;
    }

    public StringProperty statusMessageProperty() {
        return statusMessage;
    }

    public IntegerProperty latestRunIdProperty() {
        return latestRunId;
    }
}
