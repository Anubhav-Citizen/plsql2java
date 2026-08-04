package com.plsql2java.reporting;

import com.plsql2java.codegen.model.GeneratedProject;
import com.plsql2java.common.MigrationConfig;
import com.plsql2java.model.DependencyGraph;
import com.plsql2java.model.DiscoveryResult;
import com.plsql2java.model.OracleObject;
import com.plsql2java.model.OracleObjectType;
import com.plsql2java.reporting.model.*;
import com.plsql2java.scoring.model.ConfidenceReport;
import com.plsql2java.scoring.model.ObjectConfidenceScore;
import com.plsql2java.translation.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MigrationReportGeneratorServiceTest {

    private MigrationReportGeneratorService service;

    @BeforeEach
    void setUp() {
        service = new MigrationReportGeneratorService();
    }

    private ReportInput buildInput() {
        DiscoveryResult discovery = new DiscoveryResult("m1", "MY_SCHEMA", null);
        OracleObject obj = new OracleObject("ORDER_PROC", OracleObjectType.PROCEDURE, "MY_SCHEMA", "source");
        discovery.setObjects(List.of(obj));

        DependencyGraph graph = new DependencyGraph("m1");
        graph.setMigrationOrder(List.of("ORDER_PROC"));

        JavaMethodIR method = new JavaMethodIR("processOrder", "void", List.of(), "// body", List.of(), null, List.of());
        JavaIR ir = new JavaIR("ORDER_PROC", "com.example", "OrderProc", List.of(), List.of(), List.of(method), "");
        TranslationResult tr = new TranslationResult(obj, ir, List.of(), List.of());

        GeneratedProject project = new GeneratedProject("m1", "my-schema");

        ObjectConfidenceScore score = new ObjectConfidenceScore("ORDER_PROC", OracleObjectType.PROCEDURE, 85, false, List.of(), false);
        ConfidenceReport confidenceReport = new ConfidenceReport("m1", 70, List.of(score), 85);

        MigrationConfig config = new MigrationConfig();
        config.setMigrationId("m1");

        return new ReportInput(discovery, graph, List.of(tr), project, confidenceReport, config);
    }

    @Test
    void generateReport_executiveSummaryPresent() {
        MigrationReport report = service.generateReport(buildInput());
        assertThat(report.getMarkdownContent()).contains("Executive Summary");
        assertThat(report.getHtmlContent()).contains("Executive Summary");
    }

    @Test
    void generateReport_traceabilityMatrixCoverageAbove95() {
        MigrationReport report = service.generateReport(buildInput());
        TraceabilityMatrix matrix = service.buildTraceabilityMatrix(buildInput());
        assertThat(matrix.getCoveragePct()).isGreaterThanOrEqualTo(95.0);
    }

    @Test
    void generateReport_flaggedConstructsGroupedByType() {
        ReportInput input = buildInput();
        FlaggedConstructsSummary summary = service.buildFlaggedConstructsSummary(input.getTranslationResults());
        // No flagged constructs in sample — section should say "No unsupported constructs found"
        assertThat(service.generateReport(input).getMarkdownContent()).contains("No unsupported constructs found");
    }

    @Test
    void generateReport_bothFormatsProduced() {
        MigrationReport report = service.generateReport(buildInput());
        assertThat(report.getMarkdownContent()).isNotBlank();
        assertThat(report.getHtmlContent()).isNotBlank();
        assertThat(report.getHtmlContent()).startsWith("<!DOCTYPE html>");
    }

    @Test
    void generateReport_noCredentialsInOutput() {
        MigrationReport report = service.generateReport(buildInput());
        assertThat(report.getMarkdownContent()).doesNotContainIgnoringCase("password");
        assertThat(report.getMarkdownContent()).doesNotContainIgnoringCase("jdbc:");
        assertThat(report.getHtmlContent()).doesNotContainIgnoringCase("password");
    }

    @Test
    void generateReport_schemaNameAndDatePresent() {
        MigrationReport report = service.generateReport(buildInput());
        assertThat(report.getMarkdownContent()).contains("MY_SCHEMA");
        assertThat(report.getMarkdownContent()).contains("Tool Version");
    }

    @Test
    void writeReport_writesBothFiles(@TempDir Path tempDir) throws IOException {
        MigrationReport report = service.generateReport(buildInput());
        service.writeReport(report, tempDir);
        assertThat(Files.exists(tempDir.resolve("report.md"))).isTrue();
        assertThat(Files.exists(tempDir.resolve("report.html"))).isTrue();
        assertThat(Files.size(tempDir.resolve("report.md"))).isGreaterThan(0);
        assertThat(Files.size(tempDir.resolve("report.html"))).isGreaterThan(0);
    }
}
