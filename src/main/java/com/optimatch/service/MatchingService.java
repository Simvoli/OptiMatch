package com.optimatch.service;

import com.optimatch.algorithm.Chromosome;
import com.optimatch.algorithm.GeneticAlgorithm;
import com.optimatch.algorithm.GeneticAlgorithmConfig;
import com.optimatch.dao.AlgorithmRunDAO;
import com.optimatch.dao.AssignmentDAO;
import com.optimatch.dao.GenerationStatsDAO;
import com.optimatch.dao.PreferenceDAO;
import com.optimatch.dao.ProjectDAO;
import com.optimatch.dao.StudentDAO;
import com.optimatch.model.AlgorithmRun;
import com.optimatch.model.Assignment;
import com.optimatch.model.GenerationStats;
import com.optimatch.model.Preference;
import com.optimatch.model.Project;
import com.optimatch.model.Student;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service layer for the matching algorithm.
 * Orchestrates the genetic algorithm execution and persists results.
 */
public class MatchingService {

    private final StudentDAO studentDAO;
    private final ProjectDAO projectDAO;
    private final PreferenceDAO preferenceDAO;
    private final AlgorithmRunDAO algorithmRunDAO;
    private final AssignmentDAO assignmentDAO;
    private final GenerationStatsDAO generationStatsDAO;

    private GeneticAlgorithm currentAlgorithm;
    private GeneticAlgorithm.GenerationCallback progressCallback;

    /**
     * Creates a MatchingService with default DAOs.
     */
    public MatchingService() {
        this.studentDAO = new StudentDAO();
        this.projectDAO = new ProjectDAO();
        this.preferenceDAO = new PreferenceDAO();
        this.algorithmRunDAO = new AlgorithmRunDAO();
        this.assignmentDAO = new AssignmentDAO();
        this.generationStatsDAO = new GenerationStatsDAO();
    }

    /**
     * Creates a MatchingService with the specified DAOs.
     *
     * @param studentDAO          the student DAO
     * @param projectDAO          the project DAO
     * @param preferenceDAO       the preference DAO
     * @param algorithmRunDAO     the algorithm run DAO
     * @param assignmentDAO       the assignment DAO
     * @param generationStatsDAO  the generation stats DAO
     */
    public MatchingService(StudentDAO studentDAO, ProjectDAO projectDAO,
                           PreferenceDAO preferenceDAO, AlgorithmRunDAO algorithmRunDAO,
                           AssignmentDAO assignmentDAO, GenerationStatsDAO generationStatsDAO) {
        this.studentDAO = studentDAO;
        this.projectDAO = projectDAO;
        this.preferenceDAO = preferenceDAO;
        this.algorithmRunDAO = algorithmRunDAO;
        this.assignmentDAO = assignmentDAO;
        this.generationStatsDAO = generationStatsDAO;
    }

    /**
     * Runs the matching algorithm with default configuration.
     *
     * @return the matching result
     * @throws ServiceException if an error occurs
     */
    public MatchingResult runMatching() throws ServiceException {
        return runMatching(new GeneticAlgorithmConfig());
    }

    /**
     * Runs the matching algorithm with the specified configuration.
     *
     * @param config the algorithm configuration
     * @return the matching result
     * @throws ServiceException if an error occurs
     */
    public MatchingResult runMatching(GeneticAlgorithmConfig config) throws ServiceException {
        try {
            // Load data from database
            List<Student> students = studentDAO.findAll();
            List<Project> projects = projectDAO.findAll();
            List<Preference> preferences = preferenceDAO.findAll();

            // Validate data
            validateData(students, projects);

            // Create index mappings
            Map<Integer, Integer> studentIdToIndex = new HashMap<>();
            Map<Integer, Integer> indexToStudentId = new HashMap<>();
            for (int i = 0; i < students.size(); i++) {
                studentIdToIndex.put(students.get(i).getId(), i);
                indexToStudentId.put(i, students.get(i).getId());
            }

            // Create and run algorithm
            currentAlgorithm = new GeneticAlgorithm(students, projects, preferences, config);
            if (progressCallback != null) {
                currentAlgorithm.setGenerationCallback(progressCallback);
            }

            Chromosome bestSolution = currentAlgorithm.run();
            GeneticAlgorithm.AlgorithmResult algorithmResult = currentAlgorithm.getResult();

            // Save algorithm run to database
            AlgorithmRun run = createAlgorithmRun(config, algorithmResult);
            algorithmRunDAO.insert(run);

            // Convert chromosome to assignments and save
            List<Assignment> assignments = createAssignments(
                    run.getId(),
                    bestSolution,
                    indexToStudentId,
                    preferences
            );
            assignmentDAO.insertBatch(assignments);

            // Save generation statistics
            List<GenerationStats> generationStats = createGenerationStats(
                    run.getId(),
                    algorithmResult.getHistory()
            );
            generationStatsDAO.insertBatch(generationStats);

            // Create and return result
            return new MatchingResult(run, assignments, algorithmResult);

        } catch (SQLException e) {
            throw new ServiceException("Database error during matching: " + e.getMessage(), e);
        }
    }

    /**
     * Stops the currently running algorithm.
     */
    public void stopMatching() {
        if (currentAlgorithm != null && currentAlgorithm.isRunning()) {
            currentAlgorithm.stop();
        }
    }

    /**
     * Checks if the algorithm is currently running.
     *
     * @return true if running
     */
    public boolean isRunning() {
        return currentAlgorithm != null && currentAlgorithm.isRunning();
    }

    /**
     * Sets a callback for progress updates during algorithm execution.
     *
     * @param callback the callback to invoke after each generation
     */
    public void setProgressCallback(GeneticAlgorithm.GenerationCallback callback) {
        this.progressCallback = callback;
    }

    /**
     * Gets all past algorithm runs.
     *
     * @return list of algorithm runs
     * @throws ServiceException if database error occurs
     */
    public List<AlgorithmRun> getAllRuns() throws ServiceException {
        try {
            return algorithmRunDAO.findAll();
        } catch (SQLException e) {
            throw new ServiceException("Failed to get algorithm runs: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the most recent algorithm run.
     *
     * @return the latest run if any exist
     * @throws ServiceException if database error occurs
     */
    public Optional<AlgorithmRun> getLatestRun() throws ServiceException {
        try {
            return algorithmRunDAO.findLatest();
        } catch (SQLException e) {
            throw new ServiceException("Failed to get latest run: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the assignments for a specific run.
     *
     * @param runId the algorithm run ID
     * @return list of assignments
     * @throws ServiceException if database error occurs
     */
    public List<Assignment> getAssignmentsForRun(int runId) throws ServiceException {
        try {
            return assignmentDAO.findByRun(runId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to get assignments: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes an algorithm run and its assignments.
     *
     * @param runId the run ID to delete
     * @return true if deletion was successful
     * @throws ServiceException if database error occurs
     */
    public boolean deleteRun(int runId) throws ServiceException {
        try {
            assignmentDAO.deleteByRun(runId);
            return algorithmRunDAO.delete(runId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to delete run: " + e.getMessage(), e);
        }
    }

    /**
     * Validates the data before running the algorithm.
     *
     * @param students the students
     * @param projects the projects
     * @throws ServiceException if data is invalid
     */
    private void validateData(List<Student> students, List<Project> projects) throws ServiceException {
        if (students.isEmpty()) {
            throw new ServiceException("No students found. Please add students before running the algorithm.");
        }
        if (projects.isEmpty()) {
            throw new ServiceException("No projects found. Please add projects before running the algorithm.");
        }

        // Check total capacity
        int totalMaxCapacity = projects.stream().mapToInt(Project::getMaxCapacity).sum();
        if (students.size() > totalMaxCapacity) {
            throw new ServiceException(
                    String.format("Not enough capacity. %d students but only %d total slots available.",
                            students.size(), totalMaxCapacity)
            );
        }
    }

    /**
     * Creates an AlgorithmRun entity from the result.
     *
     * @param config the configuration used
     * @param result the algorithm result
     * @return the AlgorithmRun entity
     */
    private AlgorithmRun createAlgorithmRun(GeneticAlgorithmConfig config,
                                            GeneticAlgorithm.AlgorithmResult result) {
        AlgorithmRun run = new AlgorithmRun();
        run.setRunTimestamp(LocalDateTime.now());
        run.setPopulationSize(config.getPopulationSize());
        run.setGenerations(result.getGenerations());
        run.setMutationRate(config.getMutationRate());
        run.setCrossoverRate(config.getCrossoverRate());
        run.setBestFitness(result.getBestFitness());
        run.setExecutionTimeMs(result.getExecutionTimeMs());
        return run;
    }

    /**
     * Converts a chromosome to Assignment entities.
     *
     * @param runId            the algorithm run ID
     * @param chromosome       the solution chromosome
     * @param indexToStudentId mapping from chromosome index to student database ID
     * @param preferences      all preferences (for determining rank)
     * @return list of Assignment entities
     */
    private List<Assignment> createAssignments(int runId, Chromosome chromosome,
                                               Map<Integer, Integer> indexToStudentId,
                                               List<Preference> preferences) {
        // Build a map for quick preference rank lookup
        Map<String, Integer> preferenceRankMap = new HashMap<>();
        for (Preference pref : preferences) {
            String key = pref.getStudentId() + "-" + pref.getProjectId();
            preferenceRankMap.put(key, pref.getRank());
        }

        List<Assignment> assignments = new ArrayList<>();
        int[] chromosomeAssignments = chromosome.getAssignments();

        for (int i = 0; i < chromosomeAssignments.length; i++) {
            int studentId = indexToStudentId.get(i);
            int projectId = chromosomeAssignments[i];

            // Look up preference rank
            String key = studentId + "-" + projectId;
            Integer preferenceRank = preferenceRankMap.get(key);

            Assignment assignment = new Assignment(runId, studentId, projectId, preferenceRank);
            assignments.add(assignment);
        }

        return assignments;
    }

    /**
     * Converts algorithm generation history to persistable GenerationStats entities.
     *
     * @param runId   the algorithm run ID
     * @param history the generation history from the algorithm
     * @return list of GenerationStats entities
     */
    private List<GenerationStats> createGenerationStats(int runId,
                                                        List<GeneticAlgorithm.GenerationStats> history) {
        List<GenerationStats> statsList = new ArrayList<>();

        for (GeneticAlgorithm.GenerationStats gs : history) {
            GenerationStats stats = new GenerationStats(
                    runId,
                    gs.getGeneration(),
                    gs.getBestFitness(),
                    gs.getAverageFitness(),
                    gs.getWorstFitness(),
                    gs.getStandardDeviation(),
                    gs.getValidCount(),
                    gs.getBestEverFitness()
            );
            statsList.add(stats);
        }

        return statsList;
    }

    /**
     * Gets generation statistics for a specific algorithm run.
     *
     * @param runId the algorithm run ID
     * @return list of generation statistics ordered by generation number
     * @throws ServiceException if database error occurs
     */
    public List<GenerationStats> getGenerationStatsForRun(int runId) throws ServiceException {
        try {
            return generationStatsDAO.findByRun(runId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to get generation stats: " + e.getMessage(), e);
        }
    }

    /**
     * Result of a matching operation.
     */
    public static class MatchingResult {
        private final AlgorithmRun run;
        private final List<Assignment> assignments;
        private final GeneticAlgorithm.AlgorithmResult algorithmResult;

        public MatchingResult(AlgorithmRun run, List<Assignment> assignments,
                              GeneticAlgorithm.AlgorithmResult algorithmResult) {
            this.run = run;
            this.assignments = assignments;
            this.algorithmResult = algorithmResult;
        }

        public AlgorithmRun getRun() {
            return run;
        }

        public List<Assignment> getAssignments() {
            return assignments;
        }

        public GeneticAlgorithm.AlgorithmResult getAlgorithmResult() {
            return algorithmResult;
        }

        public double getBestFitness() {
            return run.getBestFitness();
        }

        public int getGenerations() {
            return run.getGenerations();
        }

        public long getExecutionTimeMs() {
            return run.getExecutionTimeMs();
        }

        public int[] getPreferenceDistribution() {
            return algorithmResult.getPreferenceDistribution();
        }

        @Override
        public String toString() {
            int[] dist = getPreferenceDistribution();
            return String.format(
                    "MatchingResult{fitness=%.2f, generations=%d, time=%.2fs, " +
                            "distribution=[none=%d, 1st=%d, 2nd=%d, 3rd=%d, 4th=%d, 5th=%d]}",
                    getBestFitness(),
                    getGenerations(),
                    getExecutionTimeMs() / 1000.0,
                    dist[0], dist[1], dist[2], dist[3], dist[4], dist[5]
            );
        }
    }
}
