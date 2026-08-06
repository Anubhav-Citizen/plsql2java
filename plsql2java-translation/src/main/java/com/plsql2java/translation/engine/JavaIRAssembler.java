package com.plsql2java.translation.engine;

import com.plsql2java.model.OracleObject;
import com.plsql2java.translation.model.ConstructTranslationResult;
import com.plsql2java.translation.model.JavaIR;
import com.plsql2java.translation.model.JavaMethodIR;
import com.plsql2java.translation.model.TranslationContext;
import com.plsql2java.translation.model.TranslationStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JavaIRAssembler {

    public JavaIR assemble(OracleObject object, TranslationContext context, String targetPackage) {
        String className = toPascalCase(object.getName()) + "Service";
        List<String> fields = new ArrayList<>();
        List<String> imports = new ArrayList<>();
        boolean hasDml = hasDmlOperations(context);

        // Standard Spring service imports
        imports.add("org.springframework.stereotype.Service");
        if (hasDml) {
            imports.add("org.springframework.transaction.annotation.Transactional");
        }

        // Collect imports and snippets from construct results
        List<String> bodyLines = new ArrayList<>();
        for (ConstructTranslationResult result : context.getConstructResults()) {
            if (result.getJavaSnippet() != null) {
                bodyLines.add(result.getJavaSnippet());
            }
            if (result.getStatus() == TranslationStatus.FLAGGED && result.getFlagReason() != null) {
                bodyLines.add("// TODO [FLAGGED - " + result.getConstructType() + " line "
                        + result.getLineNumber() + "]: " + result.getFlagReason());
                if (result.getRecommendation() != null) {
                    bodyLines.add("// Recommendation: " + result.getRecommendation());
                }
            }
        }

        // Check if DBMS_OUTPUT was translated (needs Logger field)
        boolean needsLogger = context.getConstructResults().stream()
                .anyMatch(r -> r.getJavaSnippet() != null && r.getJavaSnippet().contains("log.debug"));
        if (needsLogger) {
            imports.add("org.slf4j.Logger");
            imports.add("org.slf4j.LoggerFactory");
            fields.add("private static final Logger log = LoggerFactory.getLogger(" + className + ".class);");
        }

        // Build single translated method from all body lines
        String javadoc = "/** Translated from PL/SQL: " + object.getSchema() + "."
                + object.getName() + " */";
        List<String> annotations = new ArrayList<>();
        if (hasDml) annotations.add("@Transactional");

        JavaMethodIR method = new JavaMethodIR(
                "execute",
                "void",
                List.of(),
                String.join("\n        ", bodyLines),
                annotations,
                javadoc,
                context.getConstructResults()
        );

        // Deduplicate and sort imports
        List<String> sortedImports = deduplicateAndSort(imports);

        String rawSource = buildRawSource(targetPackage, sortedImports, className,
                fields, List.of(method), object.isHasCompilationErrors());

        return new JavaIR(object.getName(), targetPackage, className,
                sortedImports, fields, List.of(method), rawSource);
    }

    private boolean hasDmlOperations(TranslationContext context) {
        return context.getConstructResults().stream()
                .anyMatch(r -> r.getJavaSnippet() != null &&
                        (r.getJavaSnippet().contains("saveAll") ||
                         r.getJavaSnippet().contains("deleteAll") ||
                         r.getJavaSnippet().contains("save(")));
    }

    private List<String> deduplicateAndSort(List<String> imports) {
        Set<String> seen = new LinkedHashSet<>(imports);
        return seen.stream().sorted().collect(Collectors.toList());
    }

    private String buildRawSource(String packageName, List<String> imports, String className,
                                   List<String> fields, List<JavaMethodIR> methods,
                                   boolean hasCompilationErrors) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(packageName).append(";\n\n");
        imports.forEach(i -> sb.append("import ").append(i).append(";\n"));
        sb.append("\n");
        if (hasCompilationErrors) {
            sb.append("// WARNING: Source PL/SQL object had compilation errors. Review carefully.\n");
        }
        sb.append("@Service\n");
        sb.append("public class ").append(className).append(" {\n\n");
        fields.forEach(f -> sb.append("    ").append(f).append("\n"));
        if (!fields.isEmpty()) sb.append("\n");
        for (JavaMethodIR m : methods) {
            sb.append("    ").append(m.getJavadoc()).append("\n");
            m.getAnnotations().forEach(a -> sb.append("    ").append(a).append("\n"));
            sb.append("    public ").append(m.getReturnType()).append(" ").append(m.getMethodName())
              .append("(").append(String.join(", ", m.getParameters())).append(") {\n");
            if (!m.getBody().isBlank()) {
                Arrays.stream(m.getBody().split("\n"))
                      .forEach(line -> sb.append("        ").append(line).append("\n"));
            }
            sb.append("    }\n\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    public static String toPascalCase(String oracleName) {
        if (oracleName == null || oracleName.isBlank()) return "Unknown";
        String[] parts = oracleName.split("[_\\s]+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                sb.append(part.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }
}
