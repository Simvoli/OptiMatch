package com.optimatch.algorithm;

import com.optimatch.model.Project;
import com.optimatch.model.Student;

import java.util.*;

/**
 * Checks and repairs constraint violations in chromosomes.
 *
 * Constraints:
 * 1. Capacity: min <= assigned students <= max for each project
 * 2. GPA: student.gpa >= project.requiredGpa
 * 3. Partners: partner pairs must be assigned to the same project
 */
public class ConstraintChecker {

    private final List<Student> students;
    private final List<Project> projects;
    private final Map<Integer, Project> projectMap;
    private final Map<Integer, Integer> studentIndexToId;
    private final Map<Integer, Integer> studentIdToIndex;
    private final int[] projectIds;
    private final Random random;

    /**
     * Creates a ConstraintChecker with the given data.
     *
     * @param students list of all students
     * @param projects list of all projects
     * @param random   random number generator for repair operations
     */
    public ConstraintChecker(List<Student> students, List<Project> projects, Random random) {
        this.students = students;
        this.projects = projects;
        this.random = random;

        // Build project lookup map
        this.projectMap = new HashMap<>();
        this.projectIds = new int[projects.size()];
        for (int i = 0; i < projects.size(); i++) {
            Project project = projects.get(i);
            projectMap.put(project.getId(), project);
            projectIds[i] = project.getId();
        }

        // Build student index mappings
        this.studentIndexToId = new HashMap<>();
        this.studentIdToIndex = new HashMap<>();
        for (int i = 0; i < students.size(); i++) {
            int studentId = students.get(i).getId();
            studentIndexToId.put(i, studentId);
            studentIdToIndex.put(studentId, i);
        }
    }

    // ==================== Main Check Methods ====================

    /**
     * Checks all constraints and updates the chromosome's validity flag.
     *
     * @param chromosome the chromosome to check
     * @return true if all constraints are satisfied
     */
    public boolean checkAll(Chromosome chromosome) {
        boolean valid = checkCapacity(chromosome)
                && checkGpa(chromosome)
                && checkPartners(chromosome);
        chromosome.setValid(valid);
        return valid;
    }

    /**
     * Gets a detailed report of all constraint violations.
     *
     * @param chromosome the chromosome to check
     * @return ConstraintViolations containing all violations
     */
    public ConstraintViolations getViolations(Chromosome chromosome) {
        ConstraintViolations violations = new ConstraintViolations();

        // Check capacity violations
        Map<Integer, Integer> projectCounts = countStudentsPerProject(chromosome);
        for (Project project : projects) {
            int count = projectCounts.getOrDefault(project.getId(), 0);
            if (count < project.getMinCapacity()) {
                violations.addCapacityViolation(project.getId(), count,
                        project.getMinCapacity(), project.getMaxCapacity(), true);
            } else if (count > project.getMaxCapacity()) {
                violations.addCapacityViolation(project.getId(), count,
                        project.getMinCapacity(), project.getMaxCapacity(), false);
            }
        }

        // Check GPA violations
        for (int i = 0; i < chromosome.getLength(); i++) {
            Student student = students.get(i);
            int projectId = chromosome.getAssignment(i);
            Project project = projectMap.get(projectId);

            if (project != null && !project.meetsGpaRequirement(student.getGpa())) {
                violations.addGpaViolation(student.getId(), projectId,
                        student.getGpa(), project.getRequiredGpa());
            }
        }

        // Check partner violations
        for (int i = 0; i < chromosome.getLength(); i++) {
            Student student = students.get(i);
            if (student.hasPartner()) {
                Integer partnerIndex = studentIdToIndex.get(student.getPartnerId());
                if (partnerIndex != null && partnerIndex > i) { // Avoid double counting
                    int studentProject = chromosome.getAssignment(i);
                    int partnerProject = chromosome.getAssignment(partnerIndex);
                    if (studentProject != partnerProject) {
                        violations.addPartnerViolation(student.getId(),
                                student.getPartnerId(), studentProject, partnerProject);
                    }
                }
            }
        }

        return violations;
    }

    // ==================== Individual Constraint Checks ====================

    /**
     * Checks if all projects satisfy capacity constraints.
     *
     * @param chromosome the chromosome to check
     * @return true if all projects are within capacity limits
     */
    public boolean checkCapacity(Chromosome chromosome) {
        Map<Integer, Integer> projectCounts = countStudentsPerProject(chromosome);

        for (Project project : projects) {
            int count = projectCounts.getOrDefault(project.getId(), 0);
            if (count < project.getMinCapacity() || count > project.getMaxCapacity()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if all students meet GPA requirements for their assigned projects.
     *
     * @param chromosome the chromosome to check
     * @return true if all GPA requirements are met
     */
    public boolean checkGpa(Chromosome chromosome) {
        for (int i = 0; i < chromosome.getLength(); i++) {
            Student student = students.get(i);
            int projectId = chromosome.getAssignment(i);
            Project project = projectMap.get(projectId);

            if (project != null && !project.meetsGpaRequirement(student.getGpa())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if all partner pairs are assigned to the same project.
     *
     * @param chromosome the chromosome to check
     * @return true if all partners are together
     */
    public boolean checkPartners(Chromosome chromosome) {
        for (int i = 0; i < chromosome.getLength(); i++) {
            Student student = students.get(i);
            if (student.hasPartner()) {
                Integer partnerIndex = studentIdToIndex.get(student.getPartnerId());
                if (partnerIndex != null) {
                    int studentProject = chromosome.getAssignment(i);
                    int partnerProject = chromosome.getAssignment(partnerIndex);
                    if (studentProject != partnerProject) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // ==================== Repair Methods ====================

    /**
     * Repairs all constraint violations in a chromosome.
     * Applies repairs in order: Partners -> GPA -> Capacity
     *
     * @param chromosome the chromosome to repair
     * @return true if all violations were successfully repaired
     */
    public boolean repair(Chromosome chromosome) {
        boolean success = true;

        // 1. Repair partner violations first (assigns partners together)
        success &= repairPartners(chromosome);

        // 2. Repair GPA violations (move students to valid projects)
        success &= repairGpa(chromosome);

        // 3. Repair capacity violations (rebalance assignments)
        success &= repairCapacity(chromosome);

        // Update validity flag
        chromosome.setValid(checkAll(chromosome));

        return success;
    }

    /**
     * Repairs partner violations by assigning partners to the same project.
     * Uses the project of the partner with higher GPA or first partner if equal.
     *
     * @param chromosome the chromosome to repair
     * @return true if repair was successful
     */
    public boolean repairPartners(Chromosome chromosome) {
        for (int i = 0; i < chromosome.getLength(); i++) {
            Student student = students.get(i);
            if (student.hasPartner()) {
                Integer partnerIndex = studentIdToIndex.get(student.getPartnerId());
                if (partnerIndex != null && partnerIndex > i) {
                    int studentProject = chromosome.getAssignment(i);
                    int partnerProject = chromosome.getAssignment(partnerIndex);

                    if (studentProject != partnerProject) {
                        Student partner = students.get(partnerIndex);
                        // Choose project - prefer student with higher GPA
                        int chosenProject;
                        if (student.getGpa() >= partner.getGpa()) {
                            chosenProject = studentProject;
                        } else {
                            chosenProject = partnerProject;
                        }

                        // Check if chosen project meets both GPAs
                        Project project = projectMap.get(chosenProject);
                        if (project != null) {
                            boolean studentMeetsGpa = project.meetsGpaRequirement(student.getGpa());
                            boolean partnerMeetsGpa = project.meetsGpaRequirement(partner.getGpa());

                            if (studentMeetsGpa && partnerMeetsGpa) {
                                chromosome.setAssignment(i, chosenProject);
                                chromosome.setAssignment(partnerIndex, chosenProject);
                            } else {
                                // Find a project that works for both
                                Integer validProject = findValidProjectForBoth(student, partner);
                                if (validProject != null) {
                                    chromosome.setAssignment(i, validProject);
                                    chromosome.setAssignment(partnerIndex, validProject);
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Repairs GPA violations by moving students to valid projects.
     *
     * @param chromosome the chromosome to repair
     * @return true if all GPA violations were repaired
     */
    public boolean repairGpa(Chromosome chromosome) {
        boolean allRepaired = true;

        for (int i = 0; i < chromosome.getLength(); i++) {
            Student student = students.get(i);
            int projectId = chromosome.getAssignment(i);
            Project project = projectMap.get(projectId);

            if (project != null && !project.meetsGpaRequirement(student.getGpa())) {
                // Find a valid project for this student
                Integer validProject = findValidProjectForStudent(student);
                if (validProject != null) {
                    chromosome.setAssignment(i, validProject);

                    // If student has partner, move partner too
                    if (student.hasPartner()) {
                        Integer partnerIndex = studentIdToIndex.get(student.getPartnerId());
                        if (partnerIndex != null) {
                            chromosome.setAssignment(partnerIndex, validProject);
                        }
                    }
                } else {
                    allRepaired = false;
                }
            }
        }

        return allRepaired;
    }

    /**
     * Repairs capacity violations by rebalancing assignments.
     *
     * @param chromosome the chromosome to repair
     * @return true if all capacity violations were repaired
     */
    public boolean repairCapacity(Chromosome chromosome) {
        int maxIterations = students.size() * 2;
        int iterations = 0;

        while (iterations < maxIterations) {
            Map<Integer, Integer> projectCounts = countStudentsPerProject(chromosome);

            // Find overflowing and underflowing projects
            List<Integer> overflowProjects = new ArrayList<>();
            List<Integer> underflowProjects = new ArrayList<>();

            for (Project project : projects) {
                int count = projectCounts.getOrDefault(project.getId(), 0);
                if (count > project.getMaxCapacity()) {
                    overflowProjects.add(project.getId());
                } else if (count < project.getMinCapacity()) {
                    underflowProjects.add(project.getId());
                }
            }

            if (overflowProjects.isEmpty() && underflowProjects.isEmpty()) {
                return true; // All fixed
            }

            // Move students from overflow to underflow
            boolean moved = false;
            for (int overflowProjectId : overflowProjects) {
                int[] studentsInProject = chromosome.getStudentsInProject(overflowProjectId);
                Project overflowProject = projectMap.get(overflowProjectId);
                int excess = studentsInProject.length - overflowProject.getMaxCapacity();

                for (int j = 0; j < excess && j < studentsInProject.length; j++) {
                    int studentIndex = studentsInProject[studentsInProject.length - 1 - j];
                    Student student = students.get(studentIndex);

                    // Skip if has partner (handled separately)
                    if (student.hasPartner()) {
                        continue;
                    }

                    // Try to move to underflow project
                    for (int underflowProjectId : underflowProjects) {
                        Project underflowProject = projectMap.get(underflowProjectId);
                        if (underflowProject.meetsGpaRequirement(student.getGpa())) {
                            chromosome.setAssignment(studentIndex, underflowProjectId);
                            moved = true;
                            break;
                        }
                    }

                    if (moved) {
                        break;
                    }
                }

                if (moved) {
                    break;
                }
            }

            if (!moved) {
                // Try random reassignment
                if (!overflowProjects.isEmpty()) {
                    int overflowProjectId = overflowProjects.get(0);
                    int[] studentsInProject = chromosome.getStudentsInProject(overflowProjectId);
                    if (studentsInProject.length > 0) {
                        int studentIndex = studentsInProject[random.nextInt(studentsInProject.length)];
                        Student student = students.get(studentIndex);
                        if (!student.hasPartner()) {
                            Integer newProject = findValidProjectWithCapacity(student, chromosome);
                            if (newProject != null) {
                                chromosome.setAssignment(studentIndex, newProject);
                                moved = true;
                            }
                        }
                    }
                }
            }

            if (!moved) {
                iterations++;
            }
        }

        return checkCapacity(chromosome);
    }

    // ==================== Helper Methods ====================

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
     * Finds a valid project for a student (meets GPA requirement).
     *
     * @param student the student
     * @return a valid project ID, or null if none found
     */
    private Integer findValidProjectForStudent(Student student) {
        List<Integer> validProjects = new ArrayList<>();
        for (Project project : projects) {
            if (project.meetsGpaRequirement(student.getGpa())) {
                validProjects.add(project.getId());
            }
        }
        if (validProjects.isEmpty()) {
            return null;
        }
        return validProjects.get(random.nextInt(validProjects.size()));
    }

    /**
     * Finds a valid project for both partners.
     *
     * @param student1 first student
     * @param student2 second student
     * @return a valid project ID, or null if none found
     */
    private Integer findValidProjectForBoth(Student student1, Student student2) {
        double minGpa = Math.min(student1.getGpa(), student2.getGpa());
        List<Integer> validProjects = new ArrayList<>();
        for (Project project : projects) {
            if (project.meetsGpaRequirement(minGpa)) {
                validProjects.add(project.getId());
            }
        }
        if (validProjects.isEmpty()) {
            return null;
        }
        return validProjects.get(random.nextInt(validProjects.size()));
    }

    /**
     * Finds a valid project with available capacity.
     *
     * @param student    the student
     * @param chromosome current chromosome (to check counts)
     * @return a valid project ID, or null if none found
     */
    private Integer findValidProjectWithCapacity(Student student, Chromosome chromosome) {
        Map<Integer, Integer> counts = countStudentsPerProject(chromosome);
        List<Integer> validProjects = new ArrayList<>();

        for (Project project : projects) {
            int count = counts.getOrDefault(project.getId(), 0);
            if (project.meetsGpaRequirement(student.getGpa())
                    && count < project.getMaxCapacity()) {
                validProjects.add(project.getId());
            }
        }

        if (validProjects.isEmpty()) {
            return null;
        }
        return validProjects.get(random.nextInt(validProjects.size()));
    }

    /**
     * Gets the array of available project IDs.
     *
     * @return array of project IDs
     */
    public int[] getProjectIds() {
        return projectIds.clone();
    }

    // ==================== Violation Report Class ====================

    /**
     * Contains detailed information about all constraint violations.
     */
    public static class ConstraintViolations {
        private final List<CapacityViolation> capacityViolations = new ArrayList<>();
        private final List<GpaViolation> gpaViolations = new ArrayList<>();
        private final List<PartnerViolation> partnerViolations = new ArrayList<>();

        public void addCapacityViolation(int projectId, int actual, int min, int max, boolean isUnder) {
            capacityViolations.add(new CapacityViolation(projectId, actual, min, max, isUnder));
        }

        public void addGpaViolation(int studentId, int projectId, double studentGpa, double requiredGpa) {
            gpaViolations.add(new GpaViolation(studentId, projectId, studentGpa, requiredGpa));
        }

        public void addPartnerViolation(int studentId, int partnerId, int studentProject, int partnerProject) {
            partnerViolations.add(new PartnerViolation(studentId, partnerId, studentProject, partnerProject));
        }

        public List<CapacityViolation> getCapacityViolations() {
            return capacityViolations;
        }

        public List<GpaViolation> getGpaViolations() {
            return gpaViolations;
        }

        public List<PartnerViolation> getPartnerViolations() {
            return partnerViolations;
        }

        public int getTotalViolations() {
            return capacityViolations.size() + gpaViolations.size() + partnerViolations.size();
        }

        public boolean hasViolations() {
            return getTotalViolations() > 0;
        }

        @Override
        public String toString() {
            return String.format("Violations{capacity=%d, gpa=%d, partner=%d}",
                    capacityViolations.size(), gpaViolations.size(), partnerViolations.size());
        }
    }

    public static class CapacityViolation {
        public final int projectId;
        public final int actualCount;
        public final int minCapacity;
        public final int maxCapacity;
        public final boolean isUnderflow;

        public CapacityViolation(int projectId, int actualCount, int minCapacity, int maxCapacity, boolean isUnderflow) {
            this.projectId = projectId;
            this.actualCount = actualCount;
            this.minCapacity = minCapacity;
            this.maxCapacity = maxCapacity;
            this.isUnderflow = isUnderflow;
        }

        @Override
        public String toString() {
            return String.format("Capacity[project=%d, actual=%d, range=%d-%d, %s]",
                    projectId, actualCount, minCapacity, maxCapacity,
                    isUnderflow ? "UNDER" : "OVER");
        }
    }

    public static class GpaViolation {
        public final int studentId;
        public final int projectId;
        public final double studentGpa;
        public final double requiredGpa;

        public GpaViolation(int studentId, int projectId, double studentGpa, double requiredGpa) {
            this.studentId = studentId;
            this.projectId = projectId;
            this.studentGpa = studentGpa;
            this.requiredGpa = requiredGpa;
        }

        @Override
        public String toString() {
            return String.format("GPA[student=%d, project=%d, has=%.2f, needs=%.2f]",
                    studentId, projectId, studentGpa, requiredGpa);
        }
    }

    public static class PartnerViolation {
        public final int studentId;
        public final int partnerId;
        public final int studentProject;
        public final int partnerProject;

        public PartnerViolation(int studentId, int partnerId, int studentProject, int partnerProject) {
            this.studentId = studentId;
            this.partnerId = partnerId;
            this.studentProject = studentProject;
            this.partnerProject = partnerProject;
        }

        @Override
        public String toString() {
            return String.format("Partner[student=%d->project=%d, partner=%d->project=%d]",
                    studentId, studentProject, partnerId, partnerProject);
        }
    }
}
