# Unit 5 — CLI Delivery — Code Generation Plan

**Unit**: plsql2java-cli  
**Package Root**: com.plsql2java.cli  
**Stories**: 7.1 (Analyze Command), 7.2 (Generate Command), 7.3 (Report Command)  
**Dependencies**: plsql2java-orchestrator (→ plsql2java-discovery, translation, codegen)

---

## Unit Context

- **Delivery type**: Standalone CLI via Picocli + Spring Boot (no web)
- **Key classes**: PlSql2JavaCli, AnalyzeCommand, GenerateCommand, ReportCommand, ConfigLoader, CliProgressListener, MigrationConfigMapper
- **Config**: YAML + CLI flags + env vars (merge chain)
- **Packaging**: Fat JAR via spring-boot-maven-plugin
- **Security**: SECURITY-03 (no credentials in logs), SECURITY-05 (input validation), SECURITY-09 (generic error messages), SECURITY-10 (pinned deps), SECURITY-15 (global exception handler)

---

## Steps

- [x] **Step 1**: Create `plsql2java-cli/pom.xml`
  - Parent: root pom; dependency: plsql2java-orchestrator, picocli, picocli-spring-boot-starter, jackson-dataformat-yaml, spring-boot-starter
  - spring-boot-maven-plugin repackage goal; mainClass = com.plsql2java.cli.PlSql2JavaCli
  - Stories: infrastructure for 7.1, 7.2, 7.3

- [x] **Step 2**: Create domain model — `ExitCode.java` (enum)
  - Values: SUCCESS(0), PARTIAL_SUCCESS(1), VALIDATION_ERROR(2), EXECUTION_ERROR(3), IO_ERROR(4)
  - Stories: 7.1, 7.2, 7.3

- [x] **Step 3**: Create `CliConfig.java` (value object)
  - Fields: configFile, jdbcUrl, jdbcUser, jdbcPassword (char[]), ddlFiles, outputDir, targetPackage, confidenceThreshold, verbose, objectTypes
  - Stories: 7.1, 7.2, 7.3

- [x] **Step 4**: Create `ConfigLoader.java`
  - load(Path): CliConfig (YAML parse via Jackson)
  - merge(base, overrides): CliConfig (CLI flags override)
  - Reads PLSQL2JAVA_JDBC_PASSWORD env var
  - Stories: 7.1, 7.2, 7.3

- [x] **Step 5**: Create `MigrationConfigMapper.java`
  - toMigrationConfig(CliConfig): MigrationConfig
  - Validates Java package identifier (BR-CLI-03)
  - Clears password char[] after mapping
  - Stories: 7.1, 7.2, 7.3

- [x] **Step 6**: Create `CliProgressListener.java`
  - Implements MigrationProgressListener
  - Constructor: PrintStream out, boolean verbose
  - Verbose: all events; non-verbose: stage transitions only
  - Stories: 7.1, 7.2

- [x] **Step 7**: Create `AnalyzeCommand.java`
  - @Command(name="analyze"), implements Callable<Integer>
  - Options: --config, --jdbc-url, --jdbc-user, --ddl-file (multi), --output-dir, --verbose
  - Validates: source required (BR-CLI-01), output dir (BR-CLI-04), DDL files exist (BR-CLI-10)
  - Calls orchestrator.analyze(); prints AnalysisResult summary
  - Story: 7.1

- [x] **Step 8**: Create `GenerateCommand.java`
  - @Command(name="generate"), implements Callable<Integer>
  - Options: all analyze options + --target-package, --confidence-threshold, --object-types
  - Validates: BR-CLI-01, BR-CLI-02, BR-CLI-03, BR-CLI-04, BR-CLI-05, BR-CLI-10
  - Calls orchestrator.generate(); prints MigrationResult summary; lists skipped objects
  - Returns PARTIAL_SUCCESS if result.isPartial()
  - Story: 7.2

- [x] **Step 9**: Create `ReportCommand.java`
  - @Command(name="report"), implements Callable<Integer>
  - Options: --output-dir (required), --format (md|html|both, default=both)
  - Validates: output dir exists + contains prior migration result (BR-CLI-07)
  - Calls orchestrator.report(); prints report paths
  - Story: 7.3

- [x] **Step 10**: Create `PlSql2JavaCli.java` (main entry point)
  - @SpringBootApplication + @Command(name="plsql2java", subcommands={...})
  - main(): Spring context bootstrap + Picocli CommandLine.execute()
  - Global exception handler (try/catch Throwable) — SECURITY-15
  - Story: 7.1, 7.2, 7.3

- [x] **Step 11**: Create `src/main/resources/application.yml` + `migration-config-template.yml`
  - application.yml: disable Spring Boot banner, set logging to WARN for non-cli packages
  - migration-config-template.yml: documented YAML config template
  - Stories: 7.1, 7.2, 7.3

- [x] **Step 12**: Create unit tests — `ConfigLoaderTest.java`
  - Test YAML load, merge chain priority, env var override, missing password handling
  - Story: 7.1, 7.2, 7.3

- [x] **Step 13**: Create unit tests — `MigrationConfigMapperTest.java`
  - Test valid package, invalid package (BR-CLI-03), password clearing
  - Stories: 7.1, 7.2

- [x] **Step 14**: Create unit tests — `AnalyzeCommandTest.java`, `GenerateCommandTest.java`, `ReportCommandTest.java`
  - Mock MigrationOrchestratorService; test validation rules, exit codes, progress output
  - Stories: 7.1, 7.2, 7.3

- [x] **Step 15**: Create `CliIntegrationTest.java`
  - End-to-end test: real Spring context, mock orchestrator, sample DDL fixture
  - Verify exit codes, stdout content, config loading
  - Stories: 7.1, 7.2, 7.3

- [x] **Step 16**: Create `aidlc-docs/construction/unit5-cli/code/code-summary.md`

---

## Story Traceability

| Story | Steps |
|---|---|
| 7.1 Analyze Command | 1, 2, 3, 4, 5, 6, 7, 10, 11, 12, 13, 14, 15 |
| 7.2 Generate Command | 1, 2, 3, 4, 5, 6, 8, 10, 11, 12, 13, 14, 15 |
| 7.3 Report Command | 1, 2, 3, 4, 9, 10, 11, 12, 14, 15 |

---

## Security Compliance Checklist

| Rule | Step(s) | Status |
|---|---|---|
| SECURITY-03 (no credentials in logs) | 4, 5, 6, 7, 8 | Enforced |
| SECURITY-05 (input validation) | 7, 8, 9 | Enforced |
| SECURITY-09 (generic error messages) | 7, 8, 9, 10 | Enforced |
| SECURITY-10 (pinned deps) | 1 | Enforced |
| SECURITY-15 (global exception handler) | 10 | Enforced |
| SECURITY-01 (encryption at rest) | N/A — no data store | N/A |
| SECURITY-02 (access logging) | N/A — no network intermediary | N/A |
| SECURITY-04 (HTTP headers) | N/A — CLI, no HTTP | N/A |
| SECURITY-06 (least privilege) | N/A — no IAM | N/A |
| SECURITY-07 (network config) | N/A — no network config | N/A |
| SECURITY-08 (app-level access control) | N/A — no auth layer | N/A |
| SECURITY-11 (secure design) | 4, 5, 10 | Enforced |
| SECURITY-12 (auth/credential mgmt) | 4, 5 (password isolation) | Enforced |
| SECURITY-13 (integrity) | N/A — no deserialization of untrusted data | N/A |
| SECURITY-14 (alerting) | N/A — CLI, no monitoring infra | N/A |
