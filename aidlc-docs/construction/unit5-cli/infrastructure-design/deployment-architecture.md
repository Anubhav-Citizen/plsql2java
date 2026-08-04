# Deployment Architecture — Unit 5: CLI Delivery

## Architecture: Single-Process CLI

```
+--------------------------------------------------+
|  Developer Workstation / CI Server               |
|                                                  |
|  $ java -jar plsql2java-cli.jar generate \       |
|      --config migration-config.yml               |
|                                                  |
|  +--------------------------------------------+  |
|  |  JVM Process                               |  |
|  |  +--------------------------------------+  |  |
|  |  |  Spring Application Context          |  |  |
|  |  |  +-----------+  +----------------+   |  |  |
|  |  |  | Picocli   |  | Orchestrator   |   |  |  |
|  |  |  | Commands  |->| Service        |   |  |  |
|  |  |  +-----------+  +----------------+   |  |  |
|  |  |       |              |               |  |  |
|  |  |  +----v----+    +----v----+          |  |  |
|  |  |  | Config  |    | Units   |          |  |  |
|  |  |  | Loader  |    | 1-4     |          |  |  |
|  |  |  +---------+    +---------+          |  |  |
|  |  +--------------------------------------+  |  |
|  +--------------------------------------------+  |
|                                                  |
|  Input:  DDL files / Oracle JDBC                 |
|  Output: ./plsql2java-output/ (generated code)   |
|  Stdout: Progress + summary                      |
+--------------------------------------------------+
```

## Build Pipeline

```
mvn package
    └── spring-boot-maven-plugin:repackage
        └── plsql2java-cli-{version}.jar  (fat JAR)
```

## Invocation Patterns

```bash
# Analyze only (offline DDL mode)
java -jar plsql2java-cli.jar analyze \
  --ddl-file schema.sql \
  --output-dir ./output

# Full generation (JDBC mode)
export PLSQL2JAVA_JDBC_PASSWORD=secret
java -jar plsql2java-cli.jar generate \
  --jdbc-url jdbc:oracle:thin:@localhost:1521:XE \
  --jdbc-user scott \
  --target-package com.example.migrated \
  --output-dir ./output

# Config file mode
java -jar plsql2java-cli.jar generate \
  --config migration-config.yml

# Regenerate report
java -jar plsql2java-cli.jar report \
  --output-dir ./output
```
