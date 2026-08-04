package com.plsql2java.translation.engine;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TranslationRuleRegistryInitializer {

    private final TranslationRuleRegistry registry;
    private final List<TranslationRule> rules;

    public TranslationRuleRegistryInitializer(TranslationRuleRegistry registry, List<TranslationRule> rules) {
        this.registry = registry;
        this.rules = rules;
    }

    @PostConstruct
    public void init() {
        rules.forEach(registry::registerRule);
    }
}
