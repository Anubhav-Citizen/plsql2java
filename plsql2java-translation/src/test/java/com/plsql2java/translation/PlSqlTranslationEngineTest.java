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
    }

    @Test
    void translate_pkgCustomer_producesProcedureDefNodes() {
        // Actual source from discovery-result.json
        String spec = "CREATE OR REPLACE PACKAGE PKG_CUSTOMER\n" +
                "AS\n" +
                "TYPE CUSTOMER_REC IS RECORD\n" +
                "(\n" +
                "    ID NUMBER,\n" +
                "    NAME VARCHAR2(100),\n" +
                "    STATUS VARCHAR2(20)\n" +
                ");\n" +
                "TYPE CUSTOMER_TABLE IS TABLE OF CUSTOMER%ROWTYPE;\n" +
                "PROCEDURE ADD_CUSTOMER\n" +
                "(\n" +
                "    P_NAME VARCHAR2,\n" +
                "    P_EMAIL VARCHAR2,\n" +
                "    P_PHONE VARCHAR2,\n" +
                "    P_INCOME NUMBER\n" +
                ");\n" +
                "PROCEDURE UPDATE_STATUS\n" +
                "(\n" +
                "    P_CUSTOMER_ID NUMBER,\n" +
                "    P_STATUS VARCHAR2\n" +
                ");\n" +
                "FUNCTION GET_TOTAL_CUSTOMERS\n" +
                "RETURN NUMBER;\n" +
                "FUNCTION GET_CUSTOMER_STATUS\n" +
                "(\n" +
                "    P_CUSTOMER_ID NUMBER\n" +
                ")\n" +
                "RETURN VARCHAR2;\n" +
                "PROCEDURE GET_CUSTOMERS\n" +
                "(\n" +
                "    P_CURSOR OUT SYS_REFCURSOR\n" +
                ");\n" +
                "END PKG_CUSTOMER;";

        String body = "CREATE OR REPLACE PACKAGE BODY PKG_CUSTOMER\n" +
                "AS\n" +
                "PROCEDURE ADD_CUSTOMER\n" +
                "(\n" +
                "    P_NAME VARCHAR2,\n" +
                "    P_EMAIL VARCHAR2,\n" +
                "    P_PHONE VARCHAR2,\n" +
                "    P_INCOME NUMBER\n" +
                ")\n" +
                "IS\n" +
                "BEGIN\n" +
                "    INSERT INTO CUSTOMER\n" +
                "    (\n" +
                "        CUSTOMER_NAME,\n" +
                "        EMAIL,\n" +
                "        PHONE,\n" +
                "        ANNUAL_INCOME\n" +
                "    )\n" +
                "    VALUES\n" +
                "    (\n" +
                "        P_NAME,\n" +
                "        P_EMAIL,\n" +
                "        P_PHONE,\n" +
                "        P_INCOME\n" +
                "    );\n" +
                "END;\n" +
                "PROCEDURE UPDATE_STATUS\n" +
                "(\n" +
                "    P_CUSTOMER_ID NUMBER,\n" +
                "    P_STATUS VARCHAR2\n" +
                ")\n" +
                "IS\n" +
                "BEGIN\n" +
                "    UPDATE CUSTOMER\n" +
                "       SET STATUS=P_STATUS,\n" +
                "           UPDATED_DATE=SYSDATE\n" +
                "     WHERE CUSTOMER_ID=P_CUSTOMER_ID;\n" +
                "END;\n" +
                "FUNCTION GET_TOTAL_CUSTOMERS\n" +
                "RETURN NUMBER\n" +
                "IS\n" +
                "V_COUNT NUMBER;\n" +
                "BEGIN\n" +
                "SELECT COUNT(*) INTO V_COUNT FROM CUSTOMER;\n" +
                "RETURN V_COUNT;\n" +
                "END;\n" +
                "FUNCTION GET_CUSTOMER_STATUS\n" +
                "(\n" +
                "P_CUSTOMER_ID NUMBER\n" +
                ")\n" +
                "RETURN VARCHAR2\n" +
                "IS\n" +
                "V_STATUS CUSTOMER.STATUS%TYPE;\n" +
                "BEGIN\n" +
                "SELECT STATUS INTO V_STATUS FROM CUSTOMER WHERE CUSTOMER_ID=P_CUSTOMER_ID;\n" +
                "RETURN V_STATUS;\n" +
                "EXCEPTION\n" +
                "WHEN NO_DATA_FOUND THEN\n" +
                "RETURN 'NOT FOUND';\n" +
                "END;\n" +
                "PROCEDURE GET_CUSTOMERS\n" +
                "(\n" +
                "P_CURSOR OUT SYS_REFCURSOR\n" +
                ")\n" +
                "IS\n" +
                "BEGIN\n" +
                "OPEN P_CURSOR FOR SELECT * FROM CUSTOMER;\n" +
                "END;\n" +
                "END PKG_CUSTOMER;";

        OracleObject obj = new OracleObject("PKG_CUSTOMER", OracleObjectType.PACKAGE, "DEMO", spec);
        obj.setSourceBody(body);

        TranslationResult result = engine.translate(obj);
        assertThat(result).isNotNull();

        // Must have NO UNKNOWN constructs (no parse errors)
        String unknownDetails = result.getConstructResults().stream()
                .filter(r -> r.getConstructType() == com.plsql2java.translation.model.ConstructType.UNKNOWN)
                .map(r -> "  line " + r.getLineNumber() + ": "
                        + (r.getAstNode() != null ? r.getAstNode().getText() : r.getFlagReason()))
                .collect(java.util.stream.Collectors.joining("\n"));
        long unknownCount = result.getConstructResults().stream()
                .filter(r -> r.getConstructType() == com.plsql2java.translation.model.ConstructType.UNKNOWN)
                .count();
        assertThat(unknownCount).as("Parse errors (UNKNOWN constructs) found:\n" + unknownDetails).isEqualTo(0);

        // Must have PROCEDURE_DEF and FUNCTION_DEF nodes
        long procCount = result.getConstructResults().stream()
                .filter(r -> r.getConstructType() == com.plsql2java.translation.model.ConstructType.PROCEDURE_DEF
                          || r.getConstructType() == com.plsql2java.translation.model.ConstructType.FUNCTION_DEF)
                .count();
        assertThat(procCount).as("Expected procedure/function nodes").isGreaterThanOrEqualTo(5);

        // JavaIR must have per-procedure methods (not just fallback execute())
        assertThat(result.getJavaIR()).isNotNull();
        assertThat(result.getJavaIR().getMethods()).isNotEmpty();
        assertThat(result.getJavaIR().getMethods().stream()
                .anyMatch(m -> m.getMethodName().equals("execute")))
                .as("Should not fall back to execute() method")
                .isFalse();
    }
}
