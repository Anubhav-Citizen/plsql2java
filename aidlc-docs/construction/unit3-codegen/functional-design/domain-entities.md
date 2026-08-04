# Domain Entities — Unit 3: Code Generator + Confidence Scorer + Report Generator

---

## Code Generator Domain

### Entity: GenerationContext

Carries shared configuration and state for a full project generation pass.

| Field | Type | Constraints |
|---|---|---|
| migrationId | String | UUID, links to DiscoveryResult |
| targetPackage | String | Base Java package (e.g., `com.example`) |
| targetSpringBootVersion | String | e.g., `3.2.5` |
| outputDir | Path | Root output directory for generated project |
| dbDriver | String | JDBC driver class (e.g., `org.postgresql.Driver`) |
| confidenceThreshold | int | 0–100, default 70 |
| schemaName | String | Source Oracle schema name |

---

### Entity: JavaSourceFile

A single generated Java source file.

| Field | Type | Constraints |
|---|---|---|
| relativePath | String | Path relative to project root (e.g., `src/main/java/com/example/entity/OrderEntity.java`) |
| content | String | Full Java source content |
| sourceObjectName | String | Originating Oracle object name (for traceability) |
| artifactType | ArtifactType | ENTITY, REPOSITORY, SERVICE, CONTROLLER, DTO, TEST |

---

### Entity: ArtifactType (Enum)

Values: `ENTITY`, `REPOSITORY`, `SERVICE`, `CONTROLLER`, `DTO`, `TEST`, `POM_XML`, `APP_YML`, `DOCKERFILE`, `DOCKER_COMPOSE`, `OPENAPI_YAML`

---

### Entity: GeneratedProject

The complete in-memory representation of the generated Maven project.

| Field | Type | Constraints |
|---|---|---|
| migrationId | String | UUID |
| projectName | String | Maven artifactId (derived from schemaName) |
| files | List\<JavaSourceFile\> | All generated files |
| generatedAt | Instant | Generation timestamp |

**Derived**: `getFilesByType(ArtifactType)` — filters files by artifact type

---

## Confidence Scorer Domain

### Entity: MethodConfidenceScore

Confidence score for a single translated method.

| Field | Type | Constraints |
|---|---|---|
| objectName | String | Source Oracle object name |
| methodName | String | Java method name |
| score | int | 0–100 |
| belowThreshold | boolean | true if score < configured threshold |
| penaltyReasons | List\<String\> | Human-readable reasons for score reduction |

---

### Entity: ObjectConfidenceScore

Aggregate confidence score for a single Oracle object.

| Field | Type | Constraints |
|---|---|---|
| objectName | String | Source Oracle object name |
| objectType | OracleObjectType | Type of the Oracle object |
| score | int | 0–100 |
| belowThreshold | boolean | true if score < configured threshold |
| methodScores | List\<MethodConfidenceScore\> | Per-method scores |
| hasCompilationErrors | boolean | Caps score at 50 if true |

---

### Entity: ConfidenceReport

The complete confidence scoring output for all translated objects.

| Field | Type | Constraints |
|---|---|---|
| migrationId | String | UUID |
| threshold | int | Configured threshold (default 70) |
| objectScores | List\<ObjectConfidenceScore\> | All object scores |
| overallScore | int | Weighted average across all objects |
| flaggedObjectCount | int | Objects below threshold |
| flaggedMethodCount | int | Methods below threshold |
| scoredAt | Instant | Scoring timestamp |

---

## Report Generator Domain

### Entity: ReportInput

Aggregated input to the report generator.

| Field | Type | Constraints |
|---|---|---|
| discoveryResult | DiscoveryResult | From Unit 1 |
| dependencyGraph | DependencyGraph | From Unit 1 |
| translationResults | List\<TranslationResult\> | From Unit 2 |
| generatedProject | GeneratedProject | From Unit 3 codegen |
| confidenceReport | ConfidenceReport | From Unit 3 scorer |
| migrationConfig | MigrationConfig | Tool configuration |

---

### Entity: TraceabilityEntry

One row in the traceability matrix.

| Field | Type | Constraints |
|---|---|---|
| plsqlObjectName | String | Source Oracle object name |
| plsqlObjectType | OracleObjectType | Source type |
| javaClassName | String | Nullable — generated Java class name |
| javaMethodNames | List\<String\> | Generated method names |
| status | TraceabilityStatus | MIGRATED, PARTIAL, FLAGGED, SKIPPED |
| confidenceScore | int | Object-level confidence score |

---

### Entity: TraceabilityStatus (Enum)

Values: `MIGRATED`, `PARTIAL`, `FLAGGED`, `SKIPPED`

---

### Entity: TraceabilityMatrix

| Field | Type | Constraints |
|---|---|---|
| entries | List\<TraceabilityEntry\> | All traceability rows |
| coveragePct | double | (MIGRATED + PARTIAL) / total * 100 |

---

### Entity: FlaggedConstructsSummary

| Field | Type | Constraints |
|---|---|---|
| byConstructType | Map\<ConstructType, List\<FlaggedConstruct\>\> | Grouped by construct type |
| totalCount | int | Total flagged constructs |

---

### Entity: MigrationReport

The complete rendered migration report.

| Field | Type | Constraints |
|---|---|---|
| migrationId | String | UUID |
| markdownContent | String | Full Markdown report |
| htmlContent | String | Full self-contained HTML report |
| generatedAt | Instant | Report generation timestamp |
| schemaName | String | Source schema name |
