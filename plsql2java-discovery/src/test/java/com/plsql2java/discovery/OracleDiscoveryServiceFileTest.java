package com.plsql2java.discovery;

import com.plsql2java.common.ProgressListener;
import com.plsql2java.discovery.file.DdlFileParser;
import com.plsql2java.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.net.URL;
import java.nio.file.*;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class OracleDiscoveryServiceFileTest {

    private OracleDiscoveryService service;

    @BeforeEach
    void setUp() {
        service = new OracleDiscoveryService(
                null, // JdbcConnectionManager not needed for file tests
                null, // OracleDataDictionaryReader not needed for file tests
                new DdlFileParser(),
                new OracleObjectNormalizer(),
                new ResultPersistenceService());
    }

    @Test
    void discoverFromFiles_packageFile_returnsNormalizedPackage() throws Exception {
        Path file = sampleFile("sample-package.sql");
        DiscoveryResult result = service.discoverFromFiles(
                List.of(file), "test-id", "MY_SCHEMA", ProgressListener.noOp());

        assertThat(result.getDiscoveryMode()).isEqualTo(DiscoveryMode.FILE);
        assertThat(result.getSchemaName()).isEqualTo("MY_SCHEMA");
        assertThat(result.getObjects()).anyMatch(o ->
                o.getName().equals("EMP_PKG") && o.getType() == OracleObjectType.PACKAGE);

        OracleObject pkg = result.getObjects().stream()
                .filter(o -> o.getName().equals("EMP_PKG")).findFirst().orElseThrow();
        assertThat(pkg.getSourceBody()).isNotBlank(); // body merged into spec
    }

    @Test
    void discoverFromFiles_multipleFiles_aggregatesObjects() throws Exception {
        Path pkg = sampleFile("sample-package.sql");
        Path proc = sampleFile("sample-procedure.sql");
        Path trigger = sampleFile("sample-trigger.sql");
        Path view = sampleFile("sample-view.sql");

        DiscoveryResult result = service.discoverFromFiles(
                List.of(pkg, proc, trigger, view), "test-id", "SCHEMA", ProgressListener.noOp());

        assertThat(result.getTotalObjectCount()).isGreaterThanOrEqualTo(4);
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void discoverFromFiles_emptySchema_returnsEmptyResult(@TempDir Path tempDir) throws Exception {
        Path emptyFile = tempDir.resolve("empty.sql");
        Files.writeString(emptyFile, "");

        DiscoveryResult result = service.discoverFromFiles(
                List.of(emptyFile), "test-id", "SCHEMA", ProgressListener.noOp());

        assertThat(result.getObjects()).isEmpty();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void discoverFromFiles_progressEventsEmitted() throws Exception {
        Path file = sampleFile("sample-procedure.sql");
        List<String> progressMessages = new java.util.ArrayList<>();

        service.discoverFromFiles(List.of(file), "test-id", "SCHEMA",
                event -> progressMessages.add(event.getMessage()));

        assertThat(progressMessages).isNotEmpty();
    }

    private Path sampleFile(String name) throws Exception {
        URL url = getClass().getClassLoader().getResource("samples/" + name);
        assertThat(url).isNotNull();
        return Paths.get(url.toURI());
    }
}
