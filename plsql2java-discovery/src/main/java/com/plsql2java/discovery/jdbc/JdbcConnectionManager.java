package com.plsql2java.discovery.jdbc;

import com.plsql2java.common.JdbcConfig;
import com.plsql2java.discovery.DiscoveryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Component
public class JdbcConnectionManager {

    private static final Logger log = LoggerFactory.getLogger(JdbcConnectionManager.class);

    /**
     * Creates a JDBC connection from the given config.
     * Password is never logged.
     * Caller is responsible for closing the connection (use try-with-resources).
     */
    public Connection connect(JdbcConfig config) {
        log.info("Connecting to Oracle: url={}, user={}", config.getUrl(), config.getUsername());
        Properties props = new Properties();
        props.setProperty("user", config.getUsername());
        props.setProperty("password", config.getPassword()); // never logged
        props.setProperty("oracle.net.CONNECT_TIMEOUT",
                String.valueOf(config.getConnectionTimeoutSeconds() * 1000));
        try {
            Connection conn = DriverManager.getConnection(config.getUrl(), props);
            log.info("Oracle connection established for schema: {}", config.effectiveSchema());
            return conn;
        } catch (SQLException e) {
            log.error("Failed to connect to Oracle at {}: {}", config.getUrl(), e.getMessage());
            throw new DiscoveryException(
                    "Database connection failed. Check your JDBC URL, username, and password.");
        }
    }
}
