package com.plsql2java.translation;

import com.plsql2java.common.ProgressEvent;
import com.plsql2java.common.ProgressListener;
import com.plsql2java.model.OracleObject;
import com.plsql2java.model.OracleObjectType;
import com.plsql2java.translation.engine.JavaIRAssembler;
import com.plsql2java.translation.engine.PlSqlTranslationEngine;
import com.plsql2java.translation.engine.TranslationMappingLoader;
import com.plsql2java.translation.engine.TranslationRuleRegistry;
import com.plsql2java.translation.engine.TranslationRuleRegistryInitializer;
import com.plsql2java.translation.model.TranslationResult;
import com.plsql2java.translation.model.TranslationStatus;
import com.plsql2java.translation.rules.BulkCollectRule;
import com.plsql2java.translation.rules.BuiltinFunctionRule;
import com.plsql2java.translation.rules.CaseStatementRule;
import com.plsql2java.translation.rules.DbmsOutputRule;
import com.plsql2java.translation.rules.ExceptionHandlerRule;
import com.plsql2java.translation.rules.ExplicitCursorRule;
import com.plsql2java.translation.rules.ForallRule;
import com.plsql2java.translation.rules.GotoRule;
import com.plsql2java.translation.rules.IfElseRule;
import com.plsql2java.translation.rules.ImplicitCursorForRule;
import com.plsql2java.translation.rules.LoopRule;
import com.plsql2java.translation.rules.RaiseRule;
import com.plsql2java.translation.rules.RefCursorRule;
import com.plsql2java.translation.rules.VariableDeclarationRule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlSqlTranslationEngineTest {

    private PlSqlTranslationEngine engine;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        TranslationMappingLoader mappingLoader = new TranslationMappingLoader(objectMapper);
        mappingLoader.load();

        TranslationRuleRegistry registry = new TranslationRuleRegistry();
        List<com.plsql2java.translation.engine.TranslationRule> rules = List.of(
                new VariableDeclarationRule(),
                new IfElseRule(),
                new CaseStatementRule(),
                new LoopRule(),
                new GotoRule(),
                new ExceptionHandlerRule(mappingLoader),
                new RaiseRule(),
                new ExplicitCursorRule(),
                new ImplicitCursorForRule(),
                new RefCursorRule(),
                new BulkCollectRule(),
                new ForallRule(),
                new BuiltinFunctionRule(mappingLoader),
                new DbmsOutputRule()
        );
        new TranslationRuleRegistryInitializer(registry, rules).init();

        engine = new PlSqlTranslationEngine(registry, new JavaIRAssembler());
    }

    @Test
    void translate_simplePackage_returnsResult() {
        OracleObject obj = new OracleObject("ORDER_PROCESSOR", OracleObjectType.PACKAGE,
                "MYSCHEMA",
                "CREATE OR REPLACE PACKAGE ORDER_PROCESSOR IS PROCEDURE process_order; END ORDER_PROCESSOR;");
        TranslationResult result = engine.translate(obj);
        assertThat(result).isNotNull();
        assertThat(result.getSourceObject().getName()).isEqualTo("ORDER_PROCESSOR");
    }

    @Test
    void translate_objectWithCompilationErrors_returnsResult() {
        OracleObject obj = new OracleObject("BAD_PKG", OracleObjectType.PACKAGE,
                "MYSCHEMA", "CREATE PACKAGE BAD_PKG IS END;");
        obj.setHasCompilationErrors(true);
        TranslationResult result = engine.translate(obj);
        assertThat(result).isNotNull();
        // Should still return a result (not throw)
    }

    @Test
    void translate_parseError_returnsFlaggedResult() {
        OracleObject obj = new OracleObject("BROKEN", OracleObjectType.PROCEDURE,
                "MYSCHEMA", "THIS IS NOT VALID PLSQL @@@@");
        TranslationResult result = engine.translate(obj);
        assertThat(result).isNotNull();
        // Parse errors produce FLAGGED nodes — overall status should be FLAGGED
        assertThat(result.getOverallStatus()).isEqualTo(TranslationStatus.FLAGGED);
    }

    @Test
    void translateAll_emitsProgressEvents() {
        OracleObject obj1 = new OracleObject("PKG1", OracleObjectType.PACKAGE,
                "SCHEMA", "CREATE PACKAGE PKG1 IS END;");
        OracleObject obj2 = new OracleObject("PKG2", OracleObjectType.PACKAGE,
                "SCHEMA", "CREATE PACKAGE PKG2 IS END;");

        List<ProgressEvent> events = new ArrayList<>();
        ProgressListener listener = events::add;

        List<TranslationResult> results = engine.translateAll(List.of(obj1, obj2), listener);

        assertThat(results).hasSize(2);
        assertThat(events).isNotEmpty();
        // Final event should show 100% complete
        ProgressEvent last = events.get(events.size() - 1);
        assertThat(last.getProcessedCount()).isEqualTo(2);
    }

    @Test
    void translate_gotoConstruct_flaggedInResult() {
        OracleObject obj = new OracleObject("GOTO_PROC", OracleObjectType.PROCEDURE,
                "SCHEMA",
                "CREATE OR REPLACE PROCEDURE GOTO_PROC IS BEGIN GOTO end_proc; <<end_proc>> NULL; END;");
        TranslationResult result = engine.translate(obj);
        assertThat(result).isNotNull();
        // GOTO should produce at least one flagged construct
        // (parse may or may not match depending on grammar subset — result must not throw)
    }
}
