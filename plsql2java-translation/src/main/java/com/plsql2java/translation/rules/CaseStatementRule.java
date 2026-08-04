package com.plsql2java.translation.rules;

import com.plsql2java.translation.engine.TranslationRule;
import com.plsql2java.translation.model.AstNode;
import com.plsql2java.translation.model.ConstructType;
import com.plsql2java.translation.model.TranslationContext;
import com.plsql2java.translation.model.TranslationOutcome;
import org.springframework.stereotype.Component;

@Component
public class CaseStatementRule implements TranslationRule {

    @Override
    public ConstructType getConstructType() {
        return ConstructType.CASE_STATEMENT;
    }

    @Override
    public TranslationOutcome apply(AstNode node, TranslationContext context) {
        String snippet = "// CASE statement translated from PL/SQL\n"
                + "        // TODO: Replace with Java switch expression or if/else chain\n"
                + "        /* original: " + sanitize(node.getText(), 100) + " */";
        return TranslationOutcome.translated(snippet);
    }

    private String sanitize(String text, int maxLen) {
        String s = text.replaceAll("\\s+", " ").trim();
        return s.length() > maxLen ? s.substring(0, maxLen) + "..." : s;
    }
}
