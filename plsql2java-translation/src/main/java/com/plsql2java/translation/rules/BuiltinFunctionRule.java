package com.plsql2java.translation.rules;

import com.plsql2java.translation.engine.TranslationMappingLoader;
import com.plsql2java.translation.engine.TranslationRule;
import com.plsql2java.translation.model.AstNode;
import com.plsql2java.translation.model.BuiltinFunctionMapping;
import com.plsql2java.translation.model.ConstructType;
import com.plsql2java.translation.model.TranslationContext;
import com.plsql2java.translation.model.TranslationOutcome;
import org.springframework.stereotype.Component;

@Component
public class BuiltinFunctionRule implements TranslationRule {

    private final TranslationMappingLoader mappingLoader;

    public BuiltinFunctionRule(TranslationMappingLoader mappingLoader) {
        this.mappingLoader = mappingLoader;
    }

    @Override
    public ConstructType getConstructType() {
        return ConstructType.BUILTIN_FUNCTION;
    }

    @Override
    public TranslationOutcome apply(AstNode node, TranslationContext context) {
        String functionName = node.getAttribute("functionName");
        if (functionName == null) {
            return TranslationOutcome.partial(
                    "/* TODO: unknown built-in function */",
                    "Could not determine function name from AST node",
                    20
            );
        }

        BuiltinFunctionMapping mapping = mappingLoader.getBuiltinMapping(functionName);
        if (mapping == null) {
            return TranslationOutcome.partial(
                    "/* TODO: unsupported Oracle built-in: " + functionName + " */",
                    "No Java equivalent found for Oracle function: " + functionName,
                    20
            );
        }

        // Replace {0}, {1}, {2} placeholders with actual arguments from node attributes
        String javaExpr = mapping.getJavaEquivalent();
        for (int i = 0; i < 5; i++) {
            String arg = node.getAttribute("arg" + i);
            if (arg != null) {
                javaExpr = javaExpr.replace("{" + i + "}", arg);
            }
        }

        return TranslationOutcome.translated(javaExpr);
    }
}
