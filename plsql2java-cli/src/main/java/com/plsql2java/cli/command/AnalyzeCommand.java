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
import com.plsql2java.orchestration.model.AnalysisResult;
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
    name = "analyze",
    description = "Discover PL/SQL objects and analyze dependencies without generating code.",
    mixinStandardHelpOptions = true
)
public class AnalyzeCommand implements Callable<Integer> {

    private static final Logger log = LoggerFactory.getLogger(AnalyzeCommand.class);

    private final MigrationOrchestratorService orchestrator;
    private final ConfigLoader configLoader;
    private final ProgressEventBus eventBus;

    @Option(names = {"--config", "-c"}, description = "Path to YAML config file")
    private Path configFile;

    @Option(names = {"--jdbc-url"}, description = "Oracle JDBC URL (e.g. jdbc:oracle:thin:@host:1521:XE)")
    private String jdbcUrl;

    @Option(names = {"--jdbc-user"}, description = "Oracle username")
    private String jdbcUser;

    @Option(names = {"--ddl-file"}, description = "DDL file(s) for offline mode", arity = "0..*")
    private List<Path> ddlFiles = new ArrayList<>();

    @Option(names = {"--output-dir", "-o"}, description = "Output directory (default: ./plsql2java-output)")
    private Path outputDir = Path.of("plsql2java-output");

    @Option(names = {"--verbose", "-v"}, description = "Enable verbose progress output")
    private boolean verbose;

    public AnalyzeCommand(MigrationOrchestratorService orchestrator,
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
                System.out.println("[INFO] Starting analysis...");
                AnalysisResult result = orchestrator.analyze(config);
                printAnalysisSummary(result);
                return ExitCode.SUCCESS.getCode();
            } finally {
                eventBus.unregister(listener);
            }

        } catch (IllegalArgumentException e) {
            System.err.println("[ERROR] " + e.getMessage());
            log.debug("Validation error", e);
            return ExitCode.VALIDATION_ERROR.getCode();
        } catch (OrchestratorException e) {
            System.err.println("[ERROR] Analysis failed. Check logs for details.");
            log.error("Analysis execution error", e);
            return ExitCode.EXECUTION_ERROR.getCode();
        } catch (IOException e) {
            System.err.println("[ERROR] I/O error: " + e.getMessage());
            log.error("I/O error during analysis", e);
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
        // BR-CLI-01: at least one source required
        if ((config.getJdbcUrl() == null || config.getJdbcUrl().isBlank()) && config.getDdlFiles().isEmpty()) {
            System.err.println("[ERROR] At least one source is required: --jdbc-url or --ddl-file");
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

    private void printAnalysisSummary(AnalysisResult result) {
        System.out.printf("[INFO] Discovered %d objects%n", result.getDiscoveryResult().getTotalObjectCount());
        System.out.printf("[INFO] Dependency edges: %d%n", result.getDependencyGraph().getEdges().size());
        System.out.printf("[INFO] Circular dependencies: %d%n", result.getDependencyGraph().getCircularDependencies().size());
        System.out.printf("[INFO] Analysis complete. Results written to: %s%n",
                result.getDiscoveryResult() != null ? "output/analysis" : "output");
    }
}
