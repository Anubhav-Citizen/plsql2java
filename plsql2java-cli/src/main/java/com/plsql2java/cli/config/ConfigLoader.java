package com.plsql2java.cli.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

@Component
public class ConfigLoader {

    private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);
    private static final String PASSWORD_ENV_VAR = "PLSQL2JAVA_JDBC_PASSWORD";

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    /**
     * Loads a CliConfig from a YAML file.
     */
    public CliConfig load(Path configFile) throws IOException {
        log.debug("Loading config from: {}", configFile);
        CliConfig config = yamlMapper.readValue(configFile.toFile(), CliConfig.class);
        applyEnvVarPassword(config);
        return config;
    }

    /**
     * Merges two CliConfig instances. Non-null/non-default values in {@code overrides} take precedence.
     */
    public CliConfig merge(CliConfig base, CliConfig overrides) {
        CliConfig merged = new CliConfig();

        merged.setJdbcUrl(overrides.getJdbcUrl() != null ? overrides.getJdbcUrl() : base.getJdbcUrl());
        merged.setJdbcUser(overrides.getJdbcUser() != null ? overrides.getJdbcUser() : base.getJdbcUser());
        merged.setJdbcPassword(overrides.getJdbcPassword() != null ? overrides.getJdbcPassword() : base.getJdbcPassword());
        merged.setDdlFiles(!overrides.getDdlFiles().isEmpty() ? overrides.getDdlFiles() : base.getDdlFiles());
        merged.setOutputDir(overrides.getOutputDir() != null ? overrides.getOutputDir() : base.getOutputDir());
        merged.setTargetPackage(overrides.getTargetPackage() != null ? overrides.getTargetPackage() : base.getTargetPackage());
        merged.setConfidenceThreshold(overrides.getConfidenceThreshold() != 0.7 ? overrides.getConfidenceThreshold() : base.getConfidenceThreshold());
        merged.setVerbose(overrides.isVerbose() || base.isVerbose());
        merged.setObjectTypes(!overrides.getObjectTypes().isEmpty() ? overrides.getObjectTypes() : base.getObjectTypes());

        applyEnvVarPassword(merged);
        return merged;
    }

    /**
     * Reads PLSQL2JAVA_JDBC_PASSWORD env var and sets it on config if not already set.
     * SECURITY-03: password never logged.
     */
    private void applyEnvVarPassword(CliConfig config) {
        if (config.getJdbcPassword() == null || config.getJdbcPassword().length == 0) {
            String envPassword = System.getenv(PASSWORD_ENV_VAR);
            if (envPassword != null && !envPassword.isEmpty()) {
                config.setJdbcPassword(envPassword.toCharArray());
                // Clear the String reference — best-effort; JVM may still hold it
                Arrays.fill(envPassword.toCharArray(), '\0');
            }
        }
    }
}
