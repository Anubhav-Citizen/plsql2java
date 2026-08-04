package com.plsql2java.orchestration.model;

public enum PipelineStage {
    DISCOVERY,
    DEPENDENCY_ANALYSIS,
    TRANSLATION,
    CODE_GENERATION,
    CONFIDENCE_SCORING,
    REPORT_GENERATION,
    COMPLETE
}
