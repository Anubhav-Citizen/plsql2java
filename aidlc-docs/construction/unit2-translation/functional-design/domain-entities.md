# Domain Entities — Unit 2: PL/SQL Translation Engine

---

## Entity: ConstructType (Enum)

Identifies the category of a PL/SQL construct being translated.

Values:
- `VARIABLE_DECLARATION` — local variable or package-level variable
- `IF_ELSIF_ELSE` — conditional branching
- `CASE_STATEMENT` — CASE/WHEN construct
- `LOOP` — basic LOOP..END LOOP
- `WHILE_LOOP` — WHILE..LOOP
- `FOR_LOOP` — numeric or cursor FOR loop
- `EXCEPTION_HANDLER` — EXCEPTION WHEN block
- `EXPLICIT_CURSOR` — CURSOR declaration and OPEN/FETCH/CLOSE
- `IMPLICIT_CURSOR_FOR` — cursor FOR loop (implicit open/fetch/close)
- `BULK_COLLECT` — BULK COLLECT INTO
- `FORALL` — FORALL..INSERT/UPDATE/DELETE
- `BUILTIN_FUNCTION` — Oracle built-in string/date/numeric function call
- `DBMS_OUTPUT` — DBMS_OUTPUT.PUT_LINE call
- `RAISE` — RAISE or RAISE_APPLICATION_ERROR
- `GOTO` — GOTO statement (unsupported)
- `REF_CURSOR` — REF CURSOR parameter (unsupported)
- `FORALL_SAVE_EXCEPTIONS` — FORALL with SAVE EXCEPTIONS (unsupported)
- `UNKNOWN` — unrecognized construct

---

## Entity: TranslationStatus (Enum)

The outcome of applying a translation rule to a single construct.

Values:
- `TRANSLATED` — construct was fully translated to Java IR
- `FLAGGED` — construct has no direct Java equivalent; flagged for manual review
- `PARTIAL` — best-effort translation applied; confidence score reduced

---

## Entity: AstNode

Represents a single node in the PL/SQL Abstract Syntax Tree produced by ANTLR4.

| Field | Type | Constraints |
|---|---|---|
| constructType | ConstructType | Required |
| text | String | Raw PL/SQL text of this node |
| lineNumber | int | Source line number (1-based) |
| children | List\<AstNode\> | Child nodes (may be empty) |
| attributes | Map\<String, String\> | Construct-specific metadata (e.g., cursorName, exceptionName) |

---

## Entity: TranslationContext

Carries shared state across rule applications for a single OracleObject translation pass.

| Field | Type | Constraints |
|---|---|---|
| objectName | String | Name of the OracleObject being translated |
| objectType | OracleObjectType | Type of the OracleObject |
| schemaName | String | Schema owning the object |
| variableRegistry | Map\<String, String\> | PL/SQL variable name → Java type mapping |
| cursorRegistry | Map\<String, String\> | Cursor name → associated query text |
| constructResults | List\<ConstructTranslationResult\> | Accumulated results for all constructs processed so far |

---

## Entity: TranslationOutcome

The result of applying a single TranslationRule to one AstNode.

| Field | Type | Constraints |
|---|---|---|
| status | TranslationStatus | Required |
| javaSnippet | String | Nullable — generated Java code fragment (null if FLAGGED) |
| flagReason | String | Nullable — human-readable reason for flagging (null if TRANSLATED) |
| recommendation | String | Nullable — migration recommendation for flagged constructs |
| confidencePenalty | int | 0–100; subtracted from base confidence (0 = no penalty) |

---

## Entity: ConstructTranslationResult

Records the translation outcome for one construct within an OracleObject.

| Field | Type | Constraints |
|---|---|---|
| constructType | ConstructType | Required |
| lineNumber | int | Source line in original PL/SQL |
| status | TranslationStatus | Required |
| javaSnippet | String | Nullable |
| flagReason | String | Nullable |
| recommendation | String | Nullable |
| confidencePenalty | int | 0–100 |

---

## Entity: FlaggedConstruct

A construct that could not be fully translated; surfaced in reports.

| Field | Type | Constraints |
|---|---|---|
| objectName | String | Required |
| constructType | ConstructType | Required |
| lineNumber | int | Source line in original PL/SQL |
| reason | String | Human-readable explanation |
| recommendation | String | Actionable migration guidance |

---

## Entity: JavaIR (Java Intermediate Representation)

The translated Java representation of a single OracleObject.

| Field | Type | Constraints |
|---|---|---|
| objectName | String | Source OracleObject name |
| packageName | String | Target Java package (e.g., `com.example.service`) |
| className | String | Target Java class name (PascalCase) |
| imports | List\<String\> | Required Java import statements |
| fields | List\<String\> | Java field declarations |
| methods | List\<JavaMethodIR\> | Translated method representations |
| rawSource | String | Full assembled Java class source (assembled from fields + methods) |

---

## Entity: JavaMethodIR

The translated Java representation of a single PL/SQL procedure or function.

| Field | Type | Constraints |
|---|---|---|
| methodName | String | Java method name (camelCase) |
| returnType | String | Java return type (e.g., `void`, `List<Entity>`) |
| parameters | List\<String\> | Java parameter declarations |
| body | String | Java method body (translated from PL/SQL) |
| annotations | List\<String\> | Java annotations (e.g., `@Transactional`) |
| javadoc | String | Javadoc comment referencing original PL/SQL object/procedure |
| constructResults | List\<ConstructTranslationResult\> | Per-construct results within this method |

---

## Entity: TranslationResult

The complete output of translating one OracleObject.

| Field | Type | Constraints |
|---|---|---|
| sourceObject | OracleObject | The original OracleObject (from Unit 1) |
| javaIR | JavaIR | Nullable — null if translation failed entirely |
| constructResults | List\<ConstructTranslationResult\> | All per-construct outcomes |
| flaggedConstructs | List\<FlaggedConstruct\> | Constructs requiring manual review |
| translatedCount | int | Count of TRANSLATED constructs |
| flaggedCount | int | Count of FLAGGED constructs |
| partialCount | int | Count of PARTIAL constructs |
| overallStatus | TranslationStatus | TRANSLATED / PARTIAL / FLAGGED (worst-case of all constructs) |

---

## Entity: BuiltinFunctionMapping

Maps an Oracle built-in function name to its Java/Spring equivalent.

| Field | Type | Constraints |
|---|---|---|
| oracleFunction | String | Oracle function name (uppercase, e.g., `SUBSTR`) |
| javaEquivalent | String | Java expression template (e.g., `{0}.substring({1}-1, {1}-1+{2})`) |
| requiresImport | String | Nullable — Java import needed (e.g., `java.time.LocalDate`) |
| notes | String | Nullable — translation notes or caveats |

---

## Entity: OracleExceptionMapping

Maps a named Oracle exception to its Java equivalent.

| Field | Type | Constraints |
|---|---|---|
| oracleException | String | Oracle exception name (e.g., `NO_DATA_FOUND`) |
| javaException | String | Java exception class (e.g., `EmptyResultDataAccessException`) |
| requiresImport | String | Java import needed |
