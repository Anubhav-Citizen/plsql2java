# Infrastructure Design — Unit 5: CLI Delivery

## Deployment Model: Standalone Executable JAR

The CLI is distributed as a single fat JAR produced by `spring-boot-maven-plugin`. No server, no container, no cloud infrastructure required.

```
Developer Workstation / CI Server
├── JRE 17+
├── plsql2java-cli-{version}.jar   (fat JAR, ~50MB)
├── migration-config.yml           (optional, user-provided)
└── PLSQL2JAVA_JDBC_PASSWORD       (env var, optional)
```

## Runtime Environment

| Requirement | Value |
|---|---|
| JRE | Java 17+ |
| OS | Any (Windows, Linux, macOS) |
| Memory | 512MB heap minimum (JVM default sufficient for most migrations) |
| Disk | Output directory must have sufficient space for generated project |
| Network | Required only for JDBC mode (Oracle DB connectivity) |

## Build Artifact

- **Plugin**: `spring-boot-maven-plugin` repackage goal
- **Classifier**: none (replaces original JAR)
- **Executable**: `true` (Unix shell script header for `./plsql2java-cli.jar` invocation)
- **Main class**: `com.plsql2java.cli.PlSql2JavaCli`

## Distribution

- JAR published to Maven local repository during `mvn install`
- No Docker image required for CLI (Docker is Unit 6 / Web only)
- Optional: wrap in a shell script `plsql2java` for PATH-based invocation

## No Cloud Infrastructure

- CLI runs entirely on the local machine
- No AWS, Azure, or GCP services required
- Oracle JDBC connectivity is the only external dependency at runtime

## Security Hardening

- SECURITY-09: No default credentials; password via env var only
- SECURITY-10: All dependencies pinned in root pom.xml; no `latest` tags
- SECURITY-15: Global exception handler prevents unhandled JVM crashes
