package com.plsql2java.discovery.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class OracleDataDictionaryReaderTest {

    private OracleDataDictionaryReader reader;
    private Connection conn;
    private PreparedStatement stmt;
    private ResultSet rs;

    @BeforeEach
    void setUp() throws Exception {
        reader = new OracleDataDictionaryReader();
        conn = mock(Connection.class);
        stmt = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);
    }

    @Test
    void readObjectList_returnsNameTypePairs() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString(1)).thenReturn("MY_PKG", "MY_PROC");
        when(rs.getString(2)).thenReturn("PACKAGE", "PROCEDURE");

        List<String[]> result = reader.readObjectList(conn, "MYSCHEMA");

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).containsExactly("MY_PKG", "PACKAGE");
        assertThat(result.get(1)).containsExactly("MY_PROC", "PROCEDURE");
    }

    @Test
    void readSource_concatenatesLines() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString(1)).thenReturn("line1\n", "line2\n");

        String source = reader.readSource(conn, "SCHEMA", "MY_PROC", "PROCEDURE");

        assertThat(source).isEqualTo("line1\nline2\n");
    }

    @Test
    void hasCompilationErrors_returnsTrueWhenCountPositive() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(2);

        assertThat(reader.hasCompilationErrors(conn, "SCHEMA", "BAD_PKG", "PACKAGE")).isTrue();
    }

    @Test
    void hasCompilationErrors_returnsFalseWhenCountZero() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(0);

        assertThat(reader.hasCompilationErrors(conn, "SCHEMA", "GOOD_PKG", "PACKAGE")).isFalse();
    }
}
