package com.plsql2java.codegen;

import com.plsql2java.codegen.model.ArtifactType;
import com.plsql2java.codegen.model.GeneratedProject;
import com.plsql2java.codegen.model.GenerationContext;
import com.plsql2java.codegen.model.JavaSourceFile;
import com.plsql2java.translation.model.JavaMethodIR;
import com.plsql2java.translation.model.TranslationResult;
import com.plsql2java.translation.model.TranslationStatus;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JavaCodeGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(JavaCodeGeneratorService.class);

    private final Configuration freemarker;

    public JavaCodeGeneratorService() throws IOException {
        this.freemarker = new Configuration(Configuration.VERSION_2_3_33);
        this.freemarker.setClassLoaderForTemplateLoading(
                getClass().getClassLoader(), "templates");
        this.freemarker.setDefaultEncoding("UTF-8");
    }

    public GeneratedProject generateProject(List<TranslationResult> results, GenerationContext ctx) {
        MDC.put("migrationId", ctx.getMigrationId());
        MDC.put("component", "JavaCodeGeneratorService");
        String projectName = toKebabCase(ctx.getSchemaName());
        GeneratedProject project = new GeneratedProject(ctx.getMigrationId(), projectName);

        try {
            for (TranslationResult result : results) {
                MDC.put("objectName", result.getSourceObject().getName());
                if (result.getJavaIR() == null) {
                    log.warn("Skipping {} — null JavaIR (failed translation)", result.getSourceObject().getName());
                    project.addSkippedObject(result.getSourceObject().getName());
                    continue;
                }
                try {
                    project.addFile(generateService(result, ctx));
                    project.addFile(generateController(result, ctx));
                    generateDto(result.getJavaIR().getClassName(), ctx)
                            .forEach(project::addFile);
                    project.addFile(generateTest(result, ctx));
                } catch (Exception e) {
                    log.warn("Failed to generate artifacts for {}: {}", result.getSourceObject().getName(), e.getMessage());
                    project.addSkippedObject(result.getSourceObject().getName());
                }
            }

            project.addFile(generatePomXml(ctx));
            project.addFile(generateApplicationYml(ctx));
            project.addFile(generateDockerfile(ctx));
            project.addFile(generateDockerCompose(ctx));

        } finally {
            MDC.clear();
        }

        log.info("Code generation complete: {} files, {} skipped",
                project.getFiles().size(), project.getSkippedObjects().size());
        return project;
    }

    public JavaSourceFile generateService(TranslationResult result, GenerationContext ctx) {
        String className = toPascalCase(result.getJavaIR().getClassName());
        String pkg = ctx.getTargetPackage();
        String schema = result.getSourceObject().getSchema();
        String objectName = result.getSourceObject().getName();

        List<Map<String, Object>> methods = new ArrayList<>();
        for (JavaMethodIR m : result.getJavaIR().getMethods()) {
            int score = computeMethodScore(m, result);
            Map<String, Object> mMap = new HashMap<>();
            mMap.put("methodName", m.getMethodName());
            mMap.put("returnType", m.getReturnType() != null ? m.getReturnType() : "void");
            mMap.put("parameters", m.getParameters());
            mMap.put("body", m.getBody() != null ? m.getBody() : "// TODO: implement");
            mMap.put("annotations", buildMethodAnnotations(m));
            mMap.put("javadoc", m.getJavadoc() != null ? m.getJavadoc() : "");
            mMap.put("belowThreshold", score < ctx.getConfidenceThreshold());
            mMap.put("confidenceScore", score);
            methods.add(mMap);
        }

        Map<String, Object> model = new HashMap<>();
        model.put("packageName", pkg);
        model.put("className", className);
        model.put("schemaName", schema);
        model.put("objectName", objectName);
        model.put("imports", result.getJavaIR().getImports());
        model.put("fields", result.getJavaIR().getFields());
        model.put("fieldNames", extractFieldNames(result.getJavaIR().getFields()));
        model.put("methods", methods);

        String relativePath = "src/main/java/" + pkg.replace('.', '/') + "/service/" + className + "Service.java";
        return new JavaSourceFile(relativePath, render("java/service.ftl", model), objectName, ArtifactType.SERVICE);
    }

    public JavaSourceFile generateController(TranslationResult result, GenerationContext ctx) {
        String className = toPascalCase(result.getJavaIR().getClassName());
        String pkg = ctx.getTargetPackage();
        String resourcePath = toKebabCase(className);

        List<Map<String, Object>> methods = new ArrayList<>();
        for (JavaMethodIR m : result.getJavaIR().getMethods()) {
            Map<String, Object> mMap = new HashMap<>();
            mMap.put("methodName", m.getMethodName());
            mMap.put("path", toKebabCase(m.getMethodName()));
            mMap.put("summary", "Execute " + m.getMethodName());
            mMap.put("responseType", className);
            mMap.put("parameters", m.getParameters());
            mMap.put("paramNames", extractParamNames(m.getParameters()));
            mMap.put("readOnly", isReadOnly(m));
            methods.add(mMap);
        }

        Map<String, Object> model = new HashMap<>();
        model.put("packageName", pkg);
        model.put("serviceClassName", className + "Service");
        model.put("resourceName", className);
        model.put("resourcePath", resourcePath);
        model.put("schemaName", result.getSourceObject().getSchema());
        model.put("objectName", result.getSourceObject().getName());
        model.put("methods", methods);

        String relativePath = "src/main/java/" + pkg.replace('.', '/') + "/controller/" + className + "ServiceController.java";
        return new JavaSourceFile(relativePath, render("java/controller.ftl", model),
                result.getSourceObject().getName(), ArtifactType.CONTROLLER);
    }

    public List<JavaSourceFile> generateDto(String baseName, GenerationContext ctx) {
        String className = toPascalCase(baseName);
        String pkg = ctx.getTargetPackage();
        List<Map<String, Object>> fields = List.of(
                Map.of("name", "id", "type", "Long", "description", "Identifier", "required", true, "maxLength", 0),
                Map.of("name", "name", "type", "String", "description", "Name", "required", true, "maxLength", 255)
        );

        Map<String, Object> reqModel = new HashMap<>();
        reqModel.put("packageName", pkg);
        reqModel.put("baseName", className);
        reqModel.put("fields", fields);

        Map<String, Object> respModel = new HashMap<>(reqModel);

        List<JavaSourceFile> dtos = new ArrayList<>();
        String reqPath = "src/main/java/" + pkg.replace('.', '/') + "/dto/" + className + "Request.java";
        String respPath = "src/main/java/" + pkg.replace('.', '/') + "/dto/" + className + "Response.java";
        dtos.add(new JavaSourceFile(reqPath, render("java/dto-request.ftl", reqModel), baseName, ArtifactType.DTO));
        dtos.add(new JavaSourceFile(respPath, render("java/dto-response.ftl", respModel), baseName, ArtifactType.DTO));
        return dtos;
    }

    public JavaSourceFile generateTest(TranslationResult result, GenerationContext ctx) {
        String className = toPascalCase(result.getJavaIR().getClassName());
        String pkg = ctx.getTargetPackage();

        List<Map<String, Object>> methods = new ArrayList<>();
        for (JavaMethodIR m : result.getJavaIR().getMethods()) {
            methods.add(Map.of("methodName", m.getMethodName()));
        }

        Map<String, Object> model = new HashMap<>();
        model.put("packageName", pkg);
        model.put("className", className);
        model.put("schemaName", result.getSourceObject().getSchema());
        model.put("objectName", result.getSourceObject().getName());
        model.put("mockFields", result.getJavaIR().getFields());
        model.put("methods", methods);

        String relativePath = "src/test/java/" + pkg.replace('.', '/') + "/service/" + className + "ServiceTest.java";
        return new JavaSourceFile(relativePath, render("java/test.ftl", model),
                result.getSourceObject().getName(), ArtifactType.TEST);
    }

    public JavaSourceFile generatePomXml(GenerationContext ctx) {
        String artifactId = toKebabCase(ctx.getSchemaName());
        Map<String, Object> model = new HashMap<>();
        model.put("springBootVersion", ctx.getTargetSpringBootVersion());
        model.put("groupId", ctx.getTargetPackage());
        model.put("artifactId", artifactId);
        model.put("projectName", artifactId);
        model.put("dbDriverGroupId", "org.postgresql");
        model.put("dbDriverArtifactId", "postgresql");
        model.put("dbDriverVersion", "42.7.3");
        return new JavaSourceFile("pom.xml", render("maven/pom.ftl", model), ctx.getSchemaName(), ArtifactType.POM_XML);
    }

    public JavaSourceFile generateApplicationYml(GenerationContext ctx) {
        Map<String, Object> model = new HashMap<>();
        model.put("artifactId", toKebabCase(ctx.getSchemaName()));
        model.put("dbDriver", ctx.getDbDriver() != null ? ctx.getDbDriver() : "org.postgresql.Driver");
        model.put("basePackage", ctx.getTargetPackage());
        return new JavaSourceFile("src/main/resources/application.yml",
                render("maven/application-yml.ftl", model), ctx.getSchemaName(), ArtifactType.APP_YML);
    }

    public JavaSourceFile generateDockerfile(GenerationContext ctx) {
        Map<String, Object> model = Map.of("artifactId", toKebabCase(ctx.getSchemaName()));
        return new JavaSourceFile("Dockerfile", render("docker/Dockerfile.ftl", model),
                ctx.getSchemaName(), ArtifactType.DOCKERFILE);
    }

    public JavaSourceFile generateDockerCompose(GenerationContext ctx) {
        Map<String, Object> model = Map.of("artifactId", toKebabCase(ctx.getSchemaName()));
        return new JavaSourceFile("docker-compose.yml", render("docker/docker-compose.ftl", model),
                ctx.getSchemaName(), ArtifactType.DOCKER_COMPOSE);
    }

    public void writeProject(GeneratedProject project, Path outputDir) throws IOException {
        Path resolvedBase;
        try {
            resolvedBase = outputDir.toRealPath();
        } catch (IOException e) {
            Files.createDirectories(outputDir);
            resolvedBase = outputDir.toRealPath();
        }

        int written = 0;
        for (JavaSourceFile file : project.getFiles()) {
            Path target = outputDir.resolve(file.getRelativePath()).normalize();
            if (!target.toAbsolutePath().startsWith(resolvedBase.toAbsolutePath())) {
                log.warn("Path traversal attempt blocked for: {}", file.getRelativePath());
                continue;
            }
            Files.createDirectories(target.getParent());
            try (var writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
                writer.write(file.getContent());
            }
            written++;
        }
        log.info("Wrote {} files to {}", written, outputDir);
    }

    private String render(String templatePath, Map<String, Object> model) {
        try {
            Template template = freemarker.getTemplate(templatePath);
            StringWriter out = new StringWriter();
            template.process(model, out);
            return out.toString();
        } catch (IOException | TemplateException e) {
            log.warn("Template rendering failed for {}: {}", templatePath, e.getMessage());
            return "// Template rendering failed: " + e.getMessage();
        }
    }

    private int computeMethodScore(JavaMethodIR method, TranslationResult result) {
        if (method.getConstructResults() == null || method.getConstructResults().isEmpty()) return 100;
        int total = method.getConstructResults().size();
        int penalty = method.getConstructResults().stream().mapToInt(c -> c.getConfidencePenalty()).sum();
        return Math.max(0, Math.min(100, 100 - (penalty / Math.max(total, 1))));
    }

    private List<String> buildMethodAnnotations(JavaMethodIR method) {
        List<String> annotations = new ArrayList<>(method.getAnnotations() != null ? method.getAnnotations() : List.of());
        if (!annotations.stream().anyMatch(a -> a.contains("Transactional"))) {
            annotations.add("@Transactional");
        }
        return annotations;
    }

    private List<String> extractFieldNames(List<String> fields) {
        List<String> names = new ArrayList<>();
        if (fields == null) return names;
        for (String field : fields) {
            String[] parts = field.trim().split("\\s+");
            if (parts.length >= 2) names.add(parts[parts.length - 1]);
        }
        return names;
    }

    private List<String> extractParamNames(List<String> params) {
        List<String> names = new ArrayList<>();
        if (params == null) return names;
        for (String param : params) {
            String[] parts = param.trim().split("\\s+");
            if (parts.length >= 2) names.add(parts[parts.length - 1]);
        }
        return names;
    }

    private boolean isReadOnly(JavaMethodIR method) {
        String name = method.getMethodName().toLowerCase();
        return name.startsWith("get") || name.startsWith("find") || name.startsWith("list") || name.startsWith("fetch");
    }

    static String toPascalCase(String name) {
        if (name == null || name.isBlank()) return "Unknown";
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : name.toCharArray()) {
            if (c == '_' || c == '-') { capitalizeNext = true; }
            else if (capitalizeNext) { sb.append(Character.toUpperCase(c)); capitalizeNext = false; }
            else { sb.append(Character.toLowerCase(c)); }
        }
        return sb.toString();
    }

    static String toKebabCase(String name) {
        if (name == null || name.isBlank()) return "app";
        return name.toLowerCase().replace('_', '-').replace(' ', '-');
    }
}
