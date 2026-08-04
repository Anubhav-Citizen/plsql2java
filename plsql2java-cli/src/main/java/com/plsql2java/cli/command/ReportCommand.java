package com.plsql2java.cli.command;

import com.plsql2java.cli.ExitCode;
import com.plsql2java.cli.config.MigrationConfigMapper;
import com.plsql2java.cli.config.CliConfig;
import com.plsql2java.common.MigrationConfig;
import com.plsql2java.orchestration.MigrationOrchestratorService;
import com.plsql2java.orchestration.OrchestratorException;
import com.plsql2java.reporting.model.MigrationReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@Component
@Command(
    name = "report",
    description = "Regenerate the migration report from a previous analysis output directory.",
    mixinStandardHelpOptions = true
)
public class ReportCommand implements Callable<Integer> {

    private static final Logger log = LoggerFactory.getLogger(ReportCommand.class);

    private final MigrationOrchestratorService orchestrator;

    @Option(names = {"--output-dir", "-o"}, required = true,
            description = "Output directory containing prior migration results")
    private Path outputDir;

    @Option(names = {"--format"}, description = "Report format: md, html, or both (default: both)")
    private String format = "both";

    public ReportCommand(MigrationOrchestratorService orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Override
    public Integer call() {
        try {
            Integer validationError = validate();
            if (validationError != null) return validationError;

            CliConfig cliConfig = new CliConfig();
            cliConfig.setOutputDir(outputDir);
            MigrationConfig config = MigrationConfigMapper.toMigrationConfig(cliConfig);

            System.out.println("[INFO] Regenerating migration report...");
            MigrationReport report = orchestrator.report(config);

            Path reportsDir = outputDir.resolve("reports");
            System.out.printf("[INFO] Report written to: %s%n", reportsDir);
            if (report.getMarkdownContent() != null) {
                System.out.printf("[INFO]   Markdown: %s%n", reportsDir.resolve("migration-report.md"));
            }
            if (report.getHtmlContent() != null) {
                System.out.printf("[INFO]   HTML:     %s%n", reportsDir.resolve("migration-report.html"));
            }
            return ExitCode.SUCCESS.getCode();

        } catch (IllegalArgumentException e) {
            System.err.println("[ERROR] " + e.getMessage());
            log.debug("Validation error", e);
            return ExitCode.VALIDATION_ERROR.getCode();
        } catch (OrchestratorException e) {
            System.err.println("[ERROR] Report generation failed. Check logs for details.");
            log.error("Report execution error", e);
            return ExitCode.EXECUTION_ERROR.getCode();
        } catch (IOException e) {
            System.err.println("[ERROR] I/O error: " + e.getMessage());
            log.error("I/O error during report generation", e);
            return ExitCode.IO_ERROR.getCode();
        }
    }

    private Integer validate() throws IOException {
        // BR-CLI-04: output dir must exist
        if (!Files.isDirectory(outputDir)) {
            System.err.println("[ERROR] Output directory does not exist: " + outputDir);
            return ExitCode.IO_ERROR.getCode();
        }
        // BR-CLI-07: must contain prior migration result
        Path analysisDir = outputDir.resolve("analysis");
        if (!Files.isDirectory(analysisDir)) {
            System.err.println("[ERROR] No prior migration results found in: " + outputDir);
            System.err.println("[ERROR] Run 'generate' first to produce analysis output.");
            return ExitCode.VALIDATION_ERROR.getCode();
        }
        // Validate format option
        if (!format.equals("md") && !format.equals("html") && !format.equals("both")) {
            System.err.println("[ERROR] --format must be one of: md, html, both");
            return ExitCode.VALIDATION_ERROR.getCode();
        }
        return null;
    }
}
