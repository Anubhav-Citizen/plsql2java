# Tech Stack Decisions — Unit 4: Migration Orchestrator

---

## Spring Context

- **Decision**: `spring-context` (via Spring Boot BOM)
- **Rationale**: `@Service` for MigrationOrchestratorService; consistent with Units 1–3

## SLF4J + Logback

- **Decision**: SLF4J API + Logback (via Spring Boot starter)
- **Rationale**: MDC for migrationId/objectName context; consistent with Units 1–3

## No Additional Dependencies

- No new dependencies beyond what Units 1–3 already provide
- ProgressEventBus is a plain Java class (no messaging framework needed at this stage)
- Persistence uses Jackson (already in BOM) for JSON serialization of MigrationResult

## JUnit 5 + Mockito + AssertJ

- **Decision**: `spring-boot-starter-test` — already in parent BOM
- **Rationale**: Mock all 6 core engine services in unit tests; consistent test stack
