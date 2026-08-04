# Domain Entities — Unit 1: Discovery & Dependency Analysis

---

## Entity: JdbcConfig

Holds the configuration required to connect to a live Oracle database.

| Field | Type | Constraints |
|---|---|---|
| url | String | Required, format: `jdbc:oracle:thin:@host:port:sid` |
| username | String | Required, non-blank |
| password | String | Required, sourced from env var — never stored in plain text |
| connectionTimeoutSeconds | int | Default: 30, min: 5, max: 300 |
| schemaName | String | Optional — if null, uses the authenticated user's schema |

---

## Entity: OracleObjectType (Enum)

Represents the supported Oracle database object types.

Values: `PACKAGE`, `PACKAGE_BODY`, `PROCEDURE`, `FUNCTION`, `TRIGGER`, `VIEW`, `SEQUENCE`, `TYPE`, `TYPE_BODY`

---

## Entity: OracleObject

Represents a single discovered Oracle database object with its full source code.

| Field | Type | Constraints |
|---|---|---|
| name | String | Required, non-blank, uppercase (Oracle convention) |
| type | OracleObjectType | Required |
| schema | String | Required, non-blank |
| sourceSpec | String | Required for PACKAGE, PROCEDURE, FUNCTION, TRIGGER, VIEW, TYPE; the spec/declaration source |
| sourceBody | String | Nullable — present for PACKAGE_BODY, TYPE_BODY |
| hasCompilationErrors | boolean | True if the object had compilation errors in the source schema |
| lineCount | int | Total lines of source code |

**Business Rules**:
- PACKAGE and PACKAGE_BODY are linked by name — they form a single logical unit
- Objects with `hasCompilationErrors = true` are flagged in discovery results but still included
- `sourceSpec` is never null for supported types; `sourceBody` is only populated for PACKAGE_BODY and TYPE_BODY

---

## Entity: DiscoveryResult

The complete output of a schema discovery operation.

| Field | Type | Constraints |
|---|---|---|
| migrationId | String | UUID, required |
| schemaName | String | Required |
| discoveryMode | DiscoveryMode | JDBC or FILE |
| objects | List\<OracleObject\> | Required, may be empty |
| discoveredAt | Instant | Required |
| totalObjectCount | int | Derived: objects.size() |
| errorCount | int | Count of objects with hasCompilationErrors = true |
| sourceFiles | List\<String\> | File names used (FILE mode only) |

---

## Entity: DiscoveryMode (Enum)

Values: `JDBC`, `FILE`

---

## Entity: DependencyEdge

Represents a directed dependency between two Oracle objects.

| Field | Type | Constraints |
|---|---|---|
| fromObject | String | Object name that has the dependency |
| toObject | String | Object name being depended upon |
| referenceType | ReferenceType | CALL, REFERENCE, TRIGGER_ON |

---

## Entity: ReferenceType (Enum)

Values: `CALL` (procedure/function call), `REFERENCE` (view/type reference), `TRIGGER_ON` (trigger references a table/view)

---

## Entity: CircularDependency

Represents a detected cycle in the dependency graph.

| Field | Type | Constraints |
|---|---|---|
| cycle | List\<String\> | Ordered list of object names forming the cycle |
| description | String | Human-readable description of the cycle |

---

## Entity: DependencyGraph

The complete dependency graph for a discovered schema.

| Field | Type | Constraints |
|---|---|---|
| migrationId | String | UUID, links to DiscoveryResult |
| edges | List\<DependencyEdge\> | All dependency relationships |
| circularDependencies | List\<CircularDependency\> | Detected cycles |
| migrationOrder | List\<String\> | Topologically sorted object names (leaf-first) |
| leafObjects | List\<String\> | Objects with no dependencies — migration-ready |
| analyzedAt | Instant | Required |

---

## Entity: ProgressEvent

Emitted during long-running operations to report progress to the delivery layer.

| Field | Type | Constraints |
|---|---|---|
| migrationId | String | UUID |
| stage | ProgressStage | DISCOVERY, DEPENDENCY_ANALYSIS, TRANSLATION, CODE_GENERATION, SCORING, REPORTING |
| objectName | String | Nullable — current object being processed |
| processedCount | int | Objects processed so far |
| totalCount | int | Total objects to process |
| percentComplete | double | 0.0–100.0 |
| message | String | Human-readable status message |
| timestamp | Instant | Event emission time |

---

## Entity: DiscoveryError

Records an error encountered during discovery for a specific object or file.

| Field | Type | Constraints |
|---|---|---|
| source | String | Object name or file name where error occurred |
| errorType | DiscoveryErrorType | PARSE_ERROR, CONNECTION_ERROR, PERMISSION_ERROR |
| message | String | Human-readable error description |
| lineNumber | Integer | Nullable — line number for parse errors |
