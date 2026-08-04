package com.plsql2java.codegen;

import com.plsql2java.codegen.model.ArtifactType;
import com.plsql2java.codegen.model.GeneratedProject;
import com.plsql2java.codegen.model.GenerationContext;
import com.plsql2java.common.MigrationConfig;
import com.plsql2java.model.DependencyGraph;
import com.plsql2java.model.DiscoveryResult;
import com.plsql2java.model.OracleObject;
import com.plsql2java.model.OracleObjectType;
import com.plsql2java.reporting.MigrationReportGeneratorService;
import com.plsql2java.reporting.model.MigrationReport;
import com.plsql2java.reporting.model.ReportInput;
import com.plsql2java.scoring.ConfidenceScorerService;
import com.plsql2java.scoring.model.ConfidenceReport;
import com.plsql2java.translation.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CodeGenerationIntegrationTest {

    private JavaCodeGeneratorService codeGenerator;
    private ConfidenceScorerService scorer;
    private MigrationReportGeneratorService reportGenerator;

    @BeforeEach
    void setUp() throws IOException {
        codeGenerator = new JavaCodeGeneratorService();
        scorer = new ConfidenceScorerService();
        reportGenerator = new MigrationReportGeneratorService();
    }

    private List<TranslationResult> sampleResults() {
        OracleObject obj = new OracleObject("ORDER_PROC", OracleObjectType.PROCEDURE, "MY_SCHEMA", "source\nline2\nline3");
        obj.setLineCount(3);
        JavaMethodIR method = new JavaMethodIR("processOrder", "void",
                List.of("String orderId"), "// body", List.of(), null, List.of());
        JavaIR ir = new JavaIR("ORDER_PROC", "com.example", "OrderProc", List.of(), List.of(), List.of(method), "");
        return List.of(new TranslationResult(obj, ir, List.of(), List.of()));
    }

    @Test
    void fullPipeline_generateProject_allArtifactTypesPresent(@TempDir Path tempDir) throws IOException {
        GenerationContext ctx = new GenerationContext("m1", "com.example", "3.2.5",
                tempDir, "org.postgresql.Driver", 70, "MY_SCHEMA");

        GeneratedProject project = codeGenerator.generateProject(sampleResults(), ctx);

        assertThat(project.getFilesByType(ArtifactType.SERVICE)).isNotEmpty();
        assertThat(project.getFilesByType(ArtifactType.CONTROLLER)).isNotEmpty();
        assertThat(project.getFilesByType(ArtifactType.DTO)).isNotEmpty();
        assertThat(project.getFilesByType(ArtifactType.TEST)).isNotEmpty();
        assertThat(project.getFilesByType(ArtifactType.POM_XML)).isNotEmpty();
        assertThat(project.getFilesByType(ArtifactType.APP_YML)).isNotEmpty();
        assertThat(project.getFilesByType(ArtifactType.DOCKERFILE)).isNotEmpty();
        assertThat(project.getFilesByType(ArtifactType.DOCKER_COMPOSE)).isNotEmpty();
    }

    @Test
    void fullPipeline_writeProject_filesWrittenToDisk(@TempDir Path tempDir) throws IOException {
        GenerationContext ctx = new GenerationContext("m1", "com.example", "3.2.5",
                tempDir, "org.postgresql.Driver", 70, "MY_SCHEMA");
        GeneratedProject project = codeGenerator.generateProject(sampleResults(), ctx);
        codeGenerator.writeProject(project, tempDir);

        long fileCount = Files.walk(tempDir).filter(Files::isRegularFile).count();
        assertThat(fileCount).isGreaterThan(0);
    }

    @Test
    void fullPipeline_scoreAll_scoresInRange() {
        ConfidenceReport report = scorer.scoreAll(sampleResults(), 70);
        assertThat(report.getOverallScore()).isBetween(0, 100);
        assertThat(report.getObjectScores()).hasSize(1);
    }

    @Test
    void fullPipeline_generateReport_bothFormatsProduced(@TempDir Path tempDir) throws IOException {
        GenerationContext ctx = new GenerationContext("m1", "com.example", "3.2.5",
                tempDir, "org.postgresql.Driver", 70, "MY_SCHEMA");
        List<TranslationResult> results = sampleResults();
        GeneratedProject project = codeGenerator.generateProject(results, ctx);
        ConfidenceReport confidenceReport = scorer.scoreAll(results, 70);

        DiscoveryResult discovery = new DiscoveryResult("m1", "MY_SCHEMA", null);
        discovery.setObjects(List.of(results.get(0).getSourceObject()));
        DependencyGraph graph = new DependencyGraph("m1");
        MigrationConfig config = new MigrationConfig();
        config.setMigrationId("m1");

        ReportInput input = new ReportInput(discovery, graph, results, project, confidenceReport, config);
        MigrationReport report = reportGenerator.generateReport(input);

        assertThat(report.getMarkdownContent()).isNotBlank();
        assertThat(report.getHtmlContent()).startsWith("<!DOCTYPE html>");
    }
}
