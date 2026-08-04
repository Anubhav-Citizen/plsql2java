package com.plsql2java.orchestration;

import com.plsql2java.codegen.JavaCodeGeneratorService;
import com.plsql2java.codegen.model.GeneratedProject;
import com.plsql2java.codegen.model.GenerationContext;
import com.plsql2java.common.MigrationConfig;
import com.plsql2java.dependency.DependencyAnalyzerService;
import com.plsql2java.discovery.OracleDiscoveryService;
import com.plsql2java.model.DependencyGraph;
import com.plsql2java.model.DiscoveryResult;
import com.plsql2java.model.OracleObject;
import com.plsql2java.model.OracleObjectType;
import com.plsql2java.orchestration.event.ProgressEventBus;
import com.plsql2java.orchestration.model.*;
import com.plsql2java.reporting.MigrationReportGeneratorService;
import com.plsql2java.reporting.model.MigrationReport;
import com.plsql2java.reporting.model.ReportInput;
import com.plsql2java.scoring.ConfidenceScorerService;
import com.plsql2java.scoring.model.ConfidenceReport;
import com.plsql2java.translation.engine.PlSqlTranslationEngine;
import com.plsql2java.translation.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MigrationOrchestratorServiceTest {

    @Mock private OracleDiscoveryService discoveryService;
    @Mock private DependencyAnalyzerService dependencyAnalyzerService;
    @Mock private PlSqlTranslationEngine translationEngine;
    @Mock private JavaCodeGeneratorService codeGeneratorService;
    @Mock private ConfidenceScorerService confidenceScorerService;
    @Mock private MigrationReportGeneratorService reportGeneratorService;

    private ProgressEventBus eventBus;
    private MigrationOrchestratorService orchestrator;

    @TempDir Path tempDir;

    @BeforeEach
    void setUp() {
        eventBus = new ProgressEventBus();
        orchestrator = new MigrationOrchestratorService(
                discoveryService, dependencyAnalyzerService, translationEngine,
                codeGeneratorService, confidenceScorerService, reportGeneratorService, eventBus);
    }

    private MigrationConfig config() {
        MigrationConfig c = new MigrationConfig();
        c.setMigrationId("m1");
        c.setSchemaName("MY_SCHEMA");
        c.setOutputDir(tempDir);
        c.setDdlFiles(List.of());
        return c;
    }

    private DiscoveryResult sampleDiscovery() {
        DiscoveryResult d = new DiscoveryResult("m1", "MY_SCHEMA", null);
        OracleObject obj = new OracleObject("ORDER_PROC", OracleObjectType.PROCEDURE, "MY_SCHEMA", "source");
        d.setObjects(List.of(obj));
        return d;
    }

    private DependencyGraph sampleGraph() {
        DependencyGraph g = new DependencyGraph("m1");
        g.setMigrationOrder(List.of("ORDER_PROC"));
        return g;
    }

    private TranslationResult sampleTranslation(OracleObject obj) {
        JavaMethodIR method = new JavaMethodIR("processOrder", "void", List.of(), "// body", List.of(), null, List.of());
        JavaIR ir = new JavaIR("ORDER_PROC", "com.example", "OrderProc", List.of(), List.of(), List.of(method), "");
        return new TranslationResult(obj, ir, List.of(), List.of());
    }

    @Test
    void analyze_callsDiscoveryAndDependencyAnalysis() {
        MigrationConfig c = config();
        DiscoveryResult discovery = sampleDiscovery();
        DependencyGraph graph = sampleGraph();

        when(discoveryService.discoverFromFiles(any(), any(), any(), any())).thenReturn(discovery);
        when(dependencyAnalyzerService.analyze(discovery)).thenReturn(graph);

        AnalysisResult result = orchestrator.analyze(c);

        assertThat(result.getDiscoveryResult()).isEqualTo(discovery);
        assertThat(result.getDependencyGraph()).isEqualTo(graph);
        verify(discoveryService).discoverFromFiles(any(), eq("m1"), eq("MY_SCHEMA"), any());
        verify(dependencyAnalyzerService).analyze(discovery);
    }

    @Test
    void generate_callsFullPipelineInOrder() throws IOException {
        MigrationConfig c = config();
        DiscoveryResult discovery = sampleDiscovery();
        DependencyGraph graph = sampleGraph();
        OracleObject obj = discovery.getObjects().get(0);
        TranslationResult tr = sampleTranslation(obj);
        GeneratedProject project = new GeneratedProject("m1", "my-schema");
        ConfidenceReport confReport = new ConfidenceReport("m1", 70, List.of(), 90);
        MigrationReport report = new MigrationReport("m1", "# Report", "<html/>", "MY_SCHEMA");

        when(discoveryService.discoverFromFiles(any(), any(), any(), any())).thenReturn(discovery);
        when(dependencyAnalyzerService.analyze(any())).thenReturn(graph);
        when(translationEngine.translate(any(), any())).thenReturn(tr);
        when(codeGeneratorService.generateProject(any(), any())).thenReturn(project);
        when(confidenceScorerService.scoreAll(any(), anyInt())).thenReturn(confReport);
        when(reportGeneratorService.generateReport(any())).thenReturn(report);

        MigrationResult result = orchestrator.generate(c);

        assertThat(result.getMigrationId()).isEqualTo("m1");
        assertThat(result.getTranslationResults()).hasSize(1);
        assertThat(result.getGeneratedProject()).isEqualTo(project);
        assertThat(result.getConfidenceReport()).isEqualTo(confReport);
        assertThat(result.getMigrationReport()).isEqualTo(report);

        InOrder inOrder = inOrder(discoveryService, dependencyAnalyzerService, translationEngine,
                codeGeneratorService, confidenceScorerService, reportGeneratorService);
        inOrder.verify(discoveryService).discoverFromFiles(any(), any(), any(), any());
        inOrder.verify(dependencyAnalyzerService).analyze(any());
        inOrder.verify(translationEngine).translate(any(), any());
        inOrder.verify(codeGeneratorService).generateProject(any(), any());
        inOrder.verify(confidenceScorerService).scoreAll(any(), anyInt());
        inOrder.verify(reportGeneratorService).generateReport(any());
    }

    @Test
    void generate_failPartial_skipsFailedObjects() throws IOException {
        MigrationConfig c = config();
        DiscoveryResult discovery = new DiscoveryResult("m1", "MY_SCHEMA", null);
        OracleObject good = new OracleObject("GOOD", OracleObjectType.PROCEDURE, "MY_SCHEMA", "src");
        OracleObject bad = new OracleObject("BAD", OracleObjectType.PROCEDURE, "MY_SCHEMA", "src");
        discovery.setObjects(List.of(good, bad));
        DependencyGraph graph = new DependencyGraph("m1");
        TranslationResult tr = sampleTranslation(good);
        GeneratedProject project = new GeneratedProject("m1", "my-schema");
        ConfidenceReport confReport = new ConfidenceReport("m1", 70, List.of(), 80);
        MigrationReport report = new MigrationReport("m1", "# Report", "<html/>", "MY_SCHEMA");

        when(discoveryService.discoverFromFiles(any(), any(), any(), any())).thenReturn(discovery);
        when(dependencyAnalyzerService.analyze(any())).thenReturn(graph);
        when(translationEngine.translate(eq(good), any())).thenReturn(tr);
        when(translationEngine.translate(eq(bad), any())).thenThrow(new RuntimeException("parse error"));
        when(codeGeneratorService.generateProject(any(), any())).thenReturn(project);
        when(confidenceScorerService.scoreAll(any(), anyInt())).thenReturn(confReport);
        when(reportGeneratorService.generateReport(any())).thenReturn(report);

        MigrationResult result = orchestrator.generate(c);

        assertThat(result.isPartial()).isTrue();
        assertThat(result.getSkippedObjects()).contains("BAD");
        assertThat(result.getTranslationResults()).hasSize(1);
    }

    @Test
    void generate_progressEventsEmittedAtEachStage() throws IOException {
        MigrationConfig c = config();
        DiscoveryResult discovery = sampleDiscovery();
        DependencyGraph graph = sampleGraph();
        TranslationResult tr = sampleTranslation(discovery.getObjects().get(0));
        GeneratedProject project = new GeneratedProject("m1", "my-schema");
        ConfidenceReport confReport = new ConfidenceReport("m1", 70, List.of(), 90);
        MigrationReport report = new MigrationReport("m1", "# Report", "<html/>", "MY_SCHEMA");

        when(discoveryService.discoverFromFiles(any(), any(), any(), any())).thenReturn(discovery);
        when(dependencyAnalyzerService.analyze(any())).thenReturn(graph);
        when(translationEngine.translate(any(), any())).thenReturn(tr);
        when(codeGeneratorService.generateProject(any(), any())).thenReturn(project);
        when(confidenceScorerService.scoreAll(any(), anyInt())).thenReturn(confReport);
        when(reportGeneratorService.generateReport(any())).thenReturn(report);

        List<PipelineStage> stages = new java.util.ArrayList<>();
        eventBus.register(p -> stages.add(p.getStage()));

        orchestrator.generate(c);

        assertThat(stages).contains(
                PipelineStage.DISCOVERY,
                PipelineStage.TRANSLATION,
                PipelineStage.CODE_GENERATION,
                PipelineStage.CONFIDENCE_SCORING,
                PipelineStage.REPORT_GENERATION,
                PipelineStage.COMPLETE);
    }

    @Test
    void analyze_throwsOrchestratorException_onDiscoveryFailure() {
        MigrationConfig c = config();
        when(discoveryService.discoverFromFiles(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("connection refused"));

        assertThatThrownBy(() -> orchestrator.analyze(c))
                .isInstanceOf(OrchestratorException.class)
                .hasMessageContaining("Analysis failed");
    }
}
