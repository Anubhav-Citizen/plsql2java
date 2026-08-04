package com.plsql2java.cli;

import com.plsql2java.cli.command.AnalyzeCommand;
import com.plsql2java.cli.command.GenerateCommand;
import com.plsql2java.cli.command.ReportCommand;
import com.plsql2java.cli.config.ConfigLoader;
import com.plsql2java.model.DependencyGraph;
import com.plsql2java.model.DiscoveryMode;
import com.plsql2java.model.DiscoveryResult;
import com.plsql2java.orchestration.MigrationOrchestratorService;
import com.plsql2java.orchestration.event.ProgressEventBus;
import com.plsql2java.orchestration.model.AnalysisResult;
import com.plsql2java.orchestration.model.MigrationResult;
import com.plsql2java.reporting.model.MigrationReport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class CliIntegrationTest {

    @MockBean MigrationOrchestratorService orchestrator;
    @Autowired ConfigLoader configLoader;
    @Autowired ProgressEventBus eventBus;

    @TempDir Path tempDir;

    @Test
    void analyzeCommand_withDdlFile_completesSuccessfully() throws IOException {
        Path ddlFile = tempDir.resolve("schema.sql");
        Files.writeString(ddlFile, "CREATE OR REPLACE PROCEDURE test_proc AS BEGIN NULL; END;");

        DiscoveryResult discovery = new DiscoveryResult("id", "SCHEMA", DiscoveryMode.FILE);
        DependencyGraph graph = new DependencyGraph("id");
        when(orchestrator.analyze(any())).thenReturn(new AnalysisResult("id", discovery, graph));

        AnalyzeCommand cmd = new AnalyzeCommand(orchestrator, configLoader, eventBus);
        int exit = new CommandLine(cmd).execute(
                "--ddl-file", ddlFile.toString(),
                "--output-dir", tempDir.toString()
        );

        assertThat(exit).isEqualTo(ExitCode.SUCCESS.getCode());
    }

    @Test
    void generateCommand_partialResult_returnsPartialSuccess() throws IOException {
        Path ddlFile = tempDir.resolve("schema.sql");
        Files.writeString(ddlFile, "CREATE OR REPLACE PROCEDURE test_proc AS BEGIN NULL; END;");

        DiscoveryResult discovery = new DiscoveryResult("id", "SCHEMA", DiscoveryMode.FILE);
        DependencyGraph graph = new DependencyGraph("id");
        AnalysisResult analysis = new AnalysisResult("id", discovery, graph);
        MigrationResult result = new MigrationResult("id", analysis, List.of(),
                null, null, null, List.of("PROC_SKIPPED"));
        when(orchestrator.generate(any())).thenReturn(result);

        GenerateCommand cmd = new GenerateCommand(orchestrator, configLoader, eventBus);
        int exit = new CommandLine(cmd).execute(
                "--ddl-file", ddlFile.toString(),
                "--output-dir", tempDir.toString(),
                "--target-package", "com.example"
        );

        assertThat(exit).isEqualTo(ExitCode.PARTIAL_SUCCESS.getCode());
    }

    @Test
    void reportCommand_withPriorOutput_completesSuccessfully() throws IOException {
        Files.createDirectories(tempDir.resolve("analysis"));
        MigrationReport report = new MigrationReport("id", "# Report", "<html/>", "SCHEMA");
        when(orchestrator.report(any())).thenReturn(report);

        ReportCommand cmd = new ReportCommand(orchestrator);
        int exit = new CommandLine(cmd).execute("--output-dir", tempDir.toString());

        assertThat(exit).isEqualTo(ExitCode.SUCCESS.getCode());
    }

    @Test
    void configFileMode_loadsAndMergesConfig() throws IOException {
        Path ddlFile = tempDir.resolve("schema.sql");
        Files.writeString(ddlFile, "CREATE OR REPLACE PROCEDURE p AS BEGIN NULL; END;");

        Path configFile = tempDir.resolve("config.yml");
        Files.writeString(configFile, """
                output:
                  directory: %s
                  targetPackage: com.example.fromfile
                migration:
                  confidenceThreshold: 0.8
                """.formatted(tempDir.toString().replace("\\", "/")));

        DiscoveryResult discovery = new DiscoveryResult("id", "SCHEMA", DiscoveryMode.FILE);
        DependencyGraph graph = new DependencyGraph("id");
        when(orchestrator.analyze(any())).thenReturn(new AnalysisResult("id", discovery, graph));

        AnalyzeCommand cmd = new AnalyzeCommand(orchestrator, configLoader, eventBus);
        int exit = new CommandLine(cmd).execute(
                "--config", configFile.toString(),
                "--ddl-file", ddlFile.toString()
        );

        assertThat(exit).isEqualTo(ExitCode.SUCCESS.getCode());
    }
}
