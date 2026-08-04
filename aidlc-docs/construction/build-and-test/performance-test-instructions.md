# Performance Test Instructions — plsql2java

## Performance Requirements

| Metric | Target | Applies To |
|---|---|---|
| CLI startup to first output | < 2 seconds | CLI |
| Web API 202 response (analyze/generate submission) | < 200ms | Web |
| SSE event delivery latency | < 500ms | Web |
| Concurrent migration jobs | ≥ 3 simultaneous | Web |
| ZIP packaging (100-file project) | < 5 seconds | Web |
| Translation throughput | ≥ 10 objects/second | Orchestrator |

---

## CLI Performance Test

### Test: Startup Time

```bash
# Measure time from invocation to first output line
time java -jar plsql2java-cli/target/plsql2java-cli-1.0.0-SNAPSHOT.jar \
  analyze --ddl-file test.sql --output-dir /tmp/perf-test

# Expected: real < 2s
```

### Test: Large DDL File Processing

```bash
# Generate a large DDL file with 100 procedures
# Then measure full generate pipeline time
time java -jar plsql2java-cli/target/plsql2java-cli-1.0.0-SNAPSHOT.jar \
  generate --ddl-file large-schema.sql \
  --output-dir /tmp/perf-output \
  --target-package com.example.perf

# Expected: completes within reasonable time proportional to object count
```

---

## Web API Performance Test

### Prerequisites

```bash
# Install Apache Bench (ab) or curl for load testing
# Start the web application
ADMIN_PASSWORD=testpass docker-compose up -d
```

### Test: API Response Time (202 Accepted)

```bash
# Get a session cookie first
curl -c cookies.txt -d "username=admin&password=testpass" \
  http://localhost:8080/login

# Upload a DDL file
UPLOAD_ID=$(curl -b cookies.txt -s -F "file=@test.sql" \
  http://localhost:8080/api/migrations/upload | jq -r '.uploadId')

# Measure analyze submission response time
time curl -b cookies.txt -s -X POST \
  -H "Content-Type: application/json" \
  -d "{\"uploadId\":\"$UPLOAD_ID\"}" \
  http://localhost:8080/api/migrations/analyze

# Expected: < 200ms
```

### Test: Concurrent Jobs

```bash
# Submit 3 analyze jobs simultaneously
for i in 1 2 3; do
  curl -b cookies.txt -s -X POST \
    -H "Content-Type: application/json" \
    -d "{\"uploadId\":\"$UPLOAD_ID\"}" \
    http://localhost:8080/api/migrations/analyze &
done
wait

# Expected: all 3 return 202 Accepted without blocking each other
```

---

## Memory Profiling

```bash
# Run with JVM memory monitoring
java -Xmx1g -verbose:gc \
  -jar plsql2java-cli/target/plsql2java-cli-1.0.0-SNAPSHOT.jar \
  generate --ddl-file large-schema.sql --output-dir /tmp/perf

# Expected: no OutOfMemoryError; GC pauses < 500ms
```

---

## Performance Optimization Notes

If performance targets are not met:

| Issue | Investigation | Fix |
|---|---|---|
| Slow ANTLR parsing | Profile `PlSqlTranslationEngine` | Increase parser thread pool |
| Slow FreeMarker rendering | Profile `JavaCodeGeneratorService` | Enable FreeMarker template caching |
| High memory for large schemas | Heap dump analysis | Process objects in batches |
| Slow ZIP packaging | Profile `ZipPackager` | Already streaming; check disk I/O |
| Web SSE latency | Check `ThreadPoolTaskExecutor` queue | Increase core pool size |
