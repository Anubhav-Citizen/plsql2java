# Tech Stack Decisions — Unit 5: CLI Delivery

## Picocli 4.x

- **Decision**: `info.picocli:picocli:4.7.6`
- **Rationale**: Annotation-driven CLI framework; subcommand support; auto-generated `--help`; Spring Boot integration via `picocli-spring-boot-starter`; widely used in Java CLI tooling

## Spring Boot (spring-boot-starter)

- **Decision**: `spring-boot-starter` (no web, no actuator)
- **Rationale**: Enables Spring DI to inject `MigrationOrchestratorService` into command classes; consistent with all other units; `CommandLineRunner` integration with Picocli

## Jackson YAML (jackson-dataformat-yaml)

- **Decision**: `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml`
- **Rationale**: Parse `migration-config.yml` into `CliConfig`; already in BOM via Spring Boot; consistent with Units 1–4

## SLF4J + Logback

- **Decision**: Inherited from `spring-boot-starter`
- **Rationale**: Internal diagnostic logging; consistent with all units; stdout reserved for user-facing output only

## JUnit 5 + Mockito + AssertJ

- **Decision**: `spring-boot-starter-test` — already in parent BOM
- **Rationale**: Mock `MigrationOrchestratorService` in command unit tests; consistent test stack

## spring-boot-maven-plugin

- **Decision**: Repackage goal to produce executable fat JAR
- **Rationale**: Single distributable artifact; no classpath management required by end user
