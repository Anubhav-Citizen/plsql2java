# Integration Test Instructions — plsql2java

## Overview

Integration tests verify that units work together correctly through the full pipeline. They use real service implementations with sample DDL fixtures (no Oracle DB required for file-mode tests).

---

## Scenario 1: Discovery → Dependency Analysis (Units 1–1)

**Test class**: `MigrationOrchestratorIntegrationTest` in `plsql2java-orchestrator`

**What is tested**: `OracleDiscoveryService` (file mode) → `DependencyAnalyzerService` → `AnalysisResult`

**Setup**: Sample DDL files in `plsql2java-discovery/src/test/resources/`

**Run**:
```bash
mvn test -pl plsql2java-orchestrator -Dtest=MigrationOrchestratorIntegrationTest
```

**Expected**: `AnalysisResult` with discovered objects, dependency graph, migration order

---

## Scenario 2: Translation Pipeline (Units 1–2)

**Test class**: `TranslationIntegrationTest` in `plsql2java-translation`

**What is tested**: DDL file → `OracleDiscoveryService` → `PlSqlTranslationEngine` → `TranslationResult`

**Run**:
```bash
mvn test -pl plsql2java-translation -Dtest=TranslationIntegrationTest
```

**Expected**: `TranslationResult` with `JavaIR` for each PL/SQL object; flagged constructs listed

---

## Scenario 3: Full Code Generation Pipeline (Units 1–3)

**Test class**: `CodeGenerationIntegrationTest` in `plsql2java-codegen`

**What is tested**: `TranslationResult` → `JavaCodeGeneratorService` → `ConfidenceScorerService` → `MigrationReportGeneratorService` → `GeneratedProject` + `MigrationReport`

**Run**:
```bash
mvn test -pl plsql2java-codegen -Dtest=CodeGenerationIntegrationTest
```

**Expected**: Generated Java files, pom.xml, confidence report, HTML + Markdown report

---

## Scenario 4: End-to-End Orchestrator Pipeline (Units 1–4)

**Test class**: `MigrationOrchestratorIntegrationTest` in `plsql2java-orchestrator`

**What is tested**: Full `generate()` pipeline from DDL files to generated project + report

**Run**:
```bash
mvn test -pl plsql2java-orchestrator -Dtest=MigrationOrchestratorIntegrationTest
```

**Expected**: `MigrationResult` with all fields populated; output written to temp directory

---

## Scenario 5: CLI End-to-End (Units 1–5)

**Test class**: `CliIntegrationTest` in `plsql2java-cli`

**What is tested**: Picocli command parsing → `ConfigLoader` → `MigrationOrchestratorService` (mocked) → exit codes

**Run**:
```bash
mvn test -pl plsql2java-cli -Dtest=CliIntegrationTest
```

**Expected**: Correct exit codes (0, 1, 2, 3, 4) for all scenarios; progress output to stdout

---

## Scenario 6: Web API End-to-End (Units 1–6)

**Test class**: `WebIntegrationTest` in `plsql2java-web`

**What is tested**: HTTP upload → analyze → SSE events → status polling (orchestrator mocked)

**Run**:
```bash
mvn test -pl plsql2java-web -Dtest=WebIntegrationTest
```

**Expected**: HTTP 200 for upload, 202 for analyze/generate, 401 for unauthenticated requests

---

## Full Integration Test Suite

Run all integration tests across all modules:

```bash
mvn test -Dtest="*IntegrationTest"
```

---

## Manual End-to-End Test (CLI — File Mode)

After building the CLI fat JAR:

```bash
# 1. Build
mvn clean package -pl plsql2java-cli -am -DskipTests

# 2. Create a sample DDL file
echo "CREATE OR REPLACE PROCEDURE hello_world AS BEGIN DBMS_OUTPUT.PUT_LINE('Hello'); END;" > test.sql

# 3. Run analyze
java -jar plsql2java-cli/target/plsql2java-cli-1.0.0-SNAPSHOT.jar analyze \
  --ddl-file test.sql \
  --output-dir ./test-output \
  --verbose

# Expected exit code: 0
# Expected output: [INFO] Discovered N objects

# 4. Run generate
java -jar plsql2java-cli/target/plsql2java-cli-1.0.0-SNAPSHOT.jar generate \
  --ddl-file test.sql \
  --output-dir ./test-output \
  --target-package com.example.test \
  --verbose

# Expected exit code: 0 or 1 (partial)
# Expected output: generated project in ./test-output/generated/
```

---

## Manual End-to-End Test (Web — Docker)

```bash
# 1. Build and start
cd plsql2java-web
mvn clean package -DskipTests
ADMIN_PASSWORD=testpass123 docker-compose up -d

# 2. Open browser
# http://localhost:8080  (redirects to /login)
# Login: admin / testpass123

# 3. Upload a DDL file and trigger analysis via UI

# 4. Verify SSE progress updates in browser

# 5. Download generated project ZIP

# 6. Teardown
docker-compose down
```
