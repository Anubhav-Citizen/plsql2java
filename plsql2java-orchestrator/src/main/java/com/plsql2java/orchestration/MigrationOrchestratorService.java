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
import com.plsql2java.orchestration.event.ProgressEventBus;
import com.plsql2java.orchestration.model.*;
import com.plsql2java.reporting.MigrationReportGeneratorService;
import com.plsql2java.reporting.model.MigrationReport;
import com.plsql2java.reporting.model.ReportInput;
import com.plsql2java.scoring.ConfidenceScorerService;
import com.plsql2java.scoring.model.ConfidenceReport;
import com.plsql2java.translation.engine.PlSqlTranslationEngine;
import com.plsql2java.translation.model.TranslationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class MigrationOrchestratorService {

    private static final Logger log = LoggerFactory.getLogger(MigrationOrchestratorService.class);

    private final OracleDiscoveryService discoveryService;
    private final DependencyAnalyzerService dependencyAnalyzerService;
    private final PlSqlTranslationEngine translationEngine;
    private final JavaCodeGeneratorService codeGeneratorService;
    private final ConfidenceScorerService confidenceScorerService;
    private final MigrationReportGeneratorService reportGeneratorService;
    private final ProgressEventBus eventBus;

    public MigrationOrchestratorService(OracleDiscoveryService discoveryService,
                                         DependencyAnalyzerService dependencyAnalyzerService,
                                         PlSqlTranslationEngine translationEngine,
                                         JavaCodeGeneratorService codeGeneratorService,
                                         ConfidenceScorerService confidenceScorerService,
                                         MigrationReportGeneratorService reportGeneratorService,
                                         ProgressEventBus eventBus) {
        this.discoveryService = discoveryService;
        this.dependencyAnalyzerService = dependencyAnalyzerService;
        this.translationEngine = translationEngine;
        this.codeGeneratorService = codeGeneratorService;
        this.confidenceScorerService = confidenceScorerService;
        this.reportGeneratorService = reportGeneratorService;
        this.eventBus = eventBus;
    }

    public AnalysisResult analyze(MigrationConfig config) {
        MDC.put("migrationId", config.getMigrationId());
        MDC.put("mode", "ANALYZE");
        MigrationJob job = new MigrationJob(config.getMigrationId(), OperationMode.ANALYZE);
        try {
            validateOutputDir(config.getOutputDir());

            emit(config.getMigrationId(), PipelineStage.DISCOVERY, 5, "Starting discovery");
            DiscoveryResult discovery = runDiscovery(config);
            emit(config.getMigrationId(), PipelineStage.DISCOVERY, 40, "Discovery complete: " + discovery.getTotalObjectCount() + " objects");

            emit(config.getMigrationId(), PipelineStage.DEPENDENCY_ANALYSIS, 50, "Analysing dependencies");
            DependencyGraph graph = dependencyAnalyzerService.analyze(discovery);
            emit(config.getMigrationId(), PipelineStage.DEPENDENCY_ANALYSIS, 90, "Dependency analysis complete");

            Path analysisDir = config.getOutputDir().resolve("analysis");
            discoveryService.persist(discovery, analysisDir);

            job.complete();
            log.info("Analysis complete: {} objects, {} edges", discovery.getTotalObjectCount(), graph.getEdges().size());
            return new AnalysisResult(config.getMigrationId(), discovery, graph);

        } catch (Exception e) {
            job.fail(e.getMessage());
            log.error("Analysis failed: {}", e.getMessage(), e);
            throw new OrchestratorException("Analysis failed: " + e.getMessage(), e);
        } finally {
            MDC.clear();
        }
    }

    public MigrationResult generate(MigrationConfig config) {
        MDC.put("migrationId", config.getMigrationId());
        MDC.put("mode", "GENERATE");
        MigrationJob job = new MigrationJob(config.getMigrationId(), OperationMode.GENERATE);
        List<String> skippedObjects = new ArrayList<>();
        try {
            validateOutputDir(config.getOutputDir());

            AnalysisResult analysis = analyze(config);
            DependencyGraph graph = analysis.getDependencyGraph();
            DiscoveryResult discovery = analysis.getDiscoveryResult();

            // Determine processing order
            List<OracleObject> orderedObjects = resolveProcessingOrder(discovery, graph);

            // Translation — fail-partial
            // Translation occupies 20%-65% of overall progress
            emit(config.getMigrationId(), PipelineStage.TRANSLATION, 20, "Translating " + orderedObjects.size() + " objects");
            List<TranslationResult> translationResults = new ArrayList<>();
            int total = orderedObjects.size();
            for (int i = 0; i < total; i++) {
                OracleObject obj = orderedObjects.get(i);
                MDC.put("objectName", obj.getName());
                try {
                    translationResults.add(translationEngine.translate(obj, config.getTargetPackage() != null ? config.getTargetPackage() : "com.example"));
                    int translationPct = total > 0 ? 20 + ((i + 1) * 45 / total) : 65;
                    eventBus.emit(new MigrationProgress(config.getMigrationId(), PipelineStage.TRANSLATION,
                            obj.getName(), i + 1, total, translationPct, "Translated: " + obj.getName()));
                } catch (Exception e) {
                    log.warn("Skipping {} — translation error: {}", obj.getName(), e.getMessage());
                    skippedObjects.add(obj.getName());
                } finally {
                    MDC.remove("objectName");
                }
            }

            // Code generation
            emit(config.getMigrationId(), PipelineStage.CODE_GENERATION, 68, "Generating Java project");
            GenerationContext ctx = buildGenerationContext(config);
            GeneratedProject generatedProject = codeGeneratorService.generateProject(translationResults, ctx);
            skippedObjects.addAll(generatedProject.getSkippedObjects());

            // Write project files
            Path projectDir = config.getOutputDir().resolve("generated");
            codeGeneratorService.writeProject(generatedProject, projectDir);
            emit(config.getMigrationId(), PipelineStage.CODE_GENERATION, 78, "Java project written");

            // Confidence scoring
            emit(config.getMigrationId(), PipelineStage.CONFIDENCE_SCORING, 83, "Scoring confidence");
            ConfidenceReport confidenceReport = confidenceScorerService.scoreAll(translationResults, config.getConfidenceThreshold());

            // Report generation
            emit(config.getMigrationId(), PipelineStage.REPORT_GENERATION, 90, "Generating migration report");
            ReportInput reportInput = new ReportInput(discovery, graph, translationResults,
                    generatedProject, confidenceReport, config);
            MigrationReport migrationReport = reportGeneratorService.generateReport(reportInput);
            reportGeneratorService.writeReport(migrationReport, config.getOutputDir().resolve("reports"));

            emit(config.getMigrationId(), PipelineStage.COMPLETE, 99, "Migration complete");

            MigrationResult result = new MigrationResult(config.getMigrationId(), analysis,
                    translationResults, generatedProject, confidenceReport, migrationReport, skippedObjects);

            if (result.isPartial()) {
                job.completePartial();
                log.warn("Migration completed with {} skipped objects", skippedObjects.size());
            } else {
                job.complete();
                log.info("Migration completed successfully");
            }
            return result;

        } catch (OrchestratorException e) {
            job.fail(e.getMessage());
            throw e;
        } catch (Exception e) {
            job.fail(e.getMessage());
            log.error("Generation failed: {}", e.getMessage(), e);
            throw new OrchestratorException("Generation failed: " + e.getMessage(), e);
        } finally {
            MDC.clear();
        }
    }

    public MigrationReport report(MigrationConfig config) {
        MDC.put("migrationId", config.getMigrationId());
        MDC.put("mode", "REPORT");
        MigrationJob job = new MigrationJob(config.getMigrationId(), OperationMode.REPORT);
        try {
            validateOutputDir(config.getOutputDir());

            emit(config.getMigrationId(), PipelineStage.REPORT_GENERATION, 10, "Loading persisted analysis");
            DiscoveryResult discovery = discoveryService.load(config.getOutputDir().resolve("analysis"));
            DependencyGraph graph = dependencyAnalyzerService.analyze(discovery);

            ReportInput reportInput = new ReportInput(discovery, graph, List.of(), null, null, config);

            emit(config.getMigrationId(), PipelineStage.REPORT_GENERATION, 70, "Generating report");
            MigrationReport migrationReport = reportGeneratorService.generateReport(reportInput);
            reportGeneratorService.writeReport(migrationReport, config.getOutputDir().resolve("reports"));

            emit(config.getMigrationId(), PipelineStage.COMPLETE, 99, "Report complete");
            job.complete();
            log.info("Report generated for migration {}", config.getMigrationId());
            return migrationReport;

        } catch (Exception e) {
            job.fail(e.getMessage());
            log.error("Report generation failed: {}", e.getMessage(), e);
            throw new OrchestratorException("Report generation failed: " + e.getMessage(), e);
        } finally {
            MDC.clear();
        }
    }

    private DiscoveryResult runDiscovery(MigrationConfig config) {
        if (config.isJdbcMode()) {
            return discoveryService.discoverFromJdbc(config.getJdbcConfig(), config.getMigrationId(),
                    event -> {
                        int pct = event.getTotalCount() > 0
                                ? 5 + (event.getProcessedCount() * 35 / event.getTotalCount()) : 5;
                        eventBus.emit(new MigrationProgress(config.getMigrationId(),
                                PipelineStage.DISCOVERY, event.getObjectName(),
                                event.getProcessedCount(), event.getTotalCount(), pct, event.getMessage()));
                    });
        }
        return discoveryService.discoverFromFiles(config.getDdlFiles(), config.getMigrationId(),
                config.getSchemaName(),
                event -> {
                    int pct = event.getTotalCount() > 0
                            ? 5 + (event.getProcessedCount() * 35 / event.getTotalCount()) : 5;
                    eventBus.emit(new MigrationProgress(config.getMigrationId(),
                            PipelineStage.DISCOVERY, event.getObjectName(),
                            event.getProcessedCount(), event.getTotalCount(), pct, event.getMessage()));
                });
    }

    private List<OracleObject> resolveProcessingOrder(DiscoveryResult discovery, DependencyGraph graph) {
        List<String> order = graph.getMigrationOrder();
        if (order == null || order.isEmpty()) {
            return discovery.getObjects();
        }
        List<OracleObject> ordered = new ArrayList<>();
        java.util.Map<String, OracleObject> byName = new java.util.LinkedHashMap<>();
        discovery.getObjects().forEach(o -> byName.put(o.getName(), o));
        order.forEach(name -> { if (byName.containsKey(name)) ordered.add(byName.get(name)); });
        // Add any objects not in migration order (e.g. circular deps)
        discovery.getObjects().stream()
                .filter(o -> !order.contains(o.getName()))
                .forEach(ordered::add);
        return ordered;
    }

    private GenerationContext buildGenerationContext(MigrationConfig config) {
        String targetPackage = config.getTargetPackage() != null ? config.getTargetPackage() : "com.example";
        String rawSource = "";
        if (config.getDdlFiles() != null && !config.getDdlFiles().isEmpty()) {
            rawSource = config.getDdlFiles().stream()
                    .map(p -> { try { return java.nio.file.Files.readString(p); } catch (Exception e) { return ""; } })
                    .collect(java.util.stream.Collectors.joining("\n"));
        }
        return new GenerationContext(
                config.getMigrationId(),
                targetPackage,
                "3.2.5",
                config.getOutputDir().resolve("generated"),
                "org.postgresql.Driver",
                config.getConfidenceThreshold(),
                config.getSchemaName() != null ? config.getSchemaName() : "SCHEMA",
                rawSource
        );
    }

    private void emit(String migrationId, PipelineStage stage, int pct, String message) {
        eventBus.emit(new MigrationProgress(migrationId, stage, null, 0, 0, pct, message));
        log.info("[{}] {}%: {}", stage, pct, message);
    }

    private void validateOutputDir(Path outputDir) throws IOException {
        if (outputDir == null) {
            throw new OrchestratorException("outputDir must not be null");
        }
        Files.createDirectories(outputDir);
    }
}
