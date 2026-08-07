package com.plsql2java.translation.rules;

import com.plsql2java.translation.engine.TranslationRule;
import com.plsql2java.translation.model.AstNode;
import com.plsql2java.translation.model.ConstructType;
import com.plsql2java.translation.model.TranslationContext;
import com.plsql2java.translation.model.TranslationOutcome;
import org.springframework.stereotype.Component;

@Component
public class FunctionDefRule implements TranslationRule {

    @Override
    public ConstructType getConstructType() {
        return ConstructType.FUNCTION_DEF;
    }

    @Override
    public TranslationOutcome apply(AstNode node, TranslationContext context) {
        String name = node.getAttribute("name");
        String returnType = node.getAttribute("returnType");
        return TranslationOutcome.translated("// FUNCTION " + (name != null ? name : "unknown")
                + " RETURN " + (returnType != null ? returnType : "?") + " -> Java method");
    }
}
