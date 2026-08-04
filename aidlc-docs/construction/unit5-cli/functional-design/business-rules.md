# Business Rules — Unit 5: CLI Delivery

## BR-CLI-01: Source Requirement
At least one data source MUST be provided: either `--jdbc-url` (with `--jdbc-user`) or at least one `--ddl-file`. If neither is provided, the command MUST fail with `VALIDATION_ERROR` and print a clear message.

**Applies to**: `analyze`, `generate`

## BR-CLI-02: Password Security
JDBC password MUST NOT be accepted as a CLI flag (to prevent shell history exposure). It MUST be read from the `PLSQL2JAVA_JDBC_PASSWORD` environment variable or the YAML config file. If JDBC mode is used and no password source is found, fail with `VALIDATION_ERROR`.

**Applies to**: `analyze`, `generate`

## BR-CLI-03: Target Package Validation
`--target-package` MUST be a valid Java package identifier (matches `^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)*$`). Invalid values fail with `VALIDATION_ERROR`.

**Applies to**: `generate`

## BR-CLI-04: Output Directory
If `--output-dir` does not exist, the CLI MUST attempt to create it. If creation fails, exit with `IO_ERROR`. If it exists but is not writable, exit with `IO_ERROR`.

**Applies to**: `analyze`, `generate`, `report`

## BR-CLI-05: Confidence Threshold Range
`--confidence-threshold` MUST be in range [0.0, 1.0]. Values outside this range fail with `VALIDATION_ERROR`.

**Applies to**: `generate`

## BR-CLI-06: CLI Flags Override Config File
When both `--config` and explicit CLI flags are provided, CLI flags MUST take precedence over config file values for the same field.

**Applies to**: all commands

## BR-CLI-07: Report Command Requires Prior Output
`report` command MUST verify that the `--output-dir` contains a prior migration result (`analysis-result.json` or equivalent). If not found, fail with `VALIDATION_ERROR` and suggest running `generate` first.

**Applies to**: `report`

## BR-CLI-08: Exit Codes Are Meaningful
Exit codes MUST accurately reflect the outcome:
- `0` — all objects processed successfully
- `1` — completed but some objects were skipped (partial success)
- `2` — invalid arguments or configuration
- `3` — pipeline execution error
- `4` — file I/O error

**Applies to**: all commands

## BR-CLI-09: Help Always Available
`--help` / `-h` MUST be available on the root command and all subcommands. Help text MUST include: description, all options with types and defaults, and at least one usage example.

**Applies to**: all commands

## BR-CLI-10: DDL File Existence
Each path provided via `--ddl-file` MUST exist and be readable. Non-existent or unreadable files fail with `IO_ERROR` before pipeline execution begins.

**Applies to**: `analyze`, `generate`

## BR-CLI-11: No Sensitive Data in Logs
JDBC passwords and any credential values MUST NOT appear in log output, stdout, or stderr at any log level.

**Applies to**: all commands (SECURITY-03 enforcement)

## BR-CLI-12: Verbose Flag Scope
`--verbose` / `-v` increases progress output detail but MUST NOT expose internal stack traces or system paths to stdout. Stack traces go to the SLF4J logger only.

**Applies to**: all commands
