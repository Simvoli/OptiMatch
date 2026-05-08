package com.optimatch.algorithm;

import com.optimatch.model.Preference;
import com.optimatch.model.Project;
import com.optimatch.model.Student;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// fitness = sum of preference weights minus penalties for capacity, gpa and split partners
// higher is better
public class FitnessCalculator {

    // default penalty weights, can be overwritten in tests
    private static final double DEFAULT_CAPACITY_PENALTY = 50.0;
    private static final double DEFAULT_GPA_PENALTY = 30.0;
    private static final double DEFAULT_PARTNER_PENALTY = 40.0;

    private final List<Student> students;
    private final List<Project> projects;
    private final Map<Integer, Project> projectMap;
    // studentId -> (projectId -> rank)
    private final Map<Integer, Map<Integer, Integer>> preferenceMap;
    private final Map<Integer, Integer> studentIndexToId;
    private final Map<Integer, Integer> studentIdToIndex;

    private double capacityPenaltyWeight;
    private double gpaPenaltyWeight;
    private double partnerPenaltyWeight;

    // build all lookup maps once so per-chromosome fitness stays cheap
    public FitnessCalculator(List<Student> students, List<Project> projects, List<Preference> preferences) {
        this.students = students;
        this.projects = projects;
        this.capacityPenaltyWeight = DEFAULT_CAPACITY_PENALTY;
        this.gpaPenaltyWeight = DEFAULT_GPA_PENALTY;
        this.partnerPenaltyWeight = DEFAULT_PARTNER_PENALTY;

        // project id -> project
        this.projectMap = new HashMap<>();
        for (Project project : projects) {
            projectMap.put(project.getId(), project);
        }

        // chromosome index <-> student id
        this.studentIndexToId = new HashMap<>();
        this.studentIdToIndex = new HashMap<>();
        for (int i = 0; i < students.size(); i++) {
            int studentId = students.get(i).getId();
            studentIndexToId.put(i, studentId);
            studentIdToIndex.put(studentId, i);
        }

        // student id -> (project id -> rank)
        this.preferenceMap = new HashMap<>();
        for (Preference pref : preferences) {
            preferenceMap
                    .computeIfAbsent(pref.getStudentId(), k -> new HashMap<>())
                    .put(pref.getProjectId(), pref.getRank());
        }
    }

    // total fitness for a chromosome, also caches the value on the chromosome itself
    public double calculateFitness(Chromosome chromosome) {
        double preferenceScore = calculatePreferenceScore(chromosome);
        double capacityPenalty = calculateCapacityPenalty(chromosome);
        double gpaPenalty = calculateGpaPenalty(chromosome);
        double partnerPenalty = calculatePartnerPenalty(chromosome);

        double fitness = preferenceScore - capacityPenalty - gpaPenalty - partnerPenalty;

        chromosome.setFitness(fitness);
        return fitness;
    }

    // sum of preference weights, no preference contributes 0
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
        }

        return score;
    }

    // penalty for projects under min or over max capacity
    public double calculateCapacityPenalty(Chromosome chromosome) {
        double penalty = 0.0;

        Map<Integer, Integer> projectCounts = countStudentsPerProject(chromosome);

        for (Project project : projects) {
            int count = projectCounts.getOrDefault(project.getId(), 0);

            if (count < project.getMinCapacity()) {
                penalty += capacityPenaltyWeight * (project.getMinCapacity() - count);
            } else if (count > project.getMaxCapacity()) {
                penalty += capacityPenaltyWeight * (count - project.getMaxCapacity());
            }
        }

        return penalty;
    }

    // penalty for students assigned to projects above their gpa
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

    // penalty for partner pairs assigned to different projects
    public double calculatePartnerPenalty(Chromosome chromosome) {
        double penalty = 0.0;

        for (int i = 0; i < chromosome.getLength(); i++) {
            Student student = students.get(i);

            if (student.hasPartner()) {
                Integer partnerIndex = studentIdToIndex.get(student.getPartnerId());

                // RU: учитываем пару только когда смотрим со стороны меньшего индекса,
                // иначе одна и та же пара даст штраф дважды
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

    // helper: project id -> assigned student count
    private Map<Integer, Integer> countStudentsPerProject(Chromosome chromosome) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int i = 0; i < chromosome.getLength(); i++) {
            int projectId = chromosome.getAssignment(i);
            counts.merge(projectId, 1, Integer::sum);
        }
        return counts;
    }

    // number of students seen by the calculator
    public int getStudentCount() {
        return students.size();
    }

    // detailed fitness breakdown for inspection
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

    // counts per rank: index 0 = no preference, 1..5 = ranks
    public int[] countPreferenceDistribution(Chromosome chromosome) {
        int[] distribution = new int[6];

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

    // breakdown of fitness components, useful for debugging and tests
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

        // raw preference sum
        public double getPreferenceScore() {
            return preferenceScore;
        }

        // capacity penalty contribution
        public double getCapacityPenalty() {
            return capacityPenalty;
        }

        // gpa penalty contribution
        public double getGpaPenalty() {
            return gpaPenalty;
        }

        // partner penalty contribution
        public double getPartnerPenalty() {
            return partnerPenalty;
        }

        // final fitness value
        public double getTotalFitness() {
            return totalFitness;
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
