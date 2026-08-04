package com.plsql2java.translation.rules;

import com.plsql2java.translation.engine.TranslationRule;
import com.plsql2java.translation.model.AstNode;
import com.plsql2java.translation.model.ConstructType;
import com.plsql2java.translation.model.TranslationContext;
import com.plsql2java.translation.model.TranslationOutcome;
import org.springframework.stereotype.Component;

@Component
public class DbmsOutputRule implements TranslationRule {

    @Override
    public ConstructType getConstructType() {
        return ConstructType.DBMS_OUTPUT;
    }

    @Override
    public TranslationOutcome apply(AstNode node, TranslationContext context) {
        String message = node.getAttribute("message");
        if (message == null) message = "\"\"";
        // Produces log.debug call; JavaIRAssembler detects this and adds Logger field
        String snippet = "log.debug(\"{}\", " + message + ");";
        return TranslationOutcome.translated(snippet);
    }
}
