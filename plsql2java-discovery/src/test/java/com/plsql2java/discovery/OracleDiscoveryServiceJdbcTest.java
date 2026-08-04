package com.plsql2java.discovery;

import com.plsql2java.common.*;
import com.plsql2java.discovery.file.DdlFileParser;
import com.plsql2java.discovery.jdbc.*;
import com.plsql2java.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class OracleDiscoveryServiceJdbcTest {

    private OracleDiscoveryService service;
    private JdbcConnectionManager connectionManager;
    private OracleDataDictionaryReader dictionaryReader;
    private Connection conn;

    @BeforeEach
    void setUp() {
        connectionManager = mock(JdbcConnectionManager.class);
        dictionaryReader = mock(OracleDataDictionaryReader.class);
        conn = mock(Connection.class);
        service = new OracleDiscoveryService(
                connectionManager,
                dictionaryReader,
                new DdlFileParser(),
                new OracleObjectNormalizer(),
                new ResultPersistenceService());
    }

    @Test
    void discoverFromJdbc_returnsDiscoveryResult(@TempDir Path tempDir) {
        JdbcConfig config = new JdbcConfig("jdbc:oracle:thin:@localhost:1521:XE", "USER", "pass");
        when(connectionManager.connect(config)).thenReturn(conn);
        when(dictionaryReader.readObjectList(conn, "USER"))
                .thenReturn(List.of(new String[]{"MY_PROC", "PROCEDURE"}));
        when(dictionaryReader.readSource(conn, "USER", "MY_PROC", "PROCEDURE"))
                .thenReturn("CREATE OR REPLACE PROCEDURE MY_PROC IS BEGIN NULL; END;");
        when(dictionaryReader.hasCompilationErrors(conn, "USER", "MY_PROC", "PROCEDURE"))
                .thenReturn(false);

        DiscoveryResult result = service.discoverFromJdbc(config, "test-id", ProgressListener.noOp());

        assertThat(result.getDiscoveryMode()).isEqualTo(DiscoveryMode.JDBC);
        assertThat(result.getSchemaName()).isEqualTo("USER");
        assertThat(result.getObjects()).hasSize(1);
        assertThat(result.getObjects().get(0).getName()).isEqualTo("MY_PROC");
        assertThat(result.getObjects().get(0).getType()).isEqualTo(OracleObjectType.PROCEDURE);
    }

    @Test
    void discoverFromJdbc_singleObjectFailure_continuesWithRest(@TempDir Path tempDir) {
        JdbcConfig config = new JdbcConfig("jdbc:oracle:thin:@localhost:1521:XE", "USER", "pass");
        when(connectionManager.connect(config)).thenReturn(conn);
        when(dictionaryReader.readObjectList(conn, "USER"))
                .thenReturn(List.of(
                        new String[]{"GOOD_PROC", "PROCEDURE"},
                        new String[]{"BAD_PROC", "PROCEDURE"}));
        when(dictionaryReader.readSource(conn, "USER", "GOOD_PROC", "PROCEDURE"))
                .thenReturn("CREATE OR REPLACE PROCEDURE GOOD_PROC IS BEGIN NULL; END;");
        when(dictionaryReader.readSource(conn, "USER", "BAD_PROC", "PROCEDURE"))
                .thenThrow(new RuntimeException("Simulated failure"));
        when(dictionaryReader.hasCompilationErrors(any(), any(), any(), any())).thenReturn(false);

        DiscoveryResult result = service.discoverFromJdbc(config, "test-id", ProgressListener.noOp());

        assertThat(result.getObjects()).hasSize(1);
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getSource()).isEqualTo("BAD_PROC");
    }

    @Test
    void discoverFromJdbc_connectionFailure_throwsDiscoveryException() {
        JdbcConfig config = new JdbcConfig("jdbc:oracle:thin:@bad-host:1521:XE", "USER", "pass");
        when(connectionManager.connect(config))
                .thenThrow(new DiscoveryException("Database connection failed."));

        assertThatThrownBy(() -> service.discoverFromJdbc(config, "test-id", ProgressListener.noOp()))
                .isInstanceOf(DiscoveryException.class)
                .hasMessageContaining("connection failed");
    }
}
