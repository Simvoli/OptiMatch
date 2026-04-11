package com.optimatch.viewmodel;

import com.optimatch.model.AlgorithmRun;
import com.optimatch.model.GenerationStats;
import com.optimatch.service.MatchingService;
import com.optimatch.service.ReportService;
import com.optimatch.service.ReportService.MatchingSummary;
import com.optimatch.service.ReportService.ProjectAssignmentDetail;
import com.optimatch.service.ReportService.StudentAssignmentDetail;
import com.optimatch.service.ServiceException;
import com.optimatch.util.ExportUtil;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * ViewModel for the Results display screen.
 * Handles loading and displaying matching results with export functionality.
 */
public class ResultsViewModel {

    private final MatchingService matchingService;
    private final ReportService reportService;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Available runs
    private final ObservableList<AlgorithmRun> availableRuns = FXCollections.observableArrayList();
    private final ObjectProperty<AlgorithmRun> selectedRun = new SimpleObjectProperty<>();

    // Summary statistics
    private final IntegerProperty totalStudents = new SimpleIntegerProperty(0);
    private final IntegerProperty totalProjects = new SimpleIntegerProperty(0);
    private final DoubleProperty bestFitness = new SimpleDoubleProperty(0);
    private final DoubleProperty satisfactionRate = new SimpleDoubleProperty(0);
    private final IntegerProperty firstChoiceCount = new SimpleIntegerProperty(0);
    private final IntegerProperty secondChoiceCount = new SimpleIntegerProperty(0);
    private final IntegerProperty thirdChoiceCount = new SimpleIntegerProperty(0);
    private final IntegerProperty fourthChoiceCount = new SimpleIntegerProperty(0);
    private final IntegerProperty fifthChoiceCount = new SimpleIntegerProperty(0);
    private final IntegerProperty unmatchedCount = new SimpleIntegerProperty(0);

    // Assignment details
    private final ObservableList<StudentAssignmentDetail> studentAssignments = FXCollections.observableArrayList();
    private final ObservableList<ProjectAssignmentDetail> projectAssignments = FXCollections.observableArrayList();
    private final ObservableList<GenerationStats> generationStats = FXCollections.observableArrayList();

    // Current summary (for export)
    private MatchingSummary currentSummary;

    // Status
    private final StringProperty statusMessage = new SimpleStringProperty("");
    private final BooleanProperty hasData = new SimpleBooleanProperty(false);

    /**
     * Creates a ResultsViewModel with default services.
     */
    public ResultsViewModel() {
        this(new MatchingService(), new ReportService());
    }

    /**
     * Creates a ResultsViewModel with the specified services.
     *
     * @param matchingService the matching service
     * @param reportService   the report service
     */
    public ResultsViewModel(MatchingService matchingService, ReportService reportService) {
        this.matchingService = matchingService;
        this.reportService = reportService;

        // Listen for run selection changes
        selectedRun.addListener((obs, oldVal, newVal) -> loadRunData(newVal));

        refresh();
    }

    /**
     * Refreshes the list of available runs.
     */
    public void refresh() {
        try {
            availableRuns.setAll(matchingService.getAllRuns());

            if (!availableRuns.isEmpty()) {
                // Select the most recent run
                AlgorithmRun latestRun = availableRuns.get(0);
                for (AlgorithmRun run : availableRuns) {
                    if (run.getRunTimestamp().isAfter(latestRun.getRunTimestamp())) {
                        latestRun = run;
                    }
                }
                selectedRun.set(latestRun);
                statusMessage.set("Loaded " + availableRuns.size() + " runs");
            } else {
                clearData();
                statusMessage.set("No algorithm runs found. Run the algorithm first.");
            }
        } catch (ServiceException e) {
            statusMessage.set("Error loading runs: " + e.getMessage());
        }
    }

    /**
     * Loads data for a specific run.
     */
    private void loadRunData(AlgorithmRun run) {
        if (run == null) {
            clearData();
            return;
        }

        try {
            // Load summary
            currentSummary = reportService.generateSummary(run.getId());

            // Update summary properties
            totalStudents.set(currentSummary.getTotalStudents());
            totalProjects.set(currentSummary.getTotalProjects());
            bestFitness.set(run.getBestFitness());
            satisfactionRate.set(currentSummary.getSatisfactionPercentage());

            int[] dist = currentSummary.getPreferenceDistribution();
            firstChoiceCount.set(dist[1]);
            secondChoiceCount.set(dist[2]);
            thirdChoiceCount.set(dist[3]);
            fourthChoiceCount.set(dist[4]);
            fifthChoiceCount.set(dist[5]);
            unmatchedCount.set(dist[0]);

            // Load detailed assignments
            studentAssignments.setAll(reportService.generateStudentReport(run.getId()));
            projectAssignments.setAll(reportService.generateProjectReport(run.getId()));

            // Load generation statistics
            generationStats.setAll(matchingService.getGenerationStatsForRun(run.getId()));

            hasData.set(true);
            statusMessage.set("Showing results for run from " + run.getRunTimestamp().format(DATE_FORMAT));

        } catch (ServiceException e) {
            statusMessage.set("Error loading run data: " + e.getMessage());
            clearData();
        }
    }

    /**
     * Clears all displayed data.
     */
    private void clearData() {
        currentSummary = null;
        totalStudents.set(0);
        totalProjects.set(0);
        bestFitness.set(0);
        satisfactionRate.set(0);
        firstChoiceCount.set(0);
        secondChoiceCount.set(0);
        thirdChoiceCount.set(0);
        fourthChoiceCount.set(0);
        fifthChoiceCount.set(0);
        unmatchedCount.set(0);
        studentAssignments.clear();
        projectAssignments.clear();
        generationStats.clear();
        hasData.set(false);
    }

    /**
     * Exports student assignments to CSV.
     *
     * @param file the output file
     * @return true if successful
     */
    public boolean exportStudentsCsv(File file) {
        if (selectedRun.get() == null || currentSummary == null) {
            statusMessage.set("No data to export");
            return false;
        }

        try {
            ExportUtil.exportStudentsCsv(file, selectedRun.get(), currentSummary, studentAssignments);
            statusMessage.set("Exported student assignments to " + file.getName());
            return true;
        } catch (IOException e) {
            statusMessage.set("Export failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Exports project assignments to CSV.
     *
     * @param file the output file
     * @return true if successful
     */
    public boolean exportProjectsCsv(File file) {
        if (selectedRun.get() == null || currentSummary == null) {
            statusMessage.set("No data to export");
            return false;
        }

        try {
            ExportUtil.exportProjectsCsv(file, selectedRun.get(), currentSummary, projectAssignments);
            statusMessage.set("Exported project assignments to " + file.getName());
            return true;
        } catch (IOException e) {
            statusMessage.set("Export failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Exports full report to text file.
     *
     * @param file the output file
     * @return true if successful
     */
    public boolean exportFullReport(File file) {
        if (selectedRun.get() == null || currentSummary == null) {
            statusMessage.set("No data to export");
            return false;
        }

        try {
            ExportUtil.exportFullReport(file, selectedRun.get(), currentSummary,
                    studentAssignments, projectAssignments);
            statusMessage.set("Exported full report to " + file.getName());
            return true;
        } catch (IOException e) {
            statusMessage.set("Export failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Exports generation statistics to CSV.
     *
     * @param file the output file
     * @return true if successful
     */
    public boolean exportGenerationsCsv(File file) {
        if (selectedRun.get() == null || generationStats.isEmpty()) {
            statusMessage.set("No generation data to export");
            return false;
        }

        try {
            ExportUtil.exportGenerationsCsv(file, selectedRun.get(), generationStats);
            statusMessage.set("Exported generation statistics to " + file.getName());
            return true;
        } catch (IOException e) {
            statusMessage.set("Export failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes the currently selected run.
     *
     * @return true if successful
     */
    public boolean deleteSelectedRun() {
        if (selectedRun.get() == null) {
            statusMessage.set("No run selected");
            return false;
        }

        try {
            matchingService.deleteRun(selectedRun.get().getId());
            statusMessage.set("Run deleted");
            refresh();
            return true;
        } catch (ServiceException e) {
            statusMessage.set("Error deleting run: " + e.getMessage());
            return false;
        }
    }

    /**
     * Formats a run for display in a ComboBox.
     *
     * @param run the algorithm run
     * @return formatted display string
     */
    public static String formatRunForDisplay(AlgorithmRun run) {
        if (run == null) {
            return "";
        }
        return String.format("%s - Fitness: %.2f (Gen: %d)",
                run.getRunTimestamp().format(DATE_FORMAT),
                run.getBestFitness(),
                run.getGenerations());
    }

    // ==================== Property Getters ====================

    public ObservableList<AlgorithmRun> getAvailableRuns() {
        return availableRuns;
    }

    public ObjectProperty<AlgorithmRun> selectedRunProperty() {
        return selectedRun;
    }

    public IntegerProperty totalStudentsProperty() {
        return totalStudents;
    }

    public IntegerProperty totalProjectsProperty() {
        return totalProjects;
    }

    public DoubleProperty bestFitnessProperty() {
        return bestFitness;
    }

    public DoubleProperty satisfactionRateProperty() {
        return satisfactionRate;
    }

    public IntegerProperty firstChoiceCountProperty() {
        return firstChoiceCount;
    }

    public IntegerProperty secondChoiceCountProperty() {
        return secondChoiceCount;
    }

    public IntegerProperty thirdChoiceCountProperty() {
        return thirdChoiceCount;
    }

    public IntegerProperty fourthChoiceCountProperty() {
        return fourthChoiceCount;
    }

    public IntegerProperty fifthChoiceCountProperty() {
        return fifthChoiceCount;
    }

    public IntegerProperty unmatchedCountProperty() {
        return unmatchedCount;
    }

    public ObservableList<StudentAssignmentDetail> getStudentAssignments() {
        return studentAssignments;
    }

    public ObservableList<ProjectAssignmentDetail> getProjectAssignments() {
        return projectAssignments;
    }

    public ObservableList<GenerationStats> getGenerationStats() {
        return generationStats;
    }

    public StringProperty statusMessageProperty() {
        return statusMessage;
    }

    public BooleanProperty hasDataProperty() {
        return hasData;
    }
}
