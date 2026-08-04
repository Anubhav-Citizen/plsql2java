# Business Logic Model — Unit 3: Code Generator + Confidence Scorer + Report Generator

---

## Component A: JavaCodeGeneratorService

**Responsibility**: Generates all Java Spring Boot artifacts from TranslationResults.

### generateProject(List\<TranslationResult\> results, GenerationContext ctx) → GeneratedProject

1. Initialize GeneratedProject with migrationId and projectName
2. For each TranslationResult (in migration order from DependencyGraph):
   a. If javaIR is null (failed translation) → skip, log warning
   b. Generate @Service class from javaIR → add to project
   c. Generate @RestController from service → add to project
   d. Generate request/response DTOs for controller → add to project
   e. Generate JUnit 5 test class for service → add to project
3. Generate pom.xml → add to project
4. Generate application.yml → add to project
5. Generate Dockerfile → add to project
6. Generate docker-compose.yml → add to project
7. Generate openapi.yaml → add to project
8. Return GeneratedProject

### generateService(TranslationResult result, GenerationContext ctx) → JavaSourceFile

- Class name: `{PascalCase(objectName)}Service`
- Package: `{ctx.targetPackage}.service`
- Annotations: `@Service`
- For each JavaMethodIR in result.javaIR.methods:
  - Emit method with Javadoc, annotations, parameters, body
  - Embed confidence score comment if score < threshold: `// @ConfidenceScore({score}%) — review recommended`
- Constructor injection for repository dependencies
- SLF4J Logger field

### generateController(JavaSourceFile service, GenerationContext ctx) → JavaSourceFile

- Class name: `{ServiceName}Controller` (replace `Service` suffix with `Controller`)
- Package: `{ctx.targetPackage}.controller`
- Annotations: `@RestController`, `@RequestMapping("/{resource}")`
- For each service method: generate `@GetMapping` / `@PostMapping` endpoint
- Request/response wrapped in DTOs
- Returns `ResponseEntity<T>`
- Springdoc `@Operation` and `@Tag` annotations

### generateDto(String baseName, GenerationContext ctx) → List\<JavaSourceFile\>

- `{BaseName}Request` — request DTO with `@NotNull`, `@Size` validation annotations
- `{BaseName}Response` — response DTO
- Package: `{ctx.targetPackage}.dto`

### generateTest(JavaSourceFile service, GenerationContext ctx) → JavaSourceFile

- Class name: `{ServiceName}Test`
- Package: `{ctx.targetPackage}.service` (test source tree)
- Annotations: `@ExtendWith(MockitoExtension.class)`
- For each service method: one `@Test` happy-path method
- Uses `@Mock` for repository dependencies, `@InjectMocks` for service
- AssertJ assertions

### generatePomXml(GenerationContext ctx) → JavaSourceFile

- Spring Boot 3.x parent BOM
- Dependencies: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-test, springdoc-openapi-starter-webmvc-ui, spring-boot-starter-validation, configurable DB driver
- Pinned versions, JaCoCo plugin ≥80% coverage

### generateApplicationYml(GenerationContext ctx) → JavaSourceFile

- `spring.datasource.url: ${DB_URL}` — env var placeholder (never hardcoded)
- `spring.datasource.username: ${DB_USERNAME}`
- `spring.datasource.password: ${DB_PASSWORD}`
- `server.port: 8080`
- `springdoc.api-docs.path: /api-docs`

### generateDockerfile(GenerationContext ctx) → JavaSourceFile

- Multi-stage build: `eclipse-temurin:17-jdk-alpine` builder + `eclipse-temurin:17-jre-alpine` runtime
- Pinned image tags (no `latest`)
- Non-root user
- `EXPOSE 8080`

### generateDockerCompose(GenerationContext ctx) → JavaSourceFile

- `app` service + `db` service (PostgreSQL 15-alpine — pinned)
- Environment variables for DB credentials (references `.env` file)
- Health check on app service

### writeProject(GeneratedProject project, Path outputDir)

- For each JavaSourceFile: create parent directories, write content as UTF-8
- Verify each file written (size > 0)
- Log count of files written

---

## Component B: ConfidenceScorerService

**Responsibility**: Calculates confidence scores from TranslationResults.

### scoreAll(List\<TranslationResult\> results, int threshold) → ConfidenceReport

1. For each TranslationResult: call scoreObject()
2. Compute overallScore = weighted average (weighted by object line count)
3. Count flaggedObjectCount and flaggedMethodCount
4. Return ConfidenceReport

### scoreObject(TranslationResult result) → ObjectConfidenceScore

1. If result.javaIR is null → score = 0
2. If result.sourceObject.hasCompilationErrors → cap final score at 50
3. Score each method via scoreMethod()
4. objectScore = average of method scores
5. belowThreshold = objectScore < threshold

### scoreMethod(List\<ConstructTranslationResult\> constructResults, String methodName) → MethodConfidenceScore

```
totalConstructs = constructResults.size()
if totalConstructs == 0: score = 100

totalPenalty = sum(result.confidencePenalty for result in constructResults)
rawScore = 100 - (totalPenalty / max(totalConstructs, 1))
score = clamp(rawScore, 0, 100)

penaltyReasons = [result.flagReason for result where confidencePenalty > 0]
```

---

## Component C: MigrationReportGeneratorService

**Responsibility**: Assembles and renders the migration report.

### generateReport(ReportInput input) → MigrationReport

1. Build executive summary section
2. Build traceability matrix via buildTraceabilityMatrix()
3. Build flagged constructs summary via buildFlaggedConstructsSummary()
4. Build dependency graph summary (circular deps, leaf objects, migration order excerpt)
5. Build confidence score tables (per-object, per-method for flagged items)
6. Assemble ReportData from all sections
7. Render Markdown via renderMarkdown()
8. Render HTML via renderHtml()
9. Return MigrationReport

### buildTraceabilityMatrix(DiscoveryResult, GeneratedProject) → TraceabilityMatrix

- For each OracleObject in discovery: find matching JavaSourceFile(s) by sourceObjectName
- If found → MIGRATED or PARTIAL (based on TranslationResult.overallStatus)
- If not found → SKIPPED
- Compute coveragePct

### renderMarkdown(ReportData) → String

- Uses StringBuilder with Markdown headings, tables, code blocks
- No external template engine — pure string assembly
- Sections: Executive Summary, Traceability Matrix, Flagged Constructs, Dependency Graph Summary, Confidence Scores

### renderHtml(ReportData) → String

- Wraps Markdown-equivalent content in self-contained HTML
- Inline CSS (no external stylesheets)
- No JavaScript (static HTML only)
- Sections match Markdown report exactly

### writeReport(MigrationReport report, Path outputDir)

- Write `{outputDir}/report.md`
- Write `{outputDir}/report.html`
- Verify both files written (size > 0)
