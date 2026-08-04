package com.plsql2java.dependency;

import com.plsql2java.model.CircularDependency;
import org.springframework.stereotype.Component;

import java.util.*;

/** Computes leaf-first migration order using Kahn's topological sort algorithm. */
@Component
public class TopologicalSorter {

    /**
     * Returns objects in leaf-first order.
     * Objects involved in circular dependencies are appended at the end.
     */
    public List<String> sort(Map<String, Set<String>> adjacency,
                              List<CircularDependency> circularDependencies) {
        Set<String> circularNodes = new HashSet<>();
        for (CircularDependency cd : circularDependencies) circularNodes.addAll(cd.getCycle());

        // Build working graph excluding circular nodes
        Map<String, Set<String>> graph = new LinkedHashMap<>();
        Map<String, Integer> inDegree = new LinkedHashMap<>();

        for (Map.Entry<String, Set<String>> entry : adjacency.entrySet()) {
            if (circularNodes.contains(entry.getKey())) continue;
            graph.put(entry.getKey(), new LinkedHashSet<>());
            inDegree.put(entry.getKey(), 0);
        }
        for (Map.Entry<String, Set<String>> entry : graph.entrySet()) {
            for (String dep : adjacency.get(entry.getKey())) {
                if (!circularNodes.contains(dep) && graph.containsKey(dep)) {
                    entry.getValue().add(dep);
                    inDegree.merge(dep, 1, Integer::sum);
                }
            }
        }

        // Kahn's BFS
        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) queue.add(entry.getKey());
        }

        List<String> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            String node = queue.poll();
            result.add(node);
            for (String neighbor : graph.getOrDefault(node, Collections.emptySet())) {
                int newDegree = inDegree.merge(neighbor, -1, Integer::sum);
                if (newDegree == 0) queue.add(neighbor);
            }
        }

        // Append circular dependency nodes at end
        result.addAll(circularNodes);
        return result;
    }

    /** Returns nodes with in-degree 0 (no dependencies — migration-ready). */
    public List<String> findLeafNodes(Map<String, Set<String>> adjacency) {
        Map<String, Integer> inDegree = new HashMap<>();
        for (String node : adjacency.keySet()) inDegree.put(node, 0);
        for (Set<String> deps : adjacency.values()) {
            for (String dep : deps) inDegree.merge(dep, 1, Integer::sum);
        }
        List<String> leaves = new ArrayList<>();
        for (Map.Entry<String, Integer> e : inDegree.entrySet()) {
            if (e.getValue() == 0) leaves.add(e.getKey());
        }
        return leaves;
    }
}
