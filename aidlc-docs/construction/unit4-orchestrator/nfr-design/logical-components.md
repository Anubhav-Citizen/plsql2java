# Logical Components — Unit 4: Migration Orchestrator

---

## Spring Components

| Component | Type | Package |
|---|---|---|
| MigrationOrchestratorService | @Service | com.plsql2java.orchestration |
| ProgressEventBus | @Component | com.plsql2java.orchestration.event |
| OrchestratorAutoConfiguration | @Configuration | com.plsql2java.orchestration |

---

## Model Classes (Plain Java)

| Class | Package |
|---|---|
| MigrationJob | com.plsql2java.orchestration.model |
| MigrationJobStatus (enum) | com.plsql2java.orchestration.model |
| OperationMode (enum) | com.plsql2java.orchestration.model |
| AnalysisResult | com.plsql2java.orchestration.model |
| MigrationResult | com.plsql2java.orchestration.model |
| MigrationProgress | com.plsql2java.orchestration.model |
| PipelineStage (enum) | com.plsql2java.orchestration.model |

---

## Dependencies Injected into MigrationOrchestratorService

| Service | Source Module |
|---|---|
| OracleDiscoveryService | plsql2java-discovery |
| DependencyAnalyzerService | plsql2java-discovery |
| PlSqlTranslationEngine | plsql2java-translation |
| JavaCodeGeneratorService | plsql2java-codegen |
| ConfidenceScorerService | plsql2java-codegen |
| MigrationReportGeneratorService | plsql2java-codegen |
