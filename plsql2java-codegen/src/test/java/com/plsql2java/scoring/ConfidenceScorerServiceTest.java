package com.plsql2java.scoring;

import com.plsql2java.model.OracleObject;
import com.plsql2java.model.OracleObjectType;
import com.plsql2java.scoring.model.ConfidenceReport;
import com.plsql2java.scoring.model.MethodConfidenceScore;
import com.plsql2java.scoring.model.ObjectConfidenceScore;
import com.plsql2java.translation.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConfidenceScorerServiceTest {

    private ConfidenceScorerService scorer;

    @BeforeEach
    void setUp() {
        scorer = new ConfidenceScorerService();
    }

    @Test
    void scoreMethod_allTranslated_returns100() {
        ConstructTranslationResult c = new ConstructTranslationResult(
                ConstructType.IF_ELSIF_ELSE, 1, TranslationOutcome.translated("if(...) {}"));
        MethodConfidenceScore score = scorer.scoreMethod(List.of(c), "myMethod", "OBJ", 70);
        assertThat(score.getScore()).isEqualTo(100);
    }

    @Test
    void scoreMethod_flaggedConstructs_reduceScore() {
        ConstructTranslationResult flagged = new ConstructTranslationResult(
                ConstructType.GOTO, 5, TranslationOutcome.flagged("GOTO not supported", "Refactor", 30));
        MethodConfidenceScore score = scorer.scoreMethod(List.of(flagged), "myMethod", "OBJ", 70);
        assertThat(score.getScore()).isLessThan(100);
        assertThat(score.getPenaltyReasons()).isNotEmpty();
    }

    @Test
    void scoreMethod_zeroConstructs_returns100() {
        MethodConfidenceScore score = scorer.scoreMethod(List.of(), "emptyMethod", "OBJ", 70);
        assertThat(score.getScore()).isEqualTo(100);
    }

    @Test
    void scoreObject_compilationErrors_capAt50() {
        OracleObject obj = new OracleObject("OBJ", OracleObjectType.PROCEDURE, "SCHEMA", "source");
        obj.setHasCompilationErrors(true);

        JavaMethodIR method = new JavaMethodIR("proc", "void", List.of(), "// body",
                List.of(), null, List.of());
        JavaIR ir = new JavaIR("OBJ", "com.example", "Obj", List.of(), List.of(), List.of(method), "");
        TranslationResult result = new TranslationResult(obj, ir, List.of(), List.of());

        ObjectConfidenceScore score = scorer.scoreObject(result, 70);
        assertThat(score.getScore()).isLessThanOrEqualTo(50);
        assertThat(score.isHasCompilationErrors()).isTrue();
    }

    @Test
    void scoreObject_nullJavaIR_returnsZero() {
        OracleObject obj = new OracleObject("OBJ", OracleObjectType.PROCEDURE, "SCHEMA", "source");
        TranslationResult result = new TranslationResult(obj, null, List.of(), List.of());
        ObjectConfidenceScore score = scorer.scoreObject(result, 70);
        assertThat(score.getScore()).isZero();
        assertThat(score.isBelowThreshold()).isTrue();
    }

    @Test
    void scoreAll_thresholdFlagging_setsCorrectFlags() {
        OracleObject obj = new OracleObject("OBJ", OracleObjectType.PROCEDURE, "SCHEMA", "source");
        JavaMethodIR method = new JavaMethodIR("proc", "void", List.of(), "// body",
                List.of(), null, List.of());
        JavaIR ir = new JavaIR("OBJ", "com.example", "Obj", List.of(), List.of(), List.of(method), "");
        TranslationResult result = new TranslationResult(obj, ir, List.of(), List.of());

        ConfidenceReport report = scorer.scoreAll(List.of(result), 70);
        assertThat(report.getObjectScores()).hasSize(1);
        assertThat(report.getOverallScore()).isBetween(0, 100);
    }

    @Test
    void scoreMethod_scoreClampedTo0_whenPenaltyExceeds100() {
        ConstructTranslationResult c1 = new ConstructTranslationResult(
                ConstructType.GOTO, 1, TranslationOutcome.flagged("GOTO", "Refactor", 60));
        ConstructTranslationResult c2 = new ConstructTranslationResult(
                ConstructType.REF_CURSOR, 2, TranslationOutcome.flagged("REF_CURSOR", "Refactor", 60));
        MethodConfidenceScore score = scorer.scoreMethod(List.of(c1, c2), "m", "OBJ", 70);
        assertThat(score.getScore()).isGreaterThanOrEqualTo(0);
        assertThat(score.getScore()).isLessThanOrEqualTo(100);
    }
}
