package com.plsql2java.codegen;

import com.plsql2java.codegen.model.ArtifactType;
import com.plsql2java.codegen.model.GeneratedProject;
import com.plsql2java.codegen.model.GenerationContext;
import com.plsql2java.codegen.model.JavaSourceFile;
import com.plsql2java.model.OracleObject;
import com.plsql2java.model.OracleObjectType;
import com.plsql2java.translation.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JavaCodeGeneratorServiceTest {

    private JavaCodeGeneratorService generator;
    private GenerationContext ctx;

    @BeforeEach
    void setUp() throws IOException {
        generator = new JavaCodeGeneratorService();
        ctx = new GenerationContext("m1", "com.example", "3.2.5",
                Path.of("/tmp/out"), "org.postgresql.Driver", 70, "MY_SCHEMA");
    }

    private TranslationResult sampleResult(String objectName) {
        OracleObject obj = new OracleObject(objectName, OracleObjectType.PROCEDURE, "MY_SCHEMA", "source");
        JavaMethodIR method = new JavaMethodIR("processOrder", "void",
                List.of("String orderId"), "// body", List.of(), null, List.of());
        JavaIR ir = new JavaIR(objectName, "com.example", objectName, List.of(), List.of(), List.of(method), "");
        return new TranslationResult(obj, ir, List.of(), List.of());
    }

    @Test
    void generateService_producesServiceArtifact() throws IOException {
        JavaSourceFile file = generator.generateService(sampleResult("ORDER_PROC"), ctx);
        assertThat(file.getArtifactType()).isEqualTo(ArtifactType.SERVICE);
        assertThat(file.getRelativePath()).contains("Service.java");
        assertThat(file.getContent()).contains("@Service");
    }

    @Test
    void generateController_producesControllerArtifact() throws IOException {
        JavaSourceFile file = generator.generateController(sampleResult("ORDER_PROC"), ctx);
        assertThat(file.getArtifactType()).isEqualTo(ArtifactType.CONTROLLER);
        assertThat(file.getContent()).contains("@RestController");
    }

    @Test
    void generatePomXml_containsSpringBootParent() throws IOException {
        JavaSourceFile file = generator.generatePomXml(ctx);
        assertThat(file.getArtifactType()).isEqualTo(ArtifactType.POM_XML);
        assertThat(file.getContent()).contains("spring-boot-starter-parent");
        assertThat(file.getContent()).contains("3.2.5");
    }

    @Test
    void generateApplicationYml_usesEnvVarPlaceholders() throws IOException {
        JavaSourceFile file = generator.generateApplicationYml(ctx);
        assertThat(file.getArtifactType()).isEqualTo(ArtifactType.APP_YML);
        assertThat(file.getContent()).contains("${DB_URL}");
        assertThat(file.getContent()).contains("${DB_USERNAME}");
        assertThat(file.getContent()).contains("${DB_PASSWORD}");
        assertThat(file.getContent()).doesNotContain("password: secret");
    }

    @Test
    void generateDockerfile_hasNonRootUser() throws IOException {
        JavaSourceFile file = generator.generateDockerfile(ctx);
        assertThat(file.getArtifactType()).isEqualTo(ArtifactType.DOCKERFILE);
        assertThat(file.getContent()).contains("adduser");
        assertThat(file.getContent()).contains("USER appuser");
        assertThat(file.getContent()).doesNotContain(":latest");
    }

    @Test
    void generateDockerCompose_hasPinnedTags() throws IOException {
        JavaSourceFile file = generator.generateDockerCompose(ctx);
        assertThat(file.getArtifactType()).isEqualTo(ArtifactType.DOCKER_COMPOSE);
        assertThat(file.getContent()).contains("postgres:15-alpine");
        assertThat(file.getContent()).doesNotContain(":latest");
    }

    @Test
    void generateProject_skipsNullJavaIR() throws IOException {
        OracleObject obj = new OracleObject("FAILED", OracleObjectType.PROCEDURE, "SCHEMA", "src");
        TranslationResult failed = new TranslationResult(obj, null, List.of(), List.of());
        TranslationResult good = sampleResult("GOOD_PROC");

        GeneratedProject project = generator.generateProject(List.of(failed, good), ctx);
        assertThat(project.getSkippedObjects()).contains("FAILED");
        assertThat(project.getFilesByType(ArtifactType.SERVICE)).hasSize(1);
    }

    @Test
    void writeProject_writesFilesToDisk(@TempDir Path tempDir) throws IOException {
        GeneratedProject project = generator.generateProject(List.of(sampleResult("ORDER_PROC")), ctx);
        generator.writeProject(project, tempDir);
        assertThat(Files.walk(tempDir).filter(Files::isRegularFile).count()).isGreaterThan(0);
    }
}
