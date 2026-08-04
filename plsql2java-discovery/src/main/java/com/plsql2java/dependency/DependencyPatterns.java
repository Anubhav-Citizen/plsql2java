package com.plsql2java.dependency;

import java.util.regex.Pattern;

/** Externalized regex patterns for detecting cross-object references in PL/SQL source. */
public final class DependencyPatterns {

    private DependencyPatterns() {}

    /** Matches procedure/function calls: name( or schema.name( */
    public static final Pattern CALL = Pattern.compile(
            "\\b(?:\\w+\\.)?([\\w$#]+)\\s*\\(", Pattern.CASE_INSENSITIVE);

    /** Matches package member calls: package_name.member( */
    public static final Pattern PACKAGE_CALL = Pattern.compile(
            "\\b([\\w$#]+)\\.([\\w$#]+)\\s*\\(", Pattern.CASE_INSENSITIVE);

    /** Matches FROM clause references: FROM view_or_table_name */
    public static final Pattern FROM_CLAUSE = Pattern.compile(
            "\\bFROM\\s+(?:\\w+\\.)?([\\w$#]+)", Pattern.CASE_INSENSITIVE);

    /** Matches %TYPE references: object_name.column%TYPE or type_name%TYPE */
    public static final Pattern TYPE_REF = Pattern.compile(
            "\\b([\\w$#]+)(?:\\.\\w+)?%TYPE", Pattern.CASE_INSENSITIVE);

    /** Matches TRIGGER ON clause: ON schema.table_or_view */
    public static final Pattern TRIGGER_ON = Pattern.compile(
            "\\bON\\s+(?:\\w+\\.)?([\\w$#]+)", Pattern.CASE_INSENSITIVE);
}
