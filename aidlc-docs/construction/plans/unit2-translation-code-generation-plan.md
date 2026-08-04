# Unit 2 — Code Generation Plan
## plsql2java-translation: PL/SQL Translation Engine

**Workspace Root**: `c:\project\repo\plsql2java`
**Maven Module**: `plsql2java-translation/`
**Package Root**: `com.plsql2java.translation`
**Stories Implemented**: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6
**Dependencies**: `plsql2java-discovery` (OracleObject, OracleObjectType, ProgressEvent, ProgressListener, ProgressStage, MigrationConfig)

---

## Unit Context

| Item | Value |
|---|---|
| Unit | Unit 2: PL/SQL Translation Engine |
| Module | plsql2java-translation |
| Depends on | plsql2java-discovery (Unit 1) |
| Consumed by | plsql2java-orchestrator (Unit 4) |
| Key deliverables | PlSqlTranslationEngine, TranslationRuleRegistry, 11 rule classes, JavaIRAssembler, AstBuilder, mapping tables, tests |

---

## Step 1: Maven Module Setup
- [ ] Create `plsql2java-translation/pom.xml` with ANTLR4 runtime, ANTLR4 maven plugin, spring-context, jackson-databind, slf4j, spring-boot-starter-test, dependency on plsql2java-discovery
- [ ] Create Maven directory structure: `src/main/java/...`, `src/main/resources/grammar/`, `src/main/resources/rules/`, `src/test/java/...`, `src/test/resources/plsql/`
- [ ] Update root `pom.xml` to add ANTLR4 version to `<dependencyManagement>` and `antlr4-maven-plugin` to plugin management
- **Stories**: infrastructure for all Unit 2 stories

## Step 2: Domain Model Classes
- [ ] Create `com.plsql2java.translation.model.ConstructType` (enum, 18 values)
- [ ] Create `com.plsql2java.translation.model.TranslationStatus` (enum: TRANSLATED, FLAGGED, PARTIAL)
- [ ] Create `com.plsql2java.translation.model.AstNode` (constructType, text, lineNumber, children, attributes)
- [ ] Create `com.plsql2java.translation.model.TranslationContext` (objectName, objectType, schemaName, variableRegistry, cursorRegistry, constructResults)
- [ ] Create `com.plsql2java.translation.model.TranslationOutcome` (status, javaSnippet, flagReason, recommendation, confidencePenalty)
- [ ] Create `com.plsql2java.translation.model.ConstructTranslationResult` (constructType, lineNumber, status, javaSnippet, flagReason, recommendation, confidencePenalty)
- [ ] Create `com.plsql2java.translation.model.FlaggedConstruct` (objectName, constructType, lineNumber, reason, recommendation)
- [ ] Create `com.plsql2java.translation.model.JavaMethodIR` (methodName, returnType, parameters, body, annotations, javadoc, constructResults)
- [ ] Create `com.plsql2java.translation.model.JavaIR` (objectName, packageName, className, imports, fields, methods, rawSource)
- [ ] Create `com.plsql2java.translation.model.TranslationResult` (sourceObject, javaIR, constructResults, flaggedConstructs, translatedCount, flaggedCount, partialCount, overallStatus)
- [ ] Create `com.plsql2java.translation.model.BuiltinFunctionMapping` (oracleFunction, javaEquivalent, requiresImport, notes)
- [ ] Create `com.plsql2java.translation.model.OracleExceptionMapping` (oracleException, javaException, requiresImport)
- **Stories**: foundation for 3.1–3.6

## Step 3: TranslationRule Interface and Registry
- [ ] Create `com.plsql2java.translation.engine.TranslationRule` (interface: getConstructType(), apply())
- [ ] Create `com.plsql2java.translation.engine.TranslationRuleRegistry` (@Component, registerRule, getRulesForConstruct)
- [ ] Create `com.plsql2java.translation.engine.TranslationRuleRegistryInitializer` (@Component, @PostConstruct, auto-registers all TranslationRule beans)
- **Stories**: 3.1–3.6 (extensibility foundation)

## Step 4: Externalized Mapping Tables (JSON resources + loader)
- [ ] Create `src/main/resources/rules/builtin-function-mappings.json` (all 23 Oracle→Java mappings from business-logic-model.md)
- [ ] Create `src/main/resources/rules/oracle-exception-mappings.json` (all 6 Oracle→Java exception mappings)
- [ ] Create `com.plsql2java.translation.engine.TranslationMappingLoader` (@Component, @PostConstruct, loads both JSON files, exposes as immutable maps)
- **Stories**: 3.5 (built-in function translation), 3.2 (exception mapping)

## Step 5: ANTLR4 Grammar and AST Infrastructure
- [ ] Copy Oracle PL/SQL ANTLR4 grammar files (`PlSqlLexer.g4`, `PlSqlParser.g4`) to `src/main/resources/grammar/` (sourced from antlr/grammars-v4)
- [ ] Create `com.plsql2java.translation.ast.PlSqlErrorListener` (extends BaseErrorListener, accumulates errors, never throws)
- [ ] Create `com.plsql2java.translation.ast.AstBuilder` (extends PlSqlParserBaseListener, produces List\<AstNode\>)
- **Stories**: foundation for all translation stories

## Step 6: JavaIRAssembler
- [ ] Create `com.plsql2java.translation.engine.JavaIRAssembler` (assembles JavaIR from TranslationContext: class name, package, imports dedup+sort, fields, methods, rawSource)
- **Stories**: 3.1–3.6 (output assembly)

## Step 7: Control Flow Translation Rules (Story 3.1)
- [ ] Create `com.plsql2java.translation.rules.VariableDeclarationRule` (VARIABLE_DECLARATION → Java field/local var, type mapping table)
- [ ] Create `com.plsql2java.translation.rules.IfElseRule` (IF_ELSIF_ELSE → if/else if/else)
- [ ] Create `com.plsql2java.translation.rules.CaseStatementRule` (CASE_STATEMENT → switch or if/else chain)
- [ ] Create `com.plsql2java.translation.rules.LoopRule` (LOOP/WHILE_LOOP/FOR_LOOP → Java equivalents, EXIT WHEN → break)
- [ ] Create `com.plsql2java.translation.rules.GotoRule` (GOTO → always FLAGGED, confidencePenalty=30)
- **Stories**: 3.1

## Step 8: Exception Handling Translation Rules (Story 3.2)
- [ ] Create `com.plsql2java.translation.rules.ExceptionHandlerRule` (EXCEPTION_HANDLER → try/catch, uses OracleExceptionMapping, WHEN OTHERS → catch(Exception e))
- [ ] Create `com.plsql2java.translation.rules.RaiseRule` (RAISE/RAISE_APPLICATION_ERROR → throw with comment)
- **Stories**: 3.2

## Step 9: Cursor and Query Translation Rules (Story 3.3)
- [ ] Create `com.plsql2java.translation.rules.ExplicitCursorRule` (EXPLICIT_CURSOR → registers in cursorRegistry, FETCH → repository call, FOR loop → enhanced for)
- [ ] Create `com.plsql2java.translation.rules.ImplicitCursorForRule` (IMPLICIT_CURSOR_FOR → enhanced for with @Query native query)
- [ ] Create `com.plsql2java.translation.rules.RefCursorRule` (REF_CURSOR → always FLAGGED, confidencePenalty=50)
- **Stories**: 3.3

## Step 10: Bulk Operation Translation Rules (Story 3.4)
- [ ] Create `com.plsql2java.translation.rules.BulkCollectRule` (BULK_COLLECT → List fetch, LIMIT → paginated)
- [ ] Create `com.plsql2java.translation.rules.ForallRule` (FORALL → saveAll/deleteAll; FORALL_SAVE_EXCEPTIONS → FLAGGED, confidencePenalty=40)
- **Stories**: 3.4

## Step 11: Built-in Function and DBMS_OUTPUT Rules (Story 3.5)
- [ ] Create `com.plsql2java.translation.rules.BuiltinFunctionRule` (BUILTIN_FUNCTION → looks up TranslationMappingLoader, PARTIAL+TODO if not found, confidencePenalty=20)
- [ ] Create `com.plsql2java.translation.rules.DbmsOutputRule` (DBMS_OUTPUT → log.debug(), adds Logger field)
- **Stories**: 3.5

## Step 12: PlSqlTranslationEngine (Story 3.6 + all)
- [ ] Create `com.plsql2java.translation.engine.PlSqlTranslationEngine` (@Service: translate(), translateAll() with ProgressListener, fail-partial, MDC logging, ANTLR4 parse + AstBuilder + registry + assembler pipeline)
- [ ] Create `com.plsql2java.translation.TranslationAutoConfiguration` (@Configuration, @ComponentScan for translation package)
- [ ] Create `src/main/resources/META-INF/spring/com.plsql2java.translation.TranslationAutoConfiguration.imports`
- **Stories**: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6

## Step 13: Unit Tests — Model Classes
- [ ] Create `TranslationResultTest` (overallStatus derivation, counts)
- [ ] Create `JavaIRTest` (rawSource assembly, import deduplication)
- **Stories**: foundation

## Step 14: Unit Tests — Registry and Initializer
- [ ] Create `TranslationRuleRegistryTest` (register, lookup, empty list for unknown type)
- **Stories**: 3.1–3.6

## Step 15: Unit Tests — Control Flow Rules (Story 3.1)
- [ ] Create `IfElseRuleTest` (simple if, if/else, nested)
- [ ] Create `CaseStatementRuleTest` (simple CASE, searched CASE)
- [ ] Create `LoopRuleTest` (LOOP, WHILE, FOR numeric, EXIT WHEN)
- [ ] Create `GotoRuleTest` (always FLAGGED)
- **Stories**: 3.1

## Step 16: Unit Tests — Exception, Cursor, Bulk Rules (Stories 3.2–3.4)
- [ ] Create `ExceptionHandlerRuleTest` (NO_DATA_FOUND, TOO_MANY_ROWS, OTHERS, RAISE)
- [ ] Create `ExplicitCursorRuleTest` (OPEN/FETCH/CLOSE, FOR loop)
- [ ] Create `RefCursorRuleTest` (always FLAGGED)
- [ ] Create `BulkCollectRuleTest` (basic, LIMIT)
- [ ] Create `ForallRuleTest` (saveAll, SAVE EXCEPTIONS flagged)
- **Stories**: 3.2, 3.3, 3.4

## Step 17: Unit Tests — Built-in Function and DBMS_OUTPUT Rules (Story 3.5)
- [ ] Create `BuiltinFunctionRuleTest` (known functions translated, unknown → PARTIAL+TODO)
- [ ] Create `DbmsOutputRuleTest` (PUT_LINE → log.debug, Logger field added)
- **Stories**: 3.5

## Step 18: Integration Tests — PlSqlTranslationEngine (Story 3.6)
- [ ] Create `PlSqlTranslationEngineTest` (translate full sample package, translate full sample procedure, translateAll with progress events, parse error → FLAGGED result, object with compilation errors → capped confidence)
- [ ] Create sample PL/SQL test resources: `sample-package-translation.sql`, `sample-procedure-translation.sql`, `sample-unsupported-constructs.sql`
- **Stories**: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6

## Step 19: Code Summary Documentation
- [x] Create `aidlc-docs/construction/unit2-translation/code/code-summary.md`
- [x] Update `aidlc-docs/aidlc-state.md` — Unit 2 Code Generation in progress
- [x] Append to `aidlc-docs/audit.md`

---

## Story Traceability

| Story | Steps |
|---|---|
| 3.1 Control Flow Translation | 1, 2, 3, 5, 6, 7, 12, 13, 14, 15, 18 |
| 3.2 Exception Handling Translation | 1, 2, 3, 4, 5, 6, 8, 12, 13, 14, 16, 18 |
| 3.3 Cursor and Query Translation | 1, 2, 3, 5, 6, 9, 12, 13, 14, 16, 18 |
| 3.4 Bulk Operation Translation | 1, 2, 3, 5, 6, 10, 12, 13, 14, 16, 18 |
| 3.5 Built-in Function Translation | 1, 2, 3, 4, 5, 6, 11, 12, 13, 14, 17, 18 |
| 3.6 Unsupported Construct Flagging | 1, 2, 3, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 18 |
