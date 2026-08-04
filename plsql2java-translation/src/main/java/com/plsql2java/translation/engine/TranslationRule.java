package com.plsql2java.translation.engine;

import com.plsql2java.translation.model.AstNode;
import com.plsql2java.translation.model.ConstructType;
import com.plsql2java.translation.model.TranslationContext;
import com.plsql2java.translation.model.TranslationOutcome;

public interface TranslationRule {

    ConstructType getConstructType();

    TranslationOutcome apply(AstNode node, TranslationContext context);
}
