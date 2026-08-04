package com.plsql2java.cli;

import com.plsql2java.cli.config.CliConfig;
import com.plsql2java.cli.config.ConfigLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigLoaderTest {

    private final ConfigLoader loader = new ConfigLoader();

    @TempDir
    Path tempDir;

    @Test
    void load_parsesYamlConfig() throws IOException {
        Path configFile = tempDir.resolve("config.yml");
        Files.writeString(configFile, """
                oracle:
                  jdbcUrl: jdbc:oracle:thin:@host:1521:XE
                  jdbcUser: scott
                output:
                  directory: ./output
                  targetPackage: com.example
                migration:
                  confidenceThreshold: 0.8
                """);

        CliConfig config = loader.load(configFile);

        assertThat(config.getJdbcUrl()).isEqualTo("jdbc:oracle:thin:@host:1521:XE");
        assertThat(config.getJdbcUser()).isEqualTo("scott");
        assertThat(config.getTargetPackage()).isEqualTo("com.example");
        assertThat(config.getConfidenceThreshold()).isEqualTo(0.8);
    }

    @Test
    void merge_cliFlagsOverrideFileConfig() {
        CliConfig base = new CliConfig();
        base.setJdbcUrl("jdbc:oracle:thin:@base:1521:XE");
        base.setTargetPackage("com.base");
        base.setConfidenceThreshold(0.6);

        CliConfig overrides = new CliConfig();
        overrides.setTargetPackage("com.override");
        // jdbcUrl not set in overrides — should keep base value

        CliConfig merged = loader.merge(base, overrides);

        assertThat(merged.getJdbcUrl()).isEqualTo("jdbc:oracle:thin:@base:1521:XE");
        assertThat(merged.getTargetPackage()).isEqualTo("com.override");
        assertThat(merged.getConfidenceThreshold()).isEqualTo(0.6);
    }

    @Test
    void merge_verboseIsUnionOfBothConfigs() {
        CliConfig base = new CliConfig();
        base.setVerbose(true);

        CliConfig overrides = new CliConfig();
        overrides.setVerbose(false);

        CliConfig merged = loader.merge(base, overrides);
        assertThat(merged.isVerbose()).isTrue();
    }

    @Test
    void load_emptyYaml_returnsDefaultConfig() throws IOException {
        Path configFile = tempDir.resolve("empty.yml");
        Files.writeString(configFile, "{}");

        CliConfig config = loader.load(configFile);

        assertThat(config.getJdbcUrl()).isNull();
        assertThat(config.getConfidenceThreshold()).isEqualTo(0.7);
    }
}
