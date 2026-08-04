# Business Logic Model — Unit 2: PL/SQL Translation Engine

---

## Overview

The Translation Engine is a rule-based, deterministic pipeline that transforms PL/SQL source code into Java intermediate representations. It operates on one OracleObject at a time, driven by a registry of pluggable TranslationRules.

---

## Core Translation Pipeline

```
OracleObject.sourceSpec / sourceBody
        |
        v
  [ANTLR4 Parser]
  PlSqlLexer + PlSqlParser (Oracle PL/SQL grammar)
        |
        v
  ParseTree (ANTLR4 parse tree)
        |
        v
  [AstBuilder]
  Walks parse tree → produces List<AstNode>
        |
        v
  [TranslationRuleRegistry]
  For each AstNode:
    getRulesForConstruct(node.constructType)
    → apply first matching rule
    → produce TranslationOutcome
        |
        v
  [JavaIRAssembler]
  Collects all TranslationOutcomes
  → assembles JavaIR (class + methods + imports)
        |
        v
  TranslationResult
```

---

## Component: PlSqlTranslationEngine

**Responsibility**: Orchestrates the full translation pipeline for one or many OracleObjects.

### translate(OracleObject object) → TranslationResult

1. Build TranslationContext from object metadata
2. Parse `object.sourceSpec` (and `object.sourceBody` if present) via ANTLR4 → ParseTree
3. Walk ParseTree via AstBuilder → List\<AstNode\>
4. For each AstNode:
   a. Look up rules: `registry.getRulesForConstruct(node.constructType)`
   b. If no rules found → create FLAGGED outcome (UNKNOWN construct)
   c. Apply first applicable rule: `rule.apply(node, context)`
   d. Accumulate TranslationOutcome into context.constructResults
5. Assemble JavaIR via JavaIRAssembler
6. Build and return TranslationResult

### translateAll(List\<OracleObject\> objects, ProgressListener listener) → List\<TranslationResult\>

1. For each object in order:
   a. Call translate(object)
   b. Emit ProgressEvent (stage=TRANSLATION, processedCount++, objectName=object.name)
2. Return all TranslationResults

**Partial failure**: If translate(object) throws an unchecked exception, log the error, create a TranslationResult with overallStatus=FLAGGED and empty JavaIR, continue with next object.

---

## Component: TranslationRuleRegistry

**Responsibility**: Maintains the registry of all TranslationRules, keyed by ConstructType.

### registerRule(TranslationRule rule)
- Adds rule to the list for `rule.getConstructType()`
- Rules for the same ConstructType are applied in registration order (first match wins)

### getRulesForConstruct(ConstructType type) → List\<TranslationRule\>
- Returns all registered rules for the given type
- Returns empty list (never null) if no rules registered

**Initialization**: Rules are registered at startup via Spring `@PostConstruct` in `TranslationRuleRegistryInitializer`, which scans all `TranslationRule` beans.

---

## Component: AstBuilder

**Responsibility**: Walks the ANTLR4 ParseTree and produces a flat list of AstNodes representing the constructs to be translated.

- Implements ANTLR4 `PlSqlParserBaseListener` (listener pattern)
- On each `enter*` callback, creates an AstNode with constructType, text, lineNumber
- Populates `attributes` map with construct-specific data (e.g., cursor name, exception name)
- Skips structural grammar nodes that have no translation significance

---

## Component: JavaIRAssembler

**Responsibility**: Assembles a JavaIR from the accumulated TranslationOutcomes in a TranslationContext.

1. Determine target class name: `toPascalCase(object.name) + "Service"` (for PACKAGE/PROCEDURE/FUNCTION)
2. Determine target package: from MigrationConfig.targetPackage
3. Collect all non-null `javaSnippet` values from constructResults, grouped by method
4. For each method group:
   a. Build JavaMethodIR (name, return type, parameters, body, annotations, javadoc)
   b. Add `@Transactional` if original PL/SQL had DML operations
5. Collect all required imports (from rule outcomes + standard Spring imports)
6. Assemble `rawSource` as a complete Java class string
7. Return JavaIR

---

## Translation Rule Implementations

Each rule implements `TranslationRule` interface:
- `getConstructType()` — declares which ConstructType it handles
- `apply(AstNode node, TranslationContext context)` — returns TranslationOutcome

### Rule: VariableDeclarationRule (VARIABLE_DECLARATION)
- Maps PL/SQL type → Java type using type mapping table
- Produces Java field or local variable declaration
- Handles: VARCHAR2→String, NUMBER→Long/BigDecimal/int, DATE→LocalDate, BOOLEAN→boolean, %TYPE→String (with comment), %ROWTYPE→Map\<String,Object\> (with comment)

### Rule: IfElseRule (IF_ELSIF_ELSE)
- Translates IF/ELSIF/ELSE → Java if/else if/else
- Recursively handles nested conditions
- Preserves condition expressions (translated inline)

### Rule: CaseStatementRule (CASE_STATEMENT)
- Translates CASE/WHEN/ELSE → Java switch expression (Java 14+) or if/else chain
- Simple CASE (equality) → switch; searched CASE (conditions) → if/else chain

### Rule: LoopRule (LOOP / WHILE_LOOP / FOR_LOOP)
- LOOP..END LOOP → `while(true) { ... break; }` (EXIT WHEN → break)
- WHILE condition LOOP → `while (condition) { ... }`
- FOR i IN low..high LOOP → `for (int i = low; i <= high; i++) { ... }`
- FOR rec IN cursor LOOP → translated via ExplicitCursorRule

### Rule: ExceptionHandlerRule (EXCEPTION_HANDLER)
- Wraps translated body in try { } catch blocks
- WHEN NO_DATA_FOUND → catch(EmptyResultDataAccessException e)
- WHEN TOO_MANY_ROWS → catch(IncorrectResultSizeDataAccessException e)
- WHEN OTHERS → catch(Exception e) { log.warn(...) }
- RAISE → `throw new RuntimeException(message)` with comment
- RAISE_APPLICATION_ERROR → `throw new IllegalStateException(message)` with comment

### Rule: ExplicitCursorRule (EXPLICIT_CURSOR)
- CURSOR declaration → registers cursor name + query in context.cursorRegistry
- OPEN cursor → no Java equivalent (query executed on fetch)
- FETCH cursor INTO vars → `List<Entity> results = repository.findBy...()` + variable assignment
- CLOSE cursor → no Java equivalent (comment added)
- FOR rec IN cursor LOOP → `for (Entity rec : repository.findBy...()) { ... }`

### Rule: ImplicitCursorForRule (IMPLICIT_CURSOR_FOR)
- FOR rec IN (SELECT ...) LOOP → `for (Entity rec : repository.findByNativeQuery()) { ... }`
- Extracts SELECT statement → stored as @Query native query in JavaIR

### Rule: BulkCollectRule (BULK_COLLECT)
- BULK COLLECT INTO collection → `List<Entity> collection = repository.findAll()`
- BULK COLLECT INTO collection LIMIT n → paginated: `repository.findAll(PageRequest.of(0, n)).getContent()`

### Rule: ForallRule (FORALL)
- FORALL i IN collection.FIRST..collection.LAST INSERT/UPDATE/DELETE → `repository.saveAll(collection)` / `repository.deleteAll(collection)`
- FORALL with SAVE EXCEPTIONS → FLAGGED (confidencePenalty=40, recommendation: "Use Spring batch with error handling")

### Rule: BuiltinFunctionRule (BUILTIN_FUNCTION)
- Looks up oracleFunction in BuiltinFunctionMapping table
- If found → substitutes Java equivalent expression
- If not found → PARTIAL outcome with TODO comment (confidencePenalty=20)
- Mapping table covers: SUBSTR, INSTR, LENGTH, UPPER, LOWER, TRIM, LTRIM, RTRIM, REPLACE, TO_DATE, TO_CHAR, TO_NUMBER, NVL, NVL2, DECODE, COALESCE, SYSDATE, SYSTIMESTAMP, TRUNC, ROUND, MOD, ABS, SIGN

### Rule: DbmsOutputRule (DBMS_OUTPUT)
- DBMS_OUTPUT.PUT_LINE(msg) → `log.debug("{}", msg)`
- Adds SLF4J Logger field to class if not already present

### Rule: GotoRule (GOTO)
- Always returns FLAGGED (confidencePenalty=30, recommendation: "Refactor control flow to eliminate GOTO")

### Rule: RefCursorRule (REF_CURSOR)
- Always returns FLAGGED (confidencePenalty=50, recommendation: "Replace REF CURSOR with Spring Data projection or DTO query")

---

## Built-in Function Mapping Table

| Oracle Function | Java Equivalent | Import |
|---|---|---|
| SUBSTR(s,p,l) | s.substring(p-1, p-1+l) | — |
| INSTR(s,sub) | s.indexOf(sub) + 1 | — |
| LENGTH(s) | s.length() | — |
| UPPER(s) | s.toUpperCase() | — |
| LOWER(s) | s.toLowerCase() | — |
| TRIM(s) | s.trim() | — |
| LTRIM(s) | s.stripLeading() | — |
| RTRIM(s) | s.stripTrailing() | — |
| REPLACE(s,f,r) | s.replace(f, r) | — |
| TO_DATE(s,fmt) | LocalDate.parse(s, DateTimeFormatter.ofPattern(fmt)) | java.time.LocalDate, java.time.format.DateTimeFormatter |
| TO_CHAR(d,fmt) | d.format(DateTimeFormatter.ofPattern(fmt)) | java.time.format.DateTimeFormatter |
| TO_NUMBER(s) | new BigDecimal(s) | java.math.BigDecimal |
| NVL(a,b) | Optional.ofNullable(a).orElse(b) | java.util.Optional |
| NVL2(a,b,c) | a != null ? b : c | — |
| DECODE(e,s,r,...) | if/else chain | — |
| COALESCE(a,b,...) | Stream.of(a,b,...).filter(Objects::nonNull).findFirst().orElse(null) | java.util.stream.Stream, java.util.Objects |
| SYSDATE | LocalDate.now() | java.time.LocalDate |
| SYSTIMESTAMP | LocalDateTime.now() | java.time.LocalDateTime |
| TRUNC(n) | (long) n | — |
| ROUND(n) | Math.round(n) | — |
| MOD(a,b) | a % b | — |
| ABS(n) | Math.abs(n) | — |
| SIGN(n) | Integer.signum(n) | — |

---

## Oracle Exception Mapping Table

| Oracle Exception | Java Exception | Import |
|---|---|---|
| NO_DATA_FOUND | EmptyResultDataAccessException | org.springframework.dao |
| TOO_MANY_ROWS | IncorrectResultSizeDataAccessException | org.springframework.dao |
| DUP_VAL_ON_INDEX | DataIntegrityViolationException | org.springframework.dao |
| VALUE_ERROR | IllegalArgumentException | — |
| ZERO_DIVIDE | ArithmeticException | — |
| OTHERS | Exception | — |

---

## Confidence Penalty Accumulation

Each ConstructTranslationResult carries a `confidencePenalty` (0–100). The method-level confidence score is computed by the Confidence Scorer (Unit 3) as:

```
methodConfidence = 100 - sum(confidencePenalties) / totalConstructCount
```

Penalty values by status:
- TRANSLATED → 0
- PARTIAL → 10–20 (rule-specific)
- FLAGGED → 20–50 (rule-specific, higher for complex unsupported constructs)
