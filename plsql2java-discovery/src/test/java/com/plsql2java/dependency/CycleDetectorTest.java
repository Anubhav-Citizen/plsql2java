package com.plsql2java.dependency;

import com.plsql2java.model.CircularDependency;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class CycleDetectorTest {

    private final CycleDetector detector = new CycleDetector();

    @Test
    void detect_noCycles_returnsEmpty() {
        Map<String, Set<String>> graph = new LinkedHashMap<>();
        graph.put("A", Set.of("B"));
        graph.put("B", Set.of("C"));
        graph.put("C", Collections.emptySet());

        assertThat(detector.detect(graph)).isEmpty();
    }

    @Test
    void detect_simpleCycle_returnsCycle() {
        Map<String, Set<String>> graph = new LinkedHashMap<>();
        graph.put("A", new LinkedHashSet<>(Set.of("B")));
        graph.put("B", new LinkedHashSet<>(Set.of("A")));

        List<CircularDependency> cycles = detector.detect(graph);
        assertThat(cycles).isNotEmpty();
    }

    @Test
    void detect_selfReference_ignored() {
        Map<String, Set<String>> graph = new LinkedHashMap<>();
        graph.put("A", new LinkedHashSet<>(Set.of("B")));
        graph.put("B", Collections.emptySet());

        assertThat(detector.detect(graph)).isEmpty();
    }
}
