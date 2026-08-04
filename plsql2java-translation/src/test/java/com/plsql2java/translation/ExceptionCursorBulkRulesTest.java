package com.plsql2java.translation;

import com.plsql2java.model.OracleObjectType;
import com.plsql2java.translation.engine.TranslationMappingLoader;
import com.plsql2java.translation.model.AstNode;
import com.plsql2java.translation.model.ConstructType;
import com.plsql2java.translation.model.TranslationContext;
import com.plsql2java.translation.model.TranslationStatus;
import com.plsql2java.translation.rules.BulkCollectRule;
import com.plsql2java.translation.rules.ExceptionHandlerRule;
import com.plsql2java.translation.rules.ExplicitCursorRule;
import com.plsql2java.translation.rules.ForallRule;
import com.plsql2java.translation.rules.ImplicitCursorForRule;
import com.plsql2java.translation.rules.RaiseRule;
import com.plsql2java.translation.rules.RefCursorRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExceptionCursorBulkRulesTest {

    @Mock
    private TranslationMappingLoader mappingLoader;

    private TranslationContext ctx;

    @BeforeEach
    void setUp() {
        ctx = new TranslationContext("TEST_PROC", OracleObjectType.PROCEDURE, "SCHEMA");
    }

    // --- ExceptionHandlerRule ---

    @Test
    void exceptionHandlerRule_others_producesCatchException() {
        when(mappingLoader.getExceptionMapping(anyString())).thenReturn(null);
        var rule = new ExceptionHandlerRule(mappingLoader);
        var node = new AstNode(ConstructType.EXCEPTION_HANDLER, "WHEN OTHERS THEN NULL;", 10);
        node.setAttribute("exceptionName", "OTHERS");
        var outcome = rule.apply(node, ctx);
        assertThat(outcome.getStatus()).isEqualTo(TranslationStatus.TRANSLATED);
        assertThat(outcome.getJavaSnippet()).contains("catch (Exception e)");
        assertThat(outcome.getJavaSnippet()).contains("log.warn");
    }

    @Test
    void exceptionHandlerRule_noDataFound_producesMappedCatch() {
        var mapping = new com.plsql2java.translation.model.OracleExceptionMapping();
        mapping.setOracleException("NO_DATA_FOUND");
        mapping.setJavaException("EmptyResultDataAccessException");
        mapping.setRequiresImport("org.springframework.dao.EmptyResultDataAccessException");
        when(mappingLoader.getExceptionMapping("NO_DATA_FOUND")).thenReturn(mapping);
        var rule = new ExceptionHandlerRule(mappingLoader);
        var node = new AstNode(ConstructType.EXCEPTION_HANDLER, "WHEN NO_DATA_FOUND THEN NULL;", 15);
        node.setAttribute("exceptionName", "NO_DATA_FOUND");
        var outcome = rule.apply(node, ctx);
        assertThat(outcome.getStatus()).isEqualTo(TranslationStatus.TRANSLATED);
        assertThat(outcome.getJavaSnippet()).contains("EmptyResultDataAccessException");
    }

    @Test
    void raiseRule_plainRaise_producesThrow() {
        var rule = new RaiseRule();
        var node = new AstNode(ConstructType.RAISE, "RAISE;", 20);
        var outcome = rule.apply(node, ctx);
        assertThat(outcome.getStatus()).isEqualTo(TranslationStatus.TRANSLATED);
        assertThat(outcome.getJavaSnippet()).contains("throw new RuntimeException");
    }

    @Test
    void raiseRule_raiseApplicationError_producesIllegalState() {
        var rule = new RaiseRule();
        var node = new AstNode(ConstructType.RAISE, "RAISE_APPLICATION_ERROR(-20001, 'error');", 25);
        var outcome = rule.apply(node, ctx);
        assertThat(outcome.getStatus()).isEqualTo(TranslationStatus.TRANSLATED);
        assertThat(outcome.getJavaSnippet()).contains("IllegalStateException");
    }

    // --- Cursor rules ---

    @Test
    void explicitCursorRule_registersCursorAndProducesSnippet() {
        var rule = new ExplicitCursorRule();
        var node = new AstNode(ConstructType.EXPLICIT_CURSOR, "CURSOR c1 IS SELECT * FROM orders;", 30);
        node.setAttribute("cursorName", "C1");
        node.setAttribute("query", "SELECT * FROM orders");
        var outcome = rule.apply(node, ctx);
        assertThat(outcome.getStatus()).isEqualTo(TranslationStatus.TRANSLATED);
        assertThat(ctx.getCursorRegistry()).containsKey("C1");
    }

    @Test
    void implicitCursorForRule_producesEnhancedFor() {
        var rule = new ImplicitCursorForRule();
        var node = new AstNode(ConstructType.IMPLICIT_CURSOR_FOR, "FOR rec IN (SELECT * FROM t) LOOP NULL; END LOOP;", 35);
        var outcome = rule.apply(node, ctx);
        assertThat(outcome.getStatus()).isEqualTo(TranslationStatus.TRANSLATED);
        assertThat(outcome.getJavaSnippet()).contains("for (Object rec");
    }

    @Test
    void refCursorRule_alwaysFlagged() {
        var rule = new RefCursorRule();
        var node = new AstNode(ConstructType.REF_CURSOR, "p_cursor SYS_REFCURSOR", 40);
        var outcome = rule.apply(node, ctx);
        assertThat(outcome.getStatus()).isEqualTo(TranslationStatus.FLAGGED);
        assertThat(outcome.getConfidencePenalty()).isEqualTo(50);
    }

    // --- Bulk rules ---

    @Test
    void bulkCollectRule_basic_producesListFetch() {
        var rule = new BulkCollectRule();
        var node = new AstNode(ConstructType.BULK_COLLECT, "BULK COLLECT INTO v_list;", 45);
        var outcome = rule.apply(node, ctx);
        assertThat(outcome.getStatus()).isEqualTo(TranslationStatus.TRANSLATED);
        assertThat(outcome.getJavaSnippet()).contains("findAll()");
    }

    @Test
    void bulkCollectRule_withLimit_producesPaginatedFetch() {
        var rule = new BulkCollectRule();
        var node = new AstNode(ConstructType.BULK_COLLECT, "BULK COLLECT INTO v_list LIMIT 100;", 50);
        var outcome = rule.apply(node, ctx);
        assertThat(outcome.getStatus()).isEqualTo(TranslationStatus.TRANSLATED);
        assertThat(outcome.getJavaSnippet()).contains("PageRequest");
    }

    @Test
    void forallRule_insert_producesSaveAll() {
        var rule = new ForallRule();
        var node = new AstNode(ConstructType.FORALL, "FORALL i IN 1..v_list.COUNT INSERT INTO t VALUES (v_list(i));", 55);
        var outcome = rule.apply(node, ctx);
        assertThat(outcome.getStatus()).isEqualTo(TranslationStatus.TRANSLATED);
        assertThat(outcome.getJavaSnippet()).contains("saveAll");
    }

    @Test
    void forallRule_saveExceptions_alwaysFlagged() {
        var rule = new ForallRule();
        var node = new AstNode(ConstructType.FORALL_SAVE_EXCEPTIONS,
                "FORALL i IN 1..10 SAVE EXCEPTIONS INSERT INTO t VALUES (i);", 60);
        var outcome = rule.apply(node, ctx);
        assertThat(outcome.getStatus()).isEqualTo(TranslationStatus.FLAGGED);
        assertThat(outcome.getConfidencePenalty()).isEqualTo(40);
    }
}
