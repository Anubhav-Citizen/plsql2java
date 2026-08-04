# Tech Stack Decisions — Unit 1: Discovery & Dependency Analysis

---

## Java Version
- **Decision**: Java 17 (LTS)
- **Rationale**: Consistent with generated project target; LTS support; records, sealed classes available

## Build System
- **Decision**: Maven (multi-module, `plsql2java-discovery` module)
- **Rationale**: Agreed in Units Generation (Q4=B)

## JDBC Driver
- **Decision**: Oracle JDBC Driver `ojdbc11` (compatible with Oracle 12c+, Java 11+)
- **Rationale**: Official Oracle driver; supports TLS; available from Maven Central (com.oracle.database.jdbc:ojdbc11)
- **Version**: Pinned in parent pom.xml `<dependencyManagement>`

## DDL Parsing
- **Decision**: Custom regex-based tokenizer + statement classifier (no external DDL parser library for MVP)
- **Rationale**: Oracle DDL export files follow predictable patterns; a lightweight regex approach avoids heavy grammar dependencies in Unit 1; ANTLR4 grammar is reserved for Unit 2 (full PL/SQL AST parsing)
- **Fallback**: If regex approach proves insufficient for edge cases, ANTLR4 with a DDL-only grammar subset can be introduced in a later iteration

## JSON Serialization (Result Persistence)
- **Decision**: Jackson (`com.fasterxml.jackson.core:jackson-databind`) with Java Time module
- **Rationale**: Industry standard; Spring Boot compatible; handles Instant, List, Map natively
- **Version**: Pinned in parent pom.xml

## Graph Algorithms
- **Decision**: Custom implementation (DFS cycle detection + Kahn's topological sort)
- **Rationale**: Graph is small (≤500 nodes); no external graph library needed; custom implementation is simpler and fully testable
- **No external dependency**: JGraphT or similar libraries are not required

## Logging
- **Decision**: SLF4J API + Logback implementation
- **Rationale**: Standard Java logging; Spring Boot compatible; structured JSON logging via logback-classic
- **Version**: Pinned in parent pom.xml

## Testing
- **Decision**: JUnit 5 + Mockito + AssertJ
- **Rationale**: Consistent with generated project test stack; modern, expressive assertions
- **Integration Tests**: Use sample DDL files in `src/test/resources/` — no live Oracle DB required for unit tests

## Dependency Injection
- **Decision**: Spring Framework (`@Component`, `@Service`, `@Autowired`) — Spring Boot auto-configuration
- **Rationale**: Consistent with the overall platform; enables easy integration with CLI and Web modules
- **Note**: Unit 1 is a Spring Boot library module — it does not have a `main()` class; it is consumed by the orchestrator
