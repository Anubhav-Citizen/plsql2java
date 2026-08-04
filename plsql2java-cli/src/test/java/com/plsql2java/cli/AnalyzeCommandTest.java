package com.plsql2java.cli;

import com.plsql2java.cli.command.AnalyzeCommand;
import com.plsql2java.cli.config.ConfigLoader;
import com.plsql2java.model.DiscoveryMode;
import com.plsql2java.model.DependencyGraph;
import com.plsql2java.model.DiscoveryResult;
import com.plsql2java.orchestration.MigrationOrchestratorService;
import com.plsql2java.orchestration.event.ProgressEventBus;
import com.plsql2java.orchestration.model.AnalysisResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyzeCommandTest {

    @Mock MigrationOrchestratorService orchestrator;
    @Mock ConfigLoader configLoader;

    @TempDir Path tempDir;

    private CommandLine buildCli() {
        ProgressEventBus eventBus = new ProgressEventBus();
        AnalyzeCommand cmd = new AnalyzeCommand(orchestrator, configLoader, eventBus);
        return new CommandLine(cmd);
    }

    @Test
    void analyze_withDdlFile_returnsSuccess() throws Exception {
        Path ddlFile = tempDir.resolve("schema.sql");
        ddlFile.toFile().createNewFile();

        DiscoveryResult discovery = new DiscoveryResult("test", "SCHEMA", DiscoveryMode.FILE);
        DependencyGraph graph = new DependencyGraph("test");
        when(orchestrator.analyze(any())).thenReturn(new AnalysisResult("id", discovery, graph));

        CommandLine cli = buildCli();
        int exit = cli.execute("--ddl-file", ddlFile.toString(), "--output-dir", tempDir.toString());

        assertThat(exit).isEqualTo(ExitCode.SUCCESS.getCode());
    }

    @Test
    void analyze_noSource_returnsValidationError() {
        CommandLine cli = buildCli();
        int exit = cli.execute("--output-dir", tempDir.toString());

        assertThat(exit).isEqualTo(ExitCode.VALIDATION_ERROR.getCode());
    }

    @Test
    void analyze_nonExistentDdlFile_returnsIoError() {
        CommandLine cli = buildCli();
        int exit = cli.execute("--ddl-file", "/nonexistent/file.sql", "--output-dir", tempDir.toString());

        assertThat(exit).isEqualTo(ExitCode.IO_ERROR.getCode());
    }

    @Test
    void analyze_helpFlag_printsHelp() {
        StringWriter sw = new StringWriter();
        CommandLine cli = buildCli();
        cli.setOut(new PrintWriter(sw));
        cli.execute("--help");

        assertThat(sw.toString()).contains("analyze");
    }
}
