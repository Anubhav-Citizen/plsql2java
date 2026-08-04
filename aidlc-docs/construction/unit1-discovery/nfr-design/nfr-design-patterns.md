# NFR Design Patterns — Unit 1: Discovery & Dependency Analysis

---

## Pattern 1: Try-With-Resources for JDBC (Reliability + Security)

All JDBC connections, statements, and result sets MUST use try-with-resources to guarantee closure.

```java
// Pattern applied in OracleDiscoveryService
try (Connection conn = dataSource.getConnection();
     PreparedStatement stmt = conn.prepareStatement(SQL_GET_OBJECTS);
     ResultSet rs = stmt.executeQuery()) {
    // process results
}
// Connection always closed — even on exception
```

**Addresses**: NFR-U1-R2, NFR-U1-S2, SECURITY-15

---

## Pattern 2: Fail-Partial with Error Accumulation (Reliability)

Discovery processes all objects and accumulates errors rather than failing fast. Each object is processed in an isolated try/catch.

```java
// Pattern applied in OracleDiscoveryService and DdlFileParser
List<OracleObject> objects = new ArrayList<>();
List<DiscoveryError> errors = new ArrayList<>();

for (String objectName : objectNames) {
    try {
        objects.add(extractObject(objectName));
    } catch (Exception e) {
        log.warn("Failed to extract object {}: {}", objectName, e.getMessage());
        errors.add(new DiscoveryError(objectName, e.getMessage()));
    }
}
```

**Addresses**: NFR-U1-R1, BR-12

---

## Pattern 3: Observer / Event Bus for Progress (Decoupling)

Progress events are emitted via a ProgressListener interface — the discovery service has no knowledge of how progress is displayed (CLI stdout vs Web SSE).

```java
// ProgressListener interface (defined in common module)
public interface ProgressListener {
    void onProgress(ProgressEvent event);
}

// Usage in OracleDiscoveryService
listener.onProgress(ProgressEvent.of(migrationId, DISCOVERY, objectName, processed, total));
```

**Addresses**: NFR-U1-P1 (non-blocking progress), decoupling delivery from engine

---

## Pattern 4: Externalized Constants for SQL and Regex (Maintainability)

All Oracle data dictionary SQL queries and DDL parsing regex patterns are defined as named constants in dedicated classes.

```java
// OracleDataDictionaryQueries.java
public final class OracleDataDictionaryQueries {
    public static final String GET_ALL_OBJECTS =
        "SELECT OBJECT_NAME, OBJECT_TYPE FROM ALL_OBJECTS WHERE OWNER = ? AND OBJECT_TYPE IN (...)";
    public static final String GET_SOURCE =
        "SELECT TEXT FROM ALL_SOURCE WHERE OWNER = ? AND NAME = ? AND TYPE = ? ORDER BY LINE";
    // ...
}

// DdlPatterns.java
public final class DdlPatterns {
    /** Matches: CREATE OR REPLACE PACKAGE [schema.]name */
    public static final Pattern PACKAGE_SPEC =
        Pattern.compile("CREATE\\s+OR\\s+REPLACE\\s+PACKAGE\\s+(?:\\w+\\.)?([\\w$#]+)", Pattern.CASE_INSENSITIVE);
    // ...
}
```

**Addresses**: NFR-U1-M1, NFR-U1-M2

---

## Pattern 5: Structured Logging with MDC (Security + Observability)

All log statements include migrationId via SLF4J MDC (Mapped Diagnostic Context) for correlation.

```java
// Applied at the start of each operation
MDC.put("migrationId", config.getMigrationId());
MDC.put("component", "OracleDiscoveryService");
try {
    log.info("Starting JDBC discovery for schema: {}", config.getSchemaName());
    // ... never log password
} finally {
    MDC.clear();
}
```

**Addresses**: NFR-U1-L1, NFR-U1-L2, NFR-U1-L3, SECURITY-03

---

## Pattern 6: Path Traversal Prevention (Security)

All user-provided file paths are validated before use.

```java
// Applied in DdlFileParser
public static Path validateSafePath(Path userPath, Path allowedBase) {
    Path resolved = allowedBase.resolve(userPath).normalize();
    if (!resolved.startsWith(allowedBase)) {
        throw new DiscoveryException("Invalid file path: path traversal detected");
    }
    return resolved;
}
```

**Addresses**: NFR-U1-S3, SECURITY-05

---

## Pattern 7: Global Exception Sanitization (Security)

All exceptions propagated to the delivery layer are wrapped in DiscoveryException with a user-friendly message — internal details (stack traces, SQL, file paths) are logged but not exposed.

```java
// Applied in OracleDiscoveryService
} catch (SQLException e) {
    log.error("JDBC error during discovery: {}", e.getMessage(), e); // full detail in logs
    throw new DiscoveryException("Database connection failed. Check your JDBC configuration."); // safe message to user
}
```

**Addresses**: NFR-U1-S6, SECURITY-09, SECURITY-15
