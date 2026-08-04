package com.plsql2java.translation.engine;

import com.plsql2java.translation.model.ConstructType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class TranslationRuleRegistry {

    private final Map<ConstructType, List<TranslationRule>> rules = new EnumMap<>(ConstructType.class);

    public void registerRule(TranslationRule rule) {
        rules.computeIfAbsent(rule.getConstructType(), k -> new ArrayList<>()).add(rule);
    }

    public List<TranslationRule> getRulesForConstruct(ConstructType type) {
        return rules.getOrDefault(type, Collections.emptyList());
    }
}
