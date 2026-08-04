# Units of Work
## plsql2java — Oracle PL/SQL Legacy Modernization Platform

**Decomposition Strategy**: Pipeline-stage based (4 pipeline stages + 1 orchestration + 1 web delivery)
**Maven Structure**: Multi-module Maven project (each unit = one Maven module)
**Development Sequence**: Units 1 → 2 → 3 → 4 → 5 → 6 (each unit depends on prior)

---

## Unit 1: Discovery & Dependency Analysis

**Maven Module**: `plsql2java-discovery`
**Package Root**: `com.plsql2java.discovery`, `com.plsql2java.dependency`

### Components Included
- Oracle Discovery Component (`com.plsql2java.discovery`)
- Dependency Analyzer Component (`com.plsql2java.dependency`)

### Responsibilities
- Connect to Oracle via JDBC and discover all PL/SQL objects
- Parse Oracle DDL/SQL export files for offline discovery
- Normalize discovered objects into the OracleObject domain model
- Build directed dependency graph from object cross-references
- Detect circular dependencies
- Compute leaf-first migration order (topological sort)
- Persist discovery + dependency results to output directory
- Emit progress events during discovery

### Key Deliverables
- `OracleDiscoveryService` — JDBC and file-based discovery
- `DependencyAnalyzerService` — graph construction and ordering
- `OracleObject`, `DiscoveryResult`, `DependencyGraph` domain models
- `ProgressEvent` infrastructure (event class + listener interface)
- Unit tests for all service methods
- Integration test with sample DDL files

### Maven Module Structure
```
plsql2java-discovery/
  src/main/java/com/plsql2java/discovery/
  src/main/java/com/plsql2java/dependency/
  src/main/java/com/plsql2java/model/          (shared domain models)
  src/main/java/com/plsql2java/common/         (ProgressEvent, config models)
  src/test/java/
  pom.xml
```

---

## Unit 2: PL/SQL Translation Engine

**Maven Module**: `plsql2java-translation`
**Package Root**: `com.plsql2java.translation`

### Components Included
- PL/SQL Translation Engine (`com.plsql2java.translation`)

### Responsibilities
- Parse PL/SQL source code into an Abstract Syntax Tree (AST) using ANTLR4 + Oracle PL/SQL grammar
- Maintain a TranslationRuleRegistry of all registered translation rules
- Apply translation rules to each AST node to produce Java intermediate representation (IR)
- Support all required construct translations: control flow, exception handling, cursors, bulk operations, built-in functions, DBMS_OUTPUT
- Flag unsupported constructs with construct type, line reference, and recommendation
- Record per-construct translation status (translated / flagged / skipped)
- Support externalized, configurable translation rules (loaded from classpath)

### Key Deliverables
- `PlSqlTranslationEngine` — main translation orchestrator
- `TranslationRuleRegistry` — rule registration and lookup
- `TranslationRule` interface — extensibility contract
- Concrete rule implementations for all supported constructs (FR-04.2)
- `TranslationResult`, `JavaIR`, `FlaggedConstruct`, `ConstructTranslationResult` models
- Unit tests for each translation rule
- Integration tests with sample PL/SQL source files

### Maven Module Structure
```
plsql2java-translation/
  src/main/java/com/plsql2java/translation/
    engine/          (PlSqlTranslationEngine, TranslationRuleRegistry)
    rules/           (one class per translation rule)
    ast/             (AST node types, parser integration)
    model/           (TranslationResult, JavaIR, FlaggedConstruct)
  src/main/resources/
    grammar/         (Oracle PL/SQL ANTLR4 grammar files)
    rules/           (externalized rule configuration)
  src/test/java/
  pom.xml
```

### Dependencies
- `plsql2java-discovery` (OracleObject model)
- ANTLR4 runtime
- Oracle PL/SQL grammar (open-source or custom)

---

## Unit 3: Code Generator, Confidence Scorer & Report Generator

**Maven Module**: `plsql2java-codegen`
**Package Root**: `com.plsql2java.codegen`, `com.plsql2java.scoring`, `com.plsql2java.reporting`

### Components Included
- Java Code Generator Component (`com.plsql2java.codegen`)
- Confidence Scorer Component (`com.plsql2java.scoring`)
- Migration Report Generator Component (`com.plsql2java.reporting`)

### Responsibilities
- Generate all Java Spring Boot artifacts from TranslationResult (entities, repositories, services, controllers, DTOs, tests)
- Generate complete Maven project structure (pom.xml, application.yml, Dockerfile, docker-compose.yml)
- Generate OpenAPI 3 annotations and openapi.yaml spec
- Calculate per-method and per-object confidence scores
- Apply configurable confidence threshold and flag items below it
- Embed confidence score comments in generated Java source
- Aggregate all migration data into a unified report
- Render migration report as Markdown and self-contained HTML
- Build traceability matrix (PL/SQL object → Java class/method)

### Key Deliverables
- `JavaCodeGeneratorService` — full artifact generation
- `ConfidenceScorerService` — scoring and threshold management
- `MigrationReportGeneratorService` — report assembly and rendering
- FreeMarker/Mustache templates for all Java artifact types
- `GeneratedProject`, `ConfidenceReport`, `MigrationReport` models
- Unit tests for all generators and scorer
- Integration tests generating a complete project from sample translation results

### Maven Module Structure
```
plsql2java-codegen/
  src/main/java/com/plsql2java/codegen/
  src/main/java/com/plsql2java/scoring/
  src/main/java/com/plsql2java/reporting/
  src/main/resources/
    templates/java/      (entity, repo, service, controller, dto, test templates)
    templates/maven/     (pom.xml, application.yml templates)
    templates/docker/    (Dockerfile, docker-compose.yml templates)
    templates/report/    (Markdown and HTML report templates)
  src/test/java/
  pom.xml
```

### Dependencies
- `plsql2java-discovery` (OracleObject, DiscoveryResult, DependencyGraph models)
- `plsql2java-translation` (TranslationResult, JavaIR, FlaggedConstruct models)
- FreeMarker or Mustache template engine

---

## Unit 4: Migration Orchestrator

**Maven Module**: `plsql2java-orchestrator`
**Package Root**: `com.plsql2java.orchestration`

### Components Included
- Migration Orchestrator Service (`com.plsql2java.orchestration`)

### Responsibilities
- Accept MigrationConfig from delivery layer (CLI or Web)
- Sequence all core engine services: Discovery → Dependency Analysis → Translation → Code Generation → Confidence Scoring → Report Generation
- Manage migration job lifecycle (start, in-progress, complete, failed)
- Emit ProgressEvent objects for CLI stdout and Web SSE consumption
- Handle partial failures (log failed objects, continue with remaining)
- Persist migration state to output directory
- Support three operation modes: analyze-only, generate (full), report-only

### Key Deliverables
- `MigrationOrchestratorService` — full pipeline coordination
- `MigrationConfig`, `MigrationResult`, `MigrationProgress`, `AnalysisResult` models
- `ProgressEventBus` — routes events to registered listeners
- Unit tests for orchestration logic (mocked engine services)
- Integration tests running full pipeline with sample PL/SQL inputs

### Maven Module Structure
```
plsql2java-orchestrator/
  src/main/java/com/plsql2java/orchestration/
    service/         (MigrationOrchestratorService)
    model/           (MigrationConfig, MigrationResult, MigrationProgress)
    event/           (ProgressEventBus, ProgressEvent)
  src/test/java/
  pom.xml
```

### Dependencies
- `plsql2java-discovery`
- `plsql2java-translation`
- `plsql2java-codegen`

---

## Unit 5: CLI Delivery

**Maven Module**: `plsql2java-cli`
**Package Root**: `com.plsql2java.cli`

### Components Included
- CLI Component (`com.plsql2java.cli`)

### Responsibilities
- Provide `analyze`, `generate`, and `report` CLI commands via Picocli
- Parse CLI arguments and YAML config files into MigrationConfig
- Invoke MigrationOrchestratorService with appropriate config
- Stream progress output to stdout using structured logging
- Display clear, actionable error messages
- Exit with appropriate exit codes (0 = success, non-zero = failure)
- Provide `--help` documentation for all commands
- Package as an executable JAR (or native binary via GraalVM optional)

### Key Deliverables
- `PlSql2JavaCli` — main entry point
- `AnalyzeCommand`, `GenerateCommand`, `ReportCommand` — Picocli command classes
- `ConfigLoader` — YAML config + CLI flag merging
- `CliProgressListener` — routes ProgressEvents to stdout
- Unit tests for command parsing and config loading
- End-to-end CLI test with sample DDL files

### Maven Module Structure
```
plsql2java-cli/
  src/main/java/com/plsql2java/cli/
    command/         (AnalyzeCommand, GenerateCommand, ReportCommand)
    config/          (ConfigLoader, MigrationConfigMapper)
    progress/        (CliProgressListener)
  src/main/resources/
    migration-config-template.yml
  src/test/java/
  pom.xml
```

### Dependencies
- `plsql2java-orchestrator`
- Picocli
- Jackson (YAML config parsing)

---

## Unit 6: Web Application Delivery

**Maven Module**: `plsql2java-web`
**Package Root**: `com.plsql2java.web`

### Components Included
- Web Application Component (`com.plsql2java.web`)

### Responsibilities
- Expose REST API for: DDL file upload, JDBC config, trigger analysis, trigger generation, download project ZIP, view/download reports
- Serve frontend UI (static assets — technology TBD: React or Thymeleaf)
- Handle multipart file uploads with server-side validation and sandboxing
- Stream progress updates via Server-Sent Events (SSE)
- Package generated project as downloadable ZIP archive
- Enforce Spring Security authentication on all non-public endpoints
- Apply HTTP security headers on all responses (CSP, HSTS, X-Content-Type-Options, X-Frame-Options, Referrer-Policy)
- Validate all API inputs before processing
- Run as a self-contained Docker container

### Key Deliverables
- `MigrationController` — REST API endpoints
- `SecurityConfig` — Spring Security configuration with HTTP security headers
- `WebProgressListener` — routes ProgressEvents to SSE emitter
- `ZipPackager` — packages GeneratedProject as ZIP
- Frontend UI (static assets)
- `WebApplication` — Spring Boot main class
- Dockerfile and docker-compose.yml for the web application
- Unit tests for all controllers (MockMvc)
- Integration tests for file upload and generation flow

### Maven Module Structure
```
plsql2java-web/
  src/main/java/com/plsql2java/web/
    controller/      (MigrationController)
    security/        (SecurityConfig)
    progress/        (WebProgressListener, SseEmitterRegistry)
    packaging/       (ZipPackager)
  src/main/resources/
    static/          (frontend UI assets)
    application.yml
  src/test/java/
  Dockerfile
  docker-compose.yml
  pom.xml
```

### Dependencies
- `plsql2java-orchestrator`
- Spring Boot Starter Web
- Spring Security
- Springdoc OpenAPI

---

## Multi-Module Maven Root Structure

```
plsql2java/                          (root pom.xml — parent)
  plsql2java-discovery/              (Unit 1)
  plsql2java-translation/            (Unit 2)
  plsql2java-codegen/                (Unit 3)
  plsql2java-orchestrator/           (Unit 4)
  plsql2java-cli/                    (Unit 5)
  plsql2java-web/                    (Unit 6)
  aidlc-docs/                        (documentation only — not a Maven module)
  intent.md
  README.md
  pom.xml                            (parent pom — dependency management)
```

### Root pom.xml Responsibilities
- Define Spring Boot parent BOM
- Declare all dependency versions (pinned) in `<dependencyManagement>`
- List all modules in `<modules>`
- Configure shared plugins (compiler, surefire, jacoco)
