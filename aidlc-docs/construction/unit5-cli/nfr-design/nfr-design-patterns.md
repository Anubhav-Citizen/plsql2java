# NFR Design Patterns — Unit 5: CLI Delivery

## Pattern 1: Command Pattern (Picocli Subcommands)

Each CLI command (`analyze`, `generate`, `report`) is a separate `@Command`-annotated class implementing `Callable<Integer>`. The return value is the exit code. `PlSql2JavaCli` is the top-level `@Command` with `subcommands` referencing all three.

**Benefit**: Single responsibility per command; independently testable; Picocli handles routing.

## Pattern 2: Config Merge Chain (Priority Override)

`ConfigLoader` applies a merge chain: defaults → YAML file → environment variables → CLI flags. Each layer overrides the previous. Implemented as a simple sequential merge (no framework needed).

**Benefit**: Predictable precedence; easy to test each layer in isolation.

## Pattern 3: Listener Registration (Observer)

`CliProgressListener` is registered on `ProgressEventBus` before pipeline invocation and unregistered after (in a `finally` block). This prevents listener leaks in test scenarios.

**Benefit**: Decoupled progress rendering; consistent with orchestrator's event model.

## Pattern 4: Global Exception Handler (Fail-Safe)

`PlSql2JavaCli.main()` wraps the Picocli `execute()` call in a try/catch. Any unhandled `Throwable` is caught, logged via SLF4J, and exits with `EXECUTION_ERROR(3)`. User sees a generic message; details go to the log.

**Benefit**: SECURITY-15 compliance; no unhandled exceptions escape to the JVM default handler.

## Pattern 5: Credential Isolation

JDBC password is read exclusively from `PLSQL2JAVA_JDBC_PASSWORD` env var or YAML config. It is stored in a `char[]` field (not `String`) in `CliConfig` and cleared after `MigrationConfig` construction. It is never logged.

**Benefit**: SECURITY-03 + SECURITY-12 compliance; reduces credential exposure window.
