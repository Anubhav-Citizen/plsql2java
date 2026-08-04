# Component Dependencies
## plsql2java — Oracle PL/SQL Legacy Modernization Platform

---

## Dependency Matrix

| Component | Depends On | Communication Pattern |
|---|---|---|
| CLI Component | MigrationOrchestratorService, ConfigLoader | Direct method call |
| Web Application Component | MigrationOrchestratorService | Direct method call (async via Spring @Async) |
| MigrationOrchestratorService | OracleDiscoveryService, DependencyAnalyzerService, PlSqlTranslationEngine, JavaCodeGeneratorService, ConfidenceScorerService, MigrationReportGeneratorService | Sequential direct method calls |
| OracleDiscoveryService | Oracle JDBC Driver (external), DDL Parser (internal) | JDBC API, internal parser |
| DependencyAnalyzerService | OracleDiscoveryService (data only — DiscoveryResult) | Data model (no direct service call) |
| PlSqlTranslationEngine | TranslationRuleRegistry, PL/SQL AST Parser (internal) | Internal registry lookup |
| JavaCodeGeneratorService | Template Engine (FreeMarker/Mustache), TranslationResult | Template rendering |
| ConfidenceScorerService | TranslationResult (data only) | Data model (no direct service call) |
| MigrationReportGeneratorService | DiscoveryResult, DependencyGraph, TranslationResult, GeneratedProject, ConfidenceReport | Data model aggregation |

---

## Dependency Rules

1. **Delivery layer → Orchestration layer only**: CLI and Web components MUST NOT call core engine services directly
2. **Orchestration layer → Core engine**: MigrationOrchestratorService is the only caller of core engine services
3. **Core engine components are independent**: No core engine component calls another core engine component directly — all coordination goes through the orchestrator
4. **Data flows downstream**: DiscoveryResult → DependencyGraph → TranslationResult → GeneratedProject → ConfidenceReport → MigrationReport
5. **No upward dependencies**: Lower layers never depend on higher layers

---

## Data Flow Diagram

```
[Oracle DB / DDL Files]
         |
         v
[OracleDiscoveryService] --> DiscoveryResult
         |
         v
[DependencyAnalyzerService] --> DependencyGraph + ordered List<OracleObject>
         |
         v
[PlSqlTranslationEngine] --> List<TranslationResult>
         |
         v
[JavaCodeGeneratorService] --> GeneratedProject
         |
         v
[ConfidenceScorerService] --> ConfidenceReport
         |
         v
[MigrationReportGeneratorService] --> MigrationReport (.md + .html)
         |
         v
[Output Directory]
```

---

## Key Domain Models (Shared Across Components)

### MigrationConfig
- jdbcConfig: JdbcConfig (url, username, password — from env vars)
- ddlFiles: List<Path>
- outputDir: Path
- confidenceThreshold: int (default: 70)
- migrationId: String (UUID)

### OracleObject
- name: String
- type: OracleObjectType (PACKAGE, PROCEDURE, FUNCTION, TRIGGER, VIEW, SEQUENCE, TYPE)
- schema: String
- sourceSpec: String (package spec or standalone source)
- sourceBody: String (package body, null for non-packages)

### DiscoveryResult
- objects: List<OracleObject>
- discoveryMode: DiscoveryMode (JDBC / FILE)
- schemaName: String
- discoveredAt: Instant

### DependencyGraph
- nodes: Set<String> (object names)
- edges: Map<String, Set<String>> (object → objects it depends on)
- circularDependencies: List<CircularDependency>
- migrationOrder: List<String> (topologically sorted object names)

### TranslationResult
- sourceObject: OracleObject
- javaIntermediateRepresentation: JavaIR
- constructResults: List<ConstructTranslationResult> (per-construct status)
- flaggedConstructs: List<FlaggedConstruct>
- overallStatus: TranslationStatus (FULLY_TRANSLATED / PARTIALLY_TRANSLATED / FAILED)

### GeneratedProject
- files: Map<String, String> (relative path → file content)
- projectName: String
- generatedAt: Instant

### ConfidenceReport
- objectScores: Map<String, ObjectConfidenceScore>
- methodScores: Map<String, MethodConfidenceScore>
- itemsBelowThreshold: List<ConfidenceFlag>
- overallScore: double

### MigrationReport
- markdownContent: String
- htmlContent: String
- generatedAt: Instant

---

## External Dependencies

| Dependency | Purpose | Component |
|---|---|---|
| Oracle JDBC Driver (ojdbc11) | Live database connectivity | OracleDiscoveryService |
| ANTLR4 + Oracle PL/SQL Grammar | PL/SQL AST parsing | PlSqlTranslationEngine |
| FreeMarker or Mustache | Java code template rendering | JavaCodeGeneratorService |
| Spring Boot 3.x | Web framework, DI, JPA | Web Application Component |
| Spring Security | Authentication, HTTP security headers | Web Application Component |
| Springdoc OpenAPI | OpenAPI spec generation | Web Application Component |
| Picocli or Spring Shell | CLI argument parsing | CLI Component |
| SLF4J + Logback | Structured logging | All components |
| Jackson | JSON serialization (config, state persistence) | Multiple components |
