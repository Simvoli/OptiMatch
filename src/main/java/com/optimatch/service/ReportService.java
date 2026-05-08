package com.optimatch.service;

import com.optimatch.dao.AssignmentDAO;
import com.optimatch.dao.ProjectDAO;
import com.optimatch.dao.StudentDAO;
import com.optimatch.model.Assignment;
import com.optimatch.model.Preference;
import com.optimatch.model.Project;
import com.optimatch.model.Student;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Aggregates data from a single algorithm run into the view-ready summaries
 * consumed by the Results UI and the export utilities.
 */
public class ReportService {

    private final StudentDAO studentDAO;
    private final ProjectDAO projectDAO;
    private final AssignmentDAO assignmentDAO;

    public ReportService() {
        this.studentDAO = new StudentDAO();
        this.projectDAO = new ProjectDAO();
        this.assignmentDAO = new AssignmentDAO();
    }

    public MatchingSummary generateSummary(int runId) throws ServiceException {
        try {
            List<Assignment> assignments = assignmentDAO.findByRun(runId);
            List<Student> students = studentDAO.findAll();
            List<Project> projects = projectDAO.findAll();

            int[] preferenceDistribution = new int[6]; // 0=none, 1-5=ranks
            int totalSatisfaction = 0;
            int maxPossibleSatisfaction = students.size() * Preference.WEIGHT_FIRST_CHOICE;

            for (Assignment assignment : assignments) {
                Integer rank = assignment.getPreferenceRank();
                if (rank == null) {
                    preferenceDistribution[0]++;
                } else {
                    preferenceDistribution[rank]++;
                }
                totalSatisfaction += assignment.getSatisfactionScore();
            }

            double satisfactionPercentage = maxPossibleSatisfaction > 0
                    ? (totalSatisfaction * 100.0) / maxPossibleSatisfaction
                    : 0;

            return new MatchingSummary(
                    students.size(),
                    projects.size(),
                    preferenceDistribution,
                    satisfactionPercentage
            );
        } catch (SQLException e) {
            throw new ServiceException("Failed to generate summary: " + e.getMessage(), e);
        }
    }

    public List<StudentAssignmentDetail> generateStudentReport(int runId) throws ServiceException {
        try {
            List<Assignment> assignments = assignmentDAO.findByRun(runId);
            Map<Integer, Student> studentMap = studentDAO.findAll().stream()
                    .collect(Collectors.toMap(Student::getId, s -> s));
            Map<Integer, Project> projectMap = projectDAO.findAll().stream()
                    .collect(Collectors.toMap(Project::getId, p -> p));

            List<StudentAssignmentDetail> details = new ArrayList<>();
            for (Assignment assignment : assignments) {
                Student student = studentMap.get(assignment.getStudentId());
                Project project = projectMap.get(assignment.getProjectId());
                if (student != null && project != null) {
                    details.add(new StudentAssignmentDetail(
                            student,
                            project,
                            assignment.getPreferenceRank(),
                            assignment.getSatisfactionScore()
                    ));
                }
            }
            details.sort((a, b) -> a.getStudent().getName().compareTo(b.getStudent().getName()));
            return details;
        } catch (SQLException e) {
            throw new ServiceException("Failed to generate student report: " + e.getMessage(), e);
        }
    }

    public List<ProjectAssignmentDetail> generateProjectReport(int runId) throws ServiceException {
        try {
            List<Assignment> assignments = assignmentDAO.findByRun(runId);
            Map<Integer, Student> studentMap = studentDAO.findAll().stream()
                    .collect(Collectors.toMap(Student::getId, s -> s));
            List<Project> projects = projectDAO.findAll();

            Map<Integer, List<Assignment>> assignmentsByProject = assignments.stream()
                    .collect(Collectors.groupingBy(Assignment::getProjectId));

            List<ProjectAssignmentDetail> details = new ArrayList<>();
            for (Project project : projects) {
                List<Assignment> projectAssignments = assignmentsByProject.getOrDefault(
                        project.getId(), new ArrayList<>());

                List<Student> assignedStudents = projectAssignments.stream()
                        .map(a -> studentMap.get(a.getStudentId()))
                        .filter(s -> s != null)
                        .collect(Collectors.toList());

                int assignedCount = assignedStudents.size();
                boolean meetsMinCapacity = assignedCount >= project.getMinCapacity();
                boolean withinMaxCapacity = assignedCount <= project.getMaxCapacity();

                details.add(new ProjectAssignmentDetail(
                        project, assignedStudents, meetsMinCapacity, withinMaxCapacity));
            }
            details.sort((a, b) -> a.getProject().getCode().compareTo(b.getProject().getCode()));
            return details;
        } catch (SQLException e) {
            throw new ServiceException("Failed to generate project report: " + e.getMessage(), e);
        }
    }

    public static class MatchingSummary {
        private final int totalStudents;
        private final int totalProjects;
        private final int[] preferenceDistribution;
        private final double satisfactionPercentage;

        public MatchingSummary(int totalStudents, int totalProjects,
                               int[] preferenceDistribution, double satisfactionPercentage) {
            this.totalStudents = totalStudents;
            this.totalProjects = totalProjects;
            this.preferenceDistribution = preferenceDistribution;
            this.satisfactionPercentage = satisfactionPercentage;
        }

        public int getTotalStudents() {
            return totalStudents;
        }

        public int getTotalProjects() {
            return totalProjects;
        }

        public int[] getPreferenceDistribution() {
            return preferenceDistribution;
        }

        public double getSatisfactionPercentage() {
            return satisfactionPercentage;
        }
    }

    public static class StudentAssignmentDetail {
        private final Student student;
        private final Project assignedProject;
        private final Integer preferenceRank;
        private final int satisfactionScore;

        public StudentAssignmentDetail(Student student, Project assignedProject,
                                       Integer preferenceRank, int satisfactionScore) {
            this.student = student;
            this.assignedProject = assignedProject;
            this.preferenceRank = preferenceRank;
            this.satisfactionScore = satisfactionScore;
        }

        public Student getStudent() {
            return student;
        }

        public Project getAssignedProject() {
            return assignedProject;
        }

        public Integer getPreferenceRank() {
            return preferenceRank;
        }

        public int getSatisfactionScore() {
            return satisfactionScore;
        }
    }

    public static class ProjectAssignmentDetail {
        private final Project project;
        private final List<Student> assignedStudents;
        private final boolean meetsMinCapacity;
        private final boolean withinMaxCapacity;

        public ProjectAssignmentDetail(Project project, List<Student> assignedStudents,
                                       boolean meetsMinCapacity, boolean withinMaxCapacity) {
            this.project = project;
            this.assignedStudents = assignedStudents;
            this.meetsMinCapacity = meetsMinCapacity;
            this.withinMaxCapacity = withinMaxCapacity;
        }

        public Project getProject() {
            return project;
        }

        public List<Student> getAssignedStudents() {
            return assignedStudents;
        }

        public int getAssignedCount() {
            return assignedStudents.size();
        }

        public boolean isMeetsMinCapacity() {
            return meetsMinCapacity;
        }

        public boolean isValid() {
            return meetsMinCapacity && withinMaxCapacity;
        }
    }
}
