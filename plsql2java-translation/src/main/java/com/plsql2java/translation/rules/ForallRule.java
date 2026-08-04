package com.plsql2java.translation.rules;

import com.plsql2java.translation.engine.TranslationRule;
import com.plsql2java.translation.model.AstNode;
import com.plsql2java.translation.model.ConstructType;
import com.plsql2java.translation.model.TranslationContext;
import com.plsql2java.translation.model.TranslationOutcome;
import org.springframework.stereotype.Component;

@Component
public class ForallRule implements TranslationRule {

    @Override
    public ConstructType getConstructType() {
        return ConstructType.FORALL;
    }

    @Override
    public TranslationOutcome apply(AstNode node, TranslationContext context) {
        if (node.getConstructType() == ConstructType.FORALL_SAVE_EXCEPTIONS) {
            return TranslationOutcome.flagged(
                    "FORALL with SAVE EXCEPTIONS has no direct Java equivalent",
                    "Use Spring Batch ItemWriter with skip policy for equivalent error handling",
                    40
            );
        }
        String rawText = node.getText().toUpperCase();
        String snippet;
        if (rawText.contains("DELETE")) {
            snippet = "// FORALL DELETE translated to batch delete\n"
                    + "        repository.deleteAll(collection); // TODO: replace 'collection' with actual list";
        } else {
            snippet = "// FORALL INSERT/UPDATE translated to batch save\n"
                    + "        repository.saveAll(collection); // TODO: replace 'collection' with actual list";
        }
        return TranslationOutcome.translated(snippet);
    }
}
