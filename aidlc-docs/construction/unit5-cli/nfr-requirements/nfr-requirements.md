# NFR Requirements — Unit 5: CLI Delivery

## Performance
- CLI startup time: < 2 seconds to first output line (JVM warm-up acceptable)
- Progress output: events streamed in real-time (no buffering delay > 100ms)
- No performance requirements beyond the orchestrator pipeline itself (CLI is a thin wrapper)

## Usability
- `--help` output fits in a standard 80-column terminal without wrapping critical content
- Error messages include the invalid value and the expected format/range
- Progress output uses consistent `[LEVEL] message` prefix format
- Exit codes documented in `--help` output

## Reliability
- CLI MUST NOT swallow exceptions silently — all errors produce a non-zero exit code
- Partial success (some objects skipped) MUST be clearly communicated with object names listed
- Config file parse errors MUST report the line/field causing the failure

## Security
- SECURITY-03: No passwords or credentials in log output or stdout
- SECURITY-05: All CLI inputs validated before pipeline invocation
- SECURITY-09: Generic error messages to stdout; full details to SLF4J logger only
- SECURITY-10: All dependencies pinned (inherited from root pom.xml)
- SECURITY-15: Global exception handler at `PlSql2JavaCli.main()` catches all unhandled exceptions

## Maintainability
- Each command in its own class (single responsibility)
- `ConfigLoader` is independently testable (no Picocli dependency in its core logic)
- `CliProgressListener` is independently testable

## Packaging
- Packaged as a fat/uber JAR via `spring-boot-maven-plugin` with `executable=true`
- JAR name: `plsql2java-cli-{version}.jar`
- Runnable as: `java -jar plsql2java-cli.jar <command> [options]`
- Optional: native binary via GraalVM (out of scope for initial delivery)
