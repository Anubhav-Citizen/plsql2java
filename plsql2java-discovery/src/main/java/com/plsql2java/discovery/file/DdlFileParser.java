package com.plsql2java.discovery.file;

import com.plsql2java.discovery.DiscoveryException;
import com.plsql2java.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;

@Component
public class DdlFileParser {

    private static final Logger log = LoggerFactory.getLogger(DdlFileParser.class);
    private static final long MAX_FILE_SIZE_BYTES = 50L * 1024 * 1024; // 50 MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".sql", ".ddl");

    /**
     * Parses a DDL file and returns all discovered OracleObjects.
     * Errors are accumulated in the provided list — parsing continues on failure.
     */
    public List<OracleObject> parse(Path filePath, List<DiscoveryError> errors) {
        validatePath(filePath);
        validateFile(filePath, errors);

        String content = readFile(filePath, errors);
        if (content == null) return Collections.emptyList();

        List<String> statements = tokenize(content);
        List<OracleObject> objects = new ArrayList<>();
        int lineOffset = 0;

        for (String statement : statements) {
            String trimmed = statement.trim();
            if (trimmed.isEmpty()) continue;
            try {
                OracleObject obj = classify(trimmed);
                if (obj != null) {
                    objects.add(obj);
                    log.debug("Parsed {} '{}' from {}", obj.getType(), obj.getName(), filePath.getFileName());
                }
            } catch (Exception e) {
                log.warn("Failed to parse statement in {}: {}", filePath.getFileName(), e.getMessage());
                errors.add(new DiscoveryError(filePath.getFileName().toString(),
                        DiscoveryErrorType.PARSE_ERROR, e.getMessage()));
            }
            lineOffset += trimmed.split("\n").length;
        }
        return objects;
    }

    /** Validates path does not contain traversal sequences. */
    public static void validatePath(Path path) {
        String normalized = path.normalize().toString();
        if (normalized.contains("..")) {
            throw new DiscoveryException("Invalid file path: path traversal detected in " + path);
        }
    }

    private void validateFile(Path path, List<DiscoveryError> errors) {
        String fileName = path.getFileName().toString().toLowerCase();
        boolean validExt = ALLOWED_EXTENSIONS.stream().anyMatch(fileName::endsWith);
        if (!validExt) {
            errors.add(new DiscoveryError(fileName, DiscoveryErrorType.UNSUPPORTED_FILE_TYPE,
                    "Unsupported file type. Only .sql and .ddl files are accepted."));
            throw new DiscoveryException("Unsupported file type: " + fileName);
        }
        try {
            long size = Files.size(path);
            if (size > MAX_FILE_SIZE_BYTES) {
                errors.add(new DiscoveryError(fileName, DiscoveryErrorType.FILE_TOO_LARGE,
                        "File exceeds maximum size of 50MB."));
                throw new DiscoveryException("File too large: " + fileName);
            }
        } catch (IOException e) {
            errors.add(new DiscoveryError(fileName, DiscoveryErrorType.FILE_NOT_FOUND, e.getMessage()));
            throw new DiscoveryException("Cannot access file: " + fileName);
        }
    }

    private String readFile(Path path, List<DiscoveryError> errors) {
        for (Charset charset : List.of(StandardCharsets.UTF_8, StandardCharsets.ISO_8859_1)) {
            try {
                return Files.readString(path, charset);
            } catch (IOException e) {
                // try next charset
            }
        }
        errors.add(new DiscoveryError(path.getFileName().toString(),
                DiscoveryErrorType.PARSE_ERROR, "Cannot read file with UTF-8 or ISO-8859-1 encoding."));
        return null;
    }

    /** Splits DDL content into individual statements on '/' or ';' delimiters. */
    List<String> tokenize(String content) {
        // Split on lines that contain only '/' (Oracle SQL*Plus delimiter) or on ';'
        String[] parts = content.split("(?m)^\\s*/\\s*$|;");
        return Arrays.asList(parts);
    }

    /** Classifies a DDL statement and returns an OracleObject, or null if not a supported type. */
    OracleObject classify(String statement) {
        Matcher m;

        m = DdlPatterns.PACKAGE_BODY.matcher(statement);
        if (m.find()) return makeObject(m.group(1), OracleObjectType.PACKAGE_BODY, null, statement);

        m = DdlPatterns.PACKAGE_SPEC.matcher(statement);
        if (m.find()) return makeObject(m.group(1), OracleObjectType.PACKAGE, null, statement);

        m = DdlPatterns.TYPE_BODY.matcher(statement);
        if (m.find()) return makeObject(m.group(1), OracleObjectType.TYPE_BODY, null, statement);

        m = DdlPatterns.TYPE_SPEC.matcher(statement);
        if (m.find()) return makeObject(m.group(1), OracleObjectType.TYPE, null, statement);

        m = DdlPatterns.PROCEDURE.matcher(statement);
        if (m.find()) return makeObject(m.group(1), OracleObjectType.PROCEDURE, null, statement);

        m = DdlPatterns.FUNCTION.matcher(statement);
        if (m.find()) return makeObject(m.group(1), OracleObjectType.FUNCTION, null, statement);

        m = DdlPatterns.TRIGGER.matcher(statement);
        if (m.find()) return makeObject(m.group(1), OracleObjectType.TRIGGER, null, statement);

        m = DdlPatterns.VIEW.matcher(statement);
        if (m.find()) return makeObject(m.group(1), OracleObjectType.VIEW, null, statement);

        m = DdlPatterns.SEQUENCE.matcher(statement);
        if (m.find()) return makeObject(m.group(1), OracleObjectType.SEQUENCE, null, statement);

        return null; // not a supported object type
    }

    private OracleObject makeObject(String name, OracleObjectType type, String schema, String source) {
        OracleObject obj = new OracleObject();
        obj.setName(name.toUpperCase());
        obj.setType(type);
        obj.setSchema(schema != null ? schema.toUpperCase() : "UNKNOWN");
        obj.setSourceSpec(source);
        return obj;
    }
}
