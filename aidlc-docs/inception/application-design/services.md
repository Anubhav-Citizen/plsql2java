# Services
## plsql2java — Oracle PL/SQL Legacy Modernization Platform

---

## Service Layer Overview

The service layer consists of the Migration Orchestrator Service as the primary orchestration point, with each core engine component exposing its own service class. The delivery layer (CLI and Web) interacts exclusively with the Migration Orchestrator Service — never directly with core engine components.

```
CLI Component                Web Application Component
      |                               |
      +----------+   +----------------+
                 |   |
                 v   v
      +-----------------------------+
      |  MigrationOrchestratorService  |
      +-----------------------------+
         |        |        |        |
         v        v        v        v
   Discovery  Dependency Translation CodeGen
   Service    Analyzer   Engine     Service
                              |
                         Confidence  Report
                         Scorer      Generator
```

---

## Service 1: MigrationOrchestratorService

**Package**: `com.plsql2java.orchestration`
**Role**: Primary orchestration service — single entry point for all migration workflows

**Responsibilities**:
- Accepts MigrationConfig from CLI or Web Application
- Sequences core engine services in the correct order
- Manages migration job lifecycle (start, progress, complete, fail)
- Emits ProgressEvent objects consumed by CLI (stdout) and Web (SSE)
- Handles partial failures: logs failed objects, continues with remaining
- Persists migration state to output directory for report regeneration
- Supports three operation modes: analyze-only, generate (full), report-only

**Orchestration Sequence — Full Generate Mode**:
1. OracleDiscoveryService.discoverFromJdbc() or discoverFromFiles()
2. DependencyAnalyzerService.analyze() + computeMigrationOrder()
3. PlSqlTranslationEngine.translateAll() (in migration order)
4. JavaCodeGeneratorService.generateProject()
5. ConfidenceScorerService.scoreAll()
6. MigrationReportGeneratorService.generateReport()
7. Persist all results to output directory

**Orchestration Sequence — Analyze-Only Mode**:
1. OracleDiscoveryService.discoverFromJdbc() or discoverFromFiles()
2. DependencyAnalyzerService.analyze() + computeMigrationOrder()
3. Persist discovery + dependency results

**Orchestration Sequence — Report-Only Mode**:
1. Load persisted discovery, translation, generation, and confidence results
2. MigrationReportGeneratorService.generateReport()

**Error Handling**:
- Object-level failures: log error, assign 0% confidence, mark as "Failed" in report, continue
- Connection failures: propagate immediately with actionable error message
- File parse errors: log per-file error, continue with remaining files

---

## Service 2: OracleDiscoveryService

**Package**: `com.plsql2java.discovery`
**Role**: Oracle schema discovery — JDBC and DDL file modes

**Responsibilities**:
- Manage JDBC connection lifecycle (connect, query, close)
- Execute Oracle data dictionary queries (ALL_SOURCE, ALL_OBJECTS, ALL_SEQUENCES, ALL_VIEWS, ALL_TYPES)
- Parse DDL/SQL export files using an Oracle DDL parser
- Normalize all discovered objects into OracleObject model
- Emit ProgressEvent per discovered object
- Validate and sandbox DDL file content before parsing

**Interaction Pattern**: Called by MigrationOrchestratorService. Stateless between calls.

---

## Service 3: DependencyAnalyzerService

**Package**: `com.plsql2java.dependency`
**Role**: Dependency graph construction and migration order computation

**Responsibilities**:
- Scan OracleObject source code for cross-object references using regex and AST patterns
- Build adjacency list representation of the dependency graph
- Run cycle detection (DFS-based)
- Run topological sort for migration order
- Annotate circular dependency participants

**Interaction Pattern**: Called by MigrationOrchestratorService after discovery. Stateless between calls.

---

## Service 4: PlSqlTranslationEngine

**Package**: `com.plsql2java.translation`
**Role**: Rule-based PL/SQL to Java translation

**Responsibilities**:
- Maintain a TranslationRuleRegistry of all registered translation rules
- Parse PL/SQL source into AST using an Oracle PL/SQL grammar
- Walk AST nodes and apply matching rules from the registry
- Produce Java intermediate representation (IR) for each translated construct
- Record per-construct translation status (translated / flagged / skipped)
- Support externalized rule configuration (rules loaded from classpath or config directory)

**Extensibility**: New PL/SQL constructs are supported by registering a new TranslationRule implementation — no changes to the engine core required (Open/Closed Principle).

**Interaction Pattern**: Called by MigrationOrchestratorService per object in migration order. Stateless between objects.

---

## Service 5: JavaCodeGeneratorService

**Package**: `com.plsql2java.codegen`
**Role**: Java Spring Boot artifact generation

**Responsibilities**:
- Maintain a template registry for all Java artifact types (entity, repository, service, controller, DTO, test, pom.xml, Dockerfile)
- Generate Java source files from TranslationResult using code templates
- Assemble complete Maven project directory structure in memory
- Write project to output directory
- Ensure all generated files are syntactically valid Java

**Template Strategy**: Uses a template engine (e.g., FreeMarker or Mustache) for all generated artifacts. Templates are externalized and configurable.

**Interaction Pattern**: Called by MigrationOrchestratorService after translation. Stateless between calls.

---

## Service 6: ConfidenceScorerService

**Package**: `com.plsql2java.scoring`
**Role**: Confidence score calculation and threshold management

**Responsibilities**:
- Calculate method-level scores based on: % constructs translated, flagged construct count, construct complexity weights
- Aggregate method scores to object-level scores
- Apply configurable threshold (default: 70%) to flag items for review
- Embed confidence score comments in generated Java source (via coordination with CodeGeneratorService)

**Scoring Formula** (high-level):
- Base score = (translated constructs / total constructs) * 100
- Penalty applied per flagged construct based on construct complexity weight
- Minimum score = 0, Maximum score = 100

**Interaction Pattern**: Called by MigrationOrchestratorService after code generation. Stateless between calls.

---

## Service 7: MigrationReportGeneratorService

**Package**: `com.plsql2java.reporting`
**Role**: Migration report assembly and rendering

**Responsibilities**:
- Aggregate inputs from all upstream services into a unified ReportData model
- Build traceability matrix by correlating OracleObject names with GeneratedProject file names
- Build flagged constructs summary with recommendations from a recommendation registry
- Render ReportData to Markdown using a Markdown template
- Render ReportData to self-contained HTML using an HTML template (inline CSS, no external dependencies)
- Write both formats to output directory

**Interaction Pattern**: Called by MigrationOrchestratorService as the final step. Stateless between calls.

---

## Cross-Cutting Service Concerns

### Progress Event Bus
- All services emit ProgressEvent objects (stage, objectName, percentComplete, message)
- MigrationOrchestratorService subscribes and routes events to CLI stdout or Web SSE emitter
- Decouples progress reporting from business logic

### Structured Logging
- All services use SLF4J with a structured logging framework (Logback)
- Log entries include: timestamp, correlationId (migrationId), level, component, message
- No credentials, file contents, or PII in log output

### Error Handling
- All services throw typed exceptions (DiscoveryException, TranslationException, etc.)
- MigrationOrchestratorService catches object-level exceptions, logs them, and continues
- Fatal exceptions (connection failure, output directory not writable) propagate to delivery layer
