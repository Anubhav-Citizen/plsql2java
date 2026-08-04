# Build Instructions — plsql2java

## Prerequisites

| Requirement | Version | Notes |
|---|---|---|
| JDK | 17+ | `java -version` to verify |
| Maven | 3.9+ | `mvn -version` to verify |
| Docker | 24+ | Required for Web module container build only |
| Oracle JDBC driver | 23.4.0.24.05 | Auto-downloaded from Maven Central |

**Memory**: 2GB heap recommended for full build (`export MAVEN_OPTS="-Xmx2g"`)  
**Disk**: ~500MB for dependencies + build artifacts

---

## Environment Variables

| Variable | Required | Purpose |
|---|---|---|
| `MAVEN_OPTS` | No | JVM options for Maven (e.g. `-Xmx2g`) |
| `PLSQL2JAVA_JDBC_PASSWORD` | No | Only needed for JDBC-mode runtime tests |
| `ADMIN_PASSWORD` | No | Only needed for Docker Compose web deployment |

---

## Build Steps

### 1. Clone / Navigate to Project Root

```bash
cd c:\project\repo\plsql2java
```

### 2. Resolve All Dependencies

```bash
mvn dependency:resolve -q
```

### 3. Full Build (All 6 Modules)

```bash
mvn clean install -DskipTests
```

**Build order** (enforced by Maven module dependency graph):
1. `plsql2java-discovery`
2. `plsql2java-translation`
3. `plsql2java-codegen`
4. `plsql2java-orchestrator`
5. `plsql2java-cli`
6. `plsql2java-web`

### 4. Build with Tests

```bash
mvn clean install
```

### 5. Build Individual Module

```bash
# Example: rebuild only the CLI module
mvn clean install -pl plsql2java-cli -am
```

### 6. Build CLI Fat JAR

```bash
mvn clean package -pl plsql2java-cli -am -DskipTests
# Artifact: plsql2java-cli/target/plsql2java-cli-1.0.0-SNAPSHOT.jar
```

### 7. Build Web Fat JAR

```bash
mvn clean package -pl plsql2java-web -am -DskipTests
# Artifact: plsql2java-web/target/plsql2java-web-1.0.0-SNAPSHOT.jar
```

### 8. Build Docker Image (Web)

```bash
cd plsql2java-web
mvn clean package -DskipTests
docker build -t plsql2java-web:1.0.0 .
```

---

## Expected Build Output

```
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for plsql2java - Oracle PL/SQL Modernization Platform:
[INFO]   plsql2java-discovery .......................... SUCCESS
[INFO]   plsql2java-translation ........................ SUCCESS
[INFO]   plsql2java-codegen ............................ SUCCESS
[INFO]   plsql2java-orchestrator ....................... SUCCESS
[INFO]   plsql2java-cli ............................... SUCCESS
[INFO]   plsql2java-web ............................... SUCCESS
[INFO] BUILD SUCCESS
```

**Build artifacts**:
- `plsql2java-cli/target/plsql2java-cli-1.0.0-SNAPSHOT.jar` — executable CLI fat JAR
- `plsql2java-web/target/plsql2java-web-1.0.0-SNAPSHOT.jar` — executable Web fat JAR

---

## Troubleshooting

### Compilation Error: Cannot find symbol in plsql2java-orchestrator
- **Cause**: Upstream module not built first
- **Fix**: `mvn clean install -pl plsql2java-discovery,plsql2java-translation,plsql2java-codegen -am`

### ANTLR4 Grammar Generation Fails
- **Cause**: ANTLR4 Maven plugin not resolving
- **Fix**: `mvn generate-sources -pl plsql2java-translation`

### Out of Memory During Build
- **Cause**: Default Maven heap too small for ANTLR grammar compilation
- **Fix**: `export MAVEN_OPTS="-Xmx2g" && mvn clean install`

### Docker Build Fails: Base Image Not Found
- **Cause**: Pinned digest in Dockerfile may need updating
- **Fix**: Update `FROM eclipse-temurin:17-jre-alpine@sha256:<new-digest>` with current digest from Docker Hub
