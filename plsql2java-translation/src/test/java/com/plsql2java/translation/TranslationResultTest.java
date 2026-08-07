package com.plsql2java.translation;

import com.plsql2java.model.OracleObject;
import com.plsql2java.model.OracleObjectType;
import com.plsql2java.translation.model.ConstructTranslationResult;
import com.plsql2java.translation.model.ConstructType;
import com.plsql2java.translation.model.FlaggedConstruct;
import com.plsql2java.translation.model.TranslationOutcome;
import com.plsql2java.translation.model.TranslationResult;
import com.plsql2java.translation.model.TranslationStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TranslationResultTest {

    private OracleObject sampleObject() {
        return new OracleObject("TEST_PKG", OracleObjectType.PACKAGE, "MYSCHEMA",
                "CREATE PACKAGE TEST_PKG IS END;");
    }

    @Test
    void overallStatus_allTranslated_isTranslated() {
        ConstructTranslationResult r = new ConstructTranslationResult(
                ConstructType.IF_ELSIF_ELSE, 1, TranslationOutcome.translated("if (x) {}"));
        TranslationResult result = new TranslationResult(sampleObject(), null, List.of(r), List.of());
        assertThat(result.getOverallStatus()).isEqualTo(TranslationStatus.TRANSLATED);
        assertThat(result.getTranslatedCount()).isEqualTo(1);
        assertThat(result.getFlaggedCount()).isZero();
    }

    @Test
    void overallStatus_hasFlagged_isFlagged() {
        ConstructTranslationResult r = new ConstructTranslationResult(
                ConstructType.GOTO, 5, TranslationOutcome.flagged("GOTO unsupported", "Refactor", 30));
        TranslationResult result = new TranslationResult(sampleObject(), null, List.of(r), List.of());
        assertThat(result.getOverallStatus()).isEqualTo(TranslationStatus.FLAGGED);
        assertThat(result.getFlaggedCount()).isEqualTo(1);
    }

    @Test
    void overallStatus_hasPartial_isPartial() {
        ConstructTranslationResult r = new ConstructTranslationResult(
                ConstructType.VARIABLE_DECLARATION, 2,
                TranslationOutcome.partial("String x = null;", "Used %TYPE", 10));
        TranslationResult result = new TranslationResult(sampleObject(), null, List.of(r), List.of());
        assertThat(result.getOverallStatus()).isEqualTo(TranslationStatus.PARTIAL);
        assertThat(result.getPartialCount()).isEqualTo(1);
    }

    @Test
    void flaggedConstructs_areAccessible() {
        FlaggedConstruct fc = new FlaggedConstruct("TEST_PKG", ConstructType.GOTO, 10, "reason", "rec");
        TranslationResult result = new TranslationResult(sampleObject(), null, List.of(), List.of(fc));
        assertThat(result.getFlaggedConstructs()).hasSize(1);
        assertThat(result.getFlaggedConstructs().get(0).getObjectName()).isEqualTo("TEST_PKG");
    }
}
