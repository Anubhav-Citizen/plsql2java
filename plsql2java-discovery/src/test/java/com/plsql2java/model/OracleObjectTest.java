package com.plsql2java.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class OracleObjectTest {

    @Test
    void getFullSource_withBodyReturnsSpecAndBody() {
        OracleObject obj = new OracleObject("PKG", OracleObjectType.PACKAGE, "SCHEMA", "spec");
        obj.setSourceBody("body");
        assertThat(obj.getFullSource()).contains("spec").contains("body");
    }

    @Test
    void getFullSource_withoutBodyReturnsSpecOnly() {
        OracleObject obj = new OracleObject("PROC", OracleObjectType.PROCEDURE, "SCHEMA", "source");
        assertThat(obj.getFullSource()).isEqualTo("source");
    }

    @Test
    void lineCount_calculatedFromSource() {
        OracleObject obj = new OracleObject("PROC", OracleObjectType.PROCEDURE, "SCHEMA", "line1\nline2\nline3");
        assertThat(obj.getLineCount()).isEqualTo(3);
    }
}
