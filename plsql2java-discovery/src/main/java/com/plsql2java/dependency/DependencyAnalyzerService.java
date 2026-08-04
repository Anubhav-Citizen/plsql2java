package com.plsql2java.dependency;

import com.plsql2java.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Dependency analysis service.
 * Story 2.1: Builds dependency graph from discovered Oracle objects.
 * Story 2.2: Computes leaf-first migration order.
 */
@Service
public class DependencyAnalyzerService {

    private static final Logger log = LoggerFactory.getLogger(DependencyAnalyzerService.class);

    private final DependencyGraphBuilder graphBuilder;
    private final CycleDetector cycleDetector;
    private final TopologicalSorter topologicalSorter;

    public DependencyAnalyzerService(DependencyGraphBuilder graphBuilder,
                                      CycleDetector cycleDetector,
                                      TopologicalSorter topologicalSorter) {
        this.graphBuilder = graphBuilder;
        this.cycleDetector = cycleDetector;
        this.topologicalSorter = topologicalSorter;
    }

    /** Story 2.1 + 2.2: Analyze dependencies and compute migration order. */
    public DependencyGraph analyze(DiscoveryResult discovery) {
        log.info("Starting dependency analysis for {} objects", discovery.getTotalObjectCount());
        DependencyGraph graph = new DependencyGraph(discovery.getMigrationId());

        Map<String, Set<String>> adjacency = graphBuilder.build(discovery.getObjects());
        List<CircularDependency> cycles = cycleDetector.detect(adjacency);

        if (!cycles.isEmpty()) {
            log.warn("Detected {} circular dependency cycle(s)", cycles.size());
            cycles.forEach(c -> log.warn("Circular dependency: {}", c.getDescription()));
        }

        List<String> migrationOrder = topologicalSorter.sort(adjacency, cycles);
        List<String> leafObjects = topologicalSorter.findLeafNodes(adjacency);

        graph.setEdges(graphBuilder.toEdges(adjacency));
        graph.setCircularDependencies(cycles);
        graph.setMigrationOrder(migrationOrder);
        graph.setLeafObjects(leafObjects);

        log.info("Dependency analysis complete: {} edges, {} cycles, {} leaf objects",
                graph.getEdges().size(), cycles.size(), leafObjects.size());
        return graph;
    }
}
