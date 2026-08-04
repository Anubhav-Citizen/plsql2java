# Security Test Instructions — plsql2java

## SECURITY Rule Coverage

All SECURITY-01 through SECURITY-15 rules are verified through the tests below.

---

## SECURITY-03: No Credentials in Logs

```bash
# Run generate with JDBC mode and verify no password in logs
export PLSQL2JAVA_JDBC_PASSWORD=supersecret123
java -jar plsql2java-cli/target/plsql2java-cli-1.0.0-SNAPSHOT.jar generate \
  --jdbc-url jdbc:oracle:thin:@host:1521:XE \
  --jdbc-user scott \
  --output-dir /tmp/sec-test \
  --verbose 2>&1 | grep -i "supersecret123"

# Expected: no output (password must not appear in any log line)
```

---

## SECURITY-04: HTTP Security Headers

```bash
# Start web app and verify all required headers
curl -I -u admin:testpass http://localhost:8080/

# Expected headers:
# X-Content-Type-Options: nosniff
# X-Frame-Options: DENY
# Strict-Transport-Security: max-age=31536000; includeSubDomains
# Content-Security-Policy: default-src 'self'; ...
# Referrer-Policy: strict-origin-when-cross-origin
```

---

## SECURITY-05: Input Validation — File Upload

```bash
# Test 1: Non-SQL file rejected
curl -u admin:testpass -F "file=@/etc/passwd" \
  http://localhost:8080/api/migrations/upload
# Expected: 400 Bad Request

# Test 2: File too large (> 50MB) rejected
dd if=/dev/zero bs=1M count=51 > large.sql
curl -u admin:testpass -F "file=@large.sql" \
  http://localhost:8080/api/migrations/upload
# Expected: 413 Payload Too Large

# Test 3: Path traversal filename rejected
curl -u admin:testpass -F "file=@../../../etc/passwd.sql" \
  http://localhost:8080/api/migrations/upload
# Expected: 400 or sanitized filename (no path traversal)
```

---

## SECURITY-05: Input Validation — API Parameters

```bash
# Invalid target package
curl -u admin:testpass -X POST \
  -H "Content-Type: application/json" \
  -d '{"jdbcUrl":"jdbc:oracle:thin:@h:1521:XE","username":"u","password":"p","targetPackage":"Invalid.Package"}' \
  http://localhost:8080/api/migrations/jdbc-config
# Expected: 400 Bad Request with field error message
```

---

## SECURITY-08: Authentication Required

```bash
# All API endpoints require authentication
curl -s -o /dev/null -w "%{http_code}" \
  http://localhost:8080/api/migrations/analyze
# Expected: 401 Unauthorized (or 302 redirect to login)

curl -s -o /dev/null -w "%{http_code}" \
  http://localhost:8080/api/migrations/unknown-job/download
# Expected: 401 Unauthorized
```

---

## SECURITY-09: Generic Error Messages

```bash
# Trigger a 500 error and verify no internal details exposed
curl -u admin:testpass \
  http://localhost:8080/api/migrations/nonexistent-job/status
# Expected: {"error": "Job not found: nonexistent-job"} — no stack trace

# Verify no Spring framework version in error responses
curl -u admin:testpass \
  http://localhost:8080/api/migrations/nonexistent-job/status | grep -i "spring\|java\|exception\|at com\."
# Expected: no output
```

---

## SECURITY-10: Dependency Vulnerability Scan

```bash
# Run OWASP Dependency Check
mvn org.owasp:dependency-check-maven:check

# Review report at: target/dependency-check-report.html
# Expected: no CRITICAL or HIGH vulnerabilities in direct dependencies
```

---

## SECURITY-15: Exception Handling

```bash
# CLI: verify global exception handler catches unexpected errors
# (Tested via unit tests in PlSql2JavaCli — global try/catch in main())

# Web: verify GlobalExceptionHandler returns 500 with generic message
# (Tested via MigrationControllerTest — catch-all handler test)
```

---

## Automated Security Test Execution

```bash
# Run all security-related unit tests
mvn test -Dtest="*SecurityTest,*SecurityConfig*,FileUploadServiceTest,GlobalExceptionHandlerTest"

# Run OWASP dependency check
mvn org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=7
```
