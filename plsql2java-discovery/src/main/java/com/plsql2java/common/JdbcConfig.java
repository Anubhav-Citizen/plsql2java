package com.plsql2java.common;

public class JdbcConfig {

    private String url;
    private String username;
    private String password;
    private int connectionTimeoutSeconds = 30;
    private String schemaName;

    public JdbcConfig() {}

    public JdbcConfig(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    /** Password sourced from environment variable — never logged or stored to disk. */
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getConnectionTimeoutSeconds() { return connectionTimeoutSeconds; }
    public void setConnectionTimeoutSeconds(int connectionTimeoutSeconds) { this.connectionTimeoutSeconds = connectionTimeoutSeconds; }

    public String getSchemaName() { return schemaName; }
    public void setSchemaName(String schemaName) { this.schemaName = schemaName; }

    /** Returns the effective schema name: explicit schemaName or uppercased username. */
    public String effectiveSchema() {
        return schemaName != null ? schemaName.toUpperCase() : username.toUpperCase();
    }
}
