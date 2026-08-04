# Logical Components — Unit 5: CLI Delivery

## Component: PlSql2JavaCli (Entry Point)
- Top-level `@Command` with `subcommands = {AnalyzeCommand, GenerateCommand, ReportCommand}`
- `main()` method: Spring Boot application context bootstrap + Picocli execute
- Global exception handler wrapping `execute()` call
- Exits with the integer returned by the executed subcommand

## Component: AnalyzeCommand
- `@Command(name = "analyze")`
- Injects `MigrationOrchestratorService`, `ConfigLoader`, `ProgressEventBus`
- Declares all analyze-specific options (`--jdbc-url`, `--ddl-file`, `--output-dir`, etc.)
- Validates inputs, builds `MigrationConfig`, calls `orchestrator.analyze()`
- Prints `AnalysisResult` summary to stdout
- Returns `ExitCode` integer

## Component: GenerateCommand
- `@Command(name = "generate")`
- Same injections as AnalyzeCommand plus `--target-package`, `--confidence-threshold`
- Calls `orchestrator.generate()`
- Prints `MigrationResult` summary; lists skipped objects if any
- Returns `ExitCode.PARTIAL_SUCCESS` if `result.isPartial()`, else `SUCCESS`

## Component: ReportCommand
- `@Command(name = "report")`
- Minimal options: `--output-dir`, `--format`
- Validates prior migration output exists in output dir
- Calls `orchestrator.report()`
- Prints report file paths
- Returns `ExitCode.SUCCESS`

## Component: ConfigLoader
- No Picocli dependency — pure Java
- `load(Path configFile): CliConfig` — parses YAML
- `merge(CliConfig base, CliConfig overrides): CliConfig` — applies override chain
- `toMigrationConfig(CliConfig): MigrationConfig` — maps to orchestrator model
- Reads `PLSQL2JAVA_JDBC_PASSWORD` env var

## Component: CliProgressListener
- Implements `MigrationProgressListener`
- Constructor: `PrintStream out, boolean verbose`
- `onProgress(MigrationProgress)`: formats and prints to `out`
- Verbose mode: all events; non-verbose: stage transitions only

## Component: MigrationConfigMapper
- Static utility: `CliConfig → MigrationConfig`
- Validates Java package identifier format
- Clears password char[] after mapping

## No External Infrastructure Components
- No message queue, no cache, no database
- All state is in-process; CLI is stateless between invocations
