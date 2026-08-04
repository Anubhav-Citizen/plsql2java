package com.plsql2java.translation.rules;

import com.plsql2java.translation.engine.TranslationRule;
import com.plsql2java.translation.model.AstNode;
import com.plsql2java.translation.model.ConstructType;
import com.plsql2java.translation.model.TranslationContext;
import com.plsql2java.translation.model.TranslationOutcome;
import org.springframework.stereotype.Component;

@Component
public class ImplicitCursorForRule implements TranslationRule {

    @Override
    public ConstructType getConstructType() {
        return ConstructType.IMPLICIT_CURSOR_FOR;
    }

    @Override
    public TranslationOutcome apply(AstNode node, TranslationContext context) {
        String snippet = "// Implicit cursor FOR loop translated to enhanced for\n"
                + "        for (Object rec : repository.findAll()) { // TODO: replace with typed repository method\n"
                + "            // TODO: implement loop body\n"
                + "        }";
        return TranslationOutcome.translated(snippet);
    }
}
