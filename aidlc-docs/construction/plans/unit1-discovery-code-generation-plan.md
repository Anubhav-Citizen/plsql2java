# Unit 1 — Code Generation Plan
## Discovery & Dependency Analysis
## Maven Module: plsql2java-discovery

**Workspace Root**: `c:\project\repo\plsql2java`
**Module Root**: `c:\project\repo\plsql2java\plsql2java-discovery`
**Stories Covered**: 1.1, 1.2, 1.3, 1.4, 2.1, 2.2

---

## Step 1: Root Parent POM
- [x] Create `c:\project\repo\plsql2java\pom.xml` — parent POM with Spring Boot BOM, dependency management, and all 6 modules declared

## Step 2: Module Structure Setup
- [x] Create `c:\project\repo\plsql2java\plsql2java-discovery\pom.xml` — module POM with dependencies (ojdbc11, jackson, slf4j, spring-context, junit5, mockito, assertj)
- [x] Create directory structure: `src/main/java/com/plsql2java/`, `src/main/resources/`, `src/test/java/com/plsql2java/`, `src/test/resources/`

## Step 3: Common Domain Models
- [x] Create `com/plsql2java/common/ProgressEvent.java`
- [x] Create `com/plsql2java/common/ProgressStage.java` (enum)
- [x] Create `com/plsql2java/common/ProgressListener.java` (interface)
- [x] Create `com/plsql2java/common/MigrationConfig.java`
- [x] Create `com/plsql2java/common/JdbcConfig.java`

## Step 4: Discovery Domain Models
- [x] Create `com/plsql2java/model/OracleObjectType.java` (enum)
- [x] Create `com/plsql2java/model/OracleObject.java`
- [x] Create `com/plsql2java/model/DiscoveryMode.java` (enum)
- [x] Create `com/plsql2java/model/DiscoveryResult.java`
- [x] Create `com/plsql2java/model/DiscoveryError.java`
- [x] Create `com/plsql2java/model/DiscoveryErrorType.java` (enum)

## Step 5: Dependency Domain Models
- [x] Create `com/plsql2java/model/ReferenceType.java` (enum)
- [x] Create `com/plsql2java/model/DependencyEdge.java`
- [x] Create `com/plsql2java/model/CircularDependency.java`
- [x] Create `com/plsql2java/model/DependencyGraph.java`

## Step 6: Exceptions
- [x] Create `com/plsql2java/discovery/DiscoveryException.java`

## Step 7: JDBC Discovery Infrastructure
- [x] Create `com/plsql2java/discovery/jdbc/OracleDataDictionaryQueries.java` (SQL constants)
- [x] Create `com/plsql2java/discovery/jdbc/JdbcConnectionManager.java`
- [x] Create `com/plsql2java/discovery/jdbc/OracleDataDictionaryReader.java`

## Step 8: DDL File Parsing
- [x] Create `com/plsql2java/discovery/file/DdlPatterns.java` (regex constants)
- [x] Create `com/plsql2java/discovery/file/DdlFileParser.java`

## Step 9: Object Normalization
- [x] Create `com/plsql2java/discovery/OracleObjectNormalizer.java`

## Step 10: Main Discovery Service
- [x] Create `com/plsql2java/discovery/OracleDiscoveryService.java` (Stories 1.1, 1.2, 1.3, 1.4)

## Step 11: Dependency Analysis
- [x] Create `com/plsql2java/dependency/DependencyPatterns.java` (regex constants for cross-reference detection)
- [x] Create `com/plsql2java/dependency/DependencyGraphBuilder.java`
- [x] Create `com/plsql2java/dependency/CycleDetector.java`
- [x] Create `com/plsql2java/dependency/TopologicalSorter.java`
- [x] Create `com/plsql2java/dependency/DependencyAnalyzerService.java` (Stories 2.1, 2.2)

## Step 12: Result Persistence
- [x] Create `com/plsql2java/discovery/ResultPersistenceService.java`

## Step 13: Spring Configuration
- [x] Create `com/plsql2java/discovery/DiscoveryAutoConfiguration.java` (@Configuration, @ComponentScan)
- [x] Create `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

## Step 14: Unit Tests — Domain Models
- [x] Create `com/plsql2java/model/OracleObjectTest.java`
- [x] Create `com/plsql2java/model/DependencyGraphTest.java`

## Step 15: Unit Tests — JDBC Discovery
- [x] Create `com/plsql2java/discovery/jdbc/OracleDataDictionaryReaderTest.java` (mocked JDBC)
- [x] Create `com/plsql2java/discovery/OracleDiscoveryServiceJdbcTest.java` (mocked reader)

## Step 16: Unit Tests — DDL File Parsing
- [x] Create `src/test/resources/samples/sample-package.sql`
- [x] Create `src/test/resources/samples/sample-procedure.sql`
- [x] Create `src/test/resources/samples/sample-trigger.sql`
- [x] Create `src/test/resources/samples/sample-view.sql`
- [x] Create `com/plsql2java/discovery/file/DdlFileParserTest.java`
- [x] Create `com/plsql2java/discovery/OracleDiscoveryServiceFileTest.java`

## Step 17: Unit Tests — Dependency Analysis
- [x] Create `com/plsql2java/dependency/CycleDetectorTest.java`
- [x] Create `com/plsql2java/dependency/TopologicalSorterTest.java`
- [x] Create `com/plsql2java/dependency/DependencyAnalyzerServiceTest.java`

## Step 18: Unit Tests — Result Persistence
- [x] Create `com/plsql2java/discovery/ResultPersistenceServiceTest.java`

## Step 19: Code Documentation Summary
- [x] Create `aidlc-docs/construction/unit1-discovery/code/code-summary.md`

---

## Story Traceability

| Story | Implemented In |
|---|---|
| 1.1 JDBC Connection | OracleDiscoveryService.discoverFromJdbc(), JdbcConnectionManager, OracleDataDictionaryReader |
| 1.2 DDL File Import | OracleDiscoveryService.discoverFromFiles(), DdlFileParser |
| 1.3 Object Type Discovery | OracleObjectNormalizer, OracleDataDictionaryReader, DdlFileParser |
| 1.4 Discovery Progress | ProgressEvent, ProgressListener in OracleDiscoveryService |
| 2.1 Dependency Graph | DependencyGraphBuilder, CycleDetector, DependencyAnalyzerService |
| 2.2 Migration Order | TopologicalSorter, DependencyAnalyzerService.analyze() |
