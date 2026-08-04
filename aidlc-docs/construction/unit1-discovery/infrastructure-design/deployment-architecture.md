# Deployment Architecture — Unit 1: Discovery & Dependency Analysis

Unit 1 is a library module — it has no independent deployment. It is deployed as part of the CLI (Unit 5) or Web Application (Unit 6) that includes it as a Maven dependency.

## Runtime Context

```
[CLI JAR / Web App Container]
        |
        | (in-process call)
        v
[plsql2java-discovery.jar]
        |
        +---> [Oracle DB via JDBC] (external, user-provided)
        |
        +---> [Local File System] (DDL input + output directory)
```

## Environment Variables Required at Runtime

| Variable | Purpose | Required For |
|---|---|---|
| `ORACLE_PASSWORD` (or configured name) | JDBC authentication | JDBC discovery mode only |

## No Additional Infrastructure Required

Unit 1 does not require:
- Any cloud services
- Any containerized infrastructure of its own
- Any network configuration beyond JDBC connectivity to Oracle
