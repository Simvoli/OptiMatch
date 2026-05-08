package com.optimatch.algorithm;

import com.optimatch.model.Preference;
import com.optimatch.model.Project;
import com.optimatch.model.Student;

import java.util.*;

// checks and repairs the three hard constraints: capacity, gpa, partners
// repair tries to use the student's own preferences first, falls back to any feasible project
public class ConstraintChecker {

    private final List<Student> students;
    private final List<Project> projects;
    private final Map<Integer, Project> projectMap;
    private final Map<Integer, Integer> studentIndexToId;
    private final Map<Integer, Integer> studentIdToIndex;
    // studentId -> project ids ordered by rank
    private final Map<Integer, List<Integer>> studentPreferences;
    private final int[] projectIds;
    private final Random random;

    // checker without preferences (repair falls back to random valid project)
    public ConstraintChecker(List<Student> students, List<Project> projects, Random random) {
        this(students, projects, Collections.emptyList(), random);
    }

    // checker with preferences, repair will prefer ranked projects
    public ConstraintChecker(List<Student> students, List<Project> projects,
                             List<Preference> preferences, Random random) {
        this.students = students;
        this.projects = projects;
        this.random = random;

        // project id -> project, plus a flat list of project ids
        this.projectMap = new HashMap<>();
        this.projectIds = new int[projects.size()];
        for (int i = 0; i < projects.size(); i++) {
            Project project = projects.get(i);
            projectMap.put(project.getId(), project);
            projectIds[i] = project.getId();
        }

        // chromosome index <-> student id
        this.studentIndexToId = new HashMap<>();
        this.studentIdToIndex = new HashMap<>();
        for (int i = 0; i < students.size(); i++) {
            int studentId = students.get(i).getId();
            studentIndexToId.put(i, studentId);
            studentIdToIndex.put(studentId, i);
        }

        // student id -> [projectId by rank]
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

    // run all three checks and update the chromosome's valid flag
    public boolean checkAll(Chromosome chromosome) {
        boolean valid = checkCapacity(chromosome)
                && checkGpa(chromosome)
                && checkPartners(chromosome);
        chromosome.setValid(valid);
        return valid;
    }

    // ==================== Individual Constraint Checks ====================

    // every project within min..max
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

    // every student meets gpa of their assigned project
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

    // every partner pair sits on the same project
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

    // RU: порядок репэйра важен:
    // 1) сначала чиним gpa (бессмысленно тащить партнёра туда, где он провалится по gpa)
    // 2) потом сводим партнёров вместе (репэйр gpa уже учёл их пары)
    // 3) и только в конце балансируем capacity
    public boolean repair(Chromosome chromosome) {
        boolean success = true;

        success &= repairGpa(chromosome);
        success &= repairPartners(chromosome);
        success &= repairCapacity(chromosome);

        chromosome.setValid(checkAll(chromosome));

        return success;
    }

    // move partners onto the same project (must satisfy both gpas)
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

            // try to keep one of the existing assignments if it works for both
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

    // move students whose gpa is below their project requirement
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

    // shift students from over-capacity projects to under-capacity ones
    // RU: жёсткий лимит итераций нужен на случай, если перемещения зацикливаются
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

                    // skip partnered students, partner repair owns them
                    if (student.hasPartner()) {
                        continue;
                    }

                    // first try underflow projects that fit the student's gpa
                    Integer target = null;
                    for (int underId : underflowProjects) {
                        Project under = projectMap.get(underId);
                        if (under != null && under.meetsGpaRequirement(student.getGpa())) {
                            target = underId;
                            break;
                        }
                    }
                    // fall back to any project with free capacity
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
                // no progress without breaking gpa, give up
                return checkCapacity(chromosome);
            }
        }

        return checkCapacity(chromosome);
    }

    // ==================== Helper Methods ====================

    // helper: project id -> assigned student count
    private Map<Integer, Integer> countStudentsPerProject(Chromosome chromosome) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int i = 0; i < chromosome.getLength(); i++) {
            int projectId = chromosome.getAssignment(i);
            counts.merge(projectId, 1, Integer::sum);
        }
        return counts;
    }

    // pick a feasible project for one student, preferring their own preferences
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

    // pick a feasible project for both partners
    // RU: ограничение - минимальное gpa из двух; проекты ранжируются суммой рангов в их предпочтениях
    private Integer findValidProjectForBoth(Student student1, Student student2) {
        double minGpa = Math.min(student1.getGpa(), student2.getGpa());

        List<Integer> prefs1 = studentPreferences.getOrDefault(student1.getId(), Collections.emptyList());
        List<Integer> prefs2 = studentPreferences.getOrDefault(student2.getId(), Collections.emptyList());

        // меньшая сумма рангов значит обоюдно более желанный проект
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

    // pick a feasible project that still has free slots
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

    // copy of all known project ids
    public int[] getProjectIds() {
        return projectIds.clone();
    }

}
