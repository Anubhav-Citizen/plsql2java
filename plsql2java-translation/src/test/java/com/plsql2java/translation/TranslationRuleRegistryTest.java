package com.plsql2java.translation;

import com.plsql2java.translation.engine.TranslationRule;
import com.plsql2java.translation.engine.TranslationRuleRegistry;
import com.plsql2java.translation.model.AstNode;
import com.plsql2java.translation.model.ConstructType;
import com.plsql2java.translation.model.TranslationContext;
import com.plsql2java.translation.model.TranslationOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TranslationRuleRegistryTest {

    private TranslationRuleRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new TranslationRuleRegistry();
    }

    @Test
    void getRulesForConstruct_noRulesRegistered_returnsEmptyList() {
        assertThat(registry.getRulesForConstruct(ConstructType.GOTO)).isEmpty();
    }

    @Test
    void registerRule_thenGetRules_returnsRule() {
        TranslationRule rule = new StubRule(ConstructType.GOTO);
        registry.registerRule(rule);
        List<TranslationRule> rules = registry.getRulesForConstruct(ConstructType.GOTO);
        assertThat(rules).hasSize(1).contains(rule);
    }

    @Test
    void registerMultipleRules_sameType_allReturned() {
        registry.registerRule(new StubRule(ConstructType.IF_ELSIF_ELSE));
        registry.registerRule(new StubRule(ConstructType.IF_ELSIF_ELSE));
        assertThat(registry.getRulesForConstruct(ConstructType.IF_ELSIF_ELSE)).hasSize(2);
    }

    @Test
    void getRulesForConstruct_differentType_returnsEmpty() {
        registry.registerRule(new StubRule(ConstructType.GOTO));
        assertThat(registry.getRulesForConstruct(ConstructType.IF_ELSIF_ELSE)).isEmpty();
    }

    private record StubRule(ConstructType type) implements TranslationRule {
        @Override public ConstructType getConstructType() { return type; }
        @Override public TranslationOutcome apply(AstNode node, TranslationContext ctx) {
            return TranslationOutcome.translated("stub");
        }
    }
}
