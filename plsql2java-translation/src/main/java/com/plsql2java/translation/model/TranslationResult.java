package com.plsql2java.translation.model;

import com.plsql2java.model.OracleObject;

import java.util.List;

public class TranslationResult {

    private final OracleObject sourceObject;
    private final JavaIR javaIR;
    private final List<ConstructTranslationResult> constructResults;
    private final List<FlaggedConstruct> flaggedConstructs;
    private final int translatedCount;
    private final int flaggedCount;
    private final int partialCount;
    private final TranslationStatus overallStatus;

    public TranslationResult(OracleObject sourceObject, JavaIR javaIR,
                             List<ConstructTranslationResult> constructResults,
                             List<FlaggedConstruct> flaggedConstructs) {
        this.sourceObject = sourceObject;
        this.javaIR = javaIR;
        this.constructResults = constructResults;
        this.flaggedConstructs = flaggedConstructs;
        this.translatedCount = (int) constructResults.stream()
                .filter(r -> r.getStatus() == TranslationStatus.TRANSLATED).count();
        this.partialCount = (int) constructResults.stream()
                .filter(r -> r.getStatus() == TranslationStatus.PARTIAL).count();
        this.flaggedCount = (int) constructResults.stream()
                .filter(r -> r.getStatus() == TranslationStatus.FLAGGED).count();
        this.overallStatus = deriveOverallStatus();
    }

    private TranslationStatus deriveOverallStatus() {
        if (flaggedCount > 0) return TranslationStatus.FLAGGED;
        if (partialCount > 0) return TranslationStatus.PARTIAL;
        return TranslationStatus.TRANSLATED;
    }

    public OracleObject getSourceObject() { return sourceObject; }
    public JavaIR getJavaIR() { return javaIR; }
    public List<ConstructTranslationResult> getConstructResults() { return constructResults; }
    public List<FlaggedConstruct> getFlaggedConstructs() { return flaggedConstructs; }
    public int getTranslatedCount() { return translatedCount; }
    public int getFlaggedCount() { return flaggedCount; }
    public int getPartialCount() { return partialCount; }
    public TranslationStatus getOverallStatus() { return overallStatus; }
}
