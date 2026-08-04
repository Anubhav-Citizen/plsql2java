# NFR Design Patterns — Unit 4: Migration Orchestrator

---

## Pattern 1: Fail-Partial Pipeline Loop

**Addresses**: NFR-OR-R1, BR-OR02

Translation loop wraps each object in try/catch. Failed objects are logged at WARN and added to `skippedObjects`. Pipeline always continues and always returns a result.

```java
for (OracleObject obj : migrationOrder) {
    try {
        results.add(translationEngine.translate(obj));
    } catch (Exception e) {
        log.warn("Skipping {}: {}", obj.getName(), e.getMessage());
        skippedObjects.add(obj.getName());
    }
}
```

---

## Pattern 2: MDC Pipeline Context

**Addresses**: NFR-OR-S3, BR-OR09

migrationId set in MDC at pipeline entry, cleared in finally. Per-object objectName set/cleared within the translation loop.

---

## Pattern 3: Decoupled ProgressEventBus

**Addresses**: NFR-OR-M2, BR-OR04

ProgressEventBus holds a list of ProgressListeners. `emit()` iterates listeners and catches all exceptions — a broken listener never aborts the pipeline.

---

## Pattern 4: Output Directory Pre-Validation

**Addresses**: NFR-OR-S2, BR-OR08

Before any write, `outputDir.toRealPath()` is resolved and checked for writability. Created if absent. Path traversal check: all written paths must start with resolved outputDir.

---

## Pattern 5: Constructor Injection for All Engine Services

**Addresses**: NFR-OR-T1

All 6 core engine services injected via constructor — enables full mocking in unit tests without Spring context.
