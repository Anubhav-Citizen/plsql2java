package com.plsql2java.translation.model;

import com.plsql2java.model.OracleObjectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranslationContext {

    private final String objectName;
    private final OracleObjectType objectType;
    private final String schemaName;
    private final Map<String, String> variableRegistry = new HashMap<>();
    private final Map<String, String> cursorRegistry = new HashMap<>();
    private final List<ConstructTranslationResult> constructResults = new ArrayList<>();

    public TranslationContext(String objectName, OracleObjectType objectType, String schemaName) {
        this.objectName = objectName;
        this.objectType = objectType;
        this.schemaName = schemaName;
    }

    public void registerVariable(String name, String javaType) {
        variableRegistry.put(name, javaType);
    }

    public void registerCursor(String name, String queryText) {
        cursorRegistry.put(name, queryText);
    }

    public void addConstructResult(ConstructTranslationResult result) {
        constructResults.add(result);
    }

    public String getObjectName() { return objectName; }
    public OracleObjectType getObjectType() { return objectType; }
    public String getSchemaName() { return schemaName; }
    public Map<String, String> getVariableRegistry() { return variableRegistry; }
    public Map<String, String> getCursorRegistry() { return cursorRegistry; }
    public List<ConstructTranslationResult> getConstructResults() { return constructResults; }
}
