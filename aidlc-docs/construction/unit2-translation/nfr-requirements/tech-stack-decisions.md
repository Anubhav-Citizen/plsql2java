# Tech Stack Decisions — Unit 2: PL/SQL Translation Engine

---

## ANTLR4 Runtime

- **Decision**: ANTLR4 4.13.x runtime (`org.antlr:antlr4-runtime`)
- **Rationale**: Industry-standard parser generator; open-source Oracle PL/SQL grammar available in `antlr/grammars-v4`
- **Grammar**: `plsql` grammar from `antlr/grammars-v4` — bundled in `src/main/resources/grammar/`, compiled at build time via `org.antlr:antlr4-maven-plugin`
- **Version**: Pinned in parent pom.xml `<dependencyManagement>`

## Spring Framework (Spring Context)

- **Decision**: `spring-context` (already in parent BOM via Spring Boot 3.2.5)
- **Rationale**: `@Component`, `@PostConstruct`, `@Autowired` for rule registration and dependency injection — consistent with Unit 1 patterns

## SLF4J + Logback

- **Decision**: SLF4J API + Logback (via Spring Boot starter)
- **Rationale**: Structured logging consistent with Unit 1; DBMS_OUTPUT translates to `log.debug()`

## Jackson (for externalized config)

- **Decision**: Jackson (`com.fasterxml.jackson.core:jackson-databind`) — already in parent BOM
- **Rationale**: Load BuiltinFunctionMapping and OracleExceptionMapping tables from JSON/YAML classpath resources

## JUnit 5 + Mockito + AssertJ

- **Decision**: `spring-boot-starter-test` (already in parent BOM)
- **Rationale**: Consistent with Unit 1 test stack; each rule gets its own test class

## No Additional Dependencies

- No template engine (translation produces Java strings directly, not via templates — templates are Unit 3's concern)
- No database driver (translation is pure in-memory processing)
- No HTTP client (rule-based only, no external calls)
