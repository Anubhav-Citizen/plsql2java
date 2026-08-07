package com.plsql2java.translation.rules;

import com.plsql2java.translation.engine.TranslationRule;
import com.plsql2java.translation.model.AstNode;
import com.plsql2java.translation.model.ConstructType;
import com.plsql2java.translation.model.TranslationContext;
import com.plsql2java.translation.model.TranslationOutcome;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class VariableDeclarationRule implements TranslationRule {

    private static final Map<String, String> TYPE_MAP = Map.ofEntries(
            Map.entry("VARCHAR2", "String"),
            Map.entry("NUMBER", "java.math.BigDecimal"),
            Map.entry("INTEGER", "int"),
            Map.entry("DATE", "java.time.LocalDate"),
            Map.entry("BOOLEAN", "boolean")
    );

    @Override
    public ConstructType getConstructType() {
        return ConstructType.VARIABLE_DECLARATION;
    }

    @Override
    public TranslationOutcome apply(AstNode node, TranslationContext context) {
        String varName = node.getAttribute("varName");
        String plsqlType = node.getAttribute("dataType");
        if (varName == null || plsqlType == null) {
            return TranslationOutcome.partial("/* variable declaration */", "Missing variable metadata", 10);
        }

        String upperType = plsqlType.toUpperCase().replaceAll("\\(.*\\)", "").trim();

        if (upperType.contains("%TYPE") || upperType.contains("%ROWTYPE")) {
            String javaType = upperType.contains("%ROWTYPE") ? "java.util.Map<String, Object>" : "String";
            String snippet = javaType + " " + varName + " = null; // %TYPE resolved to " + javaType;
            context.registerVariable(varName, javaType);
            return TranslationOutcome.translated(snippet);
        }

        String javaType = TYPE_MAP.getOrDefault(upperType, "Object");
        String snippet = javaType + " " + varName + " = null;";
        context.registerVariable(varName, javaType);
        return TranslationOutcome.translated(snippet);
    }
}
