package com.optimatch.service;

import com.optimatch.dao.AssignmentDAO;
import com.optimatch.dao.PreferenceDAO;
import com.optimatch.dao.ProjectDAO;
import com.optimatch.dao.StudentDAO;
import com.optimatch.model.Assignment;
import com.optimatch.model.Preference;
import com.optimatch.model.Project;
import com.optimatch.model.Student;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service layer for generating reports and statistics.
 * Provides various views and analyses of matching results.
 */
public class ReportService {

    private final StudentDAO studentDAO;
    private final ProjectDAO projectDAO;
    private final PreferenceDAO preferenceDAO;
    private final AssignmentDAO assignmentDAO;

    /**
     * Creates a ReportService with default DAOs.
     */
    public ReportService() {
        this.studentDAO = new StudentDAO();
        this.projectDAO = new ProjectDAO();
        this.preferenceDAO = new PreferenceDAO();
        this.assignmentDAO = new AssignmentDAO();
    }

    /**
     * Creates a ReportService with the specified DAOs.
     *
     * @param studentDAO    the student DAO
     * @param projectDAO    the project DAO
     * @param preferenceDAO the preference DAO
     * @param assignmentDAO the assignment DAO
     */
    public ReportService(StudentDAO studentDAO, ProjectDAO projectDAO,
                         PreferenceDAO preferenceDAO, AssignmentDAO assignmentDAO) {
        this.studentDAO = studentDAO;
        this.projectDAO = projectDAO;
        this.preferenceDAO = preferenceDAO;
        this.assignmentDAO = assignmentDAO;
    }

    /**
     * Generates a summary report for a matching run.
     *
     * @param runId the algorithm run ID
     * @return the summary report
     * @throws ServiceException if database error occurs
     */
    public MatchingSummary generateSummary(int runId) throws ServiceException {
        try {
            List<Assignment> assignments = assignmentDAO.findByRun(runId);
            List<Student> students = studentDAO.findAll();
            List<Project> projects = projectDAO.findAll();

            // Calculate statistics
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
                    runId,
                    students.size(),
                    projects.size(),
                    assignments.size(),
                    preferenceDistribution,
                    totalSatisfaction,
                    satisfactionPercentage
            );

        } catch (SQLException e) {
            throw new ServiceException("Failed to generate summary: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a detailed student report for a matching run.
     *
     * @param runId the algorithm run ID
     * @return list of student assignment details
     * @throws ServiceException if database error occurs
     */
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

            // Sort by student name
            details.sort((a, b) -> a.getStudent().getName().compareTo(b.getStudent().getName()));

            return details;

        } catch (SQLException e) {
            throw new ServiceException("Failed to generate student report: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a project-centric report for a matching run.
     *
     * @param runId the algorithm run ID
     * @return list of project assignment details
     * @throws ServiceException if database error occurs
     */
    public List<ProjectAssignmentDetail> generateProjectReport(int runId) throws ServiceException {
        try {
            List<Assignment> assignments = assignmentDAO.findByRun(runId);
            Map<Integer, Student> studentMap = studentDAO.findAll().stream()
                    .collect(Collectors.toMap(Student::getId, s -> s));
            List<Project> projects = projectDAO.findAll();

            // Group assignments by project
            Map<Integer, List<Assignment>> assignmentsByProject = assignments.stream()
                    .collect(Collectors.groupingBy(Assignment::getProjectId));

            List<ProjectAssignmentDetail> details = new ArrayList<>();

            for (Project project : projects) {
                List<Assignment> projectAssignments = assignmentsByProject.getOrDefault(
                        project.getId(), new ArrayList<>()
                );

                List<Student> assignedStudents = projectAssignments.stream()
                        .map(a -> studentMap.get(a.getStudentId()))
                        .filter(s -> s != null)
                        .collect(Collectors.toList());

                int assignedCount = assignedStudents.size();
                boolean meetsMinCapacity = assignedCount >= project.getMinCapacity();
                boolean withinMaxCapacity = assignedCount <= project.getMaxCapacity();

                details.add(new ProjectAssignmentDetail(
                        project,
                        assignedStudents,
                        meetsMinCapacity,
                        withinMaxCapacity
                ));
            }

            // Sort by project code
            details.sort((a, b) -> a.getProject().getCode().compareTo(b.getProject().getCode()));

            return details;

        } catch (SQLException e) {
            throw new ServiceException("Failed to generate project report: " + e.getMessage(), e);
        }
    }

    /**
     * Gets students who were not assigned to any of their preferences.
     *
     * @param runId the algorithm run ID
     * @return list of unmatched students with their preferences
     * @throws ServiceException if database error occurs
     */
    public List<UnmatchedStudentDetail> getUnmatchedStudents(int runId) throws ServiceException {
        try {
            List<Assignment> assignments = assignmentDAO.findByRun(runId);
            Map<Integer, Student> studentMap = studentDAO.findAll().stream()
                    .collect(Collectors.toMap(Student::getId, s -> s));
            Map<Integer, Project> projectMap = projectDAO.findAll().stream()
                    .collect(Collectors.toMap(Project::getId, p -> p));

            List<UnmatchedStudentDetail> unmatched = new ArrayList<>();

            for (Assignment assignment : assignments) {
                if (assignment.getPreferenceRank() == null) {
                    Student student = studentMap.get(assignment.getStudentId());
                    Project assignedProject = projectMap.get(assignment.getProjectId());
                    List<Preference> preferences = preferenceDAO.findByStudent(assignment.getStudentId());

                    List<Project> preferredProjects = preferences.stream()
                            .map(p -> projectMap.get(p.getProjectId()))
                            .filter(p -> p != null)
                            .collect(Collectors.toList());

                    if (student != null && assignedProject != null) {
                        unmatched.add(new UnmatchedStudentDetail(
                                student,
                                assignedProject,
                                preferredProjects
                        ));
                    }
                }
            }

            return unmatched;

        } catch (SQLException e) {
            throw new ServiceException("Failed to get unmatched students: " + e.getMessage(), e);
        }
    }

    /**
     * Gets projects that don't meet their minimum capacity.
     *
     * @param runId the algorithm run ID
     * @return list of underfilled projects
     * @throws ServiceException if database error occurs
     */
    public List<ProjectAssignmentDetail> getUnderfilledProjects(int runId) throws ServiceException {
        List<ProjectAssignmentDetail> allProjects = generateProjectReport(runId);
        return allProjects.stream()
                .filter(p -> !p.isMeetsMinCapacity())
                .collect(Collectors.toList());
    }

    /**
     * Generates comparison data between multiple runs.
     *
     * @param runIds the run IDs to compare
     * @return comparison data
     * @throws ServiceException if database error occurs
     */
    public List<MatchingSummary> compareRuns(List<Integer> runIds) throws ServiceException {
        List<MatchingSummary> summaries = new ArrayList<>();
        for (int runId : runIds) {
            summaries.add(generateSummary(runId));
        }
        return summaries;
    }

    // ==================== Inner Classes ====================

    /**
     * Summary statistics for a matching run.
     */
    public static class MatchingSummary {
        private final int runId;
        private final int totalStudents;
        private final int totalProjects;
        private final int totalAssignments;
        private final int[] preferenceDistribution;
        private final int totalSatisfaction;
        private final double satisfactionPercentage;

        public MatchingSummary(int runId, int totalStudents, int totalProjects,
                               int totalAssignments, int[] preferenceDistribution,
                               int totalSatisfaction, double satisfactionPercentage) {
            this.runId = runId;
            this.totalStudents = totalStudents;
            this.totalProjects = totalProjects;
            this.totalAssignments = totalAssignments;
            this.preferenceDistribution = preferenceDistribution;
            this.totalSatisfaction = totalSatisfaction;
            this.satisfactionPercentage = satisfactionPercentage;
        }

        public int getRunId() {
            return runId;
        }

        public int getTotalStudents() {
            return totalStudents;
        }

        public int getTotalProjects() {
            return totalProjects;
        }

        public int getTotalAssignments() {
            return totalAssignments;
        }

        public int[] getPreferenceDistribution() {
            return preferenceDistribution;
        }

        public int getFirstChoiceCount() {
            return preferenceDistribution[1];
        }

        public int getUnmatchedCount() {
            return preferenceDistribution[0];
        }

        public int getTotalSatisfaction() {
            return totalSatisfaction;
        }

        public double getSatisfactionPercentage() {
            return satisfactionPercentage;
        }

        @Override
        public String toString() {
            return String.format(
                    "MatchingSummary{runId=%d, students=%d, satisfaction=%.1f%%, " +
                            "1st=%d, 2nd=%d, 3rd=%d, 4th=%d, 5th=%d, none=%d}",
                    runId, totalStudents, satisfactionPercentage,
                    preferenceDistribution[1], preferenceDistribution[2],
                    preferenceDistribution[3], preferenceDistribution[4],
                    preferenceDistribution[5], preferenceDistribution[0]
            );
        }
    }

    /**
     * Details of a student's assignment.
     */
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

        public boolean wasPreferred() {
            return preferenceRank != null;
        }
    }

    /**
     * Details of a project's assignments.
     */
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

        public boolean isWithinMaxCapacity() {
            return withinMaxCapacity;
        }

        public boolean isValid() {
            return meetsMinCapacity && withinMaxCapacity;
        }
    }

    /**
     * Details of an unmatched student.
     */
    public static class UnmatchedStudentDetail {
        private final Student student;
        private final Project assignedProject;
        private final List<Project> preferredProjects;

        public UnmatchedStudentDetail(Student student, Project assignedProject,
                                      List<Project> preferredProjects) {
            this.student = student;
            this.assignedProject = assignedProject;
            this.preferredProjects = preferredProjects;
        }

        public Student getStudent() {
            return student;
        }

        public Project getAssignedProject() {
            return assignedProject;
        }

        public List<Project> getPreferredProjects() {
            return preferredProjects;
        }
    }
}
