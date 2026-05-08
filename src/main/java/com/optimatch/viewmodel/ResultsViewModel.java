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

// view model for the results screen: pick a run, show its data, export to file
public class ResultsViewModel {

    private final MatchingService matchingService;
    private final ReportService reportService;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // available runs and currently chosen one
    private final ObservableList<AlgorithmRun> availableRuns = FXCollections.observableArrayList();
    private final ObjectProperty<AlgorithmRun> selectedRun = new SimpleObjectProperty<>();

    // top-line summary numbers
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

    // table data
    private final ObservableList<StudentAssignmentDetail> studentAssignments = FXCollections.observableArrayList();
    private final ObservableList<ProjectAssignmentDetail> projectAssignments = FXCollections.observableArrayList();
    private final ObservableList<GenerationStats> generationStats = FXCollections.observableArrayList();

    // cached for the export buttons
    private MatchingSummary currentSummary;

    // status bar message
    private final StringProperty statusMessage = new SimpleStringProperty("");

    // wires up services and loads initial data
    public ResultsViewModel() {
        this.matchingService = new MatchingService();
        this.reportService = new ReportService();

        // reload right side whenever the chosen run changes
        selectedRun.addListener((obs, oldVal, newVal) -> loadRunData(newVal));

        refresh();
    }

    // reload run list and auto-select the newest run
    public void refresh() {
        try {
            availableRuns.setAll(matchingService.getAllRuns());

            if (!availableRuns.isEmpty()) {
                // RU: DAO уже сортирует по run_timestamp DESC, так что первый - последний
                selectedRun.set(availableRuns.get(0));
                statusMessage.set("Loaded " + availableRuns.size() + " runs");
            } else {
                clearData();
                statusMessage.set("No algorithm runs found. Run the algorithm first.");
            }
        } catch (ServiceException e) {
            statusMessage.set("Error loading runs: " + e.getMessage());
        }
    }

    // populate all detail panels for a chosen run
    private void loadRunData(AlgorithmRun run) {
        if (run == null) {
            clearData();
            return;
        }

        try {
            currentSummary = reportService.generateSummary(run.getId());

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

            studentAssignments.setAll(reportService.generateStudentReport(run.getId()));
            projectAssignments.setAll(reportService.generateProjectReport(run.getId()));

            generationStats.setAll(matchingService.getGenerationStatsForRun(run.getId()));

            statusMessage.set("Showing results for run from " + run.getRunTimestamp().format(DATE_FORMAT));

        } catch (ServiceException e) {
            statusMessage.set("Error loading run data: " + e.getMessage());
            clearData();
        }
    }

    // wipe all detail panels
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
    }

    // export per-student CSV
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

    // export per-project CSV
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

    // export the full text report
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

    // export per-generation CSV
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

    // delete the chosen run
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

    // pretty label for a run, used by the dropdown
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

    // available runs
    public ObservableList<AlgorithmRun> getAvailableRuns() {
        return availableRuns;
    }

    // currently selected run
    public ObjectProperty<AlgorithmRun> selectedRunProperty() {
        return selectedRun;
    }

    // total students
    public IntegerProperty totalStudentsProperty() {
        return totalStudents;
    }

    // total projects
    public IntegerProperty totalProjectsProperty() {
        return totalProjects;
    }

    // best fitness
    public DoubleProperty bestFitnessProperty() {
        return bestFitness;
    }

    // satisfaction percentage
    public DoubleProperty satisfactionRateProperty() {
        return satisfactionRate;
    }

    // count of first-choice assignments
    public IntegerProperty firstChoiceCountProperty() {
        return firstChoiceCount;
    }

    // count of second-choice assignments
    public IntegerProperty secondChoiceCountProperty() {
        return secondChoiceCount;
    }

    // count of third-choice assignments
    public IntegerProperty thirdChoiceCountProperty() {
        return thirdChoiceCount;
    }

    // count of fourth-choice assignments
    public IntegerProperty fourthChoiceCountProperty() {
        return fourthChoiceCount;
    }

    // count of fifth-choice assignments
    public IntegerProperty fifthChoiceCountProperty() {
        return fifthChoiceCount;
    }

    // count of unmatched students
    public IntegerProperty unmatchedCountProperty() {
        return unmatchedCount;
    }

    // student assignment rows
    public ObservableList<StudentAssignmentDetail> getStudentAssignments() {
        return studentAssignments;
    }

    // project assignment rows
    public ObservableList<ProjectAssignmentDetail> getProjectAssignments() {
        return projectAssignments;
    }

    // generation stats rows
    public ObservableList<GenerationStats> getGenerationStats() {
        return generationStats;
    }

    // status message
    public StringProperty statusMessageProperty() {
        return statusMessage;
    }
}
