package com.plsql2java.dependency;

import com.plsql2java.model.CircularDependency;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class TopologicalSorterTest {

    private final TopologicalSorter sorter = new TopologicalSorter();

    @Test
    void sort_linearChain_returnsLeafFirst() {
        // A depends on B, B depends on C → order: C, B, A
        Map<String, Set<String>> graph = new LinkedHashMap<>();
        graph.put("A", new LinkedHashSet<>(Set.of("B")));
        graph.put("B", new LinkedHashSet<>(Set.of("C")));
        graph.put("C", Collections.emptySet());

        List<String> order = sorter.sort(graph, Collections.emptyList());
        assertThat(order.indexOf("C")).isLessThan(order.indexOf("B"));
        assertThat(order.indexOf("B")).isLessThan(order.indexOf("A"));
    }

    @Test
    void sort_circularNodesAppendedAtEnd() {
        Map<String, Set<String>> graph = new LinkedHashMap<>();
        graph.put("A", new LinkedHashSet<>(Set.of("B")));
        graph.put("B", new LinkedHashSet<>(Set.of("A")));
        graph.put("C", Collections.emptySet());

        List<CircularDependency> cycles = List.of(new CircularDependency(List.of("A", "B")));
        List<String> order = sorter.sort(graph, cycles);

        assertThat(order).contains("C", "A", "B");
        assertThat(order.indexOf("C")).isLessThan(order.indexOf("A"));
    }

    @Test
    void findLeafNodes_returnsNodesWithNoDependents() {
        Map<String, Set<String>> graph = new LinkedHashMap<>();
        graph.put("A", new LinkedHashSet<>(Set.of("B")));
        graph.put("B", Collections.emptySet());
        graph.put("C", Collections.emptySet());

        List<String> leaves = sorter.findLeafNodes(graph);
        assertThat(leaves).containsExactlyInAnyOrder("A", "C");
    }
}
