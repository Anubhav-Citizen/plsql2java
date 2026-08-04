package com.plsql2java.dependency;

import com.plsql2java.model.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;

@Component
public class DependencyGraphBuilder {

    /**
     * Builds an adjacency list from the given objects.
     * Only references to objects present in the knownNames set are recorded.
     */
    public Map<String, Set<String>> build(List<OracleObject> objects) {
        Set<String> knownNames = new HashSet<>();
        for (OracleObject obj : objects) knownNames.add(obj.getName().toUpperCase());

        Map<String, Set<String>> adjacency = new LinkedHashMap<>();
        for (OracleObject obj : objects) {
            adjacency.put(obj.getName(), new LinkedHashSet<>());
        }

        for (OracleObject obj : objects) {
            String source = obj.getFullSource().toUpperCase();
            String fromName = obj.getName().toUpperCase();
            Set<String> deps = adjacency.get(fromName);

            // Package member calls: PKG.PROC(
            Matcher m = DependencyPatterns.PACKAGE_CALL.matcher(source);
            while (m.find()) {
                String ref = m.group(1).toUpperCase();
                if (knownNames.contains(ref) && !ref.equals(fromName)) deps.add(ref);
            }

            // FROM clause references
            m = DependencyPatterns.FROM_CLAUSE.matcher(source);
            while (m.find()) {
                String ref = m.group(1).toUpperCase();
                if (knownNames.contains(ref) && !ref.equals(fromName)) deps.add(ref);
            }

            // %TYPE references
            m = DependencyPatterns.TYPE_REF.matcher(source);
            while (m.find()) {
                String ref = m.group(1).toUpperCase();
                if (knownNames.contains(ref) && !ref.equals(fromName)) deps.add(ref);
            }

            // TRIGGER ON references
            if (obj.getType() == OracleObjectType.TRIGGER) {
                m = DependencyPatterns.TRIGGER_ON.matcher(source);
                while (m.find()) {
                    String ref = m.group(1).toUpperCase();
                    if (knownNames.contains(ref) && !ref.equals(fromName)) deps.add(ref);
                }
            }
        }
        return adjacency;
    }

    /** Converts adjacency map to a list of DependencyEdge objects. */
    public List<DependencyEdge> toEdges(Map<String, Set<String>> adjacency) {
        List<DependencyEdge> edges = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : adjacency.entrySet()) {
            for (String dep : entry.getValue()) {
                edges.add(new DependencyEdge(entry.getKey(), dep, ReferenceType.CALL));
            }
        }
        return edges;
    }
}
