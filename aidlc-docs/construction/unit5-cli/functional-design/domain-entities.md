# Domain Entities — Unit 5: CLI Delivery

## Entities Owned by Unit 5

Unit 5 owns no new domain entities. It consumes entities from upstream units:
- `MigrationConfig` (plsql2java-discovery) — populated from CLI args + YAML
- `MigrationResult` (plsql2java-orchestrator) — displayed as CLI output
- `AnalysisResult` (plsql2java-orchestrator) — displayed for analyze command
- `MigrationProgress` (plsql2java-orchestrator) — streamed to stdout

## CLI-Specific Value Objects

### CliConfig
- `configFile: Path` — path to YAML config file (optional)
- `jdbcUrl: String` — Oracle JDBC URL (optional, overrides config file)
- `jdbcUser: String` — Oracle username (optional)
- `jdbcPassword: String` — Oracle password (optional, from env var)
- `ddlFiles: List<Path>` — DDL file paths for offline mode (optional)
- `outputDir: Path` — output directory for generated artifacts
- `targetPackage: String` — Java package for generated code
- `confidenceThreshold: double` — minimum confidence score (0.0–1.0)
- `verbose: boolean` — enable verbose progress output
- `objectTypes: List<String>` — filter by object type (optional)

### ExitCode (enum)
- `SUCCESS(0)` — operation completed successfully
- `PARTIAL_SUCCESS(1)` — completed with some skipped objects
- `VALIDATION_ERROR(2)` — invalid arguments or config
- `EXECUTION_ERROR(3)` — pipeline execution failure
- `IO_ERROR(4)` — file I/O failure

## Relationships
```
CliConfig ──mapped-to──> MigrationConfig
MigrationProgress ──consumed-by──> CliProgressListener ──writes-to──> stdout
MigrationResult ──consumed-by──> AnalyzeCommand/GenerateCommand/ReportCommand
ExitCode ──returned-by──> PlSql2JavaCli.main()
```
