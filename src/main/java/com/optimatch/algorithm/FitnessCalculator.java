package com.optimatch.algorithm;

import com.optimatch.model.Preference;
import com.optimatch.model.Project;
import com.optimatch.model.Student;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates the fitness score for chromosomes in the genetic algorithm.
 *
 * Fitness Formula:
 * Fitness = Σ(preferenceWeight[i]) - penaltyCapacity - penaltyGPA - penaltyPairs
 *
 * Higher fitness = better solution.
 */
public class FitnessCalculator {

    // Penalty weights (configurable)
    private static final double DEFAULT_CAPACITY_PENALTY = 50.0;
    private static final double DEFAULT_GPA_PENALTY = 30.0;
    private static final double DEFAULT_PARTNER_PENALTY = 40.0;

    private final List<Student> students;
    private final List<Project> projects;
    private final Map<Integer, Project> projectMap;
    private final Map<Integer, Map<Integer, Integer>> preferenceMap; // studentId -> (projectId -> rank)
    private final Map<Integer, Integer> studentIndexToId;
    private final Map<Integer, Integer> studentIdToIndex;

    private double capacityPenaltyWeight;
    private double gpaPenaltyWeight;
    private double partnerPenaltyWeight;

    /**
     * Creates a new FitnessCalculator with the given data.
     *
     * @param students    list of all students
     * @param projects    list of all projects
     * @param preferences list of all preferences
     */
    public FitnessCalculator(List<Student> students, List<Project> projects, List<Preference> preferences) {
        this.students = students;
        this.projects = projects;
        this.capacityPenaltyWeight = DEFAULT_CAPACITY_PENALTY;
        this.gpaPenaltyWeight = DEFAULT_GPA_PENALTY;
        this.partnerPenaltyWeight = DEFAULT_PARTNER_PENALTY;

        // Build project lookup map
        this.projectMap = new HashMap<>();
        for (Project project : projects) {
            projectMap.put(project.getId(), project);
        }

        // Build student index mappings
        this.studentIndexToId = new HashMap<>();
        this.studentIdToIndex = new HashMap<>();
        for (int i = 0; i < students.size(); i++) {
            int studentId = students.get(i).getId();
            studentIndexToId.put(i, studentId);
            studentIdToIndex.put(studentId, i);
        }

        // Build preference lookup: studentId -> (projectId -> rank)
        this.preferenceMap = new HashMap<>();
        for (Preference pref : preferences) {
            preferenceMap
                    .computeIfAbsent(pref.getStudentId(), k -> new HashMap<>())
                    .put(pref.getProjectId(), pref.getRank());
        }
    }

    /**
     * Calculates the fitness score for a chromosome.
     * Higher fitness = better solution.
     *
     * @param chromosome the chromosome to evaluate
     * @return the fitness score
     */
    public double calculateFitness(Chromosome chromosome) {
        double preferenceScore = calculatePreferenceScore(chromosome);
        double capacityPenalty = calculateCapacityPenalty(chromosome);
        double gpaPenalty = calculateGpaPenalty(chromosome);
        double partnerPenalty = calculatePartnerPenalty(chromosome);

        double fitness = preferenceScore - capacityPenalty - gpaPenalty - partnerPenalty;

        chromosome.setFitness(fitness);
        return fitness;
    }

    /**
     * Calculates the total preference satisfaction score.
     * Sum of weights based on how well students got their preferred choices.
     *
     * @param chromosome the chromosome to evaluate
     * @return the preference score (positive)
     */
    public double calculatePreferenceScore(Chromosome chromosome) {
        double score = 0.0;

        for (int i = 0; i < chromosome.getLength(); i++) {
            int studentId = studentIndexToId.get(i);
            int projectId = chromosome.getAssignment(i);

            Map<Integer, Integer> studentPrefs = preferenceMap.get(studentId);
            if (studentPrefs != null && studentPrefs.containsKey(projectId)) {
                int rank = studentPrefs.get(projectId);
                score += Preference.getWeightForRank(rank);
            }
            // If project not in preferences, adds 0 (WEIGHT_NO_PREFERENCE)
        }

        return score;
    }

    /**
     * Calculates the capacity violation penalty.
     * Penalty = 50 * |actual - limit| for each project exceeding min/max.
     *
     * @param chromosome the chromosome to evaluate
     * @return the capacity penalty (positive value to be subtracted)
     */
    public double calculateCapacityPenalty(Chromosome chromosome) {
        double penalty = 0.0;

        // Count students per project
        Map<Integer, Integer> projectCounts = countStudentsPerProject(chromosome);

        for (Project project : projects) {
            int count = projectCounts.getOrDefault(project.getId(), 0);

            if (count < project.getMinCapacity()) {
                // Under minimum capacity
                penalty += capacityPenaltyWeight * (project.getMinCapacity() - count);
            } else if (count > project.getMaxCapacity()) {
                // Over maximum capacity
                penalty += capacityPenaltyWeight * (count - project.getMaxCapacity());
            }
        }

        return penalty;
    }

    /**
     * Calculates the GPA requirement violation penalty.
     * Penalty = 30 * count of students below required GPA.
     *
     * @param chromosome the chromosome to evaluate
     * @return the GPA penalty (positive value to be subtracted)
     */
    public double calculateGpaPenalty(Chromosome chromosome) {
        double penalty = 0.0;

        for (int i = 0; i < chromosome.getLength(); i++) {
            Student student = students.get(i);
            int projectId = chromosome.getAssignment(i);
            Project project = projectMap.get(projectId);

            if (project != null && !project.meetsGpaRequirement(student.getGpa())) {
                penalty += gpaPenaltyWeight;
            }
        }

        return penalty;
    }

    /**
     * Calculates the partner separation penalty.
     * Penalty = 40 * count of separated partner pairs.
     * Note: Each separated pair is counted once (not twice).
     *
     * @param chromosome the chromosome to evaluate
     * @return the partner penalty (positive value to be subtracted)
     */
    public double calculatePartnerPenalty(Chromosome chromosome) {
        double penalty = 0.0;

        for (int i = 0; i < chromosome.getLength(); i++) {
            Student student = students.get(i);

            if (student.hasPartner()) {
                Integer partnerIndex = studentIdToIndex.get(student.getPartnerId());

                // Only count if partner exists and has higher index (to avoid double counting)
                if (partnerIndex != null && partnerIndex > i) {
                    int studentProject = chromosome.getAssignment(i);
                    int partnerProject = chromosome.getAssignment(partnerIndex);

                    if (studentProject != partnerProject) {
                        penalty += partnerPenaltyWeight;
                    }
                }
            }
        }

        return penalty;
    }

    /**
     * Counts students assigned to each project.
     *
     * @param chromosome the chromosome
     * @return map of projectId to student count
     */
    private Map<Integer, Integer> countStudentsPerProject(Chromosome chromosome) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int i = 0; i < chromosome.getLength(); i++) {
            int projectId = chromosome.getAssignment(i);
            counts.merge(projectId, 1, Integer::sum);
        }
        return counts;
    }

    /**
     * Calculates the theoretical maximum fitness.
     * This is the fitness if all students got their first choice
     * with no constraint violations.
     *
     * @return the theoretical maximum fitness
     */
    public double getTheoreticalMaxFitness() {
        return students.size() * Preference.WEIGHT_FIRST_CHOICE;
    }

    /**
     * Gets a detailed breakdown of fitness components for a chromosome.
     *
     * @param chromosome the chromosome to analyze
     * @return a FitnessBreakdown with all component scores
     */
    public FitnessBreakdown getBreakdown(Chromosome chromosome) {
        double preferenceScore = calculatePreferenceScore(chromosome);
        double capacityPenalty = calculateCapacityPenalty(chromosome);
        double gpaPenalty = calculateGpaPenalty(chromosome);
        double partnerPenalty = calculatePartnerPenalty(chromosome);
        double totalFitness = preferenceScore - capacityPenalty - gpaPenalty - partnerPenalty;

        return new FitnessBreakdown(
                preferenceScore,
                capacityPenalty,
                gpaPenalty,
                partnerPenalty,
                totalFitness
        );
    }

    /**
     * Counts how many students got each preference rank.
     *
     * @param chromosome the chromosome to analyze
     * @return array where index 0 = unassigned preferences, 1-5 = rank counts
     */
    public int[] countPreferenceDistribution(Chromosome chromosome) {
        int[] distribution = new int[6]; // 0 = no preference, 1-5 = ranks

        for (int i = 0; i < chromosome.getLength(); i++) {
            int studentId = studentIndexToId.get(i);
            int projectId = chromosome.getAssignment(i);

            Map<Integer, Integer> studentPrefs = preferenceMap.get(studentId);
            if (studentPrefs != null && studentPrefs.containsKey(projectId)) {
                int rank = studentPrefs.get(projectId);
                if (rank >= 1 && rank <= 5) {
                    distribution[rank]++;
                } else {
                    distribution[0]++;
                }
            } else {
                distribution[0]++;
            }
        }

        return distribution;
    }

    // Setters for penalty weights

    /**
     * Sets the capacity penalty weight.
     *
     * @param weight the penalty weight for each unit of capacity violation
     */
    public void setCapacityPenaltyWeight(double weight) {
        this.capacityPenaltyWeight = weight;
    }

    /**
     * Sets the GPA penalty weight.
     *
     * @param weight the penalty weight for each GPA violation
     */
    public void setGpaPenaltyWeight(double weight) {
        this.gpaPenaltyWeight = weight;
    }

    /**
     * Sets the partner separation penalty weight.
     *
     * @param weight the penalty weight for each separated partner pair
     */
    public void setPartnerPenaltyWeight(double weight) {
        this.partnerPenaltyWeight = weight;
    }

    /**
     * Gets the student ID for a given index.
     *
     * @param index the student index
     * @return the student ID
     */
    public int getStudentIdForIndex(int index) {
        return studentIndexToId.get(index);
    }

    /**
     * Gets the student index for a given ID.
     *
     * @param studentId the student ID
     * @return the student index, or null if not found
     */
    public Integer getIndexForStudentId(int studentId) {
        return studentIdToIndex.get(studentId);
    }

    /**
     * Holds a detailed breakdown of fitness components.
     */
    public static class FitnessBreakdown {
        private final double preferenceScore;
        private final double capacityPenalty;
        private final double gpaPenalty;
        private final double partnerPenalty;
        private final double totalFitness;

        public FitnessBreakdown(double preferenceScore, double capacityPenalty,
                                double gpaPenalty, double partnerPenalty, double totalFitness) {
            this.preferenceScore = preferenceScore;
            this.capacityPenalty = capacityPenalty;
            this.gpaPenalty = gpaPenalty;
            this.partnerPenalty = partnerPenalty;
            this.totalFitness = totalFitness;
        }

        public double getPreferenceScore() {
            return preferenceScore;
        }

        public double getCapacityPenalty() {
            return capacityPenalty;
        }

        public double getGpaPenalty() {
            return gpaPenalty;
        }

        public double getPartnerPenalty() {
            return partnerPenalty;
        }

        public double getTotalFitness() {
            return totalFitness;
        }

        public double getTotalPenalty() {
            return capacityPenalty + gpaPenalty + partnerPenalty;
        }

        @Override
        public String toString() {
            return String.format(
                    "FitnessBreakdown{preference=%.2f, capacityPenalty=%.2f, " +
                            "gpaPenalty=%.2f, partnerPenalty=%.2f, total=%.2f}",
                    preferenceScore, capacityPenalty, gpaPenalty, partnerPenalty, totalFitness
            );
        }
    }
}
