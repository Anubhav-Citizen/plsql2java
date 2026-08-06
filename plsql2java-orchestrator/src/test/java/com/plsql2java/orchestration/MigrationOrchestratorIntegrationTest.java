package com.plsql2java.orchestration;

import com.plsql2java.codegen.JavaCodeGeneratorService;
import com.plsql2java.common.MigrationConfig;
import com.plsql2java.dependency.DependencyAnalyzerService;
import com.plsql2java.dependency.CycleDetector;
import com.plsql2java.dependency.DependencyGraphBuilder;
import com.plsql2java.dependency.TopologicalSorter;
import com.plsql2java.discovery.OracleDiscoveryService;
import com.plsql2java.discovery.OracleObjectNormalizer;
import com.plsql2java.discovery.ResultPersistenceService;
import com.plsql2java.orchestration.event.ProgressEventBus;
import com.plsql2java.orchestration.model.AnalysisResult;
import com.plsql2java.orchestration.model.MigrationResult;
import com.plsql2java.reporting.MigrationReportGeneratorService;
import com.plsql2java.scoring.ConfidenceScorerService;
import com.plsql2java.translation.engine.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MigrationOrchestratorIntegrationTest {

    private MigrationOrchestratorService orchestrator;

    @TempDir Path tempDir;
    @TempDir Path ddlDir;

    @BeforeEach
    void setUp() throws IOException {
        // Wire real services
        ResultPersistenceService persistence = new ResultPersistenceService();
        OracleObjectNormalizer normalizer = new OracleObjectNormalizer();
        OracleDiscoveryService discoveryService = new OracleDiscoveryService(
                null, null,
                new com.plsql2java.discovery.file.DdlFileParser(),
                normalizer, persistence);

        DependencyAnalyzerService dependencyService = new DependencyAnalyzerService(
                new DependencyGraphBuilder(), new CycleDetector(), new TopologicalSorter());

        TranslationRuleRegistry registry = new TranslationRuleRegistry();
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        TranslationMappingLoader mappingLoader = new TranslationMappingLoader(objectMapper);
        mappingLoader.load();
        new TranslationRuleRegistryInitializer(registry, new java.util.ArrayList<>()).init();
        JavaIRAssembler assembler = new JavaIRAssembler();
        com.plsql2java.translation.engine.PlSqlTranslationEngine translationEngine =
                new com.plsql2java.translation.engine.PlSqlTranslationEngine(registry, assembler);

        JavaCodeGeneratorService codeGenerator = new JavaCodeGeneratorService();
        ConfidenceScorerService scorer = new ConfidenceScorerService();
        MigrationReportGeneratorService reportGenerator = new MigrationReportGeneratorService();
        ProgressEventBus eventBus = new ProgressEventBus();

        orchestrator = new MigrationOrchestratorService(
                discoveryService, dependencyService, translationEngine,
                codeGenerator, scorer, reportGenerator, eventBus);
    }

    private Path writeSampleDdl() throws IOException {
        Path ddlFile = ddlDir.resolve("sample.sql");
        Files.writeString(ddlFile,
                "CREATE OR REPLACE PROCEDURE order_proc AS\n" +
                "BEGIN\n" +
                "  DBMS_OUTPUT.PUT_LINE('Processing order');\n" +
                "END order_proc;\n/\n");
        return ddlFile;
    }

    @Test
    void analyze_producesAnalysisResult() throws IOException {
        MigrationConfig config = new MigrationConfig();
        config.setMigrationId("integration-test");
        config.setSchemaName("TEST_SCHEMA");
        config.setOutputDir(tempDir);
        config.setDdlFiles(List.of(writeSampleDdl()));

        AnalysisResult result = orchestrator.analyze(config);

        assertThat(result).isNotNull();
        assertThat(result.getMigrationId()).isEqualTo("integration-test");
        assertThat(result.getDiscoveryResult()).isNotNull();
        assertThat(result.getDependencyGraph()).isNotNull();
    }

    @Test
    void generate_producesMigrationResultWithAllFieldsPopulated() throws IOException {
        MigrationConfig config = new MigrationConfig();
        config.setMigrationId("integration-test");
        config.setSchemaName("TEST_SCHEMA");
        config.setOutputDir(tempDir);
        config.setDdlFiles(List.of(writeSampleDdl()));

        MigrationResult result = orchestrator.generate(config);

        assertThat(result).isNotNull();
        assertThat(result.getAnalysisResult()).isNotNull();
        assertThat(result.getTranslationResults()).isNotNull();
        assertThat(result.getGeneratedProject()).isNotNull();
        assertThat(result.getConfidenceReport()).isNotNull();
        assertThat(result.getMigrationReport()).isNotNull();
        assertThat(result.getMigrationReport().getMarkdownContent()).isNotBlank();
        assertThat(result.getMigrationReport().getHtmlContent()).startsWith("<!DOCTYPE html>");
    }
}
