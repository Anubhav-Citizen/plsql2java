package com.plsql2java.translation.model;

public class TranslationOutcome {

    private final TranslationStatus status;
    private final String javaSnippet;
    private final String flagReason;
    private final String recommendation;
    private final int confidencePenalty;

    private TranslationOutcome(Builder b) {
        this.status = b.status;
        this.javaSnippet = b.javaSnippet;
        this.flagReason = b.flagReason;
        this.recommendation = b.recommendation;
        this.confidencePenalty = b.confidencePenalty;
    }

    public static TranslationOutcome translated(String javaSnippet) {
        return new Builder(TranslationStatus.TRANSLATED).javaSnippet(javaSnippet).build();
    }

    public static TranslationOutcome partial(String javaSnippet, String flagReason, int confidencePenalty) {
        return new Builder(TranslationStatus.PARTIAL)
                .javaSnippet(javaSnippet).flagReason(flagReason).confidencePenalty(confidencePenalty).build();
    }

    public static TranslationOutcome flagged(String flagReason, String recommendation, int confidencePenalty) {
        return new Builder(TranslationStatus.FLAGGED)
                .flagReason(flagReason).recommendation(recommendation).confidencePenalty(confidencePenalty).build();
    }

    public TranslationStatus getStatus() { return status; }
    public String getJavaSnippet() { return javaSnippet; }
    public String getFlagReason() { return flagReason; }
    public String getRecommendation() { return recommendation; }
    public int getConfidencePenalty() { return confidencePenalty; }

    public static class Builder {
        private final TranslationStatus status;
        private String javaSnippet;
        private String flagReason;
        private String recommendation;
        private int confidencePenalty;

        public Builder(TranslationStatus status) { this.status = status; }
        public Builder javaSnippet(String v) { this.javaSnippet = v; return this; }
        public Builder flagReason(String v) { this.flagReason = v; return this; }
        public Builder recommendation(String v) { this.recommendation = v; return this; }
        public Builder confidencePenalty(int v) { this.confidencePenalty = v; return this; }
        public TranslationOutcome build() { return new TranslationOutcome(this); }
    }
}
