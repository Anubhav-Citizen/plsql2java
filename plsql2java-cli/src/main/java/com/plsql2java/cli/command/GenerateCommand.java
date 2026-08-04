package com.plsql2java.cli.command;

import com.plsql2java.cli.ExitCode;
import com.plsql2java.cli.config.CliConfig;
import com.plsql2java.cli.config.ConfigLoader;
import com.plsql2java.cli.config.MigrationConfigMapper;
import com.plsql2java.cli.progress.CliProgressListener;
import com.plsql2java.common.MigrationConfig;
import com.plsql2java.orchestration.MigrationOrchestratorService;
import com.plsql2java.orchestration.OrchestratorException;
import com.plsql2java.orchestration.event.ProgressEventBus;
import com.plsql2java.orchestration.model.MigrationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Component
@Command(
    name = "generate",
    description = "Run the full migration pipeline: discover, translate, generate Java code, and produce a report.",
    mixinStandardHelpOptions = true
)
public class GenerateCommand implements Callable<Integer> {

    private static final Logger log = LoggerFactory.getLogger(GenerateCommand.class);

    private final MigrationOrchestratorService orchestrator;
    private final ConfigLoader configLoader;
    private final ProgressEventBus eventBus;

    @Option(names = {"--config", "-c"}, description = "Path to YAML config file")
    private Path configFile;

    @Option(names = {"--jdbc-url"}, description = "Oracle JDBC URL")
    private String jdbcUrl;

    @Option(names = {"--jdbc-user"}, description = "Oracle username")
    private String jdbcUser;

    @Option(names = {"--ddl-file"}, description = "DDL file(s) for offline mode", arity = "0..*")
    private List<Path> ddlFiles = new ArrayList<>();

    @Option(names = {"--output-dir", "-o"}, description = "Output directory (default: ./plsql2java-output)")
    private Path outputDir = Path.of("plsql2java-output");

    @Option(names = {"--target-package", "-p"}, description = "Java package for generated code (e.g. com.example.migrated)")
    private String targetPackage;

    @Option(names = {"--confidence-threshold"}, description = "Minimum confidence score 0.0-1.0 (default: 0.7)")
    private double confidenceThreshold = 0.7;

    @Option(names = {"--object-types"}, description = "Filter by object type (e.g. PROCEDURE,FUNCTION)", split = ",")
    private List<String> objectTypes = new ArrayList<>();

    @Option(names = {"--verbose", "-v"}, description = "Enable verbose progress output")
    private boolean verbose;

    public GenerateCommand(MigrationOrchestratorService orchestrator,
                           ConfigLoader configLoader,
                           ProgressEventBus eventBus) {
        this.orchestrator = orchestrator;
        this.configLoader = configLoader;
        this.eventBus = eventBus;
    }

    @Override
    public Integer call() {
        try {
            CliConfig cliConfig = buildCliConfig();
            CliConfig merged = mergeWithFileConfig(cliConfig);

            Integer validationError = validate(merged);
            if (validationError != null) return validationError;

            MigrationConfig config = MigrationConfigMapper.toMigrationConfig(merged);

            CliProgressListener listener = new CliProgressListener(System.out, verbose);
            eventBus.register(listener);
            try {
                System.out.println("[INFO] Starting migration pipeline...");
                MigrationResult result = orchestrator.generate(config);
                printGenerateSummary(result, config);
                return result.isPartial()
                        ? ExitCode.PARTIAL_SUCCESS.getCode()
                        : ExitCode.SUCCESS.getCode();
            } finally {
                eventBus.unregister(listener);
            }

        } catch (IllegalArgumentException e) {
            System.err.println("[ERROR] " + e.getMessage());
            log.debug("Validation error", e);
            return ExitCode.VALIDATION_ERROR.getCode();
        } catch (OrchestratorException e) {
            System.err.println("[ERROR] Migration failed. Check logs for details.");
            log.error("Generation execution error", e);
            return ExitCode.EXECUTION_ERROR.getCode();
        } catch (IOException e) {
            System.err.println("[ERROR] I/O error: " + e.getMessage());
            log.error("I/O error during generation", e);
            return ExitCode.IO_ERROR.getCode();
        }
    }

    private CliConfig buildCliConfig() {
        CliConfig c = new CliConfig();
        c.setConfigFile(configFile);
        c.setJdbcUrl(jdbcUrl);
        c.setJdbcUser(jdbcUser);
        c.setDdlFiles(ddlFiles);
        c.setOutputDir(outputDir);
        c.setTargetPackage(targetPackage);
        c.setConfidenceThreshold(confidenceThreshold);
        c.setObjectTypes(objectTypes);
        c.setVerbose(verbose);
        return c;
    }

    private CliConfig mergeWithFileConfig(CliConfig cliConfig) throws IOException {
        if (configFile != null) {
            CliConfig fileConfig = configLoader.load(configFile);
            return configLoader.merge(fileConfig, cliConfig);
        }
        return cliConfig;
    }

    private Integer validate(CliConfig config) throws IOException {
        // BR-CLI-01: at least one source
        if ((config.getJdbcUrl() == null || config.getJdbcUrl().isBlank()) && config.getDdlFiles().isEmpty()) {
            System.err.println("[ERROR] At least one source is required: --jdbc-url or --ddl-file");
            return ExitCode.VALIDATION_ERROR.getCode();
        }
        // BR-CLI-02: JDBC mode requires password
        if (config.getJdbcUrl() != null && !config.getJdbcUrl().isBlank()) {
            if (config.getJdbcPassword() == null || config.getJdbcPassword().length == 0) {
                System.err.println("[ERROR] JDBC password required. Set PLSQL2JAVA_JDBC_PASSWORD environment variable.");
                return ExitCode.VALIDATION_ERROR.getCode();
            }
        }
        // BR-CLI-03: target package
        if (config.getTargetPackage() != null && !MigrationConfigMapper.isValidPackage(config.getTargetPackage())) {
            System.err.println("[ERROR] Invalid target package '" + config.getTargetPackage() +
                    "'. Must be a valid Java package (e.g. com.example.migrated)");
            return ExitCode.VALIDATION_ERROR.getCode();
        }
        // BR-CLI-05: confidence threshold range
        if (config.getConfidenceThreshold() < 0.0 || config.getConfidenceThreshold() > 1.0) {
            System.err.println("[ERROR] --confidence-threshold must be between 0.0 and 1.0");
            return ExitCode.VALIDATION_ERROR.getCode();
        }
        // BR-CLI-10: DDL files must exist
        for (Path ddl : config.getDdlFiles()) {
            if (!Files.isReadable(ddl)) {
                System.err.println("[ERROR] DDL file not found or not readable: " + ddl);
                return ExitCode.IO_ERROR.getCode();
            }
        }
        // BR-CLI-04: output dir
        try {
            Files.createDirectories(config.getOutputDir());
        } catch (IOException e) {
            System.err.println("[ERROR] Cannot create output directory: " + config.getOutputDir());
            return ExitCode.IO_ERROR.getCode();
        }
        if (!Files.isWritable(config.getOutputDir())) {
            System.err.println("[ERROR] Output directory is not writable: " + config.getOutputDir());
            return ExitCode.IO_ERROR.getCode();
        }
        return null;
    }

    private void printGenerateSummary(MigrationResult result, MigrationConfig config) {
        int translated = result.getTranslationResults() != null ? result.getTranslationResults().size() : 0;
        int skipped = result.getSkippedObjects().size();
        System.out.printf("[INFO] Translation: %d translated, %d skipped%n", translated, skipped);
        if (result.getConfidenceReport() != null) {
            System.out.printf("[INFO] Overall confidence score: %d%%%n",
                    result.getConfidenceReport().getOverallScore());
        }
        System.out.printf("[INFO] Output: %s%n", config.getOutputDir().resolve("generated"));
        System.out.printf("[INFO] Report: %s%n", config.getOutputDir().resolve("reports"));
        if (!result.getSkippedObjects().isEmpty()) {
            System.out.printf("[WARN] %d object(s) skipped:%n", skipped);
            result.getSkippedObjects().forEach(name -> System.out.println("[WARN]   - " + name));
        }
    }
}
