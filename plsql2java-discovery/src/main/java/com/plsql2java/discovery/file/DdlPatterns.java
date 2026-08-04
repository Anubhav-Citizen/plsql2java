package com.plsql2java.discovery.file;

import java.util.regex.Pattern;

/** Externalized regex patterns for Oracle DDL statement classification. */
public final class DdlPatterns {

    private DdlPatterns() {}

    /** Matches: CREATE [OR REPLACE] PACKAGE [schema.]name (not PACKAGE BODY) */
    public static final Pattern PACKAGE_SPEC = Pattern.compile(
            "CREATE\\s+(?:OR\\s+REPLACE\\s+)?PACKAGE\\s+(?!BODY\\b)(?:\\w+\\.)?([\\w$#]+)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /** Matches: CREATE [OR REPLACE] PACKAGE BODY [schema.]name */
    public static final Pattern PACKAGE_BODY = Pattern.compile(
            "CREATE\\s+(?:OR\\s+REPLACE\\s+)?PACKAGE\\s+BODY\\s+(?:\\w+\\.)?([\\w$#]+)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /** Matches: CREATE [OR REPLACE] PROCEDURE [schema.]name */
    public static final Pattern PROCEDURE = Pattern.compile(
            "CREATE\\s+(?:OR\\s+REPLACE\\s+)?PROCEDURE\\s+(?:\\w+\\.)?([\\w$#]+)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /** Matches: CREATE [OR REPLACE] FUNCTION [schema.]name */
    public static final Pattern FUNCTION = Pattern.compile(
            "CREATE\\s+(?:OR\\s+REPLACE\\s+)?FUNCTION\\s+(?:\\w+\\.)?([\\w$#]+)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /** Matches: CREATE [OR REPLACE] TRIGGER [schema.]name */
    public static final Pattern TRIGGER = Pattern.compile(
            "CREATE\\s+(?:OR\\s+REPLACE\\s+)?TRIGGER\\s+(?:\\w+\\.)?([\\w$#]+)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /** Matches: CREATE [OR REPLACE] VIEW [schema.]name */
    public static final Pattern VIEW = Pattern.compile(
            "CREATE\\s+(?:OR\\s+REPLACE\\s+)?(?:FORCE\\s+)?VIEW\\s+(?:\\w+\\.)?([\\w$#]+)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /** Matches: CREATE SEQUENCE [schema.]name */
    public static final Pattern SEQUENCE = Pattern.compile(
            "CREATE\\s+SEQUENCE\\s+(?:\\w+\\.)?([\\w$#]+)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /** Matches: CREATE [OR REPLACE] TYPE [schema.]name (not TYPE BODY) */
    public static final Pattern TYPE_SPEC = Pattern.compile(
            "CREATE\\s+(?:OR\\s+REPLACE\\s+)?TYPE\\s+(?!BODY\\b)(?:\\w+\\.)?([\\w$#]+)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /** Matches: CREATE [OR REPLACE] TYPE BODY [schema.]name */
    public static final Pattern TYPE_BODY = Pattern.compile(
            "CREATE\\s+(?:OR\\s+REPLACE\\s+)?TYPE\\s+BODY\\s+(?:\\w+\\.)?([\\w$#]+)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
}
