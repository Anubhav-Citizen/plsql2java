package com.plsql2java.translation;

import com.plsql2java.model.OracleObjectType;
import com.plsql2java.translation.model.AstNode;
import com.plsql2java.translation.model.ConstructType;
import com.plsql2java.translation.model.TranslationContext;
import com.plsql2java.translation.model.TranslationStatus;
import com.plsql2java.translation.rules.CaseStatementRule;
import com.plsql2java.translation.rules.GotoRule;
import com.plsql2java.translation.rules.IfElseRule;
import com.plsql2java.translation.rules.LoopRule;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ControlFlowRulesTest {

    private TranslationContext ctx() {
        return new TranslationContext("TEST", OracleObjectType.PROCEDURE, "SCHEMA");
    }

    // --- IfElseRule ---

    @Test
    void ifElseRule_producesTranslatedOutcome() {
        var rule = new IfElseRule();
        var node = new AstNode(ConstructType.IF_ELSIF_ELSE, "IF x > 0 THEN NULL; END IF;", 5);
        var outcome = rule.apply(node, ctx());
        assertThat(outcome.getStatus()).isEqualTo(TranslationStatus.TRANSLATED);
        assertThat(outcome.getJavaSnippet()).contains("if (");
    }

    @Test
    void ifElseRule_constructType_isIfElsif() {
        assertThat(new IfElseRule().getConstructType()).isEqualTo(ConstructType.IF_ELSIF_ELSE);
    }

    // --- CaseStatementRule ---

    @Test
    void caseRule_producesTranslatedOutcome() {
        var rule = new CaseStatementRule();
        var node = new AstNode(ConstructType.CASE_STATEMENT, "CASE x WHEN 1 THEN NULL; END CASE;", 10);
        var outcome = rule.apply(node, ctx());
        assertThat(outcome.getStatus()).isEqualTo(TranslationStatus.TRANSLATED);
        assertThat(outcome.getJavaSnippet()).contains("CASE");
    }

    // --- LoopRule ---

    @Test
    void loopRule_basicLoop_producesWhileTrue() {
        var rule = new LoopRule();
        var node = new AstNode(ConstructType.LOOP, "LOOP NULL; END LOOP;", 15);
        var outcome = rule.apply(node, ctx());
        assertThat(outcome.getStatus()).isEqualTo(TranslationStatus.TRANSLATED);
        assertThat(outcome.getJavaSnippet()).contains("while (true)");
    }

    @Test
    void loopRule_whileLoop_producesWhile() {
        var rule = new LoopRule();
        var node = new AstNode(ConstructType.WHILE_LOOP, "WHILE x > 0 LOOP NULL; END LOOP;", 20);
        var outcome = rule.apply(node, ctx());
        assertThat(outcome.getStatus()).isEqualTo(TranslationStatus.TRANSLATED);
        assertThat(outcome.getJavaSnippet()).contains("while (");
    }

    @Test
    void loopRule_forLoop_producesForLoop() {
        var rule = new LoopRule();
        var node = new AstNode(ConstructType.FOR_LOOP, "FOR i IN 1..10 LOOP NULL; END LOOP;", 25);
        var outcome = rule.apply(node, ctx());
        assertThat(outcome.getStatus()).isEqualTo(TranslationStatus.TRANSLATED);
        assertThat(outcome.getJavaSnippet()).contains("for (int i");
    }

    // --- GotoRule ---

    @Test
    void gotoRule_alwaysFlagged() {
        var rule = new GotoRule();
        var node = new AstNode(ConstructType.GOTO, "GOTO label;", 30);
        var outcome = rule.apply(node, ctx());
        assertThat(outcome.getStatus()).isEqualTo(TranslationStatus.FLAGGED);
        assertThat(outcome.getConfidencePenalty()).isEqualTo(30);
        assertThat(outcome.getRecommendation()).contains("GOTO");
    }

    @Test
    void gotoRule_constructType_isGoto() {
        assertThat(new GotoRule().getConstructType()).isEqualTo(ConstructType.GOTO);
    }
}
