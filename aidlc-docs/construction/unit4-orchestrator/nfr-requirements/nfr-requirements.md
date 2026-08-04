# NFR Requirements — Unit 4: Migration Orchestrator

---

## Performance

- **NFR-OR-P1**: Full pipeline (GENERATE mode) for a 500-object schema MUST complete within 15 minutes
- **NFR-OR-P2**: ANALYZE mode MUST complete within 5 minutes for a 500-object schema
- **NFR-OR-P3**: REPORT mode MUST complete within 60 seconds

## Reliability

- **NFR-OR-R1**: Fail-partial — pipeline MUST continue after per-object failures
- **NFR-OR-R2**: MigrationResult MUST always be returned (never null) even on partial failure
- **NFR-OR-R3**: All pipeline stages MUST be wrapped in try/catch at the object level

## Security

- **NFR-OR-S1**: No credentials in progress events or log output (SECURITY-03)
- **NFR-OR-S2**: Output directory path validated before write (SECURITY-05)
- **NFR-OR-S3**: MDC cleared in finally blocks (SECURITY-03)

## Maintainability

- **NFR-OR-M1**: Each pipeline stage is a discrete method — independently testable
- **NFR-OR-M2**: ProgressEventBus is decoupled from orchestrator — listeners registered externally

## Testability

- **NFR-OR-T1**: All core engine services injected via constructor — mockable in unit tests
- **NFR-OR-T2**: Integration test runs full pipeline with sample DDL fixtures
