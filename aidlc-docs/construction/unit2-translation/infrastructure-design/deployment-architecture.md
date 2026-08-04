# Deployment Architecture — Unit 2: PL/SQL Translation Engine

---

## Deployment Model

Unit 2 is a Maven library module. It has no standalone deployment — it is embedded in the host process.

```
CLI JAR (Unit 5)                    Web Container (Unit 6)
      |                                      |
      v                                      v
plsql2java-orchestrator (Unit 4)    plsql2java-orchestrator (Unit 4)
      |                                      |
      v                                      v
plsql2java-translation (Unit 2)  <-- same JAR, in-process
      |
      v
plsql2java-discovery (Unit 1)  <-- OracleObject model dependency
```

## Maven Dependency Declaration

In `plsql2java-orchestrator/pom.xml`:
```xml
<dependency>
    <groupId>com.plsql2java</groupId>
    <artifactId>plsql2java-translation</artifactId>
    <version>${project.version}</version>
</dependency>
```

## Build-Time Grammar Compilation

ANTLR4 grammar files in `src/main/resources/grammar/` are compiled to Java source during `mvn generate-sources` by `antlr4-maven-plugin`. Generated parser/lexer classes are placed in `target/generated-sources/antlr4/`.

## No Runtime Infrastructure Required

- No database
- No message broker
- No cache server
- No external service endpoints
