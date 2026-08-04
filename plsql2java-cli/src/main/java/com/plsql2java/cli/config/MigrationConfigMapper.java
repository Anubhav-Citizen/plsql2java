package com.plsql2java.cli.config;

import com.plsql2java.common.JdbcConfig;
import com.plsql2java.common.MigrationConfig;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.regex.Pattern;

public final class MigrationConfigMapper {

    // BR-CLI-03: valid Java package identifier
    private static final Pattern PACKAGE_PATTERN =
            Pattern.compile("^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)*$");

    private MigrationConfigMapper() {}

    /**
     * Maps a validated CliConfig to MigrationConfig.
     * Clears the password char[] after use (SECURITY-03).
     *
     * @throws IllegalArgumentException if targetPackage is invalid (BR-CLI-03)
     */
    public static MigrationConfig toMigrationConfig(CliConfig cli) {
        if (cli.getTargetPackage() != null && !PACKAGE_PATTERN.matcher(cli.getTargetPackage()).matches()) {
            throw new IllegalArgumentException(
                    "Invalid target package '" + cli.getTargetPackage() +
                    "'. Must match: ^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)*$");
        }

        MigrationConfig config = new MigrationConfig();
        config.setOutputDir(cli.getOutputDir() != null ? cli.getOutputDir() : Path.of("plsql2java-output"));
        config.setTargetPackage(cli.getTargetPackage());
        config.setDdlFiles(cli.getDdlFiles());
        config.setConfidenceThreshold((int) (cli.getConfidenceThreshold() * 100));

        if (cli.getJdbcUrl() != null) {
            JdbcConfig jdbc = new JdbcConfig();
            jdbc.setUrl(cli.getJdbcUrl());
            jdbc.setUsername(cli.getJdbcUser());
            if (cli.getJdbcPassword() != null) {
                jdbc.setPassword(new String(cli.getJdbcPassword()));
                // Clear password from CliConfig after transfer (SECURITY-03)
                Arrays.fill(cli.getJdbcPassword(), '\0');
            }
            config.setJdbcConfig(jdbc);
        }

        return config;
    }

    public static boolean isValidPackage(String pkg) {
        return pkg != null && PACKAGE_PATTERN.matcher(pkg).matches();
    }
}
