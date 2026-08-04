# Unit Test Execution Instructions — plsql2java

## Overview

Each module has its own unit test suite. Tests use JUnit 5 + Mockito + AssertJ. All external dependencies (Oracle JDBC, orchestrator services) are mocked.

---

## Run All Unit Tests

```bash
cd c:\project\repo\plsql2java
mvn test
```

---

## Run Tests Per Module

```bash
# Unit 1 — Discovery & Dependency
mvn test -pl plsql2java-discovery

# Unit 2 — Translation Engine
mvn test -pl plsql2java-translation

# Unit 3 — Code Generator + Scoring + Reporting
mvn test -pl plsql2java-codegen

# Unit 4 — Migration Orchestrator
mvn test -pl plsql2java-orchestrator

# Unit 5 — CLI Delivery
mvn test -pl plsql2java-cli

# Unit 6 — Web Application
mvn test -pl plsql2java-web
```

---

## Test Classes by Module

### plsql2java-discovery
| Test Class | Coverage |
|---|---|
| `OracleDiscoveryServiceTest` | JDBC + file discovery, error handling |
| `DependencyAnalyzerServiceTest` | Graph construction, topological sort, circular detection |
| `DiscoveryResultTest` | Domain model validation |
| `DependencyGraphTest` | Edge cases, empty graph |

### plsql2java-translation
| Test Class | Coverage |
|---|---|
| `PlSqlTranslationEngineTest` | Full translation pipeline |
| `TranslationRuleRegistryTest` | Rule registration and lookup |
| `IfElseRuleTest`, `LoopRuleTest`, etc. | One test class per translation rule (14 rules) |
| `TranslationIntegrationTest` | Sample PL/SQL files end-to-end |

### plsql2java-codegen
| Test Class | Coverage |
|---|---|
| `JavaCodeGeneratorServiceTest` | All artifact types, fail-partial |
| `ConfidenceScorerServiceTest` | Score clamping, threshold alerting |
| `MigrationReportGeneratorServiceTest` | Markdown + HTML output |
| `CodeGenerationIntegrationTest` | Full project generation from sample input |

### plsql2java-orchestrator
| Test Class | Coverage |
|---|---|
| `MigrationOrchestratorServiceTest` | Pipeline order, fail-partial, progress events, exception wrapping |
| `ProgressEventBusTest` | Listener registration, exception swallowing |
| `MigrationResultTest` | isPartial() derived field |
| `MigrationProgressTest` | pct calculation |
| `MigrationOrchestratorIntegrationTest` | Real services + sample DDL fixture |

### plsql2java-cli
| Test Class | Coverage |
|---|---|
| `ConfigLoaderTest` | YAML parse, merge chain, env var |
| `MigrationConfigMapperTest` | Field mapping, package validation, password clearing |
| `AnalyzeCommandTest` | Exit codes, validation rules |
| `GenerateCommandTest` | Partial success, all validation rules |
| `ReportCommandTest` | Prior output check, format validation |
| `CliIntegrationTest` | Spring context + mock orchestrator |

### plsql2java-web
| Test Class | Coverage |
|---|---|
| `FileUploadServiceTest` | Sandbox, sanitization, path traversal, delete |
| `ZipPackagerTest` | ZIP creation, hidden file exclusion, stream-and-delete |
| `MigrationJobRegistryTest` | Register/get, TTL, JobNotFoundException |
| `MigrationControllerTest` | All endpoints, HTTP status codes, security headers |
| `WebIntegrationTest` | Full Spring context, upload+analyze flow, auth redirect |

---

## Expected Results

```
Tests run: ~120, Failures: 0, Errors: 0, Skipped: 0
```

**Coverage target**: ≥ 80% line coverage per module (enforced by JaCoCo in root pom.xml)

---

## Test Reports

After `mvn test`, reports are at:
```
{module}/target/surefire-reports/          # JUnit XML reports
{module}/target/site/jacoco/index.html     # JaCoCo coverage report
```

---

## Running a Single Test Class

```bash
mvn test -pl plsql2java-cli -Dtest=ConfigLoaderTest
```

## Running a Single Test Method

```bash
mvn test -pl plsql2java-cli -Dtest=ConfigLoaderTest#merge_cliFlagsOverrideFileConfig
```
