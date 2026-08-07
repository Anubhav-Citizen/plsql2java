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
        // adjacency: A -> {B, C} means A depends on B and C
        // For leaf-first order: process nodes whose dependencies are all resolved
        Map<String, Set<String>> deps = new LinkedHashMap<>();   // node -> its dependencies
        Map<String, Set<String>> dependents = new LinkedHashMap<>(); // node -> nodes that depend on it
        Map<String, Integer> inDegree = new LinkedHashMap<>();   // number of unresolved dependencies

        for (String node : adjacency.keySet()) {
            if (circularNodes.contains(node)) continue;
            deps.put(node, new LinkedHashSet<>());
            dependents.put(node, new LinkedHashSet<>());
            inDegree.put(node, 0);
        }
        for (Map.Entry<String, Set<String>> entry : adjacency.entrySet()) {
            String node = entry.getKey();
            if (circularNodes.contains(node)) continue;
            for (String dep : entry.getValue()) {
                if (circularNodes.contains(dep) || !inDegree.containsKey(dep)) continue;
                deps.get(node).add(dep);
                dependents.get(dep).add(node);
                inDegree.merge(node, 1, Integer::sum);
            }
        }

        // Kahn's BFS — start with nodes that have no dependencies
        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) queue.add(entry.getKey());
        }

        List<String> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            String node = queue.poll();
            result.add(node);
            for (String dependent : dependents.getOrDefault(node, Collections.emptySet())) {
                int newDegree = inDegree.merge(dependent, -1, Integer::sum);
                if (newDegree == 0) queue.add(dependent);
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
