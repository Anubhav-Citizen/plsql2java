package com.plsql2java.cli;

import com.plsql2java.cli.config.CliConfig;
import com.plsql2java.cli.config.MigrationConfigMapper;
import com.plsql2java.common.MigrationConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MigrationConfigMapperTest {

    @Test
    void toMigrationConfig_mapsAllFields() {
        CliConfig cli = new CliConfig();
        cli.setOutputDir(Path.of("./output"));
        cli.setTargetPackage("com.example.migrated");
        cli.setConfidenceThreshold(0.8);

        MigrationConfig config = MigrationConfigMapper.toMigrationConfig(cli);

        assertThat(config.getOutputDir()).isEqualTo(Path.of("./output"));
        assertThat(config.getTargetPackage()).isEqualTo("com.example.migrated");
        assertThat(config.getConfidenceThreshold()).isEqualTo(80);
    }

    @Test
    void toMigrationConfig_withJdbc_buildsJdbcConfig() {
        CliConfig cli = new CliConfig();
        cli.setJdbcUrl("jdbc:oracle:thin:@host:1521:XE");
        cli.setJdbcUser("scott");
        cli.setJdbcPassword("secret".toCharArray());

        MigrationConfig config = MigrationConfigMapper.toMigrationConfig(cli);

        assertThat(config.getJdbcConfig()).isNotNull();
        assertThat(config.getJdbcConfig().getUrl()).isEqualTo("jdbc:oracle:thin:@host:1521:XE");
        assertThat(config.getJdbcConfig().getUsername()).isEqualTo("scott");
    }

    @Test
    void toMigrationConfig_clearsPasswordAfterMapping() {
        char[] password = "secret".toCharArray();
        CliConfig cli = new CliConfig();
        cli.setJdbcUrl("jdbc:oracle:thin:@host:1521:XE");
        cli.setJdbcPassword(password);

        MigrationConfigMapper.toMigrationConfig(cli);

        // Password char[] should be zeroed out (SECURITY-03)
        assertThat(password).containsOnly('\0');
    }

    @ParameterizedTest
    @ValueSource(strings = {"com.example", "com.example.migrated", "mypackage", "com.example.sub_pkg"})
    void isValidPackage_acceptsValidPackages(String pkg) {
        assertThat(MigrationConfigMapper.isValidPackage(pkg)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Com.Example", "com.Example", "123invalid", "com..double", "com.example.", ".leading"})
    void isValidPackage_rejectsInvalidPackages(String pkg) {
        assertThat(MigrationConfigMapper.isValidPackage(pkg)).isFalse();
    }

    @Test
    void toMigrationConfig_invalidPackage_throwsIllegalArgumentException() {
        CliConfig cli = new CliConfig();
        cli.setTargetPackage("Invalid.Package");

        assertThatThrownBy(() -> MigrationConfigMapper.toMigrationConfig(cli))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid target package");
    }

    @Test
    void toMigrationConfig_nullOutputDir_usesDefault() {
        CliConfig cli = new CliConfig();
        cli.setOutputDir(null);

        MigrationConfig config = MigrationConfigMapper.toMigrationConfig(cli);

        assertThat(config.getOutputDir()).isEqualTo(Path.of("plsql2java-output"));
    }
}
