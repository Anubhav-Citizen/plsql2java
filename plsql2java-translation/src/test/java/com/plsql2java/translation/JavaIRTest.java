package com.plsql2java.translation;

import com.plsql2java.translation.engine.JavaIRAssembler;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JavaIRTest {

    @Test
    void toPascalCase_underscoreSeparated() {
        assertThat(JavaIRAssembler.toPascalCase("MY_PACKAGE")).isEqualTo("MyPackage");
    }

    @Test
    void toPascalCase_singleWord() {
        assertThat(JavaIRAssembler.toPascalCase("ORDERS")).isEqualTo("Orders");
    }

    @Test
    void toPascalCase_null_returnsUnknown() {
        assertThat(JavaIRAssembler.toPascalCase(null)).isEqualTo("Unknown");
    }

    @Test
    void toPascalCase_blank_returnsUnknown() {
        assertThat(JavaIRAssembler.toPascalCase("  ")).isEqualTo("Unknown");
    }
}
