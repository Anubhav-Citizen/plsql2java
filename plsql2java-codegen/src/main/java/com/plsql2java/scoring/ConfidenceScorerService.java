package com.plsql2java.scoring;

import com.plsql2java.scoring.model.ConfidenceReport;
import com.plsql2java.scoring.model.MethodConfidenceScore;
import com.plsql2java.scoring.model.ObjectConfidenceScore;
import com.plsql2java.translation.model.ConstructTranslationResult;
import com.plsql2java.translation.model.TranslationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConfidenceScorerService {

    private static final Logger log = LoggerFactory.getLogger(ConfidenceScorerService.class);

    public ConfidenceReport scoreAll(List<TranslationResult> results, int threshold) {
        MDC.put("component", "ConfidenceScorerService");
        try {
            List<ObjectConfidenceScore> objectScores = results.stream()
                    .map(r -> scoreObject(r, threshold))
                    .collect(Collectors.toList());

            int overallScore = computeWeightedOverall(results, objectScores);
            String migrationId = results.isEmpty() ? "unknown" : results.get(0).getSourceObject().getSchema();
            log.info("Scoring complete: {} objects, overall score={}", objectScores.size(), overallScore);
            return new ConfidenceReport(migrationId, threshold, objectScores, overallScore);
        } finally {
            MDC.clear();
        }
    }

    public ObjectConfidenceScore scoreObject(TranslationResult result, int threshold) {
        MDC.put("objectName", result.getSourceObject().getName());
        boolean hasCompilationErrors = result.getSourceObject().isHasCompilationErrors();

        if (result.getJavaIR() == null) {
            return new ObjectConfidenceScore(
                    result.getSourceObject().getName(),
                    result.getSourceObject().getType(),
                    0, true, List.of(), hasCompilationErrors);
        }

        List<MethodConfidenceScore> methodScores = result.getJavaIR().getMethods().stream()
                .map(m -> scoreMethod(m.getConstructResults(), m.getMethodName(),
                        result.getSourceObject().getName(), threshold))
                .collect(Collectors.toList());

        int rawScore = methodScores.isEmpty() ? 100
                : (int) methodScores.stream().mapToInt(MethodConfidenceScore::getScore).average().orElse(100);

        int finalScore = hasCompilationErrors ? Math.min(rawScore, 50) : rawScore;
        finalScore = Math.max(0, Math.min(100, finalScore));

        return new ObjectConfidenceScore(
                result.getSourceObject().getName(),
                result.getSourceObject().getType(),
                finalScore,
                finalScore < threshold,
                methodScores,
                hasCompilationErrors);
    }

    public MethodConfidenceScore scoreMethod(List<ConstructTranslationResult> constructResults,
                                              String methodName, String objectName, int threshold) {
        if (constructResults == null || constructResults.isEmpty()) {
            return new MethodConfidenceScore(objectName, methodName, 100, false, List.of());
        }

        int total = constructResults.size();
        int totalPenalty = constructResults.stream().mapToInt(ConstructTranslationResult::getConfidencePenalty).sum();
        int rawScore = 100 - (totalPenalty / Math.max(total, 1));
        int score = Math.max(0, Math.min(100, rawScore));

        List<String> reasons = constructResults.stream()
                .filter(c -> c.getConfidencePenalty() > 0 && c.getFlagReason() != null)
                .map(ConstructTranslationResult::getFlagReason)
                .collect(Collectors.toList());

        return new MethodConfidenceScore(objectName, methodName, score, score < threshold, reasons);
    }

    private int computeWeightedOverall(List<TranslationResult> results, List<ObjectConfidenceScore> scores) {
        if (scores.isEmpty()) return 100;
        long totalLines = results.stream().mapToLong(r -> r.getSourceObject().getLineCount()).sum();
        if (totalLines == 0) {
            return (int) scores.stream().mapToInt(ObjectConfidenceScore::getScore).average().orElse(100);
        }
        double weighted = 0;
        for (int i = 0; i < results.size(); i++) {
            long lines = results.get(i).getSourceObject().getLineCount();
            weighted += scores.get(i).getScore() * (double) lines / totalLines;
        }
        return Math.max(0, Math.min(100, (int) Math.round(weighted)));
    }
}
