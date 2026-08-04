# NFR Requirements — Unit 2: PL/SQL Translation Engine

---

## Performance

- **NFR-T-P1**: Translation of a single OracleObject MUST complete within 2 seconds (p99) for objects up to 5,000 lines
- **NFR-T-P2**: Full translation of a 500-object schema MUST complete within the overall 15-minute generation budget (NFR-01.3)
- **NFR-T-P3**: ANTLR4 parsing MUST NOT load grammar files from disk at runtime — grammar is compiled into the JAR at build time via ANTLR4 Maven plugin

## Accuracy

- **NFR-T-A1**: ≥90% automated translation coverage for all constructs listed in FR-04.2 (NFR-02.2)
- **NFR-T-A2**: Generated Java code MUST be syntactically valid for all TRANSLATED constructs (NFR-03.3)
- **NFR-T-A3**: <5% manual rework for supported PL/SQL constructs (NFR-02.6)

## Reliability

- **NFR-T-R1**: A parse failure for one OracleObject MUST NOT propagate to other objects (BR-T05)
- **NFR-T-R2**: Malformed PL/SQL that causes ANTLR4 parse errors MUST be caught and result in a FLAGGED TranslationResult — never an unhandled exception
- **NFR-T-R3**: ANTLR4 error listeners MUST be registered to capture parse errors without throwing exceptions

## Maintainability

- **NFR-T-M1**: Adding a new translation rule MUST require only: implementing TranslationRule, declaring as a Spring bean — no changes to PlSqlTranslationEngine (NFR-04.2)
- **NFR-T-M2**: Built-in function mappings and Oracle exception mappings MUST be externalized to configuration (not hardcoded in rule classes)
- **NFR-T-M3**: Unit test coverage ≥80% (NFR-04.3)
- **NFR-T-M4**: Each TranslationRule implementation MUST have its own dedicated test class

## Security

- **NFR-T-S1**: PL/SQL source code is treated as data only — ANTLR4 parsing MUST NOT execute any PL/SQL statements (SECURITY-09)
- **NFR-T-S2**: No credentials, passwords, or connection strings from PL/SQL source MUST appear in generated Java output (BR-T14, SECURITY-03)
- **NFR-T-S3**: Exception messages surfaced to users MUST be generic — no internal ANTLR4 stack traces (SECURITY-09, SECURITY-15)
- **NFR-T-S4**: All external calls (file I/O for grammar resources) MUST have explicit error handling (SECURITY-15)

## Portability

- **NFR-T-PO1**: Translation engine runs on Java 17, Linux/macOS/Windows (NFR-07.1)
- **NFR-T-PO2**: No OS-specific file path handling — use `java.nio.file.Path` throughout
