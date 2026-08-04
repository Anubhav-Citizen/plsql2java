package com.plsql2java.cli;

import com.plsql2java.cli.command.AnalyzeCommand;
import com.plsql2java.cli.command.GenerateCommand;
import com.plsql2java.cli.command.ReportCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IFactory;

@SpringBootApplication
@Command(
    name = "plsql2java",
    description = "Oracle PL/SQL to Java Spring Boot migration platform.",
    subcommands = {AnalyzeCommand.class, GenerateCommand.class, ReportCommand.class},
    mixinStandardHelpOptions = true,
    version = "1.0.0"
)
public class PlSql2JavaCli implements CommandLineRunner, ExitCodeGenerator {

    private static final Logger log = LoggerFactory.getLogger(PlSql2JavaCli.class);

    private final IFactory factory;
    private int exitCode;

    public PlSql2JavaCli(IFactory factory) {
        this.factory = factory;
    }

    @Override
    public void run(String... args) {
        exitCode = new CommandLine(this, factory).execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    /**
     * Main entry point. Global exception handler (SECURITY-15) catches all unhandled Throwables,
     * logs them internally, and exits with EXECUTION_ERROR — never exposing internal details to stdout.
     */
    public static void main(String[] args) {
        try {
            System.exit(SpringApplication.exit(SpringApplication.run(PlSql2JavaCli.class, args)));
        } catch (Throwable t) {
            // SECURITY-15: global handler — generic message to user, details to log only
            System.err.println("[ERROR] An unexpected error occurred. Check application logs for details.");
            log.error("Unhandled exception in main", t);
            System.exit(ExitCode.EXECUTION_ERROR.getCode());
        }
    }
}
