package com.optimatch.util;

import com.optimatch.model.AlgorithmRun;
import com.optimatch.model.GenerationStats;
import com.optimatch.service.ReportService.MatchingSummary;
import com.optimatch.service.ReportService.ProjectAssignmentDetail;
import com.optimatch.service.ReportService.StudentAssignmentDetail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

// CSV and text exports of matching results
public class ExportUtil {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String CSV_SEPARATOR = ",";
    // RU: BOM нужен Excel-у, чтобы открыть UTF-8 csv корректно
    private static final String UTF8_BOM = "﻿";
    // RU: Excel в русской локали по умолчанию использует ; как разделитель.
    // Директива sep=, заставляет его использовать запятую независимо от локали.
    // LibreOffice 7+ её тоже понимает
    private static final String EXCEL_SEPARATOR_HINT = "sep=,";

    // utility class, no instances
    private ExportUtil() {
    }

    // open utf-8 buffered writer, optionally write the BOM and Excel separator hint
    private static BufferedWriter openWriter(File file, boolean csv) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8));
        if (csv) {
            writer.write(UTF8_BOM);
            writer.write(EXCEL_SEPARATOR_HINT);
            writer.newLine();
        }
        return writer;
    }

    // CSV with one row per student
    public static void exportStudentsCsv(File file, AlgorithmRun run, MatchingSummary summary,
                                         List<StudentAssignmentDetail> details) throws IOException {
        try (BufferedWriter writer = openWriter(file, true)) {
            // metadata header
            writer.write("# OptiMatch - Student Assignment Results");
            writer.newLine();
            writer.write("# Run Date: " + run.getRunTimestamp().format(DATE_FORMAT));
            writer.newLine();
            writer.write("# Best Fitness: " + String.format("%.2f", run.getBestFitness()));
            writer.newLine();
            writer.write("# Satisfaction: " + String.format("%.1f%%", summary.getSatisfactionPercentage()));
            writer.newLine();
            writer.newLine();

            // column header
            writer.write("Student ID,Student Name,Email,GPA,Project Code,Project Name,Preference Rank,Satisfaction Score");
            writer.newLine();

            // data rows
            for (StudentAssignmentDetail detail : details) {
                writer.write(escapeCsv(detail.getStudent().getStudentId()));
                writer.write(CSV_SEPARATOR);
                writer.write(escapeCsv(detail.getStudent().getName()));
                writer.write(CSV_SEPARATOR);
                writer.write(escapeCsv(detail.getStudent().getEmail() != null ? detail.getStudent().getEmail() : ""));
                writer.write(CSV_SEPARATOR);
                writer.write(String.format("%.2f", detail.getStudent().getGpa()));
                writer.write(CSV_SEPARATOR);
                writer.write(escapeCsv(detail.getAssignedProject().getCode()));
                writer.write(CSV_SEPARATOR);
                writer.write(escapeCsv(detail.getAssignedProject().getName()));
                writer.write(CSV_SEPARATOR);
                writer.write(detail.getPreferenceRank() != null ? detail.getPreferenceRank().toString() : "N/A");
                writer.write(CSV_SEPARATOR);
                writer.write(String.valueOf(detail.getSatisfactionScore()));
                writer.newLine();
            }
        }
    }

    // CSV with one row per project
    public static void exportProjectsCsv(File file, AlgorithmRun run, MatchingSummary summary,
                                         List<ProjectAssignmentDetail> details) throws IOException {
        try (BufferedWriter writer = openWriter(file, true)) {
            // metadata header
            writer.write("# OptiMatch - Project Assignment Results");
            writer.newLine();
            writer.write("# Run Date: " + run.getRunTimestamp().format(DATE_FORMAT));
            writer.newLine();
            writer.write("# Best Fitness: " + String.format("%.2f", run.getBestFitness()));
            writer.newLine();
            writer.newLine();

            // column header
            writer.write("Project Code,Project Name,Min Capacity,Max Capacity,Assigned Count,Status,Assigned Students");
            writer.newLine();

            // data rows
            for (ProjectAssignmentDetail detail : details) {
                writer.write(escapeCsv(detail.getProject().getCode()));
                writer.write(CSV_SEPARATOR);
                writer.write(escapeCsv(detail.getProject().getName()));
                writer.write(CSV_SEPARATOR);
                writer.write(String.valueOf(detail.getProject().getMinCapacity()));
                writer.write(CSV_SEPARATOR);
                writer.write(String.valueOf(detail.getProject().getMaxCapacity()));
                writer.write(CSV_SEPARATOR);
                writer.write(String.valueOf(detail.getAssignedCount()));
                writer.write(CSV_SEPARATOR);
                writer.write(detail.isValid() ? "OK" : (detail.isMeetsMinCapacity() ? "Over" : "Under"));
                writer.write(CSV_SEPARATOR);

                // join student names with semicolons inside one cell
                StringBuilder students = new StringBuilder();
                for (int i = 0; i < detail.getAssignedStudents().size(); i++) {
                    if (i > 0) {
                        students.append("; ");
                    }
                    students.append(detail.getAssignedStudents().get(i).getName());
                }
                writer.write(escapeCsv(students.toString()));
                writer.newLine();
            }
        }
    }

    // human-readable text report covering students and projects
    public static void exportFullReport(File file, AlgorithmRun run, MatchingSummary summary,
                                        List<StudentAssignmentDetail> studentDetails,
                                        List<ProjectAssignmentDetail> projectDetails) throws IOException {
        try (BufferedWriter writer = openWriter(file, false)) {
            // title
            writer.write("================================================================================");
            writer.newLine();
            writer.write("                    OptiMatch - Matching Results Report");
            writer.newLine();
            writer.write("================================================================================");
            writer.newLine();
            writer.newLine();

            // run info
            writer.write("RUN INFORMATION");
            writer.newLine();
            writer.write("---------------");
            writer.newLine();
            writer.write("Run Date:        " + run.getRunTimestamp().format(DATE_FORMAT));
            writer.newLine();
            writer.write("Population Size: " + run.getPopulationSize());
            writer.newLine();
            writer.write("Generations:     " + run.getGenerations());
            writer.newLine();
            writer.write("Mutation Rate:   " + String.format("%.3f", run.getMutationRate()));
            writer.newLine();
            writer.write("Crossover Rate:  " + String.format("%.3f", run.getCrossoverRate()));
            writer.newLine();
            writer.write("Execution Time:  " + String.format("%.2f seconds", run.getExecutionTimeSeconds()));
            writer.newLine();
            writer.newLine();

            // summary block
            writer.write("SUMMARY STATISTICS");
            writer.newLine();
            writer.write("------------------");
            writer.newLine();
            writer.write("Total Students:     " + summary.getTotalStudents());
            writer.newLine();
            writer.write("Total Projects:     " + summary.getTotalProjects());
            writer.newLine();
            writer.write("Best Fitness:       " + String.format("%.2f", run.getBestFitness()));
            writer.newLine();
            writer.write("Satisfaction Rate:  " + String.format("%.1f%%", summary.getSatisfactionPercentage()));
            writer.newLine();
            writer.newLine();

            // preference distribution block
            writer.write("PREFERENCE DISTRIBUTION");
            writer.newLine();
            writer.write("-----------------------");
            writer.newLine();
            int[] dist = summary.getPreferenceDistribution();
            writer.write("1st Choice:  " + dist[1] + " students (" + percentage(dist[1], summary.getTotalStudents()) + ")");
            writer.newLine();
            writer.write("2nd Choice:  " + dist[2] + " students (" + percentage(dist[2], summary.getTotalStudents()) + ")");
            writer.newLine();
            writer.write("3rd Choice:  " + dist[3] + " students (" + percentage(dist[3], summary.getTotalStudents()) + ")");
            writer.newLine();
            writer.write("4th Choice:  " + dist[4] + " students (" + percentage(dist[4], summary.getTotalStudents()) + ")");
            writer.newLine();
            writer.write("5th Choice:  " + dist[5] + " students (" + percentage(dist[5], summary.getTotalStudents()) + ")");
            writer.newLine();
            writer.write("No Match:    " + dist[0] + " students (" + percentage(dist[0], summary.getTotalStudents()) + ")");
            writer.newLine();
            writer.newLine();

            // student assignments block
            writer.write("================================================================================");
            writer.newLine();
            writer.write("STUDENT ASSIGNMENTS");
            writer.newLine();
            writer.write("================================================================================");
            writer.newLine();
            writer.newLine();

            writer.write(String.format("%-15s %-25s %-15s %-25s %s",
                    "Student ID", "Name", "Project Code", "Project Name", "Rank"));
            writer.newLine();
            writer.write("-".repeat(95));
            writer.newLine();

            for (StudentAssignmentDetail detail : studentDetails) {
                writer.write(String.format("%-15s %-25s %-15s %-25s %s",
                        truncate(detail.getStudent().getStudentId(), 15),
                        truncate(detail.getStudent().getName(), 25),
                        truncate(detail.getAssignedProject().getCode(), 15),
                        truncate(detail.getAssignedProject().getName(), 25),
                        detail.getPreferenceRank() != null ? "#" + detail.getPreferenceRank() : "N/A"));
                writer.newLine();
            }
            writer.newLine();

            // project assignments block
            writer.write("================================================================================");
            writer.newLine();
            writer.write("PROJECT ASSIGNMENTS");
            writer.newLine();
            writer.write("================================================================================");
            writer.newLine();
            writer.newLine();

            for (ProjectAssignmentDetail detail : projectDetails) {
                writer.write(detail.getProject().getCode() + " - " + detail.getProject().getName());
                writer.newLine();
                writer.write("Capacity: " + detail.getProject().getMinCapacity() + "-" +
                        detail.getProject().getMaxCapacity() + " | Assigned: " + detail.getAssignedCount() +
                        " | Status: " + (detail.isValid() ? "OK" : (detail.isMeetsMinCapacity() ? "OVER" : "UNDER")));
                writer.newLine();
                writer.write("Students: ");
                if (detail.getAssignedStudents().isEmpty()) {
                    writer.write("(none)");
                } else {
                    for (int i = 0; i < detail.getAssignedStudents().size(); i++) {
                        if (i > 0) {
                            writer.write(", ");
                        }
                        writer.write(detail.getAssignedStudents().get(i).getName());
                    }
                }
                writer.newLine();
                writer.newLine();
            }

            // footer
            writer.write("================================================================================");
            writer.newLine();
            writer.write("                         End of Report");
            writer.newLine();
            writer.write("================================================================================");
            writer.newLine();
        }
    }

    // CSV with one row per generation
    public static void exportGenerationsCsv(File file, AlgorithmRun run,
                                            List<GenerationStats> stats) throws IOException {
        try (BufferedWriter writer = openWriter(file, true)) {
            // metadata header
            writer.write("# OptiMatch - Generation Statistics");
            writer.newLine();
            writer.write("# Run Date: " + run.getRunTimestamp().format(DATE_FORMAT));
            writer.newLine();
            writer.write("# Best Fitness: " + String.format("%.2f", run.getBestFitness()));
            writer.newLine();
            writer.write("# Total Generations: " + run.getGenerations());
            writer.newLine();
            writer.write("# Population Size: " + run.getPopulationSize());
            writer.newLine();
            writer.newLine();

            // column header
            writer.write("Generation,Best Fitness,Average Fitness,Worst Fitness,Std Deviation,Valid Count,Best Ever");
            writer.newLine();

            // data rows
            for (GenerationStats stat : stats) {
                writer.write(String.valueOf(stat.getGeneration()));
                writer.write(CSV_SEPARATOR);
                writer.write(String.format("%.2f", stat.getBestFitness()));
                writer.write(CSV_SEPARATOR);
                writer.write(String.format("%.2f", stat.getAverageFitness()));
                writer.write(CSV_SEPARATOR);
                writer.write(String.format("%.2f", stat.getWorstFitness()));
                writer.write(CSV_SEPARATOR);
                writer.write(String.format("%.4f", stat.getStandardDeviation()));
                writer.write(CSV_SEPARATOR);
                writer.write(String.valueOf(stat.getValidCount()));
                writer.write(CSV_SEPARATOR);
                writer.write(String.format("%.2f", stat.getBestEverFitness()));
                writer.newLine();
            }
        }
    }

    // wrap a CSV value in quotes if it contains a separator, quote or newline
    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // formatted percentage string, "0.0%" if total is zero
    private static String percentage(int count, int total) {
        if (total == 0) {
            return "0.0%";
        }
        return String.format("%.1f%%", (count * 100.0) / total);
    }

    // shorten a value to fit a column, adds ... when truncated
    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }
}
