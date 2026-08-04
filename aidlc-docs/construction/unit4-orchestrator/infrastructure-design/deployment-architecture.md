# Deployment Architecture — Unit 4: Migration Orchestrator

---

## Deployment Model

Unit 4 is a library — it has no standalone deployment. It is embedded in:

- **Unit 5 (CLI)**: `plsql2java-cli` executable JAR includes orchestrator on classpath
- **Unit 6 (Web)**: `plsql2java-web` Spring Boot application includes orchestrator on classpath

## No External Infrastructure Required

- No database
- No message broker
- No container image for this module alone
- File system I/O to a configurable `outputDir`

## Spring Context Integration

```
CLI Spring Context / Web Spring Context
  └── OrchestratorAutoConfiguration
        └── MigrationOrchestratorService
              ├── OracleDiscoveryService (from plsql2java-discovery)
              ├── DependencyAnalyzerService (from plsql2java-discovery)
              ├── PlSqlTranslationEngine (from plsql2java-translation)
              ├── JavaCodeGeneratorService (from plsql2java-codegen)
              ├── ConfidenceScorerService (from plsql2java-codegen)
              └── MigrationReportGeneratorService (from plsql2java-codegen)
```
