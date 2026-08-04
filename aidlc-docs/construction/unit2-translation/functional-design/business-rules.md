# Business Rules — Unit 2: PL/SQL Translation Engine

---

## BR-T01: Rule-Based Only — No LLM
- Translation MUST be deterministic and rule-based only
- No calls to any LLM, AI service, or external API are permitted
- Given the same PL/SQL input, the engine MUST always produce the same Java output

## BR-T02: Single-Pass Translation
- Each OracleObject is translated in a single pass through its AST
- Rules are applied node-by-node in tree traversal order
- No multi-pass or iterative refinement is performed

## BR-T03: First-Match Rule Application
- For a given ConstructType, rules are evaluated in registration order
- The first rule whose `apply()` returns a non-null outcome is used
- Remaining rules for that construct are not evaluated

## BR-T04: Unknown Construct Handling
- If no rule is registered for a ConstructType, the construct is treated as UNKNOWN
- UNKNOWN constructs produce a FLAGGED outcome with confidencePenalty=30
- A TODO comment is inserted at the corresponding location in the Java output

## BR-T05: Partial Failure Isolation
- A translation exception for one OracleObject MUST NOT stop translation of remaining objects
- Failed objects produce a TranslationResult with overallStatus=FLAGGED, empty JavaIR, and the exception message as flagReason
- The exception is logged at ERROR level (no stack trace exposed to user output)

## BR-T06: Objects with Compilation Errors
- OracleObjects with `hasCompilationErrors=true` are translated with best-effort
- Their overall confidence score is capped at 50% (enforced by Confidence Scorer in Unit 3)
- A warning comment is prepended to the generated Java class

## BR-T07: GOTO is Always Flagged
- GOTO statements have no Java equivalent and MUST always be FLAGGED
- confidencePenalty = 30 per GOTO occurrence
- Recommendation: "Refactor control flow to eliminate GOTO before migration"

## BR-T08: REF CURSOR is Always Flagged
- REF CURSOR parameters have no direct Java equivalent and MUST always be FLAGGED
- confidencePenalty = 50 per REF CURSOR occurrence
- Recommendation: "Replace REF CURSOR with Spring Data projection or DTO-based query"

## BR-T09: FORALL SAVE EXCEPTIONS is Always Flagged
- FORALL with SAVE EXCEPTIONS has no direct Java equivalent and MUST always be FLAGGED
- confidencePenalty = 40
- Recommendation: "Use Spring Batch ItemWriter with skip policy for equivalent error handling"

## BR-T10: DBMS_OUTPUT Translation
- DBMS_OUTPUT.PUT_LINE(msg) MUST be translated to `log.debug("{}", msg)`
- A private static final SLF4J Logger field MUST be added to the class if not already present
- No other DBMS_* packages are translated (they are FLAGGED)

## BR-T11: Unsupported DBMS_* Packages
- All DBMS_* package calls other than DBMS_OUTPUT are FLAGGED
- confidencePenalty = 40 per occurrence
- Recommendation includes the Spring/Java equivalent where known (e.g., DBMS_SCHEDULER → @Scheduled)

## BR-T12: Variable Type Mapping
- %TYPE and %ROWTYPE references are translated to String and Map<String,Object> respectively
- A comment is added: `// TODO: Replace with actual type — original was <OBJECT>%TYPE`
- confidencePenalty = 10 per %TYPE or %ROWTYPE occurrence

## BR-T13: Null Safety in Translations
- NVL(a, b) MUST be translated to `Optional.ofNullable(a).orElse(b)` — never to a raw null check
- NVL2(a, b, c) MUST be translated to `a != null ? b : c`

## BR-T14: No Credentials in Translation Output
- Translation rules MUST NOT emit any credential, password, or connection string into generated Java code
- JDBC URLs or schema names appearing in PL/SQL comments are stripped from generated output

## BR-T15: Javadoc Traceability
- Every generated Java method MUST include a Javadoc comment in the format:
  `/** Translated from PL/SQL: [SCHEMA].[OBJECT_NAME].[PROCEDURE_NAME] (line [N]) */`
- This ensures full traceability from generated Java back to source PL/SQL

## BR-T16: Transactional Annotation
- If a translated method contains any DML operation (INSERT, UPDATE, DELETE via cursor or FORALL), it MUST be annotated with `@Transactional`
- Read-only methods (SELECT only) are annotated with `@Transactional(readOnly = true)`

## BR-T17: Import Deduplication
- The JavaIRAssembler MUST deduplicate import statements
- Imports are sorted alphabetically in the assembled Java source

## BR-T18: Class Naming Convention
- Generated class names MUST follow PascalCase
- Package names MUST follow lowercase dot-notation
- Method names MUST follow camelCase
- Oracle object names (uppercase) are converted: `MY_PACKAGE` → `MyPackageService`

## BR-T19: Translation Coverage Target
- The engine MUST achieve ≥90% automated translation coverage for the constructs listed in FR-04.2
- Coverage is measured as: (TRANSLATED constructs / total constructs) * 100 across a representative test corpus

## BR-T20: Grammar Source
- The ANTLR4 Oracle PL/SQL grammar is sourced from the open-source `antlr/grammars-v4` repository (plsql grammar)
- The grammar files are bundled in `src/main/resources/grammar/` and are not fetched at runtime
- Grammar version is pinned and documented in pom.xml
