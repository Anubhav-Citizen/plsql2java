package com.plsql2java.discovery.jdbc;

/** Externalized Oracle data dictionary SQL queries. */
public final class OracleDataDictionaryQueries {

    private OracleDataDictionaryQueries() {}

    /** Retrieves all supported object names and types for a given schema owner. */
    public static final String GET_ALL_OBJECTS =
            "SELECT OBJECT_NAME, OBJECT_TYPE FROM ALL_OBJECTS " +
            "WHERE OWNER = ? AND OBJECT_TYPE IN " +
            "('PACKAGE','PACKAGE BODY','PROCEDURE','FUNCTION','TRIGGER','VIEW','SEQUENCE','TYPE','TYPE BODY') " +
            "ORDER BY OBJECT_TYPE, OBJECT_NAME";

    /** Retrieves source lines for a named object. Results must be ordered by LINE. */
    public static final String GET_SOURCE =
            "SELECT TEXT FROM ALL_SOURCE WHERE OWNER = ? AND NAME = ? AND TYPE = ? ORDER BY LINE";

    /** Retrieves the view definition text for a named view. */
    public static final String GET_VIEW_TEXT =
            "SELECT TEXT FROM ALL_VIEWS WHERE OWNER = ? AND VIEW_NAME = ?";

    /** Counts compilation errors for a named object (0 = no errors). */
    public static final String COUNT_ERRORS =
            "SELECT COUNT(*) FROM ALL_ERRORS WHERE OWNER = ? AND NAME = ? AND TYPE = ?";
}
