# Deployment Architecture — Unit 3: Code Generator + Confidence Scorer + Report Generator

## Deployment Model

Unit 3 is a Maven library module embedded in the host process.

```
CLI JAR (Unit 5)                    Web Container (Unit 6)
      |                                      |
      v                                      v
plsql2java-orchestrator (Unit 4)    plsql2java-orchestrator (Unit 4)
      |                                      |
      v                                      v
plsql2java-codegen (Unit 3)      <-- same JAR, in-process
      |
      +-- plsql2java-translation (Unit 2)
      +-- plsql2java-discovery (Unit 1)
```

## Maven Dependency Declaration

In `plsql2java-orchestrator/pom.xml`:
```xml
<dependency>
    <groupId>com.plsql2java</groupId>
    <artifactId>plsql2java-codegen</artifactId>
    <version>${project.version}</version>
</dependency>
```

## No Runtime Infrastructure Required

- No database, no message broker, no cache, no external service endpoints
