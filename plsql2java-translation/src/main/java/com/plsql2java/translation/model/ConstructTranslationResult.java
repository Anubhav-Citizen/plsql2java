package com.plsql2java.translation.model;

public class ConstructTranslationResult {

    private final ConstructType constructType;
    private final int lineNumber;
    private final TranslationStatus status;
    private final String javaSnippet;
    private final String flagReason;
    private final String recommendation;
    private final int confidencePenalty;
    private AstNode astNode;

    public ConstructTranslationResult(ConstructType constructType, int lineNumber, TranslationOutcome outcome) {
        this.constructType = constructType;
        this.lineNumber = lineNumber;
        this.status = outcome.getStatus();
        this.javaSnippet = outcome.getJavaSnippet();
        this.flagReason = outcome.getFlagReason();
        this.recommendation = outcome.getRecommendation();
        this.confidencePenalty = outcome.getConfidencePenalty();
    }

    public void setAstNode(AstNode node) { this.astNode = node; }
    public AstNode getAstNode() { return astNode; }

    public ConstructType getConstructType() { return constructType; }
    public int getLineNumber() { return lineNumber; }
    public TranslationStatus getStatus() { return status; }
    public String getJavaSnippet() { return javaSnippet; }
    public String getFlagReason() { return flagReason; }
    public String getRecommendation() { return recommendation; }
    public int getConfidencePenalty() { return confidencePenalty; }
}
