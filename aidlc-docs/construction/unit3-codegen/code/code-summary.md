# Code Summary — Unit 3: plsql2java-codegen

## Module
`plsql2java-codegen/` — Code Generator + Confidence Scorer + Report Generator

---

## Created Files

### Maven Module
- `plsql2java-codegen/pom.xml` — freemarker, jackson, spring-context, plsql2java-discovery, plsql2java-translation deps

### Code Generator Domain (`com.plsql2java.codegen.model`)
- `ArtifactType.java` — enum: ENTITY, REPOSITORY, SERVICE, CONTROLLER, DTO, TEST, POM_XML, APP_YML, DOCKERFILE, DOCKER_COMPOSE, OPENAPI_YAML
- `JavaSourceFile.java` — relativePath, content, sourceObjectName, artifactType
- `GenerationContext.java` — migrationId, targetPackage, targetSpringBootVersion, outputDir, dbDriver, confidenceThreshold, schemaName
- `GeneratedProject.java` — files list, skippedObjects, getFilesByType(), generatedAt

### Confidence Scorer Domain (`com.plsql2java.scoring.model`)
- `MethodConfidenceScore.java` — objectName, methodName, score, belowThreshold, penaltyReasons
- `ObjectConfidenceScore.java` — objectName, objectType, score, belowThreshold, methodScores, hasCompilationErrors
- `ConfidenceReport.java` — migrationId, threshold, objectScores, overallScore, flaggedObjectCount (derived), flaggedMethodCount (derived), scoredAt

### Report Generator Domain (`com.plsql2java.reporting.model`)
- `TraceabilityStatus.java` — enum: MIGRATED, PARTIAL, FLAGGED, SKIPPED
- `TraceabilityEntry.java` — plsqlObjectName, plsqlObjectType, javaClassName, javaMethodNames, status, confidenceScore
- `TraceabilityMatrix.java` — entries, coveragePct (derived)
- `FlaggedConstructsSummary.java` — byConstructType (EnumMap), totalCount (derived)
- `ReportInput.java` — aggregates all inputs from Units 1–3
- `MigrationReport.java` — migrationId, markdownContent, htmlContent, generatedAt, schemaName

### FreeMarker Templates (`src/main/resources/templates/`)
- `java/service.ftl` — @Service class with constructor injection, Javadoc, confidence score comments
- `java/controller.ftl` — @RestController with @Operation, ResponseEntity
- `java/dto-request.ftl` — request DTO with @NotNull/@Size/@NotBlank
- `java/dto-response.ftl` — response DTO with @Schema
- `java/test.ftl` — JUnit 5 + Mockito + AssertJ test class
- `maven/pom.ftl` — Spring Boot 3.x pom with pinned deps, JaCoCo ≥80%
- `maven/application-yml.ftl` — env var placeholders (${DB_URL}, ${DB_USERNAME}, ${DB_PASSWORD})
- `docker/Dockerfile.ftl` — multi-stage, eclipse-temurin:17 pinned, non-root user
- `docker/docker-compose.ftl` — app + postgres:15-alpine, .env reference, health checks

### Services
- `com.plsql2java.codegen.JavaCodeGeneratorService` — generateProject (fail-partial), generateService/Controller/Dto/Test/PomXml/ApplicationYml/Dockerfile/DockerCompose, writeProject (path traversal validation, try-with-resources)
- `com.plsql2java.scoring.ConfidenceScorerService` — scoreAll, scoreObject, scoreMethod (clamping, compilation error cap at 50, weighted overall)
- `com.plsql2java.reporting.MigrationReportGeneratorService` — generateReport, buildTraceabilityMatrix, buildFlaggedConstructsSummary, renderMarkdown, renderHtml, writeReport (self-contained HTML, no credentials)

### Spring Configuration
- `com.plsql2java.codegen.CodegenAutoConfiguration` — @ComponentScan for codegen + scoring + reporting
- `META-INF/spring/com.plsql2java.codegen.CodegenAutoConfiguration.imports`

### Tests
- `GeneratedProjectTest` — getFilesByType, generatedAt, skippedObjects
- `ConfidenceReportTest` — flaggedObjectCount/flaggedMethodCount derivation
- `TraceabilityMatrixTest` — coveragePct calculation (50%, 100% empty, 100% all migrated)
- `ConfidenceScorerServiceTest` — all translated=100%, flagged reduces score, compilation cap, null IR=0, threshold flagging, score clamping
- `JavaCodeGeneratorServiceTest` — @Service, @RestController, Spring Boot parent, env vars, non-root Docker, pinned tags, null IR skipped, writeProject
- `MigrationReportGeneratorServiceTest` — executive summary, ≥95% coverage, flagged grouping, dual format, no credentials, schema+date present, writeReport
- `CodeGenerationIntegrationTest` — full pipeline: all artifact types, writeProject, scoreAll range, generateReport dual format

---

## Business Rules Implemented
BR-CG01–12, BR-CS01–05, BR-RG01–07 — all enforced
