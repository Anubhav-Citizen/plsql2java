# Business Rules — Unit 3: Code Generator + Confidence Scorer + Report Generator

---

## Code Generator Rules

## BR-CG01: No Credentials in Generated Code
- Generated `application.yml` MUST use environment variable placeholders for all sensitive config
- `${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}` — never hardcoded values
- Dockerfile and docker-compose.yml MUST NOT contain hardcoded credentials

## BR-CG02: Pinned Dependency Versions
- Generated `pom.xml` MUST use pinned versions for all dependencies (no `LATEST` or ranges)
- Generated Dockerfile MUST use pinned image tags (no `latest`)
- Generated docker-compose.yml MUST use pinned image tags

## BR-CG03: Non-Root Docker User
- Generated Dockerfile MUST create and use a non-root user for the application process

## BR-CG04: Constructor Injection Only
- Generated `@Service` classes MUST use constructor injection — never `@Autowired` field injection

## BR-CG05: Transactional Boundaries
- Service methods containing DML operations MUST be annotated `@Transactional`
- Read-only service methods MUST be annotated `@Transactional(readOnly = true)`

## BR-CG06: Traceability Javadoc
- Every generated service method MUST include a Javadoc comment:
  `/** Translated from PL/SQL: [SCHEMA].[OBJECT].[METHOD] */`

## BR-CG07: Confidence Score Embedding
- Methods with confidence score below threshold MUST have a comment embedded:
  `// @ConfidenceScore({score}%) — manual review recommended`

## BR-CG08: Failed Translation Skipped
- TranslationResults with null javaIR MUST be skipped during code generation
- Skipped objects MUST be logged at WARN level and recorded in the traceability matrix as SKIPPED

## BR-CG09: Generated Code Must Compile
- Generated Java source MUST be syntactically valid for all TRANSLATED constructs (NFR-03.3)
- PARTIAL constructs produce compilable stubs with TODO comments

## BR-CG10: OpenAPI Annotations
- All `@RestController` methods MUST have `@Operation(summary = "...")` annotation
- All DTOs MUST have `@Schema` annotations on fields

## BR-CG11: Input Validation on DTOs
- All request DTO fields MUST have appropriate validation annotations (`@NotNull`, `@Size`, `@NotBlank`)
- Controllers MUST use `@Valid` on request body parameters

## BR-CG12: REST Conventions
- Resource URLs MUST be lowercase, hyphen-separated (e.g., `/order-processor`)
- POST for operations that create/modify, GET for read-only operations
- Return `ResponseEntity<T>` with appropriate HTTP status codes

---

## Confidence Scorer Rules

## BR-CS01: Score Range
- All confidence scores MUST be in range 0–100 (inclusive)
- Scores are clamped: `max(0, min(100, rawScore))`

## BR-CS02: Compilation Error Cap
- Objects with `hasCompilationErrors = true` MUST have their final score capped at 50

## BR-CS03: Zero Constructs = 100%
- Objects/methods with zero constructs (empty body) score 100%

## BR-CS04: Threshold Default
- Default confidence threshold is 70 if not configured
- Threshold is configurable via MigrationConfig

## BR-CS05: Overall Score Weighting
- Overall migration score is weighted by object line count (larger objects have more weight)

---

## Report Generator Rules

## BR-RG01: Traceability Coverage Target
- Report MUST achieve ≥95% traceability coverage (FR-08.3)
- Coverage = (MIGRATED + PARTIAL entries) / total discovered objects * 100

## BR-RG02: Self-Contained HTML
- HTML report MUST be self-contained — no external CSS, JS, or font dependencies
- All styling MUST be inline or in a `<style>` block within the HTML

## BR-RG03: Identical Content
- Markdown and HTML reports MUST contain identical content (same sections, same data)

## BR-RG04: Executive Summary First
- Executive summary MUST be the first section in both report formats

## BR-RG05: Flagged Constructs Grouped
- Flagged constructs section MUST group entries by ConstructType for easy scanning
- If no flagged constructs: section states "No unsupported constructs found"

## BR-RG06: Schema Name and Date in Report
- Report header MUST include: schema name, migration date (ISO 8601), tool version

## BR-RG07: No Credentials in Reports
- Reports MUST NOT contain JDBC URLs, passwords, or any credential information
- Schema name is included; connection details are not
