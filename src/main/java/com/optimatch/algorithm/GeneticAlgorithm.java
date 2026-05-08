package com.optimatch.algorithm;

import com.optimatch.model.GenerationStats;
import com.optimatch.model.Preference;
import com.optimatch.model.Project;
import com.optimatch.model.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// main GA loop: init population, evaluate, evolve generation by generation, return best
public class GeneticAlgorithm {

    // operators
    private final FitnessCalculator fitnessCalculator;
    private final SelectionOperator selectionOperator;
    private final CrossoverOperator crossoverOperator;
    private final MutationOperator mutationOperator;
    private final ElitismOperator elitismOperator;
    private final ConstraintChecker constraintChecker;
    private final Random random;

    // settings
    private final GeneticAlgorithmConfig config;

    // mutable state for the running algorithm
    private Population population;
    private Chromosome bestEver;
    private int currentGeneration;
    private volatile boolean isStopped;
    private volatile boolean isRunning;

    // per-generation history
    private final List<GenerationStats> generationHistory;
    private long startTimeMs;
    private long endTimeMs;

    // optional callback after each generation
    private GenerationCallback generationCallback;

    // GA with default config
    public GeneticAlgorithm(List<Student> students, List<Project> projects, List<Preference> preferences) {
        this(students, projects, preferences, new GeneticAlgorithmConfig());
    }

    // GA with explicit config
    public GeneticAlgorithm(List<Student> students, List<Project> projects,
                            List<Preference> preferences, GeneticAlgorithmConfig config) {
        config.validate();
        this.config = config;
        this.random = config.getSeed() != null ? new Random(config.getSeed()) : new Random();

        // operators that need data
        this.fitnessCalculator = new FitnessCalculator(students, projects, preferences);
        this.constraintChecker = new ConstraintChecker(students, projects, preferences, random);

        this.selectionOperator = new SelectionOperator(random);
        this.selectionOperator.setTournamentSize(config.getTournamentSize());

        this.crossoverOperator = new CrossoverOperator(random);
        this.crossoverOperator.setCrossoverRate(config.getCrossoverRate());

        this.mutationOperator = new MutationOperator(random);
        this.mutationOperator.setMutationRate(config.getMutationRate());

        this.elitismOperator = new ElitismOperator(config.getElitePercentage());

        // initial state
        this.generationHistory = new ArrayList<>();
        this.currentGeneration = 0;
        this.isRunning = false;
        this.isStopped = false;
    }

    // run the full GA loop and return the best chromosome found
    public Chromosome run() {
        isRunning = true;
        isStopped = false;
        startTimeMs = System.currentTimeMillis();

        try {
            initializePopulation();
            evaluatePopulation(population);

            bestEver = population.getBest().copy();
            recordGenerationStats();

            while (!shouldTerminate()) {
                evolveOneGeneration();
                currentGeneration++;

                Chromosome currentBest = population.getBest();
                if (currentBest.getFitness() > bestEver.getFitness()) {
                    bestEver = currentBest.copy();
                }

                recordGenerationStats();

                if (generationCallback != null) {
                    generationCallback.onGeneration(currentGeneration, population, bestEver);
                }
            }

        } finally {
            endTimeMs = System.currentTimeMillis();
            isRunning = false;
        }

        return bestEver;
    }

    // build a random starting population, optionally repair it
    private void initializePopulation() {
        int numStudents = fitnessCalculator.getStudentCount();
        int[] projectIds = constraintChecker.getProjectIds();

        population = Population.createRandom(
                config.getPopulationSize(),
                numStudents,
                projectIds,
                random
        );

        if (config.isRepairEnabled()) {
            for (int i = 0; i < population.getCurrentSize(); i++) {
                Chromosome chromosome = population.getChromosome(i);
                constraintChecker.repair(chromosome);
            }
        }
    }

    // produce the next generation: elite + offspring from selection/crossover/mutation
    private void evolveOneGeneration() {
        List<Chromosome> elite = elitismOperator.selectElite(population);

        List<Chromosome> newChromosomes = new ArrayList<>(config.getPopulationSize());
        for (Chromosome eliteChromosome : elite) {
            newChromosomes.add(eliteChromosome);
        }

        while (newChromosomes.size() < config.getPopulationSize()) {
            Chromosome parent1 = selectionOperator.select(population);
            Chromosome parent2 = selectionOperator.select(population);

            Chromosome[] offspring = crossoverOperator.crossover(parent1, parent2);

            mutationOperator.mutate(offspring[0]);
            mutationOperator.mutate(offspring[1]);

            if (config.isRepairEnabled()) {
                constraintChecker.repair(offspring[0]);
                constraintChecker.repair(offspring[1]);
            }

            // RU: добавляем второго отпрыска только если ещё есть место,
            // иначе финальный размер популяции вырастет сверх лимита
            newChromosomes.add(offspring[0]);
            if (newChromosomes.size() < config.getPopulationSize()) {
                newChromosomes.add(offspring[1]);
            }
        }

        population.replaceAll(newChromosomes);
        evaluatePopulation(population);
    }

    // calculate fitness for any chromosomes that need it
    private void evaluatePopulation(Population pop) {
        for (int i = 0; i < pop.getCurrentSize(); i++) {
            Chromosome chromosome = pop.getChromosome(i);
            if (!chromosome.isFitnessCalculated()) {
                fitnessCalculator.calculateFitness(chromosome);
                constraintChecker.checkAll(chromosome);
            }
        }
    }

    // stop on user request, max generations, or convergence
    private boolean shouldTerminate() {
        if (isStopped) {
            return true;
        }

        if (currentGeneration >= config.getMaxGenerations()) {
            return true;
        }

        return config.isConvergenceEnabled() && isConverged();
    }

    // true if best fitness has not improved enough over the convergence window
    private boolean isConverged() {
        int windowSize = config.getConvergenceGenerations();
        if (generationHistory.size() < windowSize) {
            return false;
        }

        double currentBest = bestEver.getFitness();
        int startIndex = generationHistory.size() - windowSize;
        double oldBest = generationHistory.get(startIndex).getBestFitness();

        double improvement = currentBest - oldBest;
        return improvement < config.getConvergenceThreshold();
    }

    // append a stats row for the current generation
    private void recordGenerationStats() {
        GenerationStats stats = new GenerationStats(
                currentGeneration,
                population.getBestFitness(),
                population.getAverageFitness(),
                population.getWorstFitness(),
                population.getFitnessStdDev(),
                population.countValid(),
                bestEver.getFitness()
        );
        generationHistory.add(stats);
    }

    // request a graceful stop
    public void stop() {
        isStopped = true;
    }

    // is the GA still running
    public boolean isRunning() {
        return isRunning;
    }

    // register a per-generation callback
    public void setGenerationCallback(GenerationCallback callback) {
        this.generationCallback = callback;
    }

    // assemble the final result object
    public AlgorithmResult getResult() {
        long executionTime = isRunning
                ? System.currentTimeMillis() - startTimeMs
                : endTimeMs - startTimeMs;
        return new AlgorithmResult(
                bestEver,
                currentGeneration,
                executionTime,
                generationHistory,
                fitnessCalculator.countPreferenceDistribution(bestEver)
        );
    }

    // hook fired after each generation
    public interface GenerationCallback {
        void onGeneration(int generation, Population population, Chromosome bestEver);
    }

    // immutable summary of one finished GA run
    public static class AlgorithmResult {
        private final Chromosome bestSolution;
        private final int generations;
        private final long executionTimeMs;
        private final List<GenerationStats> history;
        private final int[] preferenceDistribution;

        public AlgorithmResult(Chromosome bestSolution, int generations, long executionTimeMs,
                               List<GenerationStats> history,
                               int[] preferenceDistribution) {
            this.bestSolution = bestSolution;
            this.generations = generations;
            this.executionTimeMs = executionTimeMs;
            this.history = history;
            this.preferenceDistribution = preferenceDistribution;
        }

        // total generations actually run
        public int getGenerations() {
            return generations;
        }

        // wall clock time in ms
        public long getExecutionTimeMs() {
            return executionTimeMs;
        }

        // per-generation stats
        public List<GenerationStats> getHistory() {
            return history;
        }

        // counts per preference rank
        public int[] getPreferenceDistribution() {
            return preferenceDistribution;
        }

        // best fitness reached
        public double getBestFitness() {
            return bestSolution.getFitness();
        }
    }
}
