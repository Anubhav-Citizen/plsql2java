package com.plsql2java.reporting;

import com.plsql2java.codegen.model.ArtifactType;
import com.plsql2java.codegen.model.JavaSourceFile;
import com.plsql2java.model.OracleObject;
import com.plsql2java.reporting.model.*;
import com.plsql2java.scoring.model.ObjectConfidenceScore;
import com.plsql2java.translation.model.ConstructType;
import com.plsql2java.translation.model.FlaggedConstruct;
import com.plsql2java.translation.model.TranslationResult;
import com.plsql2java.translation.model.TranslationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MigrationReportGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(MigrationReportGeneratorService.class);
    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .withZone(ZoneOffset.UTC);
    private static final String TOOL_VERSION = "1.0.0";

    public MigrationReport generateReport(ReportInput input) {
        MDC.put("migrationId", input.getMigrationConfig().getMigrationId());
        MDC.put("component", "MigrationReportGeneratorService");
        try {
            TraceabilityMatrix matrix = buildTraceabilityMatrix(input);
            FlaggedConstructsSummary flagged = buildFlaggedConstructsSummary(input.getTranslationResults());

            String markdown = renderMarkdown(input, matrix, flagged);
            String html = renderHtml(input, matrix, flagged);

            log.info("Report generated: coverage={:.1f}%, flagged={}", matrix.getCoveragePct(), flagged.getTotalCount());
            return new MigrationReport(
                    input.getMigrationConfig().getMigrationId(),
                    markdown, html,
                    input.getDiscoveryResult().getSchemaName());
        } finally {
            MDC.clear();
        }
    }

    public TraceabilityMatrix buildTraceabilityMatrix(ReportInput input) {
        List<TraceabilityEntry> entries = new ArrayList<>();
        Map<String, ObjectConfidenceScore> scoreMap = new HashMap<>();
        if (input.getConfidenceReport() != null) {
            input.getConfidenceReport().getObjectScores()
                    .forEach(s -> scoreMap.put(s.getObjectName(), s));
        }
        Map<String, TranslationResult> translationMap = new HashMap<>();
        input.getTranslationResults().forEach(r -> translationMap.put(r.getSourceObject().getName(), r));

        for (OracleObject obj : input.getDiscoveryResult().getObjects()) {
            TranslationResult tr = translationMap.get(obj.getName());
            ObjectConfidenceScore score = scoreMap.get(obj.getName());
            int confScore = score != null ? score.getScore() : 0;

            TraceabilityStatus status;
            String javaClass = null;
            List<String> methods = List.of();

            if (tr == null || tr.getJavaIR() == null) {
                status = TraceabilityStatus.SKIPPED;
            } else {
                javaClass = domainName(obj.getName()) + "Service";
                methods = tr.getJavaIR().getMethods().stream()
                        .map(m -> m.getMethodName()).collect(Collectors.toList());
                status = switch (tr.getOverallStatus()) {
                    case TRANSLATED -> TraceabilityStatus.MIGRATED;
                    case PARTIAL -> TraceabilityStatus.PARTIAL;
                    case FLAGGED -> TraceabilityStatus.FLAGGED;
                };
            }
            entries.add(new TraceabilityEntry(obj.getName(), obj.getType(), javaClass, methods, status, confScore));
        }
        return new TraceabilityMatrix(entries);
    }

    public FlaggedConstructsSummary buildFlaggedConstructsSummary(List<TranslationResult> results) {
        Map<ConstructType, List<FlaggedConstruct>> grouped = new EnumMap<>(ConstructType.class);
        for (TranslationResult r : results) {
            for (FlaggedConstruct fc : r.getFlaggedConstructs()) {
                grouped.computeIfAbsent(fc.getConstructType(), k -> new ArrayList<>()).add(fc);
            }
        }
        return new FlaggedConstructsSummary(grouped);
    }

    private String renderMarkdown(ReportInput input, TraceabilityMatrix matrix, FlaggedConstructsSummary flagged) {
        StringBuilder sb = new StringBuilder();
        sb.append(buildExecutiveSummaryMd(input, matrix, flagged));
        sb.append(buildTraceabilitySectionMd(matrix));
        sb.append(buildFlaggedConstructsSectionMd(flagged));
        sb.append(buildDependencyGraphSectionMd(input));
        sb.append(buildConfidenceScoresSectionMd(input));
        return sb.toString();
    }

    private String renderHtml(ReportInput input, TraceabilityMatrix matrix, FlaggedConstructsSummary flagged) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">");
        sb.append("<title>Migration Report — ").append(esc(input.getDiscoveryResult().getSchemaName())).append("</title>");
        sb.append("<style>");
        sb.append("body{font-family:Arial,sans-serif;margin:2rem;color:#222;}");
        sb.append("h1{color:#1a5276;}h2{color:#2874a6;border-bottom:1px solid #aed6f1;padding-bottom:4px;}");
        sb.append("table{border-collapse:collapse;width:100%;margin-bottom:1rem;}");
        sb.append("th,td{border:1px solid #ccc;padding:6px 10px;text-align:left;}");
        sb.append("th{background:#d6eaf8;}tr:nth-child(even){background:#f2f9ff;}");
        sb.append(".badge-migrated{color:#1e8449;}.badge-partial{color:#d68910;}");
        sb.append(".badge-flagged{color:#c0392b;}.badge-skipped{color:#7f8c8d;}");
        sb.append("</style></head><body>");
        sb.append(buildExecutiveSummaryHtml(input, matrix, flagged));
        sb.append(buildTraceabilitySectionHtml(matrix));
        sb.append(buildFlaggedConstructsSectionHtml(flagged));
        sb.append(buildDependencyGraphSectionHtml(input));
        sb.append(buildConfidenceScoresSectionHtml(input));
        sb.append("</body></html>");
        return sb.toString();
    }

    // --- Markdown section builders ---

    private String buildExecutiveSummaryMd(ReportInput input, TraceabilityMatrix matrix, FlaggedConstructsSummary flagged) {
        String schema = input.getDiscoveryResult().getSchemaName();
        String date = ISO_FMT.format(Instant.now());
        StringBuilder sb = new StringBuilder();
        sb.append("# Migration Report — ").append(schema).append("\n\n");
        sb.append("**Schema**: ").append(schema).append("  \n");
        sb.append("**Migration Date**: ").append(date).append("  \n");
        sb.append("**Tool Version**: ").append(TOOL_VERSION).append("  \n\n");
        sb.append("## Executive Summary\n\n");
        sb.append("| Metric | Value |\n|---|---|\n");
        sb.append("| Total Objects Discovered | ").append(input.getDiscoveryResult().getTotalObjectCount()).append(" |\n");
        sb.append("| Objects Translated | ").append(countByStatus(matrix, TraceabilityStatus.MIGRATED)).append(" |\n");
        sb.append("| Objects Partial | ").append(countByStatus(matrix, TraceabilityStatus.PARTIAL)).append(" |\n");
        sb.append("| Objects Flagged | ").append(countByStatus(matrix, TraceabilityStatus.FLAGGED)).append(" |\n");
        sb.append("| Objects Skipped | ").append(countByStatus(matrix, TraceabilityStatus.SKIPPED)).append(" |\n");
        sb.append("| Traceability Coverage | ").append(String.format("%.1f%%", matrix.getCoveragePct())).append(" |\n");
        sb.append("| Flagged Constructs | ").append(flagged.getTotalCount()).append(" |\n");
        if (input.getConfidenceReport() != null) {
            sb.append("| Overall Confidence Score | ").append(input.getConfidenceReport().getOverallScore()).append("% |\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    private String buildTraceabilitySectionMd(TraceabilityMatrix matrix) {
        StringBuilder sb = new StringBuilder("## Traceability Matrix\n\n");
        sb.append("| PL/SQL Object | Type | Java Class | Methods | Status | Confidence |\n");
        sb.append("|---|---|---|---|---|---|\n");
        for (TraceabilityEntry e : matrix.getEntries()) {
            sb.append("| ").append(e.getPlsqlObjectName())
              .append(" | ").append(e.getPlsqlObjectType())
              .append(" | ").append(e.getJavaClassName() != null ? e.getJavaClassName() : "-")
              .append(" | ").append(e.getJavaMethodNames().size())
              .append(" | ").append(e.getStatus())
              .append(" | ").append(e.getConfidenceScore()).append("% |\n");
        }
        sb.append("\n**Coverage**: ").append(String.format("%.1f%%", matrix.getCoveragePct())).append("\n\n");
        return sb.toString();
    }

    private String buildFlaggedConstructsSectionMd(FlaggedConstructsSummary flagged) {
        StringBuilder sb = new StringBuilder("## Flagged Constructs\n\n");
        if (flagged.getTotalCount() == 0) {
            sb.append("No unsupported constructs found.\n\n");
            return sb.toString();
        }
        for (Map.Entry<ConstructType, List<FlaggedConstruct>> entry : flagged.getByConstructType().entrySet()) {
            sb.append("### ").append(entry.getKey()).append("\n\n");
            sb.append("| Object | Line | Reason | Recommendation |\n|---|---|---|---|\n");
            for (FlaggedConstruct fc : entry.getValue()) {
                sb.append("| ").append(fc.getObjectName())
                  .append(" | ").append(fc.getLineNumber())
                  .append(" | ").append(fc.getReason())
                  .append(" | ").append(fc.getRecommendation()).append(" |\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private String buildDependencyGraphSectionMd(ReportInput input) {
        StringBuilder sb = new StringBuilder("## Dependency Graph Summary\n\n");
        var graph = input.getDependencyGraph();
        if (graph == null) { sb.append("No dependency graph available.\n\n"); return sb.toString(); }
        sb.append("- **Circular Dependencies**: ").append(graph.getCircularDependencies().size()).append("\n");
        sb.append("- **Leaf Objects**: ").append(graph.getLeafObjects().size()).append("\n");
        if (!graph.getMigrationOrder().isEmpty()) {
            sb.append("- **Migration Order (first 10)**: ");
            sb.append(graph.getMigrationOrder().stream().limit(10).collect(Collectors.joining(", ")));
            sb.append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    private String buildConfidenceScoresSectionMd(ReportInput input) {
        if (input.getConfidenceReport() == null) return "";
        StringBuilder sb = new StringBuilder("## Confidence Scores\n\n");
        sb.append("| Object | Type | Score | Status |\n|---|---|---|---|\n");
        for (ObjectConfidenceScore s : input.getConfidenceReport().getObjectScores()) {
            sb.append("| ").append(s.getObjectName())
              .append(" | ").append(s.getObjectType())
              .append(" | ").append(s.getScore()).append("%")
              .append(" | ").append(s.isBelowThreshold() ? "⚠ Below Threshold" : "✓ OK").append(" |\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    // --- HTML section builders ---

    private String buildExecutiveSummaryHtml(ReportInput input, TraceabilityMatrix matrix, FlaggedConstructsSummary flagged) {
        String schema = input.getDiscoveryResult().getSchemaName();
        String date = ISO_FMT.format(Instant.now());
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Migration Report &mdash; ").append(esc(schema)).append("</h1>");
        sb.append("<p><strong>Schema</strong>: ").append(esc(schema)).append("<br/>");
        sb.append("<strong>Migration Date</strong>: ").append(date).append("<br/>");
        sb.append("<strong>Tool Version</strong>: ").append(TOOL_VERSION).append("</p>");
        sb.append("<h2>Executive Summary</h2>");
        sb.append("<table><tr><th>Metric</th><th>Value</th></tr>");
        sb.append("<tr><td>Total Objects Discovered</td><td>").append(input.getDiscoveryResult().getTotalObjectCount()).append("</td></tr>");
        sb.append("<tr><td>Objects Translated</td><td>").append(countByStatus(matrix, TraceabilityStatus.MIGRATED)).append("</td></tr>");
        sb.append("<tr><td>Objects Partial</td><td>").append(countByStatus(matrix, TraceabilityStatus.PARTIAL)).append("</td></tr>");
        sb.append("<tr><td>Objects Flagged</td><td>").append(countByStatus(matrix, TraceabilityStatus.FLAGGED)).append("</td></tr>");
        sb.append("<tr><td>Objects Skipped</td><td>").append(countByStatus(matrix, TraceabilityStatus.SKIPPED)).append("</td></tr>");
        sb.append("<tr><td>Traceability Coverage</td><td>").append(String.format("%.1f%%", matrix.getCoveragePct())).append("</td></tr>");
        sb.append("<tr><td>Flagged Constructs</td><td>").append(flagged.getTotalCount()).append("</td></tr>");
        if (input.getConfidenceReport() != null) {
            sb.append("<tr><td>Overall Confidence Score</td><td>").append(input.getConfidenceReport().getOverallScore()).append("%</td></tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    private String buildTraceabilitySectionHtml(TraceabilityMatrix matrix) {
        StringBuilder sb = new StringBuilder("<h2>Traceability Matrix</h2>");
        sb.append("<table><tr><th>PL/SQL Object</th><th>Type</th><th>Java Class</th><th>Methods</th><th>Status</th><th>Confidence</th></tr>");
        for (TraceabilityEntry e : matrix.getEntries()) {
            String badgeClass = "badge-" + e.getStatus().name().toLowerCase();
            sb.append("<tr><td>").append(esc(e.getPlsqlObjectName()))
              .append("</td><td>").append(e.getPlsqlObjectType())
              .append("</td><td>").append(e.getJavaClassName() != null ? esc(e.getJavaClassName()) : "-")
              .append("</td><td>").append(e.getJavaMethodNames().size())
              .append("</td><td class=\"").append(badgeClass).append("\">").append(e.getStatus())
              .append("</td><td>").append(e.getConfidenceScore()).append("%</td></tr>");
        }
        sb.append("</table>");
        sb.append("<p><strong>Coverage</strong>: ").append(String.format("%.1f%%", matrix.getCoveragePct())).append("</p>");
        return sb.toString();
    }

    private String buildFlaggedConstructsSectionHtml(FlaggedConstructsSummary flagged) {
        StringBuilder sb = new StringBuilder("<h2>Flagged Constructs</h2>");
        if (flagged.getTotalCount() == 0) {
            sb.append("<p>No unsupported constructs found.</p>");
            return sb.toString();
        }
        for (Map.Entry<ConstructType, List<FlaggedConstruct>> entry : flagged.getByConstructType().entrySet()) {
            sb.append("<h3>").append(entry.getKey()).append("</h3>");
            sb.append("<table><tr><th>Object</th><th>Line</th><th>Reason</th><th>Recommendation</th></tr>");
            for (FlaggedConstruct fc : entry.getValue()) {
                sb.append("<tr><td>").append(esc(fc.getObjectName()))
                  .append("</td><td>").append(fc.getLineNumber())
                  .append("</td><td>").append(esc(fc.getReason()))
                  .append("</td><td>").append(esc(fc.getRecommendation())).append("</td></tr>");
            }
            sb.append("</table>");
        }
        return sb.toString();
    }

    private String buildDependencyGraphSectionHtml(ReportInput input) {
        StringBuilder sb = new StringBuilder("<h2>Dependency Graph Summary</h2>");
        var graph = input.getDependencyGraph();
        if (graph == null) { sb.append("<p>No dependency graph available.</p>"); return sb.toString(); }
        sb.append("<ul>");
        sb.append("<li><strong>Circular Dependencies</strong>: ").append(graph.getCircularDependencies().size()).append("</li>");
        sb.append("<li><strong>Leaf Objects</strong>: ").append(graph.getLeafObjects().size()).append("</li>");
        if (!graph.getMigrationOrder().isEmpty()) {
            sb.append("<li><strong>Migration Order (first 10)</strong>: ")
              .append(esc(graph.getMigrationOrder().stream().limit(10).collect(Collectors.joining(", "))))
              .append("</li>");
        }
        sb.append("</ul>");
        return sb.toString();
    }

    private String buildConfidenceScoresSectionHtml(ReportInput input) {
        if (input.getConfidenceReport() == null) return "";
        StringBuilder sb = new StringBuilder("<h2>Confidence Scores</h2>");
        sb.append("<table><tr><th>Object</th><th>Type</th><th>Score</th><th>Status</th></tr>");
        for (ObjectConfidenceScore s : input.getConfidenceReport().getObjectScores()) {
            sb.append("<tr><td>").append(esc(s.getObjectName()))
              .append("</td><td>").append(s.getObjectType())
              .append("</td><td>").append(s.getScore()).append("%")
              .append("</td><td>").append(s.isBelowThreshold() ? "&#9888; Below Threshold" : "&#10003; OK")
              .append("</td></tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    public void writeReport(MigrationReport report, Path outputDir) throws IOException {
        Files.createDirectories(outputDir);
        Path mdFile = outputDir.resolve("report.md");
        Path htmlFile = outputDir.resolve("report.html");
        try (var w = Files.newBufferedWriter(mdFile, StandardCharsets.UTF_8)) {
            w.write(report.getMarkdownContent());
        }
        try (var w = Files.newBufferedWriter(htmlFile, StandardCharsets.UTF_8)) {
            w.write(report.getHtmlContent());
        }
        log.info("Report written to {}", outputDir);
    }

    private long countByStatus(TraceabilityMatrix matrix, TraceabilityStatus status) {
        return matrix.getEntries().stream().filter(e -> e.getStatus() == status).count();
    }

    private static final List<String> STRIP_PREFIXES = List.of("PKG_", "TRG_", "SEQ_", "SP_", "FN_", "VW_");

    private static String domainName(String oracleName) {
        if (oracleName == null || oracleName.isBlank()) return "Unknown";
        String upper = oracleName.toUpperCase();
        for (String prefix : STRIP_PREFIXES) {
            if (upper.startsWith(prefix)) { oracleName = oracleName.substring(prefix.length()); break; }
        }
        StringBuilder sb = new StringBuilder();
        boolean cap = true;
        for (char c : oracleName.toCharArray()) {
            if (c == '_') { cap = true; }
            else if (cap) { sb.append(Character.toUpperCase(c)); cap = false; }
            else { sb.append(Character.toLowerCase(c)); }
        }
        return sb.toString();
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
