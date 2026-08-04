# NFR Requirements — Unit 3: Code Generator + Confidence Scorer + Report Generator

---

## Performance

- **NFR-CG-P1**: Full code generation for a 500-object schema MUST complete within the 15-minute budget (NFR-01.3)
- **NFR-CG-P2**: Confidence scoring for 500 objects MUST complete within 30 seconds
- **NFR-CG-P3**: Report generation for a 500-object schema MUST complete within 60 seconds (FR-09 story 7.3 AC)
- **NFR-CG-P4**: File I/O for writing generated project MUST use buffered writers — no unbuffered single-byte writes

## Accuracy

- **NFR-CG-A1**: Generated Java code MUST be syntactically valid for all TRANSLATED constructs (NFR-03.3)
- **NFR-CG-A2**: ≥95% traceability coverage in generated report (FR-08.3)
- **NFR-CG-A3**: ≥90% migration confidence score for schemas using supported constructs (NFR-02.5)

## Reliability

- **NFR-CG-R1**: A generation failure for one object MUST NOT stop generation of remaining objects (fail-partial)
- **NFR-CG-R2**: Report generation MUST succeed even if some objects were skipped or failed
- **NFR-CG-R3**: File write failures MUST throw a descriptive exception — never silently produce empty files

## Maintainability

- **NFR-CG-M1**: FreeMarker templates for Java artifacts are in `src/main/resources/templates/` — not hardcoded strings
- **NFR-CG-M2**: Report sections are independently renderable (each section is a separate method)
- **NFR-CG-M3**: Unit test coverage ≥80% (NFR-04.3)

## Security

- **NFR-CG-S1**: Generated `application.yml` MUST use env var placeholders — no hardcoded credentials (BR-CG01, SECURITY-12)
- **NFR-CG-S2**: Generated Dockerfile MUST use non-root user (BR-CG03, SECURITY-09)
- **NFR-CG-S3**: Generated Dockerfile and docker-compose.yml MUST use pinned image tags (BR-CG02, SECURITY-10)
- **NFR-CG-S4**: Reports MUST NOT contain credentials or connection strings (BR-RG07, SECURITY-03)
- **NFR-CG-S5**: All file write operations MUST use try-with-resources (SECURITY-15)
- **NFR-CG-S6**: Output directory path MUST be validated — no path traversal (SECURITY-05)
