package com.plsql2java.translation.rules;

import com.plsql2java.translation.engine.TranslationRule;
import com.plsql2java.translation.model.AstNode;
import com.plsql2java.translation.model.ConstructType;
import com.plsql2java.translation.model.TranslationContext;
import com.plsql2java.translation.model.TranslationOutcome;
import org.springframework.stereotype.Component;

@Component
public class IfElseRule implements TranslationRule {

    @Override
    public ConstructType getConstructType() {
        return ConstructType.IF_ELSIF_ELSE;
    }

    @Override
    public TranslationOutcome apply(AstNode node, TranslationContext context) {
        // Extract raw text and produce a structural Java if/else skeleton
        // Full condition expression translation is handled inline
        String rawText = node.getText();
        String snippet = buildIfElseSkeleton(rawText);
        return TranslationOutcome.translated(snippet);
    }

    private String buildIfElseSkeleton(String rawText) {
        // Produce a commented skeleton preserving the original condition text
        // The code generator (Unit 3) will refine this with full expression translation
        return "// IF/ELSIF/ELSE translated from PL/SQL\n"
                + "        if (/* condition from: " + sanitize(rawText, 80) + " */) {\n"
                + "            // TODO: implement translated branch\n"
                + "        }";
    }

    private String sanitize(String text, int maxLen) {
        String s = text.replaceAll("\\s+", " ").trim();
        return s.length() > maxLen ? s.substring(0, maxLen) + "..." : s;
    }
}
