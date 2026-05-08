package com.optimatch.algorithm;

import com.optimatch.model.Preference;
import com.optimatch.model.Project;
import com.optimatch.model.Student;

import java.util.*;

/**
 * Checks and repairs constraint violations in chromosomes.
 *
 * Constraints:
 * 1. Capacity: min &lt;= assigned students &lt;= max for each project
 * 2. GPA: student.gpa &gt;= project.requiredGpa
 * 3. Partners: partner pairs must be assigned to the same project
 *
 * Repair operations are preference-aware: when a student has to be
 * reassigned, projects from their preference list are tried first
 * (in rank order) before falling back to any GPA-eligible project.
 */
public class ConstraintChecker {

    private final List<Student> students;
    private final List<Project> projects;
    private final Map<Integer, Project> projectMap;
    private final Map<Integer, Integer> studentIndexToId;
    private final Map<Integer, Integer> studentIdToIndex;
    private final Map<Integer, List<Integer>> studentPreferences; // studentId -> projectIds in rank order
    private final int[] projectIds;
    private final Random random;

    /**
     * Creates a ConstraintChecker without preference information.
     * Repair will fall back to random valid projects.
     *
     * @param students list of all students
     * @param projects list of all projects
     * @param random   random number generator for repair operations
     */
    public ConstraintChecker(List<Student> students, List<Project> projects, Random random) {
        this(students, projects, Collections.emptyList(), random);
    }

    /**
     * Creates a ConstraintChecker with preference information used to
     * guide repair toward higher-preference projects when possible.
     *
     * @param students    list of all students
     * @param projects    list of all projects
     * @param preferences list of all preferences (may be empty)
     * @param random      random number generator for repair operations
     */
    public ConstraintChecker(List<Student> students, List<Project> projects,
                             List<Preference> preferences, Random random) {
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

        // Build preference lookup: studentId -> projectIds ordered by rank
        this.studentPreferences = new HashMap<>();
        Map<Integer, List<Preference>> grouped = new HashMap<>();
        for (Preference pref : preferences) {
            grouped.computeIfAbsent(pref.getStudentId(), k -> new ArrayList<>()).add(pref);
        }
        for (Map.Entry<Integer, List<Preference>> entry : grouped.entrySet()) {
            entry.getValue().sort(Comparator.comparingInt(Preference::getRank));
            List<Integer> orderedProjects = new ArrayList<>(entry.getValue().size());
            for (Preference pref : entry.getValue()) {
                orderedProjects.add(pref.getProjectId());
            }
            studentPreferences.put(entry.getKey(), orderedProjects);
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
     * Applies repairs in order: GPA -&gt; Partners -&gt; Capacity.
     *
     * @param chromosome the chromosome to repair
     * @return true if all violations were successfully repaired
     */
    public boolean repair(Chromosome chromosome) {
        boolean success = true;

        // 1. Fix GPA violations first so partner repair has correct constraints
        success &= repairGpa(chromosome);

        // 2. Bring partner pairs together (preserves both GPAs)
        success &= repairPartners(chromosome);

        // 3. Rebalance capacity last
        success &= repairCapacity(chromosome);

        // Update validity flag
        chromosome.setValid(checkAll(chromosome));

        return success;
    }

    /**
     * Repairs partner violations by assigning partners to the same project.
     * The chosen project must satisfy both partners' GPA requirements.
     *
     * @param chromosome the chromosome to repair
     * @return true if all partner violations were repaired
     */
    public boolean repairPartners(Chromosome chromosome) {
        boolean allRepaired = true;
        for (int i = 0; i < chromosome.getLength(); i++) {
            Student student = students.get(i);
            if (!student.hasPartner()) {
                continue;
            }
            Integer partnerIndex = studentIdToIndex.get(student.getPartnerId());
            if (partnerIndex == null || partnerIndex <= i) {
                continue;
            }
            int studentProject = chromosome.getAssignment(i);
            int partnerProject = chromosome.getAssignment(partnerIndex);
            if (studentProject == partnerProject) {
                continue;
            }

            Student partner = students.get(partnerIndex);

            // Try keeping one of the existing assignments if it works for both
            Project sp = projectMap.get(studentProject);
            Project pp = projectMap.get(partnerProject);
            Integer chosen = null;
            if (sp != null && sp.meetsGpaRequirement(student.getGpa())
                    && sp.meetsGpaRequirement(partner.getGpa())) {
                chosen = studentProject;
            } else if (pp != null && pp.meetsGpaRequirement(student.getGpa())
                    && pp.meetsGpaRequirement(partner.getGpa())) {
                chosen = partnerProject;
            } else {
                chosen = findValidProjectForBoth(student, partner);
            }

            if (chosen != null) {
                chromosome.setAssignment(i, chosen);
                chromosome.setAssignment(partnerIndex, chosen);
            } else {
                allRepaired = false;
            }
        }
        return allRepaired;
    }

    /**
     * Repairs GPA violations by moving students to projects whose
     * requirements they meet. If the student has a partner, both are
     * relocated to a project that satisfies both GPAs.
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

            if (project == null || project.meetsGpaRequirement(student.getGpa())) {
                continue;
            }

            Integer validProject;
            Integer partnerIndex = student.hasPartner()
                    ? studentIdToIndex.get(student.getPartnerId())
                    : null;

            if (partnerIndex != null) {
                Student partner = students.get(partnerIndex);
                validProject = findValidProjectForBoth(student, partner);
            } else {
                validProject = findValidProjectForStudent(student);
            }

            if (validProject == null) {
                allRepaired = false;
                continue;
            }

            chromosome.setAssignment(i, validProject);
            if (partnerIndex != null) {
                chromosome.setAssignment(partnerIndex, validProject);
            }
        }

        return allRepaired;
    }

    /**
     * Repairs capacity violations by moving students from over-capacity
     * projects to under-capacity ones. Bounded by a hard iteration cap so
     * the loop cannot run unboundedly even if move semantics change.
     *
     * @param chromosome the chromosome to repair
     * @return true if all capacity violations were resolved
     */
    public boolean repairCapacity(Chromosome chromosome) {
        int maxIterations = Math.max(students.size() * 4, 64);

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            Map<Integer, Integer> projectCounts = countStudentsPerProject(chromosome);

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
                return true;
            }

            boolean moved = false;
            for (int overflowProjectId : overflowProjects) {
                int[] inProject = chromosome.getStudentsInProject(overflowProjectId);
                for (int j = inProject.length - 1; j >= 0; j--) {
                    int studentIndex = inProject[j];
                    Student student = students.get(studentIndex);

                    // Skip partnered students; partner repair handles them
                    if (student.hasPartner()) {
                        continue;
                    }

                    // Prefer underflow projects that fit the student's GPA
                    Integer target = null;
                    for (int underId : underflowProjects) {
                        Project under = projectMap.get(underId);
                        if (under != null && under.meetsGpaRequirement(student.getGpa())) {
                            target = underId;
                            break;
                        }
                    }
                    // Otherwise any project with free capacity
                    if (target == null) {
                        target = findValidProjectWithCapacity(student, chromosome);
                    }

                    if (target != null && target != overflowProjectId) {
                        chromosome.setAssignment(studentIndex, target);
                        moved = true;
                        break;
                    }
                }
                if (moved) {
                    break;
                }
            }

            if (!moved) {
                // No progress possible without breaking GPA — give up
                return checkCapacity(chromosome);
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
     * Finds a valid project for a student. Preference list is consulted
     * first (in rank order); only if no preferred project is GPA-eligible
     * the search falls back to a random eligible project.
     *
     * @param student the student
     * @return a valid project ID, or null if none found
     */
    private Integer findValidProjectForStudent(Student student) {
        List<Integer> prefs = studentPreferences.get(student.getId());
        if (prefs != null) {
            for (int projectId : prefs) {
                Project p = projectMap.get(projectId);
                if (p != null && p.meetsGpaRequirement(student.getGpa())) {
                    return projectId;
                }
            }
        }

        List<Integer> eligible = new ArrayList<>();
        for (Project project : projects) {
            if (project.meetsGpaRequirement(student.getGpa())) {
                eligible.add(project.getId());
            }
        }
        if (eligible.isEmpty()) {
            return null;
        }
        return eligible.get(random.nextInt(eligible.size()));
    }

    /**
     * Finds a valid project for both partners. The lower of the two GPAs
     * defines the constraint. Preferences from either partner are
     * preferred (highest combined rank wins).
     *
     * @param student1 first student
     * @param student2 second student
     * @return a valid project ID, or null if none found
     */
    private Integer findValidProjectForBoth(Student student1, Student student2) {
        double minGpa = Math.min(student1.getGpa(), student2.getGpa());

        List<Integer> prefs1 = studentPreferences.getOrDefault(student1.getId(), Collections.emptyList());
        List<Integer> prefs2 = studentPreferences.getOrDefault(student2.getId(), Collections.emptyList());

        // Combined ranking: lower combined rank means higher mutual preference
        Map<Integer, Integer> combinedRank = new HashMap<>();
        for (int i = 0; i < prefs1.size(); i++) {
            combinedRank.merge(prefs1.get(i), i + 1, Integer::sum);
        }
        for (int i = 0; i < prefs2.size(); i++) {
            combinedRank.merge(prefs2.get(i), i + 1, Integer::sum);
        }

        List<Map.Entry<Integer, Integer>> ranked = new ArrayList<>(combinedRank.entrySet());
        ranked.sort(Map.Entry.comparingByValue());
        for (Map.Entry<Integer, Integer> entry : ranked) {
            Project p = projectMap.get(entry.getKey());
            if (p != null && p.meetsGpaRequirement(minGpa)) {
                return entry.getKey();
            }
        }

        List<Integer> eligible = new ArrayList<>();
        for (Project project : projects) {
            if (project.meetsGpaRequirement(minGpa)) {
                eligible.add(project.getId());
            }
        }
        if (eligible.isEmpty()) {
            return null;
        }
        return eligible.get(random.nextInt(eligible.size()));
    }

    /**
     * Finds a valid project with available capacity. Preferences are
     * consulted first.
     *
     * @param student    the student
     * @param chromosome current chromosome (to check counts)
     * @return a valid project ID, or null if none found
     */
    private Integer findValidProjectWithCapacity(Student student, Chromosome chromosome) {
        Map<Integer, Integer> counts = countStudentsPerProject(chromosome);

        List<Integer> prefs = studentPreferences.get(student.getId());
        if (prefs != null) {
            for (int projectId : prefs) {
                Project p = projectMap.get(projectId);
                if (p == null) {
                    continue;
                }
                int count = counts.getOrDefault(projectId, 0);
                if (p.meetsGpaRequirement(student.getGpa()) && count < p.getMaxCapacity()) {
                    return projectId;
                }
            }
        }

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
