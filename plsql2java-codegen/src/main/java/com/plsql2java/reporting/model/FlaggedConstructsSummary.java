package com.plsql2java.reporting.model;

import com.plsql2java.translation.model.ConstructType;
import com.plsql2java.translation.model.FlaggedConstruct;

import java.util.List;
import java.util.Map;

public class FlaggedConstructsSummary {

    private final Map<ConstructType, List<FlaggedConstruct>> byConstructType;
    private final int totalCount;

    public FlaggedConstructsSummary(Map<ConstructType, List<FlaggedConstruct>> byConstructType) {
        this.byConstructType = byConstructType;
        this.totalCount = byConstructType.values().stream().mapToInt(List::size).sum();
    }

    public Map<ConstructType, List<FlaggedConstruct>> getByConstructType() { return byConstructType; }
    public int getTotalCount() { return totalCount; }
}
