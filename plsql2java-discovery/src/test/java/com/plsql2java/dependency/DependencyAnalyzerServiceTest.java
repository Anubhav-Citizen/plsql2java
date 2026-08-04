package com.plsql2java.dependency;

import com.plsql2java.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class DependencyAnalyzerServiceTest {

    private DependencyAnalyzerService service;

    @BeforeEach
    void setUp() {
        service = new DependencyAnalyzerService(
                new DependencyGraphBuilder(),
                new CycleDetector(),
                new TopologicalSorter());
    }

    @Test
    void analyze_emptyDiscovery_returnsEmptyGraph() {
        DiscoveryResult discovery = new DiscoveryResult("id", "SCHEMA", DiscoveryMode.FILE);
        DependencyGraph graph = service.analyze(discovery);

        assertThat(graph.getEdges()).isEmpty();
        assertThat(graph.getCircularDependencies()).isEmpty();
        assertThat(graph.getMigrationOrder()).isEmpty();
    }

    @Test
    void analyze_singleObject_noEdges() {
        OracleObject proc = new OracleObject("MY_PROC", OracleObjectType.PROCEDURE, "SCHEMA",
                "CREATE OR REPLACE PROCEDURE MY_PROC IS BEGIN NULL; END;");
        DiscoveryResult discovery = new DiscoveryResult("id", "SCHEMA", DiscoveryMode.FILE);
        discovery.setObjects(List.of(proc));

        DependencyGraph graph = service.analyze(discovery);
        assertThat(graph.getEdges()).isEmpty();
        assertThat(graph.getMigrationOrder()).containsExactly("MY_PROC");
        assertThat(graph.getLeafObjects()).containsExactly("MY_PROC");
    }

    @Test
    void analyze_packageCallingProcedure_createsEdge() {
        OracleObject proc = new OracleObject("HELPER_PROC", OracleObjectType.PROCEDURE, "SCHEMA",
                "CREATE OR REPLACE PROCEDURE HELPER_PROC IS BEGIN NULL; END;");
        OracleObject pkg = new OracleObject("MY_PKG", OracleObjectType.PACKAGE, "SCHEMA",
                "CREATE OR REPLACE PACKAGE MY_PKG AS PROCEDURE RUN; END;");
        pkg.setSourceBody("CREATE OR REPLACE PACKAGE BODY MY_PKG AS " +
                "PROCEDURE RUN IS BEGIN HELPER_PROC(); END; END;");

        DiscoveryResult discovery = new DiscoveryResult("id", "SCHEMA", DiscoveryMode.FILE);
        discovery.setObjects(List.of(proc, pkg));

        DependencyGraph graph = service.analyze(discovery);
        assertThat(graph.getMigrationOrder().indexOf("HELPER_PROC"))
                .isLessThan(graph.getMigrationOrder().indexOf("MY_PKG"));
    }
}
