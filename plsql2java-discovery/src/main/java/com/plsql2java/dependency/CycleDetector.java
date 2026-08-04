package com.plsql2java.dependency;

import com.plsql2java.model.CircularDependency;
import org.springframework.stereotype.Component;

import java.util.*;

/** Detects cycles in a directed graph using DFS with WHITE/GRAY/BLACK node coloring. */
@Component
public class CycleDetector {

    private enum Color { WHITE, GRAY, BLACK }

    public List<CircularDependency> detect(Map<String, Set<String>> adjacency) {
        Map<String, Color> color = new HashMap<>();
        Map<String, String> parent = new HashMap<>();
        List<CircularDependency> cycles = new ArrayList<>();

        for (String node : adjacency.keySet()) color.put(node, Color.WHITE);

        for (String node : adjacency.keySet()) {
            if (color.get(node) == Color.WHITE) {
                dfs(node, adjacency, color, parent, cycles);
            }
        }
        return cycles;
    }

    private void dfs(String node, Map<String, Set<String>> adjacency,
                     Map<String, Color> color, Map<String, String> parent,
                     List<CircularDependency> cycles) {
        color.put(node, Color.GRAY);
        for (String neighbor : adjacency.getOrDefault(node, Collections.emptySet())) {
            if (!color.containsKey(neighbor)) continue;
            if (color.get(neighbor) == Color.GRAY) {
                cycles.add(extractCycle(neighbor, node, parent));
            } else if (color.get(neighbor) == Color.WHITE) {
                parent.put(neighbor, node);
                dfs(neighbor, adjacency, color, parent, cycles);
            }
        }
        color.put(node, Color.BLACK);
    }

    private CircularDependency extractCycle(String start, String end, Map<String, String> parent) {
        List<String> cycle = new ArrayList<>();
        cycle.add(start);
        String current = end;
        while (!current.equals(start)) {
            cycle.add(current);
            current = parent.getOrDefault(current, start);
        }
        Collections.reverse(cycle);
        return new CircularDependency(cycle);
    }
}
