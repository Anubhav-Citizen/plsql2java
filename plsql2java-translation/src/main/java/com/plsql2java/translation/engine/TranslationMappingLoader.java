package com.plsql2java.translation.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plsql2java.translation.model.BuiltinFunctionMapping;
import com.plsql2java.translation.model.OracleExceptionMapping;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TranslationMappingLoader {

    private static final Logger log = LoggerFactory.getLogger(TranslationMappingLoader.class);

    private static final String BUILTIN_RESOURCE = "/rules/builtin-function-mappings.json";
    private static final String EXCEPTION_RESOURCE = "/rules/oracle-exception-mappings.json";

    private final ObjectMapper objectMapper;

    private Map<String, BuiltinFunctionMapping> builtinMappings = Collections.emptyMap();
    private Map<String, OracleExceptionMapping> exceptionMappings = Collections.emptyMap();

    public TranslationMappingLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void load() {
        builtinMappings = loadResource(BUILTIN_RESOURCE,
                new TypeReference<List<BuiltinFunctionMapping>>() {}).stream()
                .collect(Collectors.toUnmodifiableMap(
                        m -> m.getOracleFunction().toUpperCase(),
                        m -> m));

        exceptionMappings = loadResource(EXCEPTION_RESOURCE,
                new TypeReference<List<OracleExceptionMapping>>() {}).stream()
                .collect(Collectors.toUnmodifiableMap(
                        m -> m.getOracleException().toUpperCase(),
                        m -> m));

        log.debug("Loaded {} builtin function mappings, {} exception mappings",
                builtinMappings.size(), exceptionMappings.size());
    }

    private <T> List<T> loadResource(String resourcePath, TypeReference<List<T>> typeRef) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                log.error("Mapping resource not found: {}", resourcePath);
                return Collections.emptyList();
            }
            return objectMapper.readValue(is, typeRef);
        } catch (IOException e) {
            log.error("Failed to load mapping resource: {}", resourcePath);
            return Collections.emptyList();
        }
    }

    public BuiltinFunctionMapping getBuiltinMapping(String oracleFunction) {
        return builtinMappings.get(oracleFunction.toUpperCase());
    }

    public OracleExceptionMapping getExceptionMapping(String oracleException) {
        return exceptionMappings.get(oracleException.toUpperCase());
    }

    public Map<String, BuiltinFunctionMapping> getAllBuiltinMappings() {
        return builtinMappings;
    }

    public Map<String, OracleExceptionMapping> getAllExceptionMappings() {
        return exceptionMappings;
    }
}
