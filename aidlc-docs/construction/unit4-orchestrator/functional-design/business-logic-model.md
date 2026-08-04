# Business Logic Model — Unit 4: Migration Orchestrator

---

## Component: MigrationOrchestratorService

**Responsibility**: Sequences all core engine services for all three operation modes.

### analyze(MigrationConfig config, ProgressListener listener) → AnalysisResult

1. Create MigrationJob (mode=ANALYZE, status=RUNNING)
2. Emit DISCOVERY stage progress
3. If config.isJdbcMode(): call `OracleDiscoveryService.discoverFromJdbc()`
   Else: call `OracleDiscoveryService.discoverFromFiles()`
4. Emit DEPENDENCY_ANALYSIS stage progress
5. Call `DependencyAnalyzerService.analyze(discoveryResult)`
6. Persist AnalysisResult to `config.outputDir/analysis/`
7. Update job status = COMPLETED
8. Return AnalysisResult

### generate(MigrationConfig config, ProgressListener listener) → MigrationResult

1. Create MigrationJob (mode=GENERATE, status=RUNNING)
2. Run analyze() to get AnalysisResult
3. Emit TRANSLATION stage progress
4. For each OracleObject in migrationOrder (from DependencyGraph):
   - Call `PlSqlTranslationEngine.translate(object)`
   - Collect TranslationResult (fail-partial: catch per-object exceptions)
5. Emit CODE_GENERATION stage progress
6. Build GenerationContext from MigrationConfig
7. Call `JavaCodeGeneratorService.generateProject(translationResults, ctx)`
8. Emit CONFIDENCE_SCORING stage progress
9. Call `ConfidenceScorerService.scoreAll(translationResults, config.confidenceThreshold)`
10. Emit REPORT_GENERATION stage progress
11. Build ReportInput from all prior results
12. Call `MigrationReportGeneratorService.generateReport(reportInput)`
13. Call `JavaCodeGeneratorService.writeProject(generatedProject, outputDir)`
14. Call `MigrationReportGeneratorService.writeReport(migrationReport, outputDir)`
15. Persist MigrationResult to `config.outputDir/`
16. Emit COMPLETE stage progress
17. Update job status = COMPLETED (or PARTIAL if skippedObjects non-empty)
18. Return MigrationResult

### report(MigrationConfig config, ProgressListener listener) → MigrationReport

1. Create MigrationJob (mode=REPORT, status=RUNNING)
2. Load persisted MigrationResult from `config.outputDir/`
3. Rebuild ReportInput from loaded data
4. Call `MigrationReportGeneratorService.generateReport(reportInput)`
5. Call `MigrationReportGeneratorService.writeReport(migrationReport, outputDir)`
6. Update job status = COMPLETED
7. Return MigrationReport

---

## Component: ProgressEventBus

**Responsibility**: Routes ProgressEvents to all registered listeners.

### emit(MigrationProgress event)

- For each registered ProgressListener: call `listener.onProgress(event)`
- Exceptions from individual listeners are caught and logged — never propagate to caller

### register(ProgressListener listener)

- Add listener to internal list

### unregister(ProgressListener listener)

- Remove listener from internal list

---

## Fail-Partial Strategy

- Translation loop wraps each object in try/catch
- Failed objects are logged at WARN with object name and exception message
- Failed object names collected in `skippedObjects` list
- Pipeline continues for all remaining objects
- Final MigrationResult always returned (never null)
- `MigrationJobStatus.PARTIAL` set if any objects were skipped
