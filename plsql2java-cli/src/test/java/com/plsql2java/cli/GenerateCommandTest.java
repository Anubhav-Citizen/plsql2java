package com.plsql2java.cli;

import com.plsql2java.cli.command.GenerateCommand;
import com.plsql2java.cli.config.ConfigLoader;
import com.plsql2java.orchestration.MigrationOrchestratorService;
import com.plsql2java.orchestration.event.ProgressEventBus;
import com.plsql2java.orchestration.model.AnalysisResult;
import com.plsql2java.orchestration.model.MigrationResult;
import com.plsql2java.scoring.model.ConfidenceReport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateCommandTest {

    @Mock MigrationOrchestratorService orchestrator;
    @Mock ConfigLoader configLoader;
    @Mock AnalysisResult analysisResult;

    @TempDir Path tempDir;

    private CommandLine buildCli() {
        ProgressEventBus eventBus = new ProgressEventBus();
        GenerateCommand cmd = new GenerateCommand(orchestrator, configLoader, eventBus);
        return new CommandLine(cmd);
    }

    @Test
    void generate_successfulMigration_returnsSuccess() throws Exception {
        Path ddlFile = tempDir.resolve("schema.sql");
        ddlFile.toFile().createNewFile();

        ConfidenceReport confidenceReport = new ConfidenceReport("id", 70, List.of(), 87);
        MigrationResult result = new MigrationResult("id", analysisResult, List.of(),
                null, confidenceReport, null, List.of());
        when(orchestrator.generate(any())).thenReturn(result);

        CommandLine cli = buildCli();
        int exit = cli.execute(
                "--ddl-file", ddlFile.toString(),
                "--output-dir", tempDir.toString(),
                "--target-package", "com.example"
        );

        assertThat(exit).isEqualTo(ExitCode.SUCCESS.getCode());
    }

    @Test
    void generate_partialMigration_returnsPartialSuccess() throws Exception {
        Path ddlFile = tempDir.resolve("schema.sql");
        ddlFile.toFile().createNewFile();

        MigrationResult result = new MigrationResult("id", analysisResult, List.of(),
                null, null, null, List.of("PROC_SKIPPED"));
        when(orchestrator.generate(any())).thenReturn(result);

        CommandLine cli = buildCli();
        int exit = cli.execute(
                "--ddl-file", ddlFile.toString(),
                "--output-dir", tempDir.toString()
        );

        assertThat(exit).isEqualTo(ExitCode.PARTIAL_SUCCESS.getCode());
    }

    @Test
    void generate_noSource_returnsValidationError() {
        CommandLine cli = buildCli();
        int exit = cli.execute("--output-dir", tempDir.toString());

        assertThat(exit).isEqualTo(ExitCode.VALIDATION_ERROR.getCode());
    }

    @Test
    void generate_invalidPackage_returnsValidationError() throws Exception {
        Path ddlFile = tempDir.resolve("schema.sql");
        ddlFile.toFile().createNewFile();

        CommandLine cli = buildCli();
        int exit = cli.execute(
                "--ddl-file", ddlFile.toString(),
                "--output-dir", tempDir.toString(),
                "--target-package", "Invalid.Package"
        );

        assertThat(exit).isEqualTo(ExitCode.VALIDATION_ERROR.getCode());
    }

    @Test
    void generate_confidenceThresholdOutOfRange_returnsValidationError() throws Exception {
        Path ddlFile = tempDir.resolve("schema.sql");
        ddlFile.toFile().createNewFile();

        CommandLine cli = buildCli();
        int exit = cli.execute(
                "--ddl-file", ddlFile.toString(),
                "--output-dir", tempDir.toString(),
                "--confidence-threshold", "1.5"
        );

        assertThat(exit).isEqualTo(ExitCode.VALIDATION_ERROR.getCode());
    }
}
