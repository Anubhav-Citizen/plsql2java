# Code Summary — Unit 2: PL/SQL Translation Engine

## Module
`plsql2java-translation`

## Stories Implemented
| Story | Title | Status |
|---|---|---|
| 3.1 | Control Flow Translation | ✅ Implemented |
| 3.2 | Exception Handling Translation | ✅ Implemented |
| 3.3 | Cursor and Query Translation | ✅ Implemented |
| 3.4 | Bulk Operation Translation | ✅ Implemented |
| 3.5 | Built-in Function Translation | ✅ Implemented |
| 3.6 | Unsupported Construct Flagging | ✅ Implemented |

## Generated Files

### Build Configuration
- `plsql2java-translation/pom.xml` — module POM with ANTLR4 runtime, ANTLR4 maven plugin, spring-context, jackson-databind, plsql2java-discovery dependency

### ANTLR4 Grammar
- `src/main/antlr4/com/plsql2java/translation/PlSqlLexer.g4` — Oracle PL/SQL lexer grammar (subset covering all supported constructs)
- `src/main/antlr4/com/plsql2java/translation/PlSqlParser.g4` — Oracle PL/SQL parser grammar (subset)

### Domain Model (`com.plsql2java.translation.model`)
- `ConstructType.java` — enum (18 values)
- `TranslationStatus.java` — enum (TRANSLATED, PARTIAL, FLAGGED)
- `AstNode.java` — parse tree node with attributes map
- `TranslationContext.java` — per-object translation state (variable/cursor registries)
- `TranslationOutcome.java` — rule application result (builder pattern, factory methods)
- `ConstructTranslationResult.java` — per-construct outcome record
- `FlaggedConstruct.java` — flagged construct for reporting
- `JavaMethodIR.java` — translated Java method representation
- `JavaIR.java` — translated Java class representation
- `TranslationResult.java` — complete translation output (auto-derives overallStatus)
- `BuiltinFunctionMapping.java` — Jackson-deserializable mapping model
- `OracleExceptionMapping.java` — Jackson-deserializable mapping model

### Engine (`com.plsql2java.translation.engine`)
- `TranslationRule.java` — interface (getConstructType, apply)
- `TranslationRuleRegistry.java` — EnumMap-backed rule registry
- `TranslationRuleRegistryInitializer.java` — @PostConstruct auto-registers all TranslationRule beans
- `TranslationMappingLoader.java` — loads builtin-function-mappings.json and oracle-exception-mappings.json at startup
- `JavaIRAssembler.java` — assembles JavaIR from TranslationContext (import dedup, rawSource generation)
- `PlSqlTranslationEngine.java` — main orchestrator (translate, translateAll with ProgressListener, fail-partial, MDC logging)

### AST Infrastructure (`com.plsql2java.translation.ast`)
- `PlSqlErrorListener.java` — captures ANTLR4 parse errors without throwing
- `AstBuilder.java` — ANTLR4 listener that produces List\<AstNode\>

### Translation Rules (`com.plsql2java.translation.rules`)
- `VariableDeclarationRule.java` — PL/SQL types → Java types (Story 3.1)
- `IfElseRule.java` — IF/ELSIF/ELSE → if/else if/else (Story 3.1)
- `CaseStatementRule.java` — CASE/WHEN → switch/if-else (Story 3.1)
- `LoopRule.java` — LOOP/WHILE/FOR → Java equivalents (Story 3.1)
- `GotoRule.java` — GOTO → always FLAGGED, penalty=30 (Story 3.6)
- `ExceptionHandlerRule.java` — EXCEPTION WHEN → try/catch using OracleExceptionMapping (Story 3.2)
- `RaiseRule.java` — RAISE/RAISE_APPLICATION_ERROR → throw (Story 3.2)
- `ExplicitCursorRule.java` — CURSOR → repository query, registers in cursorRegistry (Story 3.3)
- `ImplicitCursorForRule.java` — FOR rec IN (...) → enhanced for (Story 3.3)
- `RefCursorRule.java` — REF CURSOR → always FLAGGED, penalty=50 (Story 3.6)
- `BulkCollectRule.java` — BULK COLLECT → List fetch / paginated (Story 3.4)
- `ForallRule.java` — FORALL → saveAll/deleteAll; SAVE EXCEPTIONS → FLAGGED, penalty=40 (Story 3.4, 3.6)
- `BuiltinFunctionRule.java` — Oracle built-ins → Java via TranslationMappingLoader (Story 3.5)
- `DbmsOutputRule.java` — DBMS_OUTPUT.PUT_LINE → log.debug() (Story 3.5)

### Externalized Configuration
- `src/main/resources/rules/builtin-function-mappings.json` — 23 Oracle→Java function mappings
- `src/main/resources/rules/oracle-exception-mappings.json` — 6 Oracle→Java exception mappings

### Spring Configuration
- `TranslationAutoConfiguration.java` — @Configuration @ComponentScan
- `META-INF/spring/com.plsql2java.translation.TranslationAutoConfiguration.imports`

### Tests
- `TranslationResultTest.java` — overallStatus derivation, counts
- `JavaIRTest.java` — toPascalCase utility
- `TranslationRuleRegistryTest.java` — register, lookup, empty list
- `ControlFlowRulesTest.java` — IfElseRule, CaseStatementRule, LoopRule, GotoRule
- `ExceptionCursorBulkRulesTest.java` — ExceptionHandlerRule, RaiseRule, cursor rules, bulk rules
- `BuiltinFunctionAndDbmsOutputRulesTest.java` — BuiltinFunctionRule, DbmsOutputRule
- `PlSqlTranslationEngineTest.java` — integration: translate, translateAll, parse errors, progress events

### Test Resources
- `src/test/resources/plsql/sample-package-translation.sql`
- `src/test/resources/plsql/sample-procedure-translation.sql`
- `src/test/resources/plsql/sample-unsupported-constructs.sql`

## Security Compliance

| Rule | Status | Notes |
|---|---|---|
| SECURITY-01 | N/A | No data stores in this module |
| SECURITY-02 | N/A | No network intermediaries |
| SECURITY-03 | ✅ Compliant | SLF4J + MDC; no credentials or PL/SQL source in logs |
| SECURITY-04 | N/A | No HTTP endpoints |
| SECURITY-05 | N/A | No API endpoints |
| SECURITY-06 | N/A | No IAM policies |
| SECURITY-07 | N/A | No network configuration |
| SECURITY-08 | N/A | No application endpoints |
| SECURITY-09 | ✅ Compliant | PL/SQL treated as data only; ANTLR4 parses, never executes |
| SECURITY-10 | ✅ Compliant | All dependency versions pinned in parent pom.xml |
| SECURITY-11 | N/A | No public-facing endpoints |
| SECURITY-12 | N/A | No authentication in this module |
| SECURITY-13 | ✅ Compliant | No deserialization of untrusted data; Jackson reads classpath resources only |
| SECURITY-14 | N/A | No alerting infrastructure in this module |
| SECURITY-15 | ✅ Compliant | Try-with-resources for classpath I/O; fail-partial isolation; generic error messages to callers |
