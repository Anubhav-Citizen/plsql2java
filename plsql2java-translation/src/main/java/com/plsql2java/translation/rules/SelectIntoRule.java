package com.plsql2java.translation.rules;

import com.plsql2java.translation.engine.TranslationRule;
import com.plsql2java.translation.model.AstNode;
import com.plsql2java.translation.model.ConstructType;
import com.plsql2java.translation.model.TranslationContext;
import com.plsql2java.translation.model.TranslationOutcome;
import org.springframework.stereotype.Component;

@Component
public class SelectIntoRule implements TranslationRule {

    @Override
    public ConstructType getConstructType() {
        return ConstructType.SELECT_INTO;
    }

    @Override
    public TranslationOutcome apply(AstNode node, TranslationContext context) {
        return TranslationOutcome.translated("// SELECT INTO -> jdbcTemplate.queryForObject(...)");
    }
}
