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

// orchestrates one matching: load data, run GA, save run/assignments/stats
public class MatchingService {

    private final StudentDAO studentDAO;
    private final ProjectDAO projectDAO;
    private final PreferenceDAO preferenceDAO;
    private final AlgorithmRunDAO algorithmRunDAO;
    private final AssignmentDAO assignmentDAO;
    private final GenerationStatsDAO generationStatsDAO;

    private GeneticAlgorithm currentAlgorithm;
    private GeneticAlgorithm.GenerationCallback progressCallback;

    // wires up default DAOs
    public MatchingService() {
        this.studentDAO = new StudentDAO();
        this.projectDAO = new ProjectDAO();
        this.preferenceDAO = new PreferenceDAO();
        this.algorithmRunDAO = new AlgorithmRunDAO();
        this.assignmentDAO = new AssignmentDAO();
        this.generationStatsDAO = new GenerationStatsDAO();
    }

    // load data, run the GA, persist everything, return the saved run
    public AlgorithmRun runMatching(GeneticAlgorithmConfig config) throws ServiceException {
        try {
            List<Student> students = studentDAO.findAll();
            List<Project> projects = projectDAO.findAll();
            List<Preference> preferences = preferenceDAO.findAll();

            validateData(students, projects);

            // RU: GA внутри использует индексы 0..N-1, а в БД у студентов реальные id,
            // поэтому держим карту индекс -> studentId
            Map<Integer, Integer> indexToStudentId = new HashMap<>();
            for (int i = 0; i < students.size(); i++) {
                indexToStudentId.put(i, students.get(i).getId());
            }

            currentAlgorithm = new GeneticAlgorithm(students, projects, preferences, config);
            if (progressCallback != null) {
                currentAlgorithm.setGenerationCallback(progressCallback);
            }

            Chromosome bestSolution = currentAlgorithm.run();
            GeneticAlgorithm.AlgorithmResult algorithmResult = currentAlgorithm.getResult();

            AlgorithmRun run = createAlgorithmRun(config, algorithmResult);
            algorithmRunDAO.insert(run);

            List<Assignment> assignments = createAssignments(
                    run.getId(), bestSolution, indexToStudentId, preferences);
            assignmentDAO.insertBatch(assignments);

            // attach run id to each generation stats row before insert
            List<GenerationStats> generationStats = algorithmResult.getHistory();
            for (GenerationStats stats : generationStats) {
                stats.setRunId(run.getId());
            }
            generationStatsDAO.insertBatch(generationStats);

            return run;
        } catch (SQLException e) {
            throw new ServiceException("Database error during matching: " + e.getMessage(), e);
        }
    }

    // request a graceful stop on the running GA
    public void stopMatching() {
        if (currentAlgorithm != null && currentAlgorithm.isRunning()) {
            currentAlgorithm.stop();
        }
    }

    // register a callback for live progress updates
    public void setProgressCallback(GeneticAlgorithm.GenerationCallback callback) {
        this.progressCallback = callback;
    }

    // load all past runs newest first
    public List<AlgorithmRun> getAllRuns() throws ServiceException {
        try {
            return algorithmRunDAO.findAll();
        } catch (SQLException e) {
            throw new ServiceException("Failed to get algorithm runs: " + e.getMessage(), e);
        }
    }

    // load per-generation stats for one run
    public List<GenerationStats> getGenerationStatsForRun(int runId) throws ServiceException {
        try {
            return generationStatsDAO.findByRun(runId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to get generation stats: " + e.getMessage(), e);
        }
    }

    // delete a run; assignments and stats fall via ON DELETE CASCADE
    public boolean deleteRun(int runId) throws ServiceException {
        try {
            return algorithmRunDAO.delete(runId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to delete run: " + e.getMessage(), e);
        }
    }

    // sanity checks before kicking off the GA
    private void validateData(List<Student> students, List<Project> projects) throws ServiceException {
        if (students.isEmpty()) {
            throw new ServiceException("No students found. Please add students before running the algorithm.");
        }
        if (projects.isEmpty()) {
            throw new ServiceException("No projects found. Please add projects before running the algorithm.");
        }

        int totalMaxCapacity = projects.stream().mapToInt(Project::getMaxCapacity).sum();
        if (students.size() > totalMaxCapacity) {
            throw new ServiceException(
                    String.format("Not enough capacity. %d students but only %d total slots available.",
                            students.size(), totalMaxCapacity)
            );
        }
    }

    // build the AlgorithmRun row from config + result
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

    // turn the chromosome into Assignment rows, including preference rank if any
    private List<Assignment> createAssignments(int runId, Chromosome chromosome,
                                               Map<Integer, Integer> indexToStudentId,
                                               List<Preference> preferences) {
        // composite key "studentId-projectId" -> rank
        Map<String, Integer> preferenceRankMap = new HashMap<>();
        for (Preference pref : preferences) {
            preferenceRankMap.put(pref.getStudentId() + "-" + pref.getProjectId(), pref.getRank());
        }

        List<Assignment> assignments = new ArrayList<>();
        int[] chromosomeAssignments = chromosome.getAssignments();
        for (int i = 0; i < chromosomeAssignments.length; i++) {
            int studentId = indexToStudentId.get(i);
            int projectId = chromosomeAssignments[i];
            Integer preferenceRank = preferenceRankMap.get(studentId + "-" + projectId);
            assignments.add(new Assignment(runId, studentId, projectId, preferenceRank));
        }
        return assignments;
    }
}
