# Code Summary — Unit 5: CLI Delivery

## Module: plsql2java-cli

**Package Root**: `com.plsql2java.cli`  
**Stories Implemented**: 7.1 (Analyze), 7.2 (Generate), 7.3 (Report)

---

## Application Code

### Entry Point
- `PlSql2JavaCli.java` — `@SpringBootApplication` + `@Command`; Spring Boot + Picocli bootstrap; global exception handler (SECURITY-15)

### Commands (`command/`)
- `AnalyzeCommand.java` — Story 7.1; `--ddl-file`/`--jdbc-url` source; validates BR-CLI-01/04/10; calls `orchestrator.analyze()`
- `GenerateCommand.java` — Story 7.2; full validation BR-CLI-01–05/10; calls `orchestrator.generate()`; returns `PARTIAL_SUCCESS` if skipped objects
- `ReportCommand.java` — Story 7.3; validates prior output exists (BR-CLI-07); calls `orchestrator.report()`

### Config (`config/`)
- `CliConfig.java` — value object; password as `char[]` (SECURITY-03)
- `ConfigLoader.java` — YAML parse (Jackson); merge chain (file → env var → CLI flags); reads `PLSQL2JAVA_JDBC_PASSWORD`
- `MigrationConfigMapper.java` — `CliConfig → MigrationConfig`; Java package validation (BR-CLI-03); password clearing after mapping

### Progress (`progress/`)
- `CliProgressListener.java` — `MigrationProgressListener` impl; verbose/non-verbose modes; stage transition detection

### Domain
- `ExitCode.java` — enum: SUCCESS(0), PARTIAL_SUCCESS(1), VALIDATION_ERROR(2), EXECUTION_ERROR(3), IO_ERROR(4)

### Resources
- `application.yml` — Spring Boot banner off; web-application-type none; logging levels
- `migration-config-template.yml` — documented YAML config template for end users

---

## Tests

- `ConfigLoaderTest.java` — YAML load, merge chain priority, verbose union, empty config
- `MigrationConfigMapperTest.java` — field mapping, JDBC config, password clearing, package validation (valid + invalid), null output dir default
- `AnalyzeCommandTest.java` — DDL file success, no source error, non-existent file, help flag
- `GenerateCommandTest.java` — success, partial success, no source, invalid package, confidence threshold out of range
- `ReportCommandTest.java` — success, missing output dir, no prior analysis, invalid format
- `CliIntegrationTest.java` — Spring context wired; analyze/generate/report end-to-end with mock orchestrator; config file merge

---

## Security Compliance

| Rule | Status | Notes |
|---|---|---|
| SECURITY-03 | Compliant | Password in `char[]`; cleared after mapping; never logged |
| SECURITY-05 | Compliant | All inputs validated before pipeline invocation |
| SECURITY-09 | Compliant | Generic error messages to stderr; details to SLF4J only |
| SECURITY-10 | Compliant | All deps pinned in root pom.xml |
| SECURITY-11 | Compliant | Credential logic isolated in ConfigLoader + MigrationConfigMapper |
| SECURITY-12 | Compliant | Password via env var only; char[] cleared after use |
| SECURITY-15 | Compliant | Global exception handler in `PlSql2JavaCli.main()` |
| SECURITY-01/02/04/06/07/08/13/14 | N/A | CLI: no data store, no HTTP, no auth layer, no network config |

---

## Build Artifact

- Fat JAR: `plsql2java-cli-1.0.0-SNAPSHOT.jar` (via `spring-boot-maven-plugin` repackage)
- Main class: `com.plsql2java.cli.PlSql2JavaCli`
- Invocation: `java -jar plsql2java-cli.jar <analyze|generate|report> [options]`
