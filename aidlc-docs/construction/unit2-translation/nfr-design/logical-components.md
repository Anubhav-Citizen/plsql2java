# Logical Components — Unit 2: PL/SQL Translation Engine

---

## In-Process Components (no external infrastructure)

Unit 2 is a pure in-memory processing module. All components run in-process with no external infrastructure dependencies.

| Component | Type | Purpose |
|---|---|---|
| PlSqlTranslationEngine | Spring Service | Orchestrates full translation pipeline |
| TranslationRuleRegistry | Spring Component | Rule registration and lookup |
| TranslationRuleRegistryInitializer | Spring Component (@PostConstruct) | Auto-registers all TranslationRule beans |
| AstBuilder | ANTLR4 Listener | Walks ParseTree → List\<AstNode\> |
| PlSqlErrorListener | ANTLR4 ErrorListener | Captures parse errors without throwing |
| JavaIRAssembler | Plain Java | Assembles JavaIR from TranslationOutcomes |
| TranslationMappingLoader | Spring Component (@PostConstruct) | Loads mapping tables from classpath JSON |
| [11 Rule classes] | Spring Components | One per ConstructType |

## Classpath Resources (bundled in JAR)

| Resource | Purpose |
|---|---|
| `grammar/PlSqlLexer.g4` (compiled) | ANTLR4 Oracle PL/SQL lexer grammar |
| `grammar/PlSqlParser.g4` (compiled) | ANTLR4 Oracle PL/SQL parser grammar |
| `rules/builtin-function-mappings.json` | Oracle → Java built-in function mapping table |
| `rules/oracle-exception-mappings.json` | Oracle → Java exception mapping table |

## No External Infrastructure

- No database
- No message queue
- No cache
- No external HTTP calls
- All processing is in-memory, stateless per translation call
