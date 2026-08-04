# Domain Entities — Unit 4: Migration Orchestrator

---

## Entity: MigrationJobStatus (Enum)

Values: `PENDING`, `RUNNING`, `COMPLETED`, `PARTIAL`, `FAILED`

---

## Entity: OperationMode (Enum)

Values: `ANALYZE`, `GENERATE`, `REPORT`

---

## Entity: MigrationJob

Represents a single migration execution lifecycle.

| Field | Type | Constraints |
|---|---|---|
| jobId | String | UUID, immutable |
| migrationId | String | UUID, links to MigrationConfig |
| mode | OperationMode | ANALYZE / GENERATE / REPORT |
| status | MigrationJobStatus | Current lifecycle state |
| startedAt | Instant | Set on job start |
| completedAt | Instant | Nullable — set on completion |
| errorMessage | String | Nullable — set on failure |

---

## Entity: AnalysisResult

Output of the ANALYZE operation mode.

| Field | Type | Constraints |
|---|---|---|
| migrationId | String | UUID |
| discoveryResult | DiscoveryResult | From Unit 1 |
| dependencyGraph | DependencyGraph | From Unit 1 |
| analyzedAt | Instant | Timestamp |

---

## Entity: MigrationResult

Output of the GENERATE operation mode (full pipeline).

| Field | Type | Constraints |
|---|---|---|
| migrationId | String | UUID |
| analysisResult | AnalysisResult | Discovery + dependency |
| translationResults | List\<TranslationResult\> | From Unit 2 |
| generatedProject | GeneratedProject | From Unit 3 |
| confidenceReport | ConfidenceReport | From Unit 3 |
| migrationReport | MigrationReport | From Unit 3 |
| completedAt | Instant | Timestamp |
| skippedObjects | List\<String\> | Objects that failed and were skipped |

**Derived**: `isPartial()` — true if skippedObjects is non-empty

---

## Entity: MigrationProgress

A single progress event emitted during pipeline execution.

| Field | Type | Constraints |
|---|---|---|
| migrationId | String | UUID |
| stage | PipelineStage | Current pipeline stage |
| objectName | String | Nullable — current object being processed |
| processed | int | Objects processed so far |
| total | int | Total objects to process |
| pct | int | 0–100 percentage |
| message | String | Human-readable status message |
| timestamp | Instant | Event timestamp |

---

## Entity: PipelineStage (Enum)

Values: `DISCOVERY`, `DEPENDENCY_ANALYSIS`, `TRANSLATION`, `CODE_GENERATION`, `CONFIDENCE_SCORING`, `REPORT_GENERATION`, `COMPLETE`
