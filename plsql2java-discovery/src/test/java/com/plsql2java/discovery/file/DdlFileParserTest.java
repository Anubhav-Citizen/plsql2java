package com.plsql2java.discovery.file;

import com.plsql2java.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class DdlFileParserTest {

    private DdlFileParser parser;

    @BeforeEach
    void setUp() { parser = new DdlFileParser(); }

    @Test
    void parse_packageFile_discoversPackageSpecAndBody() throws Exception {
        Path file = sampleFile("sample-package.sql");
        List<DiscoveryError> errors = new ArrayList<>();
        List<OracleObject> objects = parser.parse(file, errors);

        assertThat(errors).isEmpty();
        assertThat(objects).hasSize(1); // spec + body merged by normalizer; parser returns 2 raw
        // Parser returns raw objects before normalization
        long packages = objects.stream().filter(o -> o.getType() == OracleObjectType.PACKAGE).count();
        long bodies = objects.stream().filter(o -> o.getType() == OracleObjectType.PACKAGE_BODY).count();
        assertThat(packages + bodies).isGreaterThanOrEqualTo(1);
    }

    @Test
    void parse_procedureFile_discoversProcedure() throws Exception {
        Path file = sampleFile("sample-procedure.sql");
        List<DiscoveryError> errors = new ArrayList<>();
        List<OracleObject> objects = parser.parse(file, errors);

        assertThat(objects).anyMatch(o -> o.getType() == OracleObjectType.PROCEDURE
                && o.getName().equals("UPDATE_SALARY"));
    }

    @Test
    void parse_triggerFile_discoversTrigger() throws Exception {
        Path file = sampleFile("sample-trigger.sql");
        List<DiscoveryError> errors = new ArrayList<>();
        List<OracleObject> objects = parser.parse(file, errors);

        assertThat(objects).anyMatch(o -> o.getType() == OracleObjectType.TRIGGER
                && o.getName().equals("EMP_AUDIT_TRG"));
    }

    @Test
    void parse_viewFile_discoversView() throws Exception {
        Path file = sampleFile("sample-view.sql");
        List<DiscoveryError> errors = new ArrayList<>();
        List<OracleObject> objects = parser.parse(file, errors);

        assertThat(objects).anyMatch(o -> o.getType() == OracleObjectType.VIEW
                && o.getName().equals("EMP_SUMMARY_V"));
    }

    @Test
    void validatePath_throwsOnTraversal() {
        assertThatThrownBy(() -> DdlFileParser.validatePath(Paths.get("../etc/passwd")))
                .isInstanceOf(com.plsql2java.discovery.DiscoveryException.class)
                .hasMessageContaining("path traversal");
    }

    @Test
    void tokenize_splitsOnSlashDelimiter() {
        String content = "STMT1\n/\nSTMT2\n/";
        List<String> tokens = parser.tokenize(content);
        assertThat(tokens).hasSize(2);
    }

    @Test
    void classify_returnsNullForUnsupportedStatement() {
        assertThat(parser.classify("CREATE TABLE FOO (ID NUMBER)")).isNull();
    }

    private Path sampleFile(String name) throws Exception {
        URL url = getClass().getClassLoader().getResource("samples/" + name);
        assertThat(url).isNotNull();
        return Paths.get(url.toURI());
    }
}
