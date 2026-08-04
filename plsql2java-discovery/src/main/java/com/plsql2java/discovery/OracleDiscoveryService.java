package com.plsql2java.discovery;

import com.plsql2java.common.*;
import com.plsql2java.discovery.file.DdlFileParser;
import com.plsql2java.discovery.jdbc.*;
import com.plsql2java.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.*;

/**
 * Oracle schema discovery service.
 * Supports JDBC live connection (Story 1.1) and DDL file import (Story 1.2).
 * Discovers all supported object types (Story 1.3) and emits progress events (Story 1.4).
 */
@Service
public class OracleDiscoveryService {

    private static final Logger log = LoggerFactory.getLogger(OracleDiscoveryService.class);

    private final JdbcConnectionManager connectionManager;
    private final OracleDataDictionaryReader dictionaryReader;
    private final DdlFileParser ddlFileParser;
    private final OracleObjectNormalizer normalizer;
    private final ResultPersistenceService persistenceService;

    public OracleDiscoveryService(JdbcConnectionManager connectionManager,
                                   OracleDataDictionaryReader dictionaryReader,
                                   DdlFileParser ddlFileParser,
                                   OracleObjectNormalizer normalizer,
                                   ResultPersistenceService persistenceService) {
        this.connectionManager = connectionManager;
        this.dictionaryReader = dictionaryReader;
        this.ddlFileParser = ddlFileParser;
        this.normalizer = normalizer;
        this.persistenceService = persistenceService;
    }

    /** Story 1.1: Discover schema objects via JDBC live connection. */
    public DiscoveryResult discoverFromJdbc(JdbcConfig config, String migrationId,
                                             ProgressListener listener) {
        MDC.put("migrationId", migrationId);
        MDC.put("component", "OracleDiscoveryService");
        log.info("Starting JDBC discovery for schema: {}", config.effectiveSchema());

        DiscoveryResult result = new DiscoveryResult(migrationId, config.effectiveSchema(), DiscoveryMode.JDBC);
        List<OracleObject> rawObjects = new ArrayList<>();
        List<DiscoveryError> errors = new ArrayList<>();

        try (Connection conn = connectionManager.connect(config)) {
            String schema = config.effectiveSchema();
            List<String[]> objectList = dictionaryReader.readObjectList(conn, schema);
            int total = objectList.size();
            int processed = 0;

            for (String[] entry : objectList) {
                String name = entry[0];
                String type = entry[1];
                try {
                    OracleObject obj = extractObject(conn, schema, name, type);
                    rawObjects.add(obj);
                    log.debug("Discovered {} '{}'", type, name);
                } catch (Exception e) {
                    log.warn("Failed to extract {}.{} [{}]: {}", schema, name, type, e.getMessage());
                    errors.add(new DiscoveryError(name, DiscoveryErrorType.PARSE_ERROR, e.getMessage()));
                }
                processed++;
                listener.onProgress(ProgressEvent.builder(migrationId, ProgressStage.DISCOVERY)
                        .objectName(name).processed(processed, total)
                        .message("Discovered: " + name).build());
            }
        } catch (DiscoveryException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during JDBC discovery: {}", e.getMessage(), e);
            throw new DiscoveryException("Schema discovery failed unexpectedly. Check logs for details.");
        } finally {
            MDC.clear();
        }

        result.setObjects(normalizer.normalize(rawObjects));
        result.setErrors(errors);
        log.info("JDBC discovery complete: {} objects, {} errors", result.getTotalObjectCount(), errors.size());
        return result;
    }

    /** Story 1.2: Discover schema objects from DDL/SQL export files. */
    public DiscoveryResult discoverFromFiles(List<Path> ddlFiles, String migrationId,
                                              String schemaName, ProgressListener listener) {
        MDC.put("migrationId", migrationId);
        MDC.put("component", "OracleDiscoveryService");
        log.info("Starting file-based discovery from {} file(s)", ddlFiles.size());

        DiscoveryResult result = new DiscoveryResult(migrationId,
                schemaName != null ? schemaName.toUpperCase() : "UNKNOWN", DiscoveryMode.FILE);
        List<OracleObject> rawObjects = new ArrayList<>();
        List<DiscoveryError> errors = new ArrayList<>();
        List<String> sourceFileNames = new ArrayList<>();

        int total = ddlFiles.size();
        int processed = 0;

        for (Path file : ddlFiles) {
            sourceFileNames.add(file.getFileName().toString());
            try {
                List<OracleObject> fileObjects = ddlFileParser.parse(file, errors);
                fileObjects.forEach(o -> {
                    if (result.getSchemaName() != null && !"UNKNOWN".equals(result.getSchemaName())) {
                        o.setSchema(result.getSchemaName());
                    }
                });
                rawObjects.addAll(fileObjects);
                log.debug("Parsed {} objects from {}", fileObjects.size(), file.getFileName());
            } catch (DiscoveryException e) {
                log.warn("Skipping file {} due to error: {}", file.getFileName(), e.getMessage());
            }
            processed++;
            listener.onProgress(ProgressEvent.builder(migrationId, ProgressStage.DISCOVERY)
                    .objectName(file.getFileName().toString()).processed(processed, total)
                    .message("Parsed file: " + file.getFileName()).build());
        }

        result.setObjects(normalizer.normalize(rawObjects));
        result.setErrors(errors);
        result.setSourceFiles(sourceFileNames);
        MDC.clear();
        log.info("File discovery complete: {} objects from {} files", result.getTotalObjectCount(), total);
        return result;
    }

    /** Persists discovery result to the output directory. */
    public void persist(DiscoveryResult result, Path outputDir) {
        persistenceService.saveDiscoveryResult(result, outputDir);
    }

    /** Loads a previously persisted discovery result. */
    public DiscoveryResult load(Path outputDir) {
        return persistenceService.loadDiscoveryResult(outputDir);
    }

    private OracleObject extractObject(Connection conn, String schema, String name, String type) {
        OracleObject obj = new OracleObject();
        obj.setName(name.toUpperCase());
        obj.setSchema(schema.toUpperCase());
        obj.setHasCompilationErrors(dictionaryReader.hasCompilationErrors(conn, schema, name, type));

        OracleObjectType objectType = mapType(type);
        obj.setType(objectType);

        if (objectType == OracleObjectType.VIEW) {
            obj.setSourceSpec(dictionaryReader.readViewText(conn, schema, name));
        } else if (objectType == OracleObjectType.SEQUENCE) {
            obj.setSourceSpec("-- SEQUENCE: " + name);
        } else {
            obj.setSourceSpec(dictionaryReader.readSource(conn, schema, name, type));
        }
        return obj;
    }

    private OracleObjectType mapType(String oracleType) {
        return switch (oracleType.toUpperCase().trim()) {
            case "PACKAGE" -> OracleObjectType.PACKAGE;
            case "PACKAGE BODY" -> OracleObjectType.PACKAGE_BODY;
            case "PROCEDURE" -> OracleObjectType.PROCEDURE;
            case "FUNCTION" -> OracleObjectType.FUNCTION;
            case "TRIGGER" -> OracleObjectType.TRIGGER;
            case "VIEW" -> OracleObjectType.VIEW;
            case "SEQUENCE" -> OracleObjectType.SEQUENCE;
            case "TYPE" -> OracleObjectType.TYPE;
            case "TYPE BODY" -> OracleObjectType.TYPE_BODY;
            default -> throw new DiscoveryException("Unsupported object type: " + oracleType);
        };
    }
}
