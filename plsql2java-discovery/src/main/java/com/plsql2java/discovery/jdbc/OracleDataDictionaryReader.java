package com.plsql2java.discovery.jdbc;

import com.plsql2java.discovery.DiscoveryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;

@Component
public class OracleDataDictionaryReader {

    private static final Logger log = LoggerFactory.getLogger(OracleDataDictionaryReader.class);
    private static final int FETCH_SIZE = 100;

    /** Returns list of [objectName, objectType] pairs for the given schema. */
    public List<String[]> readObjectList(Connection conn, String schema) {
        List<String[]> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(OracleDataDictionaryQueries.GET_ALL_OBJECTS)) {
            stmt.setFetchSize(FETCH_SIZE);
            stmt.setString(1, schema.toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new String[]{rs.getString(1), rs.getString(2)});
                }
            }
        } catch (SQLException e) {
            log.error("Failed to read object list for schema {}: {}", schema, e.getMessage());
            throw new DiscoveryException("Failed to read Oracle object list. Check schema permissions.");
        }
        log.debug("Found {} objects in schema {}", result.size(), schema);
        return result;
    }

    /** Returns concatenated source lines for the given object. */
    public String readSource(Connection conn, String schema, String name, String type) {
        StringBuilder sb = new StringBuilder();
        try (PreparedStatement stmt = conn.prepareStatement(OracleDataDictionaryQueries.GET_SOURCE)) {
            stmt.setFetchSize(FETCH_SIZE);
            stmt.setString(1, schema.toUpperCase());
            stmt.setString(2, name.toUpperCase());
            stmt.setString(3, type.toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sb.append(rs.getString(1));
                }
            }
        } catch (SQLException e) {
            log.warn("Failed to read source for {}.{} [{}]: {}", schema, name, type, e.getMessage());
            return "";
        }
        return sb.toString();
    }

    /** Returns the view definition text. */
    public String readViewText(Connection conn, String schema, String viewName) {
        try (PreparedStatement stmt = conn.prepareStatement(OracleDataDictionaryQueries.GET_VIEW_TEXT)) {
            stmt.setString(1, schema.toUpperCase());
            stmt.setString(2, viewName.toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (SQLException e) {
            log.warn("Failed to read view text for {}.{}: {}", schema, viewName, e.getMessage());
        }
        return "";
    }

    /** Returns true if the object has compilation errors in ALL_ERRORS. */
    public boolean hasCompilationErrors(Connection conn, String schema, String name, String type) {
        try (PreparedStatement stmt = conn.prepareStatement(OracleDataDictionaryQueries.COUNT_ERRORS)) {
            stmt.setString(1, schema.toUpperCase());
            stmt.setString(2, name.toUpperCase());
            stmt.setString(3, type.toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            log.warn("Failed to check errors for {}.{}: {}", schema, name, e.getMessage());
            return false;
        }
    }
}
