package com.plsql2java.discovery;

import com.plsql2java.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class ResultPersistenceServiceTest {

    private final ResultPersistenceService service = new ResultPersistenceService();

    @Test
    void saveAndLoad_discoveryResult_roundTrips(@TempDir Path tempDir) {
        DiscoveryResult original = new DiscoveryResult("test-id", "MY_SCHEMA", DiscoveryMode.FILE);
        OracleObject obj = new OracleObject("MY_PROC", OracleObjectType.PROCEDURE, "MY_SCHEMA", "source");
        original.setObjects(List.of(obj));

        service.saveDiscoveryResult(original, tempDir);
        DiscoveryResult loaded = service.loadDiscoveryResult(tempDir);

        assertThat(loaded.getMigrationId()).isEqualTo("test-id");
        assertThat(loaded.getSchemaName()).isEqualTo("MY_SCHEMA");
        assertThat(loaded.getObjects()).hasSize(1);
        assertThat(loaded.getObjects().get(0).getName()).isEqualTo("MY_PROC");
    }

    @Test
    void saveAndLoad_dependencyGraph_roundTrips(@TempDir Path tempDir) {
        DependencyGraph original = new DependencyGraph("test-id");
        original.setMigrationOrder(List.of("A", "B", "C"));

        service.saveDependencyGraph(original, tempDir);
        DependencyGraph loaded = service.loadDependencyGraph(tempDir);

        assertThat(loaded.getMigrationId()).isEqualTo("test-id");
        assertThat(loaded.getMigrationOrder()).containsExactly("A", "B", "C");
    }

    @Test
    void loadDiscoveryResult_missingFile_throwsDiscoveryException(@TempDir Path tempDir) {
        assertThatThrownBy(() -> service.loadDiscoveryResult(tempDir))
                .isInstanceOf(DiscoveryException.class)
                .hasMessageContaining("No discovery result found");
    }

    @Test
    void saveDiscoveryResult_createsOutputDirIfMissing(@TempDir Path tempDir) {
        Path subDir = tempDir.resolve("new-output");
        DiscoveryResult result = new DiscoveryResult("id", "SCHEMA", DiscoveryMode.JDBC);

        assertThatNoException().isThrownBy(() -> service.saveDiscoveryResult(result, subDir));
        assertThat(subDir).exists();
    }
}
