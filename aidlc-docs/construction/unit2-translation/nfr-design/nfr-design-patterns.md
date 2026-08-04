# NFR Design Patterns — Unit 2: PL/SQL Translation Engine

---

## Pattern 1: Strategy Pattern — TranslationRule

**Addresses**: NFR-T-M1 (extensibility — new rules without engine changes)

Each translation rule is an independent Strategy implementing the `TranslationRule` interface. The engine delegates to the registry without knowing concrete rule types. Adding a new construct requires only a new `@Component` class — zero changes to `PlSqlTranslationEngine`.

```
TranslationRule (interface)
  ├── IfElseRule
  ├── CaseStatementRule
  ├── LoopRule
  ├── ExceptionHandlerRule
  ├── ExplicitCursorRule
  ├── BulkCollectRule
  ├── ForallRule
  ├── BuiltinFunctionRule
  ├── DbmsOutputRule
  ├── GotoRule
  └── RefCursorRule
```

---

## Pattern 2: Registry Pattern — TranslationRuleRegistry

**Addresses**: NFR-T-M1, NFR-T-M2

Rules self-register via Spring bean scanning. `TranslationRuleRegistryInitializer` collects all `TranslationRule` beans and calls `registry.registerRule()` for each. No manual wiring required.

---

## Pattern 3: Fail-Partial Error Accumulation

**Addresses**: NFR-T-R1, NFR-T-R2, SECURITY-15

Mirrors Unit 1's fail-partial pattern:
- ANTLR4 parse errors → caught by custom `ErrorListener`, accumulated as FLAGGED constructs
- Rule application exceptions → caught in `PlSqlTranslationEngine.translate()`, produce FLAGGED result
- Object-level failures → caught in `translateAll()`, logged, continue with next object

No exception propagates past `translateAll()` for individual object failures.

---

## Pattern 4: Externalized Configuration — Mapping Tables

**Addresses**: NFR-T-M2 (no hardcoded mappings)

`BuiltinFunctionMapping` and `OracleExceptionMapping` tables are loaded from classpath JSON files at startup:
- `src/main/resources/rules/builtin-function-mappings.json`
- `src/main/resources/rules/oracle-exception-mappings.json`

Loaded once via `@PostConstruct` in `TranslationMappingLoader`. Rules reference the loaded maps — no hardcoded strings in rule logic.

---

## Pattern 5: MDC Structured Logging

**Addresses**: SECURITY-03, NFR-T-S2

Consistent with Unit 1:
- `MDC.put("objectName", object.name)` before translating each object
- `MDC.clear()` in finally block after each object
- Log messages include object context without embedding PL/SQL source content
- No credentials or PL/SQL source text in log output

---

## Pattern 6: ANTLR4 Error Listener (Fail-Safe Parsing)

**Addresses**: NFR-T-R2, NFR-T-R3, SECURITY-15

Custom `PlSqlErrorListener extends BaseErrorListener`:
- Overrides `syntaxError()` — accumulates errors into a list instead of throwing
- Registered on both Lexer and Parser: `lexer.removeErrorListeners(); lexer.addErrorListener(errorListener)`
- After parsing: if errors present → create FLAGGED AstNodes for each error location
- ANTLR4 default error listener (which prints to stderr) is always removed

---

## Pattern 7: Try-With-Resources for Resource Safety

**Addresses**: SECURITY-15, NFR-T-S4

All classpath resource loading (grammar files, mapping JSON) uses try-with-resources:
```java
try (InputStream is = getClass().getResourceAsStream("/rules/builtin-function-mappings.json")) {
    // load mappings
}
```
