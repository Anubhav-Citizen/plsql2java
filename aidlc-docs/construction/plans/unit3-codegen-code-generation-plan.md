# Unit 3 — Code Generation Plan
## plsql2java-codegen: Code Generator + Confidence Scorer + Report Generator

**Workspace Root**: `c:\project\repo\plsql2java`
**Maven Module**: `plsql2java-codegen/`
**Package Root**: `com.plsql2java.codegen`, `com.plsql2java.scoring`, `com.plsql2java.reporting`
**Stories Implemented**: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 5.1, 5.2, 5.3, 6.1, 6.2, 6.3, 6.4
**Dependencies**: `plsql2java-discovery` (Unit 1 models), `plsql2java-translation` (Unit 2 models)

---

## Unit Context

| Item | Value |
|---|---|
| Unit | Unit 3: Code Generator + Confidence Scorer + Report Generator |
| Module | plsql2java-codegen |
| Depends on | plsql2java-discovery (Unit 1), plsql2java-translation (Unit 2) |
| Consumed by | plsql2java-orchestrator (Unit 4) |
| Key deliverables | JavaCodeGeneratorService, ConfidenceScorerService, MigrationReportGeneratorService, FreeMarker templates, domain models, tests |

---

## Step 1: Maven Module Setup
- [x] Create `plsql2java-codegen/` directory structure (src/main/java, src/main/resources/templates, src/test/java, src/test/resources)
- [x] Create `plsql2java-codegen/pom.xml` with freemarker, jackson, spring-context, slf4j, spring-boot-starter-test, plsql2java-discovery, plsql2java-translation dependencies
- **Stories**: infrastructure for all Unit 3 stories

## Step 2: Domain Model Classes (Code Generator)
- [x] Create `com.plsql2java.codegen.model.ArtifactType` (enum: ENTITY, REPOSITORY, SERVICE, CONTROLLER, DTO, TEST, POM_XML, APP_YML, DOCKERFILE, DOCKER_COMPOSE, OPENAPI_YAML)
- [x] Create `com.plsql2java.codegen.model.JavaSourceFile` (relativePath, content, sourceObjectName, artifactType)
- [x] Create `com.plsql2java.codegen.model.GenerationContext` (migrationId, targetPackage, targetSpringBootVersion, outputDir, dbDriver, confidenceThreshold, schemaName)
- [x] Create `com.plsql2java.codegen.model.GeneratedProject` (migrationId, projectName, files, generatedAt, getFilesByType())
- **Stories**: 4.1–4.7

## Step 3: Domain Model Classes (Confidence Scorer)
- [x] Create `com.plsql2java.scoring.model.MethodConfidenceScore` (objectName, methodName, score, belowThreshold, penaltyReasons)
- [x] Create `com.plsql2java.scoring.model.ObjectConfidenceScore` (objectName, objectType, score, belowThreshold, methodScores, hasCompilationErrors)
- [x] Create `com.plsql2java.scoring.model.ConfidenceReport` (migrationId, threshold, objectScores, overallScore, flaggedObjectCount, flaggedMethodCount, scoredAt)
- **Stories**: 5.1, 5.2, 5.3

## Step 4: Domain Model Classes (Report Generator)
- [x] Create `com.plsql2java.reporting.model.TraceabilityStatus` (enum: MIGRATED, PARTIAL, FLAGGED, SKIPPED)
- [x] Create `com.plsql2java.reporting.model.TraceabilityEntry` (plsqlObjectName, plsqlObjectType, javaClassName, javaMethodNames, status, confidenceScore)
- [x] Create `com.plsql2java.reporting.model.TraceabilityMatrix` (entries, coveragePct)
- [x] Create `com.plsql2java.reporting.model.FlaggedConstructsSummary` (byConstructType, totalCount)
- [x] Create `com.plsql2java.reporting.model.ReportInput` (discoveryResult, dependencyGraph, translationResults, generatedProject, confidenceReport, migrationConfig)
- [x] Create `com.plsql2java.reporting.model.MigrationReport` (migrationId, markdownContent, htmlContent, generatedAt, schemaName)
- **Stories**: 6.1–6.4

## Step 5: FreeMarker Templates
- [x] Create `templates/java/service.ftl` — @Service class template (package, imports, class declaration, constructor injection, methods with Javadoc + confidence comment)
- [x] Create `templates/java/controller.ftl` — @RestController template (RequestMapping, methods with @Operation, ResponseEntity)
- [x] Create `templates/java/dto-request.ftl` — request DTO with @NotNull/@Size validation
- [x] Create `templates/java/dto-response.ftl` — response DTO with @Schema
- [x] Create `templates/java/test.ftl` — JUnit 5 + Mockito + AssertJ test class template
- [x] Create `templates/maven/pom.ftl` — Spring Boot 3.x pom.xml with pinned deps, JaCoCo
- [x] Create `templates/maven/application-yml.ftl` — application.yml with env var placeholders
- [x] Create `templates/docker/Dockerfile.ftl` — multi-stage, non-root user, pinned tags
- [x] Create `templates/docker/docker-compose.ftl` — app + db services, pinned tags, .env reference
- **Stories**: 4.1–4.7

## Step 6: JavaCodeGeneratorService
- [x] Create `com.plsql2java.codegen.JavaCodeGeneratorService` (@Service: generateProject, generateService, generateController, generateDto, generateTest, generatePomXml, generateApplicationYml, generateDockerfile, generateDockerCompose, writeProject — fail-partial, path traversal validation, try-with-resources, MDC logging)
- **Stories**: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7

## Step 7: ConfidenceScorerService
- [x] Create `com.plsql2java.scoring.ConfidenceScorerService` (@Service: scoreAll, scoreObject, scoreMethod — score clamping, compilation error cap at 50, weighted overall score)
- **Stories**: 5.1, 5.2, 5.3

## Step 8: MigrationReportGeneratorService
- [x] Create `com.plsql2java.reporting.MigrationReportGeneratorService` (@Service: generateReport, buildTraceabilityMatrix, buildFlaggedConstructsSummary, renderMarkdown, renderHtml, writeReport — section-based assembly, self-contained HTML, no credentials in output)
- **Stories**: 6.1, 6.2, 6.3, 6.4

## Step 9: Spring Auto-Configuration
- [x] Create `com.plsql2java.codegen.CodegenAutoConfiguration` (@Configuration, @ComponentScan for codegen + scoring + reporting packages)
- [x] Create `src/main/resources/META-INF/spring/com.plsql2java.codegen.CodegenAutoConfiguration.imports`
- **Stories**: infrastructure

## Step 10: Unit Tests — Domain Models
- [x] Create `GeneratedProjectTest` (getFilesByType filtering, generatedAt set)
- [x] Create `ConfidenceReportTest` (flaggedObjectCount, flaggedMethodCount derivation)
- [x] Create `TraceabilityMatrixTest` (coveragePct calculation)
- **Stories**: foundation

## Step 11: Unit Tests — ConfidenceScorerService (Stories 5.1–5.3)
- [x] Create `ConfidenceScorerServiceTest` (all translated → 100%, flagged constructs reduce score, compilation errors cap at 50, zero constructs → 100%, threshold flagging, weighted overall score)
- **Stories**: 5.1, 5.2, 5.3

## Step 12: Unit Tests — JavaCodeGeneratorService (Stories 4.1–4.7)
- [x] Create `JavaCodeGeneratorServiceTest` (generateService produces @Service class, generateController produces @RestController, generatePomXml contains Spring Boot parent, generateApplicationYml uses env vars, generateDockerfile has non-root user, generateDockerCompose has pinned tags, failed translation skipped)
- **Stories**: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7

## Step 13: Unit Tests — MigrationReportGeneratorService (Stories 6.1–6.4)
- [x] Create `MigrationReportGeneratorServiceTest` (executive summary present, traceability matrix coverage ≥95%, flagged constructs grouped by type, both Markdown and HTML produced, no credentials in output)
- **Stories**: 6.1, 6.2, 6.3, 6.4

## Step 14: Integration Test — Full Generation Pipeline
- [x] Create `CodeGenerationIntegrationTest` (generateProject from sample TranslationResults → verify all artifact types present, writeProject to temp dir → verify files written, scoreAll → verify scores in range, generateReport → verify both formats produced)
- [x] Create sample TranslationResult test fixtures
- **Stories**: 4.1–6.4

## Step 15: Code Summary Documentation
- [x] Create `aidlc-docs/construction/unit3-codegen/code/code-summary.md`
- [x] Update `aidlc-docs/aidlc-state.md`
- [x] Append to `aidlc-docs/audit.md`

---

## Story Traceability

| Story | Steps |
|---|---|
| 4.1 JPA Entity Generation | 1, 2, 5, 6, 10, 12, 14 |
| 4.2 Spring Data JPA Repository Generation | 1, 2, 5, 6, 10, 12, 14 |
| 4.3 Service Class Generation | 1, 2, 5, 6, 10, 12, 14 |
| 4.4 REST Controller Generation | 1, 2, 5, 6, 10, 12, 14 |
| 4.5 OpenAPI Specification Generation | 1, 2, 5, 6, 10, 12, 14 |
| 4.6 Unit Test Generation | 1, 2, 5, 6, 10, 12, 14 |
| 4.7 Complete Maven Project Generation | 1, 2, 5, 6, 9, 10, 12, 14 |
| 5.1 Object-Level Confidence Score | 1, 3, 7, 10, 11, 14 |
| 5.2 Method-Level Confidence Score | 1, 3, 7, 10, 11, 14 |
| 5.3 Confidence Threshold Alerting | 1, 3, 7, 10, 11, 14 |
| 6.1 Executive Summary Report | 1, 4, 8, 10, 13, 14 |
| 6.2 Traceability Matrix | 1, 4, 8, 10, 13, 14 |
| 6.3 Flagged Constructs Report | 1, 4, 8, 10, 13, 14 |
| 6.4 Dual-Format Report Output | 1, 4, 8, 9, 10, 13, 14 |
