# Code Summary — Unit 1: Discovery & Dependency Analysis

## Maven Module: `plsql2java-discovery`

---

## Files Created

### Root
- `pom.xml` — Parent POM (multi-module, Spring Boot BOM, dependency management, JaCoCo ≥80% coverage)

### Module POM
- `plsql2java-discovery/pom.xml` — Module POM (ojdbc11, jackson, spring-context, slf4j, test deps)

### Common Infrastructure (`com.plsql2java.common`)
- `ProgressStage.java` — Enum: DISCOVERY, DEPENDENCY_ANALYSIS, TRANSLATION, CODE_GENERATION, SCORING, REPORTING
- `ProgressEvent.java` — Progress event with builder pattern; calculates percentComplete automatically
- `ProgressListener.java` — Functional interface; includes noOp() factory method
- `MigrationConfig.java` — Migration configuration (migrationId, jdbcConfig, ddlFiles, outputDir, threshold)
- `JdbcConfig.java` — JDBC connection config; effectiveSchema() derives schema from username if not set

### Domain Models (`com.plsql2java.model`)
- `OracleObjectType.java` — Enum: PACKAGE, PACKAGE_BODY, PROCEDURE, FUNCTION, TRIGGER, VIEW, SEQUENCE, TYPE, TYPE_BODY
- `OracleObject.java` — Core domain model; getFullSource() returns spec+body
- `DiscoveryMode.java` — Enum: JDBC, FILE
- `DiscoveryErrorType.java` — Enum: PARSE_ERROR, CONNECTION_ERROR, PERMISSION_ERROR, FILE_NOT_FOUND, FILE_TOO_LARGE, UNSUPPORTED_FILE_TYPE
- `DiscoveryError.java` — Error record with source, type, message, optional lineNumber
- `DiscoveryResult.java` — Discovery output: objects, errors, sourceFiles, discoveredAt
- `ReferenceType.java` — Enum: CALL, REFERENCE, TRIGGER_ON
- `DependencyEdge.java` — Directed edge: fromObject → toObject with ReferenceType
- `CircularDependency.java` — Cycle record with auto-generated description
- `DependencyGraph.java` — Full graph: edges, cycles, migrationOrder, leafObjects

### Discovery Infrastructure (`com.plsql2java.discovery`)
- `DiscoveryException.java` — Runtime exception; user-safe messages only
- `OracleObjectNormalizer.java` — Merges PACKAGE_BODY into PACKAGE spec; TYPE_BODY into TYPE spec
- `ResultPersistenceService.java` — JSON serialization/deserialization of DiscoveryResult and DependencyGraph
- `OracleDiscoveryService.java` — Main service: discoverFromJdbc(), discoverFromFiles(), persist(), load()
- `DiscoveryAutoConfiguration.java` — Spring @Configuration + @ComponentScan

### JDBC Infrastructure (`com.plsql2java.discovery.jdbc`)
- `OracleDataDictionaryQueries.java` — Externalized SQL constants (GET_ALL_OBJECTS, GET_SOURCE, GET_VIEW_TEXT, COUNT_ERRORS)
- `JdbcConnectionManager.java` — Creates JDBC connections; never logs password; throws DiscoveryException on failure
- `OracleDataDictionaryReader.java` — Executes data dictionary queries; fetch size 100; safe error handling

### DDL File Parsing (`com.plsql2java.discovery.file`)
- `DdlPatterns.java` — Externalized regex patterns for all 9 supported Oracle object types
- `DdlFileParser.java` — Tokenizes DDL files; classifies statements; validates paths and file size; sandboxed

### Dependency Analysis (`com.plsql2java.dependency`)
- `DependencyPatterns.java` — Externalized regex patterns for cross-reference detection
- `DependencyGraphBuilder.java` — Builds adjacency list from OracleObject sources; filters external refs
- `CycleDetector.java` — DFS cycle detection with WHITE/GRAY/BLACK node coloring
- `TopologicalSorter.java` — Kahn's algorithm; appends circular nodes at end; findLeafNodes()
- `DependencyAnalyzerService.java` — Orchestrates graph build + cycle detection + topological sort

### Spring Auto-Configuration
- `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

---

## Test Files Created

- `OracleObjectTest.java` — getFullSource, lineCount
- `DependencyGraphTest.java` — hasCircularDependencies
- `DdlFileParserTest.java` — All 4 object types, path traversal, tokenizer, unsupported type
- `OracleDiscoveryServiceFileTest.java` — Package normalization, multi-file, empty schema, progress events
- `CycleDetectorTest.java` — No cycles, simple cycle, self-reference
- `TopologicalSorterTest.java` — Linear chain, circular nodes appended, leaf nodes
- `DependencyAnalyzerServiceTest.java` — Empty discovery, single object, package calling procedure
- `ResultPersistenceServiceTest.java` — Round-trip save/load, missing file error, auto-create output dir

### Test Resources
- `samples/sample-package.sql` — EMP_PKG package spec + body
- `samples/sample-procedure.sql` — UPDATE_SALARY procedure
- `samples/sample-trigger.sql` — EMP_AUDIT_TRG trigger
- `samples/sample-view.sql` — EMP_SUMMARY_V view

---

## Stories Implemented

| Story | Status |
|---|---|
| 1.1 JDBC Live Database Connection | ✅ OracleDiscoveryService.discoverFromJdbc() |
| 1.2 DDL File Import | ✅ OracleDiscoveryService.discoverFromFiles() |
| 1.3 Object Type Discovery | ✅ DdlFileParser + OracleDataDictionaryReader + OracleObjectNormalizer |
| 1.4 Discovery Progress Reporting | ✅ ProgressEvent emitted per object/file |
| 2.1 Dependency Graph Generation | ✅ DependencyAnalyzerService.analyze() |
| 2.2 Migration Order Recommendation | ✅ TopologicalSorter.sort() |

## Security Compliance

| Rule | Status | Notes |
|---|---|---|
| SECURITY-01 | Compliant | TLS enforced via JDBC connection properties |
| SECURITY-03 | Compliant | SLF4J + MDC structured logging; no password in logs |
| SECURITY-05 | Compliant | Path traversal validation in DdlFileParser.validatePath() |
| SECURITY-09 | Compliant | DDL files parsed only, never executed; DiscoveryException sanitizes messages |
| SECURITY-12 | N/A | No user authentication in this unit |
| SECURITY-15 | Compliant | try-with-resources for all JDBC; fail-partial error accumulation |
| SECURITY-02,04,06,07,08,10,11,13,14 | N/A | Not applicable to a library module with no web endpoints or IAM |
