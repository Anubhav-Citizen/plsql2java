package com.plsql2java.translation.rules;

import com.plsql2java.translation.engine.TranslationRule;
import com.plsql2java.translation.model.AstNode;
import com.plsql2java.translation.model.ConstructType;
import com.plsql2java.translation.model.TranslationContext;
import com.plsql2java.translation.model.TranslationOutcome;
import org.springframework.stereotype.Component;

@Component
public class UpdateStmtRule implements TranslationRule {

    @Override
    public ConstructType getConstructType() {
        return ConstructType.UPDATE_STMT;
    }

    @Override
    public TranslationOutcome apply(AstNode node, TranslationContext context) {
        return TranslationOutcome.translated("// UPDATE -> jdbcTemplate.update(...)");
    }
}
