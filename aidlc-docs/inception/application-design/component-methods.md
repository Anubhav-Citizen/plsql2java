# Component Methods
## plsql2java — Oracle PL/SQL Legacy Modernization Platform

*Note: Detailed business logic for each method is defined in Functional Design (per-unit, CONSTRUCTION phase). This document defines method signatures, inputs, outputs, and high-level purpose.*

---

## Component 1: Oracle Discovery Component

### OracleDiscoveryService

`discoverFromJdbc(JdbcConfig config, ProgressListener listener) : DiscoveryResult`
- Connects to Oracle via JDBC, discovers all supported object types, returns normalized results
- Emits progress events via listener

`discoverFromFiles(List<Path> ddlFiles, ProgressListener listener) : DiscoveryResult`
- Parses DDL/SQL export files, discovers all supported object types, returns normalized results
- Emits progress events via listener

`persistDiscoveryResult(DiscoveryResult result, Path outputDir) : void`
- Serializes discovery result to output directory for later use by generate/report commands

`loadDiscoveryResult(Path outputDir) : DiscoveryResult`
- Loads a previously persisted discovery result from output directory

---

## Component 2: Dependency Analyzer Component

### DependencyAnalyzerService

`analyze(DiscoveryResult discovery) : DependencyGraph`
- Parses all object sources to extract cross-references, builds directed dependency graph

`detectCircularDependencies(DependencyGraph graph) : List<CircularDependency>`
- Identifies cycles in the dependency graph and returns them for reporting

`computeMigrationOrder(DependencyGraph graph) : List<OracleObject>`
- Performs topological sort to produce leaf-first migration order
- Objects involved in circular dependencies are appended at the end with a warning flag

---

## Component 3: PL/SQL Translation Engine

### PlSqlTranslationEngine

`translate(OracleObject object) : TranslationResult`
- Parses PL/SQL source into AST, applies all applicable translation rules, returns Java IR and status

`translateAll(List<OracleObject> objects, ProgressListener listener) : List<TranslationResult>`
- Translates all objects in migration order, emits progress events

### TranslationRuleRegistry

`registerRule(TranslationRule rule) : void`
- Registers a translation rule for a specific PL/SQL construct type

`getRulesForConstruct(ConstructType type) : List<TranslationRule>`
- Returns all registered rules applicable to the given construct type

### TranslationRule (interface)

`getConstructType() : ConstructType`
- Returns the PL/SQL construct type this rule handles

`apply(AstNode node, TranslationContext context) : TranslationOutcome`
- Applies the rule to the given AST node, returns translated Java IR or a flagged outcome

---

## Component 4: Java Code Generator Component

### JavaCodeGeneratorService

`generateProject(List<TranslationResult> results, MigrationConfig config) : GeneratedProject`
- Orchestrates generation of all Java artifacts and Maven project structure

`generateEntity(OracleTableMetadata table, GenerationContext ctx) : JavaSourceFile`
- Generates a JPA @Entity class for the given Oracle table/view

`generateRepository(JavaSourceFile entity, List<QueryPattern> queries, GenerationContext ctx) : JavaSourceFile`
- Generates a Spring Data JPA repository interface with CRUD and custom query methods

`generateService(TranslationResult result, GenerationContext ctx) : JavaSourceFile`
- Generates a @Service class from a translated PL/SQL package or procedure

`generateController(JavaSourceFile service, GenerationContext ctx) : JavaSourceFile`
- Generates a @RestController class exposing the service as REST endpoints

`generateDto(JavaSourceFile controller, GenerationContext ctx) : List<JavaSourceFile>`
- Generates request/response DTO classes with validation annotations

`generateTest(JavaSourceFile target, GenerationContext ctx) : JavaSourceFile`
- Generates a JUnit 5 + Mockito + AssertJ test class for the given service or repository

`generatePomXml(MigrationConfig config) : String`
- Generates pom.xml with all required Spring Boot 3.x dependencies

`generateDockerfile(MigrationConfig config) : String`
- Generates a Dockerfile for the generated Spring Boot application

`generateDockerCompose(MigrationConfig config) : String`
- Generates docker-compose.yml for local development

`writeProject(GeneratedProject project, Path outputDir) : void`
- Writes all generated files to the output directory in Maven project layout

---

## Component 5: Confidence Scorer Component

### ConfidenceScorerService

`scoreMethod(TranslationResult result, String methodName) : MethodConfidenceScore`
- Calculates confidence score for a single translated method based on construct translation status

`scoreObject(OracleObject object, List<MethodConfidenceScore> methodScores) : ObjectConfidenceScore`
- Calculates aggregate confidence score for an Oracle object from its method scores

`scoreAll(List<TranslationResult> results) : ConfidenceReport`
- Scores all translated objects and methods, applies threshold, returns full confidence report

`applyThreshold(ConfidenceReport report, int threshold) : ConfidenceReport`
- Marks all objects and methods below the threshold as requiring review

---

## Component 6: Migration Report Generator Component

### MigrationReportGeneratorService

`generateReport(ReportInput input) : MigrationReport`
- Aggregates all migration data and generates both Markdown and HTML report content

`renderMarkdown(ReportData data) : String`
- Renders the report data as a Markdown string

`renderHtml(ReportData data) : String`
- Renders the report data as a self-contained HTML string (no external dependencies)

`writeReport(MigrationReport report, Path outputDir) : void`
- Writes report.md and report.html to the output directory

`buildTraceabilityMatrix(DiscoveryResult discovery, GeneratedProject project) : TraceabilityMatrix`
- Maps each PL/SQL object to its generated Java class(es) and method(s)

`buildFlaggedConstructsSummary(List<TranslationResult> results) : FlaggedConstructsSummary`
- Aggregates all flagged constructs with recommendations across all translation results

---

## Component 7: Migration Orchestrator Service

### MigrationOrchestratorService

`analyze(MigrationConfig config) : AnalysisResult`
- Runs Discovery + Dependency Analysis only; persists results; returns summary

`generate(MigrationConfig config) : MigrationResult`
- Runs full pipeline: Discovery → Dependency Analysis → Translation → Code Generation → Confidence Scoring → Report Generation

`report(MigrationConfig config) : MigrationReport`
- Loads persisted analysis results and regenerates reports without re-running migration

`getProgress(String migrationId) : MigrationProgress`
- Returns current progress state for a running migration (used by Web UI polling/SSE)

---

## Component 8: CLI Component

### PlSql2JavaCli (main entry point)

`main(String[] args) : void`
- Parses command and delegates to appropriate command handler

### AnalyzeCommand

`execute(AnalyzeOptions options) : int`
- Loads config, invokes MigrationOrchestratorService.analyze(), streams progress to stdout, returns exit code

### GenerateCommand

`execute(GenerateOptions options) : int`
- Loads config, invokes MigrationOrchestratorService.generate(), streams progress to stdout, returns exit code

### ReportCommand

`execute(ReportOptions options) : int`
- Loads config, invokes MigrationOrchestratorService.report(), writes report files, returns exit code

### ConfigLoader

`load(Path configFile, AnalyzeOptions cliOverrides) : MigrationConfig`
- Merges YAML config file with CLI flag overrides into a MigrationConfig

---

## Component 9: Web Application Component

### MigrationController (REST API)

`uploadDdlFiles(MultipartFile[] files, HttpSession session) : ResponseEntity<UploadResponse>`
- Accepts DDL file uploads, validates, stores temporarily, returns upload confirmation

`configureJdbc(JdbcConfigRequest request, HttpSession session) : ResponseEntity<ConnectionTestResponse>`
- Accepts JDBC config, tests connection, stores in session on success

`startAnalysis(HttpSession session) : ResponseEntity<MigrationJobResponse>`
- Triggers async analysis job, returns job ID for progress tracking

`startGeneration(HttpSession session) : ResponseEntity<MigrationJobResponse>`
- Triggers async generation job, returns job ID for progress tracking

`getProgress(String jobId) : SseEmitter`
- Returns Server-Sent Events stream for real-time progress updates

`downloadProject(String jobId, HttpServletResponse response) : void`
- Streams generated Maven project as a ZIP archive download

`getReport(String jobId) : ResponseEntity<ReportResponse>`
- Returns report content (HTML inline view + download links)

`downloadReport(String jobId, String format, HttpServletResponse response) : void`
- Streams report file (.md or .html) as a download

### SecurityConfig

`securityFilterChain(HttpSecurity http) : SecurityFilterChain`
- Configures authentication, CORS, CSRF, and HTTP security headers for all endpoints
