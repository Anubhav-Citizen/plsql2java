package com.plsql2java.translation;

import com.plsql2java.model.OracleObjectType;
import com.plsql2java.translation.engine.TranslationMappingLoader;
import com.plsql2java.translation.model.AstNode;
import com.plsql2java.translation.model.BuiltinFunctionMapping;
import com.plsql2java.translation.model.ConstructType;
import com.plsql2java.translation.model.TranslationContext;
import com.plsql2java.translation.model.TranslationStatus;
import com.plsql2java.translation.rules.BuiltinFunctionRule;
import com.plsql2java.translation.rules.DbmsOutputRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuiltinFunctionAndDbmsOutputRulesTest {

    @Mock
    private TranslationMappingLoader mappingLoader;

    private TranslationContext ctx;

    @BeforeEach
    void setUp() {
        ctx = new TranslationContext("TEST", OracleObjectType.PROCEDURE, "SCHEMA");
    }

    @Test
    void builtinFunctionRule_knownFunction_producesTranslated() {
        BuiltinFunctionMapping mapping = new BuiltinFunctionMapping();
        mapping.setOracleFunction("UPPER");
        mapping.setJavaEquivalent("{0}.toUpperCase()");
        when(mappingLoader.getBuiltinMapping("UPPER")).thenReturn(mapping);

        var rule = new BuiltinFunctionRule(mappingLoader);
        var node = new AstNode(ConstructType.BUILTIN_FUNCTION, "UPPER(name)", 5);
        node.setAttribute("functionName", "UPPER");
        node.setAttribute("arg0", "name");

        var outcome = rule.apply(node, ctx);
        assertThat(outcome.getStatus()).isEqualTo(TranslationStatus.TRANSLATED);
        assertThat(outcome.getJavaSnippet()).isEqualTo("name.toUpperCase()");
    }

    @Test
    void builtinFunctionRule_unknownFunction_producesPartialWithTodo() {
        when(mappingLoader.getBuiltinMapping("DBMS_RANDOM")).thenReturn(null);

        var rule = new BuiltinFunctionRule(mappingLoader);
        var node = new AstNode(ConstructType.BUILTIN_FUNCTION, "DBMS_RANDOM.VALUE", 10);
        node.setAttribute("functionName", "DBMS_RANDOM");

        var outcome = rule.apply(node, ctx);
        assertThat(outcome.getStatus()).isEqualTo(TranslationStatus.PARTIAL);
        assertThat(outcome.getConfidencePenalty()).isEqualTo(20);
        assertThat(outcome.getJavaSnippet()).contains("TODO");
    }

    @Test
    void builtinFunctionRule_nullFunctionName_producesPartial() {
        var rule = new BuiltinFunctionRule(mappingLoader);
        var node = new AstNode(ConstructType.BUILTIN_FUNCTION, "unknown()", 15);
        // no functionName attribute set
        var outcome = rule.apply(node, ctx);
        assertThat(outcome.getStatus()).isEqualTo(TranslationStatus.PARTIAL);
    }

    @Test
    void dbmsOutputRule_producesLogDebug() {
        var rule = new DbmsOutputRule();
        var node = new AstNode(ConstructType.DBMS_OUTPUT, "DBMS_OUTPUT.PUT_LINE('hello');", 20);
        node.setAttribute("message", "'hello'");
        var outcome = rule.apply(node, ctx);
        assertThat(outcome.getStatus()).isEqualTo(TranslationStatus.TRANSLATED);
        assertThat(outcome.getJavaSnippet()).contains("log.debug");
        assertThat(outcome.getJavaSnippet()).contains("'hello'");
    }

    @Test
    void dbmsOutputRule_nullMessage_usesEmptyString() {
        var rule = new DbmsOutputRule();
        var node = new AstNode(ConstructType.DBMS_OUTPUT, "DBMS_OUTPUT.PUT_LINE();", 25);
        // no message attribute
        var outcome = rule.apply(node, ctx);
        assertThat(outcome.getStatus()).isEqualTo(TranslationStatus.TRANSLATED);
        assertThat(outcome.getJavaSnippet()).contains("log.debug");
    }
}
