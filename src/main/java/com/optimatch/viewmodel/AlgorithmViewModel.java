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
import com.optimatch.util.AppLifecycle;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.concurrent.ExecutorService;

// view model for the algorithm screen: parameters, run/stop, live progress, history
public class AlgorithmViewModel {

    private final MatchingService matchingService;
    private final StudentService studentService;
    private final ProjectService projectService;

    // RU: один общий executor на все ViewModel-ы, останавливается при выходе из приложения
    private final ExecutorService executor = AppLifecycle.getBackgroundExecutor();

    // data summary at the top of the screen
    private final IntegerProperty studentCount = new SimpleIntegerProperty(0);
    private final IntegerProperty projectCount = new SimpleIntegerProperty(0);
    private final IntegerProperty totalCapacity = new SimpleIntegerProperty(0);
    private final IntegerProperty minRequired = new SimpleIntegerProperty(0);

    // configuration sliders/spinners
    private final IntegerProperty populationSize = new SimpleIntegerProperty(200);
    private final IntegerProperty maxGenerations = new SimpleIntegerProperty(1000);
    private final DoubleProperty mutationRate = new SimpleDoubleProperty(0.02);
    private final DoubleProperty crossoverRate = new SimpleDoubleProperty(0.80);
    private final DoubleProperty elitePercentage = new SimpleDoubleProperty(0.05);
    private final IntegerProperty tournamentSize = new SimpleIntegerProperty(3);
    private final BooleanProperty convergenceEnabled = new SimpleBooleanProperty(true);
    private final IntegerProperty convergenceGenerations = new SimpleIntegerProperty(50);

    // execution state shown during a run
    private final BooleanProperty running = new SimpleBooleanProperty(false);
    private final IntegerProperty currentGeneration = new SimpleIntegerProperty(0);
    private final DoubleProperty bestFitness = new SimpleDoubleProperty(0);
    private final DoubleProperty averageFitness = new SimpleDoubleProperty(0);
    private final DoubleProperty progress = new SimpleDoubleProperty(0);
    private final LongProperty elapsedTimeMs = new SimpleLongProperty(0);

    // past runs shown in the history table
    private final ObservableList<AlgorithmRun> runHistory = FXCollections.observableArrayList();

    // status bar message
    private final StringProperty statusMessage = new SimpleStringProperty("");

    private long startTime;

    // wires up services and loads initial data
    public AlgorithmViewModel() {
        this.matchingService = new MatchingService();
        this.studentService = new StudentService();
        this.projectService = new ProjectService();

        refresh();
    }

    // reload data summary and run history
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

    // copy values from a named preset into the form
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

    // assemble a config from the current form values
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

    // start the GA on the background executor
    public void runAlgorithm() {
        if (running.get()) {
            return;
        }

        // pre-flight checks
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

        // reset progress state
        running.set(true);
        currentGeneration.set(0);
        bestFitness.set(0);
        averageFitness.set(0);
        progress.set(0);
        elapsedTimeMs.set(0);
        startTime = System.currentTimeMillis();

        statusMessage.set("Running algorithm...");

        matchingService.setProgressCallback(this::onGenerationComplete);

        // run on background thread, post UI updates back to FX thread
        executor.submit(() -> {
            try {
                GeneticAlgorithmConfig config = buildConfig();
                AlgorithmRun run = matchingService.runMatching(config);

                Platform.runLater(() -> {
                    running.set(false);
                    refresh();
                    statusMessage.set(String.format(
                            "Completed! Fitness: %.2f, Generations: %d, Time: %.2fs",
                            run.getBestFitness(),
                            run.getGenerations(),
                            run.getExecutionTimeMs() / 1000.0
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

    // request a graceful stop
    public void stopAlgorithm() {
        if (running.get()) {
            matchingService.stopMatching();
            statusMessage.set("Stopping algorithm...");
        }
    }

    // per-generation callback: snapshot values on the GA thread, then push to FX thread
    private void onGenerationComplete(int generation, Population population, Chromosome bestEver) {
        // RU: считаем avg fitness прямо здесь, в потоке GA, чтобы не дёргать population из UI потока
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

    // remove a run from history
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

    // format ms as either "M:SS" or "X.Xs"
    public static String formatElapsedTime(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        if (minutes > 0) {
            return String.format("%d:%02d", minutes, seconds);
        }
        return String.format("%.1fs", ms / 1000.0);
    }

    // ==================== Property Getters ====================

    // student count
    public IntegerProperty studentCountProperty() {
        return studentCount;
    }

    // project count
    public IntegerProperty projectCountProperty() {
        return projectCount;
    }

    // total max capacity
    public IntegerProperty totalCapacityProperty() {
        return totalCapacity;
    }

    // total min capacity
    public IntegerProperty minRequiredProperty() {
        return minRequired;
    }

    // population size field
    public IntegerProperty populationSizeProperty() {
        return populationSize;
    }

    // max generations field
    public IntegerProperty maxGenerationsProperty() {
        return maxGenerations;
    }

    // mutation rate field
    public DoubleProperty mutationRateProperty() {
        return mutationRate;
    }

    // crossover rate field
    public DoubleProperty crossoverRateProperty() {
        return crossoverRate;
    }

    // elite percentage field
    public DoubleProperty elitePercentageProperty() {
        return elitePercentage;
    }

    // tournament size field
    public IntegerProperty tournamentSizeProperty() {
        return tournamentSize;
    }

    // convergence toggle
    public BooleanProperty convergenceEnabledProperty() {
        return convergenceEnabled;
    }

    // convergence window field
    public IntegerProperty convergenceGenerationsProperty() {
        return convergenceGenerations;
    }

    // running flag
    public BooleanProperty runningProperty() {
        return running;
    }

    // current generation number
    public IntegerProperty currentGenerationProperty() {
        return currentGeneration;
    }

    // current best fitness
    public DoubleProperty bestFitnessProperty() {
        return bestFitness;
    }

    // current average fitness
    public DoubleProperty averageFitnessProperty() {
        return averageFitness;
    }

    // progress in [0, 1]
    public DoubleProperty progressProperty() {
        return progress;
    }

    // elapsed time in ms
    public LongProperty elapsedTimeMsProperty() {
        return elapsedTimeMs;
    }

    // history of past runs
    public ObservableList<AlgorithmRun> getRunHistory() {
        return runHistory;
    }

    // status message
    public StringProperty statusMessageProperty() {
        return statusMessage;
    }
}
