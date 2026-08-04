package com.plsql2java.translation.rules;

import com.plsql2java.translation.engine.TranslationRule;
import com.plsql2java.translation.model.AstNode;
import com.plsql2java.translation.model.ConstructType;
import com.plsql2java.translation.model.TranslationContext;
import com.plsql2java.translation.model.TranslationOutcome;
import org.springframework.stereotype.Component;

@Component
public class RaiseRule implements TranslationRule {

    @Override
    public ConstructType getConstructType() {
        return ConstructType.RAISE;
    }

    @Override
    public TranslationOutcome apply(AstNode node, TranslationContext context) {
        String rawText = node.getText().trim();
        // RAISE_APPLICATION_ERROR pattern
        if (rawText.toUpperCase().contains("RAISE_APPLICATION_ERROR")) {
            String snippet = "throw new IllegalStateException(\"Application error\"); "
                    + "// TODO: original was RAISE_APPLICATION_ERROR — review message and exception type";
            return TranslationOutcome.translated(snippet);
        }
        // Plain RAISE (re-raise)
        String snippet = "throw new RuntimeException(\"Re-raised from PL/SQL\"); "
                + "// TODO: original was RAISE — review exception type";
        return TranslationOutcome.translated(snippet);
    }
}
