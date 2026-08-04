# Components
## plsql2java — Oracle PL/SQL Legacy Modernization Platform

---

## Component Overview

The platform is composed of 9 core components organized into 3 layers:

```
+----------------------------------------------------------+
|                   DELIVERY LAYER                         |
|  +-------------------+   +---------------------------+  |
|  |   CLI Component   |   |  Web Application Component|  |
|  +-------------------+   +---------------------------+  |
+----------------------------------------------------------+
                          |
+----------------------------------------------------------+
|                  ORCHESTRATION LAYER                     |
|  +----------------------------------------------------+  |
|  |           Migration Orchestrator Service           |  |
|  +----------------------------------------------------+  |
+----------------------------------------------------------+
                          |
+----------------------------------------------------------+
|                    CORE ENGINE LAYER                     |
|  +--------------+  +----------------+  +-------------+  |
|  |   Discovery  |  |  Translation   |  |    Code     |  |
|  |   Component  |  |    Engine      |  |  Generator  |  |
|  +--------------+  +----------------+  +-------------+  |
|  +--------------+  +----------------+  +-------------+  |
|  |  Dependency  |  |   Confidence   |  |   Report    |  |
|  |   Analyzer   |  |    Scorer      |  |  Generator  |  |
|  +--------------+  +----------------+  +-------------+  |
+----------------------------------------------------------+
                          |
+----------------------------------------------------------+
|                   SHARED INFRASTRUCTURE                  |
|  +----------------------------------------------------+  |
|  |         Configuration & Persistence Component      |  |
|  +----------------------------------------------------+  |
+----------------------------------------------------------+
```

---

## Component 1: Oracle Discovery Component

**Package**: `com.plsql2java.discovery`

**Responsibility**: Connects to Oracle databases (JDBC) or parses DDL export files to discover and extract all PL/SQL objects and their source code.

**Key Responsibilities**:
- Establish and manage JDBC connections to live Oracle databases
- Parse Oracle DDL/SQL export files (offline mode)
- Extract source code for: packages (spec + body), procedures, functions, triggers, views, sequences, types
- Normalize discovered objects into a unified OracleObject model
- Persist discovery results for downstream components
- Report discovery progress via events

**Interfaces**:
- Input: JDBC connection config OR DDL file path(s)
- Output: List of OracleObject (normalized metadata + source)

---

## Component 2: Dependency Analyzer Component

**Package**: `com.plsql2java.dependency`

**Responsibility**: Analyzes relationships between discovered Oracle objects to build a dependency graph and determine optimal migration order.

**Key Responsibilities**:
- Parse object source code to identify cross-object references
- Build a directed dependency graph (object → object)
- Detect circular dependencies
- Compute leaf-first migration order (topological sort)
- Identify migration-ready objects (no unresolved dependencies)

**Interfaces**:
- Input: List of OracleObject from Discovery Component
- Output: DependencyGraph, ordered List of OracleObject (migration sequence)

---

## Component 3: PL/SQL Translation Engine

**Package**: `com.plsql2java.translation`

**Responsibility**: Translates PL/SQL constructs to Java equivalents using a rule-based, deterministic translation engine. Flags unsupported constructs for manual review.

**Key Responsibilities**:
- Parse PL/SQL source into an Abstract Syntax Tree (AST)
- Apply translation rules for each supported construct type
- Produce a Java AST or intermediate representation per translated unit
- Flag unsupported constructs with recommendations
- Assign per-construct translation status (translated / flagged / skipped)
- Support externalized, configurable translation rules

**Interfaces**:
- Input: OracleObject (source code)
- Output: TranslationResult (Java IR, flagged constructs, per-construct status)

---

## Component 4: Java Code Generator Component

**Package**: `com.plsql2java.codegen`

**Responsibility**: Generates complete Java Spring Boot source files from translation results, including entities, repositories, services, controllers, DTOs, OpenAPI annotations, unit tests, and Maven project structure.

**Key Responsibilities**:
- Generate JPA @Entity classes from Oracle table/view metadata
- Generate Spring Data JPA repository interfaces
- Generate @Service classes from translated PL/SQL packages/procedures
- Generate @RestController classes with REST endpoint mappings
- Generate request/response DTOs with validation annotations
- Generate OpenAPI 3 annotations and openapi.yaml spec
- Generate JUnit 5 + Mockito + AssertJ + Spring Boot Test unit tests
- Generate complete Maven project (pom.xml, directory layout, application.yml)
- Generate Dockerfile and docker-compose.yml

**Interfaces**:
- Input: TranslationResult, OracleObject metadata
- Output: GeneratedProject (in-memory file tree, written to output directory)

---

## Component 5: Confidence Scorer Component

**Package**: `com.plsql2java.scoring`

**Responsibility**: Calculates confidence scores at both object-level and method-level based on translation results, flagged constructs, and complexity metrics.

**Key Responsibilities**:
- Calculate per-method confidence score (0–100%)
- Calculate per-object confidence score (0–100%) as aggregate of method scores
- Apply configurable confidence threshold (default: 70%)
- Flag objects and methods below threshold
- Embed confidence score annotations/comments in generated code
- Produce confidence score summary for reporting

**Interfaces**:
- Input: TranslationResult (per-construct status, flagged constructs)
- Output: ConfidenceReport (per-object scores, per-method scores, flagged items)

---

## Component 6: Migration Report Generator Component

**Package**: `com.plsql2java.reporting`

**Responsibility**: Generates comprehensive migration reports in Markdown and HTML formats, including executive summary, traceability matrix, flagged constructs, dependency graph summary, and confidence scores.

**Key Responsibilities**:
- Aggregate data from Discovery, Dependency Analyzer, Translation Engine, Code Generator, and Confidence Scorer
- Generate executive summary section
- Generate traceability matrix (PL/SQL object → Java class/method)
- Generate flagged constructs section with recommendations
- Generate dependency graph summary
- Generate per-object and per-method confidence score tables
- Render report as Markdown (.md) and self-contained HTML (.html)

**Interfaces**:
- Input: DiscoveryResult, DependencyGraph, TranslationResult, GeneratedProject, ConfidenceReport
- Output: MigrationReport (Markdown string, HTML string, written to output directory)

---

## Component 7: Migration Orchestrator Service

**Package**: `com.plsql2java.orchestration`

**Responsibility**: Coordinates the end-to-end migration workflow by sequencing calls to all core engine components. Provides a single entry point for both CLI and Web Application delivery layers.

**Key Responsibilities**:
- Accept migration configuration (JDBC config or DDL file paths, output directory, options)
- Orchestrate: Discovery → Dependency Analysis → Translation → Code Generation → Confidence Scoring → Report Generation
- Emit progress events for CLI stdout and Web UI progress tracking
- Handle partial failures (skip failed objects, continue with remaining)
- Persist migration state for report regeneration
- Support analyze-only mode (Discovery + Dependency Analysis only)
- Support report-only mode (Report Generation from persisted state)

**Interfaces**:
- Input: MigrationConfig
- Output: MigrationResult (status, GeneratedProject, MigrationReport, ConfidenceReport)

---

## Component 8: CLI Component

**Package**: `com.plsql2java.cli`

**Responsibility**: Provides the command-line interface with `analyze`, `generate`, and `report` commands. Delegates all business logic to the Migration Orchestrator Service.

**Key Responsibilities**:
- Parse CLI arguments and YAML config files
- Invoke Migration Orchestrator Service with appropriate MigrationConfig
- Stream progress output to stdout using structured logging
- Handle and display errors with actionable messages
- Exit with appropriate exit codes (0 = success, non-zero = failure)
- Provide `--help` documentation for all commands

**Interfaces**:
- Input: Command-line arguments, YAML config file
- Output: stdout progress, exit code, files written to output directory

---

## Component 9: Web Application Component

**Package**: `com.plsql2java.web`

**Responsibility**: Provides the browser-based interface as a Spring Boot web application with a REST API backend and a frontend UI. Delegates all business logic to the Migration Orchestrator Service.

**Key Responsibilities**:
- Expose REST API endpoints for: file upload, JDBC config, trigger analysis, trigger generation, download project ZIP, view/download reports
- Serve the frontend UI (static assets)
- Handle multipart file uploads with validation and sandboxing
- Stream progress updates to the UI (Server-Sent Events or WebSocket)
- Package generated project as a downloadable ZIP archive
- Enforce authentication on all non-public endpoints
- Apply HTTP security headers on all responses
- Validate all API inputs before processing

**Interfaces**:
- Input: HTTP requests (REST API)
- Output: HTTP responses (JSON, file downloads, SSE progress events)
