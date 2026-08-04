package com.plsql2java.discovery;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.plsql2java.model.DependencyGraph;
import com.plsql2java.model.DiscoveryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;

@Service
public class ResultPersistenceService {

    private static final Logger log = LoggerFactory.getLogger(ResultPersistenceService.class);
    private static final String DISCOVERY_FILE = "discovery-result.json";
    private static final String DEPENDENCY_FILE = "dependency-graph.json";

    private final ObjectMapper mapper;

    public ResultPersistenceService() {
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void saveDiscoveryResult(DiscoveryResult result, Path outputDir) {
        ensureOutputDir(outputDir);
        write(result, outputDir.resolve(DISCOVERY_FILE));
        log.info("Discovery result saved to {}", outputDir.resolve(DISCOVERY_FILE));
    }

    public void saveDependencyGraph(DependencyGraph graph, Path outputDir) {
        ensureOutputDir(outputDir);
        write(graph, outputDir.resolve(DEPENDENCY_FILE));
        log.info("Dependency graph saved to {}", outputDir.resolve(DEPENDENCY_FILE));
    }

    public DiscoveryResult loadDiscoveryResult(Path outputDir) {
        Path file = outputDir.resolve(DISCOVERY_FILE);
        if (!Files.exists(file)) {
            throw new DiscoveryException("No discovery result found at: " + file +
                    ". Run 'analyze' or 'generate' first.");
        }
        return read(file, DiscoveryResult.class);
    }

    public DependencyGraph loadDependencyGraph(Path outputDir) {
        Path file = outputDir.resolve(DEPENDENCY_FILE);
        if (!Files.exists(file)) {
            throw new DiscoveryException("No dependency graph found at: " + file +
                    ". Run 'analyze' or 'generate' first.");
        }
        return read(file, DependencyGraph.class);
    }

    private void ensureOutputDir(Path outputDir) {
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            throw new DiscoveryException("Cannot create output directory: " + outputDir);
        }
    }

    private void write(Object obj, Path path) {
        try {
            mapper.writeValue(path.toFile(), obj);
        } catch (IOException e) {
            log.error("Failed to write {}: {}", path, e.getMessage(), e);
            throw new DiscoveryException("Failed to save results to: " + path);
        }
    }

    private <T> T read(Path path, Class<T> type) {
        try {
            return mapper.readValue(path.toFile(), type);
        } catch (IOException e) {
            log.error("Failed to read {}: {}", path, e.getMessage(), e);
            throw new DiscoveryException("Failed to load results from: " + path);
        }
    }
}
