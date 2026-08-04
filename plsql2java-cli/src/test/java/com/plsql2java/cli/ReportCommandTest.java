package com.plsql2java.cli;

import com.plsql2java.cli.command.ReportCommand;
import com.plsql2java.orchestration.MigrationOrchestratorService;
import com.plsql2java.reporting.model.MigrationReport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportCommandTest {

    @Mock MigrationOrchestratorService orchestrator;

    @TempDir Path tempDir;

    private CommandLine buildCli() {
        return new CommandLine(new ReportCommand(orchestrator));
    }

    @Test
    void report_withPriorOutput_returnsSuccess() throws IOException {
        // Create analysis dir to satisfy BR-CLI-07
        Files.createDirectories(tempDir.resolve("analysis"));

        MigrationReport report = new MigrationReport("id", "# Report", "<html/>", "SCHEMA");
        when(orchestrator.report(any())).thenReturn(report);

        int exit = buildCli().execute("--output-dir", tempDir.toString());

        assertThat(exit).isEqualTo(ExitCode.SUCCESS.getCode());
    }

    @Test
    void report_missingOutputDir_returnsIoError() {
        int exit = buildCli().execute("--output-dir", "/nonexistent/path");

        assertThat(exit).isEqualTo(ExitCode.IO_ERROR.getCode());
    }

    @Test
    void report_noPriorAnalysis_returnsValidationError() throws IOException {
        // Output dir exists but no analysis subdir
        int exit = buildCli().execute("--output-dir", tempDir.toString());

        assertThat(exit).isEqualTo(ExitCode.VALIDATION_ERROR.getCode());
    }

    @Test
    void report_invalidFormat_returnsValidationError() throws IOException {
        Files.createDirectories(tempDir.resolve("analysis"));

        int exit = buildCli().execute("--output-dir", tempDir.toString(), "--format", "pdf");

        assertThat(exit).isEqualTo(ExitCode.VALIDATION_ERROR.getCode());
    }
}
