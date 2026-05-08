# OptiMatch

> Student-to-Project Assignment System using a Genetic Algorithm

OptiMatch is a JavaFX desktop application that solves the **student–project matching problem** — an NP-hard combinatorial optimization task — by evolving high-quality assignments with a genetic algorithm. The goal is to maximize student satisfaction based on their ranked preferences while respecting hard constraints on project capacity, GPA requirements, and partner pairings.

The project is built around a clean MVVM architecture with a clear separation between the algorithm core, the persistence layer, and the JavaFX user interface.

---

## Table of Contents

1. [Problem Statement](#problem-statement)
2. [Features](#features)
3. [Tech Stack](#tech-stack)
4. [Architecture Overview](#architecture-overview)
5. [Genetic Algorithm](#genetic-algorithm)
6. [Getting Started](#getting-started)
7. [Database Setup](#database-setup)
8. [Running the Application](#running-the-application)
9. [Using OptiMatch](#using-optimatch)
10. [Configuration Presets](#configuration-presets)
11. [Project Structure](#project-structure)
12. [Testing](#testing)
13. [Test Data](#test-data)
14. [Building a Distributable](#building-a-distributable)

---

## Problem Statement

Given a set of students, a set of projects, and each student's ranked preferences, assign every student to exactly one project so that overall satisfaction is maximized while satisfying the following constraints:

- **Capacity** — each project must have between `min_capacity` and `max_capacity` students.
- **GPA** — a student can only be assigned to a project if their GPA meets the project's `required_gpa`.
- **Partners** — students paired together (via `partner_id`) must end up on the same project.

This is an **NP-hard** problem: with `N` students and `M` projects, the search space has up to `M^N` candidate assignments, which makes exhaustive search intractable for any realistic class size. OptiMatch sidesteps this using a **genetic algorithm**, which delivers high-quality near-optimal solutions in seconds.

## Features

- Full CRUD management of students, projects, and preferences through a JavaFX UI
- Complete genetic-algorithm pipeline with selection, crossover, mutation, elitism, and constraint repair
- Five built-in configuration presets (Small / Medium / Large / Quick / High Quality) plus full manual control over every parameter
- Live progress tracking during a run — current generation, best fitness, average fitness, elapsed time
- Persistent run history — every algorithm run, its parameters, its assignments, and its per-generation statistics are saved to the database
- Detailed results view with preference distribution (1st through 5th choice + unmatched), per-student assignment, per-project capacity status, and convergence curve
- Export to CSV (UTF-8 with BOM, opens correctly in Excel) and a human-readable text report
- Reproducible runs via configurable random seed
- Optional right-to-left UI orientation for Hebrew users (`-Doptimatch.orientation=rtl`)
- Comprehensive JUnit 5 test suite (unit + integration), ~190 tests

## Tech Stack

| Layer            | Technology                                     |
|------------------|------------------------------------------------|
| Language         | Java 17                                        |
| UI               | JavaFX 21.0.1 (Controls + FXML)                |
| Build            | Apache Maven                                   |
| Database         | MySQL 8 via JDBC (`mysql-connector-j` 8.2.0)   |
| Modules          | Java Platform Module System (`module-info.java`) |
| Testing          | JUnit Jupiter 5.10.1                           |
| Architecture     | MVVM (Model — View — ViewModel)                |

## Architecture Overview

OptiMatch follows a layered MVVM architecture. Each layer has a single, clear responsibility, which keeps the algorithm core isolated from both persistence and presentation concerns.

```
┌─────────────────────────────────────────────────────┐
│                    View (FXML + CSS)                │
│  students.fxml  projects.fxml  algorithm.fxml       │
│  results.fxml   main.fxml                           │
└──────────────────────┬──────────────────────────────┘
                       │ data binding
┌──────────────────────▼──────────────────────────────┐
│         ViewModel (JavaFX Properties)               │
│  StudentVM   ProjectVM   AlgorithmVM   ResultsVM    │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│                    Service Layer                    │
│  StudentService   ProjectService   MatchingService  │
│  ReportService    ServiceException                  │
└──────────────────────┬──────────────────────────────┘
                       │
        ┌──────────────┴───────────────┐
        ▼                              ▼
┌───────────────────┐        ┌──────────────────────┐
│   Algorithm Core  │        │      DAO Layer       │
│  GeneticAlgorithm │        │  StudentDAO          │
│  FitnessCalculator│        │  ProjectDAO          │
│  ConstraintChecker│        │  PreferenceDAO       │
│  Selection /      │        │  AssignmentDAO       │
│  Crossover /      │        │  AlgorithmRunDAO     │
│  Mutation /       │        │  GenerationStatsDAO  │
│  Elitism Operators│        │  DatabaseConnection  │
└───────────────────┘        └──────────┬───────────┘
                                        │ JDBC
                                        ▼
                                  ┌───────────┐
                                  │  MySQL    │
                                  └───────────┘
```

**Why MVVM?** ViewModels expose JavaFX `Property` objects that the FXML controllers bind to, so the UI updates automatically whenever model state changes. The algorithm runs on a shared daemon executor (`AppLifecycle.getBackgroundExecutor()`); UI updates are marshalled back to the JavaFX Application Thread via `Platform.runLater`, keeping the interface responsive even during long runs. On application exit, `App.stop()` invokes `AppLifecycle.shutdown()` to release the background executor cleanly.

## Genetic Algorithm

### Chromosome Representation

A chromosome is an integer array of length `N` (number of students), where the `i`-th value is the project ID assigned to student `i`. This direct encoding makes both fitness evaluation and constraint checks straightforward.

### Fitness Function

```
Fitness = PreferenceScore − CapacityPenalty − GpaPenalty − PartnerPenalty
```

| Component         | Definition                                                              |
|-------------------|-------------------------------------------------------------------------|
| PreferenceScore   | Sum of weights based on preference rank (higher = better)               |
| CapacityPenalty   | `50` per student over/under each project's capacity bounds              |
| GpaPenalty        | `30` per student assigned to a project they don't qualify for           |
| PartnerPenalty    | `40` per partner pair separated into different projects                 |

**Preference weights** (defined in `Preference.java`):

| Rank          | 1st | 2nd | 3rd | 4th | 5th | none |
|---------------|-----|-----|-----|-----|-----|------|
| Weight        | 100 | 80  | 60  | 40  | 20  | 0    |

The theoretical maximum fitness is `N × 100` — every student getting their first choice with no constraint violations.

### Evolution Loop

1. **Initialization** — generate a random population (default size: 200).
2. **Evaluation** — compute fitness for every chromosome.
3. **Elitism** — copy the top 5% (configurable) directly into the next generation.
4. **Selection** — tournament selection picks parents (default tournament size: 3, ties broken randomly).
5. **Crossover** — uniform crossover mixes parent genes (default rate: 0.8).
6. **Mutation** — per-gene swap mutation perturbs offspring; the configured rate is the probability that **each individual gene** is swapped with another random gene (default 0.02 per gene).
7. **Repair** — `ConstraintChecker` repairs partner, GPA, and capacity violations using a preference-aware strategy (it picks replacement projects from the affected student's own preference list before falling back to any feasible project).
8. **Replacement** — assemble the new generation from elites and offspring.
9. **Termination** — stop when (a) `maxGenerations` is reached, (b) convergence is detected (no improvement above threshold for `convergenceGenerations` consecutive generations), or (c) the user clicks Stop.

### Operators

| Operator     | Implementation                                                    |
|--------------|-------------------------------------------------------------------|
| Selection    | Tournament (size configurable, default 3, random tie-break)       |
| Crossover    | Uniform (each gene independently inherited from either parent)    |
| Mutation     | Per-gene swap (each gene independently rolls against the rate)    |
| Elitism      | Top *N*% of the sorted population, deep-copied into the next gen  |

### Reproducibility

`GeneticAlgorithmConfig.seed(long)` accepts a fixed RNG seed. Setting it makes the entire run fully deterministic — invaluable for unit testing, debugging, and demonstrations to the graduation committee.

## Getting Started

### Prerequisites

- **JDK 17** (newer JDKs work, but the project targets release 17)
- **Apache Maven 3.8+**
- **MySQL 8** (running locally or remotely)

Verify your installation:

```bash
java -version    # Should report 17 or higher
mvn -v
mysql --version
```

### Clone and Install

```bash
git clone https://github.com/<your-username>/optimatch.git
cd optimatch
mvn clean install
```

## Database Setup

1. Start your MySQL server and create the database:

   ```sql
   CREATE DATABASE optimatch CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. Apply the schema from `src/main/resources/sql/schema.sql`:

   ```bash
   mysql -u root -p optimatch < src/main/resources/sql/schema.sql
   ```

   The schema creates six tables — `students`, `projects`, `preferences`, `algorithm_runs`, `assignments`, `generation_stats` — with cascading deletes from `algorithm_runs` so removing a run cleans up its assignments and generation history automatically.

3. *(Optional)* Load test data — choose one:

   ```bash
   # Small dataset
   mysql -u root -p optimatch < src/main/resources/sql/test_data.sql

   # Large dataset
   mysql -u root -p optimatch < src/main/resources/sql/test_data_large.sql
   ```

4. Configure database credentials. Copy the example file and edit it:

   ```bash
   cp src/main/resources/config.properties.example src/main/resources/config.properties
   ```

   ```properties
   db.url=jdbc:mysql://localhost:3306/optimatch
   db.user=root
   db.password=YOUR_PASSWORD_HERE
   ```

   `config.properties` is gitignored, so your credentials never end up in version control. Each call to `DatabaseConnection.getConnection()` returns a fresh JDBC connection — callers own its lifecycle (`try-with-resources` is used everywhere in the DAO layer).

## Running the Application

```bash
mvn javafx:run
```

This launches the OptiMatch desktop window (1200×800, minimum 800×600). The status bar at the bottom confirms whether the database connection succeeded.

To launch with a right-to-left UI for Hebrew users:

```bash
mvn javafx:run -Djavafx.args="-Doptimatch.orientation=rtl"
```

## Using OptiMatch

OptiMatch presents four screens, navigable from the top bar:

1. **Students** — add, edit, search, or delete students; manage their five ranked project preferences and partner assignments.
2. **Projects** — manage projects with their code, name, description, capacity range, and GPA requirement.
3. **Run Algorithm** — pick a preset (or set parameters manually), click *Run Algorithm*, and watch the progress bar, current generation, and live fitness values update in real time.
4. **Results** — select any past run from the dropdown to inspect:
   - Summary metrics (total students, total projects, best fitness, satisfaction percentage)
   - Preference distribution (how many got 1st / 2nd / … / no choice)
   - Per-student assignments with their preference rank and satisfaction score
   - Per-project capacity status (OK / Under / Over)
   - Per-generation statistics (best, average, worst, std dev, valid count)
   - Export to CSV (students, projects, generation statistics) or full text report

## Configuration Presets

| Preset       | Population | Generations | Mutation | Crossover | Elite | Tournament | Use case                          |
|--------------|-----------:|------------:|---------:|----------:|------:|-----------:|-----------------------------------|
| Small        | 100        | 500         | 0.03     | 0.8       | 10%   | 3          | Datasets under 50 students        |
| Medium       | 200        | 1000        | 0.02     | 0.8       | 5%    | 4          | 50–200 students *(default)*       |
| Large        | 500        | 2000        | 0.01     | 0.85      | 5%    | 5          | More than 200 students            |
| Quick        | 50         | 100         | 0.05     | 0.9       | 10%   | 3          | Smoke testing — runs in seconds   |
| High Quality | 750        | 3000        | 0.025    | 0.85      | 10%   | 5          | Highest quality, longest runtime  |

Mutation and crossover rates are interpreted **per gene** and **per offspring**, respectively. You can also tune every parameter manually in the Algorithm screen.

## Project Structure

```
optimatch/
├── pom.xml                          Maven build configuration
├── README.md
├── .gitignore
└── src/
    ├── main/
    │   ├── java/com/optimatch/
    │   │   ├── App.java                       Application entry point
    │   │   ├── module-info.java               Java module descriptor
    │   │   ├── algorithm/                     Genetic algorithm core
    │   │   │   ├── GeneticAlgorithm.java
    │   │   │   ├── GeneticAlgorithmConfig.java
    │   │   │   ├── Chromosome.java
    │   │   │   ├── Population.java
    │   │   │   ├── FitnessCalculator.java
    │   │   │   ├── ConstraintChecker.java
    │   │   │   ├── SelectionOperator.java
    │   │   │   ├── CrossoverOperator.java
    │   │   │   ├── MutationOperator.java
    │   │   │   └── ElitismOperator.java
    │   │   ├── model/                         Domain entities
    │   │   │   ├── Student.java
    │   │   │   ├── Project.java
    │   │   │   ├── Preference.java
    │   │   │   ├── Assignment.java
    │   │   │   ├── AlgorithmRun.java
    │   │   │   └── GenerationStats.java
    │   │   ├── dao/                           JDBC data access
    │   │   │   ├── DatabaseConnection.java
    │   │   │   ├── StudentDAO.java
    │   │   │   ├── ProjectDAO.java
    │   │   │   ├── PreferenceDAO.java
    │   │   │   ├── AssignmentDAO.java
    │   │   │   ├── AlgorithmRunDAO.java
    │   │   │   └── GenerationStatsDAO.java
    │   │   ├── service/                       Business logic
    │   │   │   ├── StudentService.java
    │   │   │   ├── ProjectService.java
    │   │   │   ├── MatchingService.java
    │   │   │   ├── ReportService.java
    │   │   │   └── ServiceException.java
    │   │   ├── viewmodel/                     MVVM ViewModels
    │   │   │   ├── StudentViewModel.java
    │   │   │   ├── ProjectViewModel.java
    │   │   │   ├── AlgorithmViewModel.java
    │   │   │   └── ResultsViewModel.java
    │   │   ├── view/                          JavaFX controllers
    │   │   │   ├── MainController.java
    │   │   │   ├── StudentController.java
    │   │   │   ├── ProjectController.java
    │   │   │   ├── AlgorithmController.java
    │   │   │   └── ResultsController.java
    │   │   └── util/
    │   │       ├── AppLifecycle.java          Shared executor + shutdown hooks
    │   │       └── ExportUtil.java            CSV / text report export
    │   └── resources/
    │       ├── config.properties.example
    │       ├── css/styles.css
    │       ├── fxml/                          UI layouts
    │       │   ├── main.fxml
    │       │   ├── students.fxml
    │       │   ├── projects.fxml
    │       │   ├── algorithm.fxml
    │       │   └── results.fxml
    │       └── sql/
    │           ├── schema.sql
    │           ├── test_data.sql
    │           └── test_data_large.sql
    └── test/java/com/optimatch/               JUnit 5 tests
        ├── algorithm/
        │   ├── ChromosomeTest.java
        │   ├── ConstraintCheckerTest.java
        │   ├── CrossoverOperatorTest.java
        │   ├── FitnessCalculatorTest.java
        │   ├── GeneticAlgorithmConfigTest.java
        │   ├── GeneticAlgorithmIntegrationTest.java
        │   ├── MutationOperatorTest.java
        │   ├── PopulationTest.java
        │   └── SelectionOperatorTest.java
        ├── model/
        │   ├── AlgorithmRunTest.java
        │   ├── AssignmentTest.java
        │   ├── PreferenceTest.java
        │   ├── ProjectTest.java
        │   └── StudentTest.java
        └── util/
            └── ExportUtilTest.java
```

## Testing

The project ships with a comprehensive JUnit 5 test suite covering the algorithm core, the domain models, and the export utilities. Tests are deterministic — they use fixed RNG seeds — so they're fully reproducible.

```bash
# Run all tests
mvn test

# Run a single test class
mvn test "-Dtest=FitnessCalculatorTest"

# Run a single test method
mvn test "-Dtest=FitnessCalculatorTest#allFirstChoices"
```

The Maven Surefire plugin is preconfigured with the `--add-opens` and `--add-reads` flags required by the Java module system, so JUnit reflection-based discovery works out of the box.

> **Running tests from IntelliJ IDEA.** Because the project ships a `module-info.java`, IntelliJ's built-in test runner sometimes fails to find the `com.optimatch` module on the module path. The most reliable workaround is to create a **Maven** run configuration with the goal `test` (or `test "-Dtest=ClassName"`) instead of relying on the green-arrow JUnit runner. Make sure the Project SDK is set to JDK 17.

## Test Data

Two SQL fixtures are provided to cover different demonstration scenarios:

| Fixture                | Purpose                                                                            |
|------------------------|------------------------------------------------------------------------------------|
| `test_data.sql`        | Quick demo dataset — handful of students and projects, fast convergence.           |
| `test_data_large.sql`  | Stress-test dataset — more students and projects with deliberate GPA conflicts.    |

The large dataset is designed to exercise the constraint solver: highly competitive projects (AI, SEC, QUANT) have strict GPA requirements and limited capacity, while medium-GPA students cluster around WEB, DATA, and BLOCK — forcing the algorithm to make meaningful trade-offs.

## Building a Distributable

To produce a runnable JAR:

```bash
mvn clean package
```

The artifact lands in `target/optimatch-1.0-SNAPSHOT.jar`. Note that JavaFX is treated as a modular dependency, so launching is easiest via `mvn javafx:run` during development; for distribution, consider `jpackage` or `jlink` to create a self-contained runtime image.

---

**OptiMatch** — built as a graduation project to demonstrate the application of evolutionary computation to a real-world combinatorial assignment problem.
