package com.plsql2java.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class DependencyGraphTest {

    @Test
    void hasCircularDependencies_returnsTrueWhenCyclesPresent() {
        DependencyGraph graph = new DependencyGraph("test-id");
        graph.setCircularDependencies(List.of(new CircularDependency(List.of("A", "B"))));
        assertThat(graph.hasCircularDependencies()).isTrue();
    }

    @Test
    void hasCircularDependencies_returnsFalseWhenNoCycles() {
        DependencyGraph graph = new DependencyGraph("test-id");
        assertThat(graph.hasCircularDependencies()).isFalse();
    }
}
