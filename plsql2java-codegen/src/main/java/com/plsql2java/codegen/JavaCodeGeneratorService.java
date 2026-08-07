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

    // Prefixes to strip when deriving a domain class name from an Oracle object name
    private static final List<String> STRIP_PREFIXES = List.of("PKG_", "TRG_", "SEQ_", "SP_", "FN_", "VW_");

    public GeneratedProject generateProject(List<TranslationResult> results, GenerationContext ctx) {
        MDC.put("migrationId", ctx.getMigrationId());
        MDC.put("component", "JavaCodeGeneratorService");
        String projectName = toKebabCase(ctx.getSchemaName());
        GeneratedProject project = new GeneratedProject(ctx.getMigrationId(), projectName);

        // Collect distinct domain names from ALL objects (PKG, TRG, SEQ all map to same domain)
        List<String> entityNames = results.stream()
                .map(r -> domainName(r.getSourceObject().getName()))
                .distinct()
                .toList();

        // Detect special PL/SQL patterns from raw source file (anonymous blocks not in discovery)
        String allSource = ctx.getRawSource().toUpperCase();
        if (allSource.isBlank()) {
            // fallback to discovered object sources
            allSource = results.stream()
                    .map(r -> r.getSourceObject().getFullSource())
                    .collect(java.util.stream.Collectors.joining("\n")).toUpperCase();
        }
        boolean hasRefCursor   = allSource.contains("SYS_REFCURSOR");
        boolean hasBulkCollect = allSource.contains("BULK COLLECT") || allSource.contains("FORALL");
        boolean hasDynamicSql  = allSource.contains("EXECUTE IMMEDIATE");

        // Packages get a dedicated procedure wrapper
        List<TranslationResult> packages = results.stream()
                .filter(r -> r.getSourceObject().getType() == com.plsql2java.model.OracleObjectType.PACKAGE
                          || r.getSourceObject().getType() == com.plsql2java.model.OracleObjectType.PACKAGE_BODY)
                .toList();

        try {
            for (TranslationResult result : results) {
                MDC.put("objectName", result.getSourceObject().getName());
                // Triggers and sequences are handled via entity @PrePersist and @SequenceGenerator
                com.plsql2java.model.OracleObjectType objType = result.getSourceObject().getType();
                if (objType == com.plsql2java.model.OracleObjectType.TRIGGER
                        || objType == com.plsql2java.model.OracleObjectType.SEQUENCE) {
                    log.debug("Skipping service/controller generation for {} ({})",
                            result.getSourceObject().getName(), objType);
                    continue;
                }
                if (result.getJavaIR() == null) {
                    log.warn("Skipping {} — null JavaIR (failed translation)", result.getSourceObject().getName());
                    project.addSkippedObject(result.getSourceObject().getName());
                    continue;
                }
                try {
                    generateServicePair(result, ctx).forEach(project::addFile);
                    project.addFile(generateController(result, ctx));
                    generateDto(result, ctx).forEach(project::addFile);
                    project.addFile(generateTest(result, ctx));
                } catch (Exception e) {
                    log.warn("Failed to generate artifacts for {}: {}", result.getSourceObject().getName(), e.getMessage());
                    project.addSkippedObject(result.getSourceObject().getName());
                }
            }

            // Entity + Repository for every domain name
            entityNames.forEach(name -> {
                project.addFile(generateEntity(name, ctx));
                project.addFile(generateRepository(name, ctx));
            });

            // Pattern-detected cross-cutting files
            if (hasRefCursor)   project.addFile(generateRefCursorService(ctx));
            if (hasBulkCollect) project.addFile(generateBulkUpdateService(ctx));
            if (hasDynamicSql)  project.addFile(generateDynamicSqlService(ctx));

            project.addFile(generateDateUtils(ctx));
            project.addFile(generateApplication(ctx));
            project.addFile(generateDatabaseConfig(ctx));
            project.addFile(generateTransactionConfig(ctx));
            project.addFile(generateCustomerNotFoundException(ctx));
            project.addFile(generateGlobalExceptionHandler(ctx));
            project.addFile(generateCustomerMapper(ctx));
            project.addFile(generateCustomerDto(ctx));
            project.addFile(generateCustomerStatusDto(ctx));
            project.addFile(generateJdbcUtil(ctx));
            project.addFile(generateSqlConstants(ctx));
            project.addFile(generateCustomerRepositoryTest(ctx));
            project.addFile(generateLogback(ctx));
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

    public List<JavaSourceFile> generateServicePair(TranslationResult result, GenerationContext ctx) {
        String className = domainName(result.getSourceObject().getName());
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
        List<String> injectedOnly = result.getJavaIR().getFields().stream()
                .filter(f -> !f.contains("static"))
                .map(f -> f.replaceFirst("^private final ", "").replaceFirst(";", "").trim())
                .toList();

        Map<String, Object> model = new HashMap<>();
        model.put("packageName", pkg);
        model.put("className", className);
        model.put("schemaName", schema);
        model.put("objectName", objectName);
        model.put("imports", result.getJavaIR().getImports());
        model.put("fields", injectedOnly);
        model.put("fieldNames", extractFieldNames(injectedOnly));
        model.put("methods", methods);

        String basePath = "src/main/java/" + pkg.replace('.', '/');
        JavaSourceFile iface = new JavaSourceFile(
                basePath + "/service/" + className + "Service.java",
                render("java/service-interface.ftl", model), objectName, ArtifactType.SERVICE);
        JavaSourceFile impl = new JavaSourceFile(
                basePath + "/service/impl/" + className + "ServiceImpl.java",
                render("java/service-impl.ftl", model), objectName, ArtifactType.SERVICE);
        return List.of(iface, impl);
    }

    /** @deprecated use generateServicePair */
    public JavaSourceFile generateService(TranslationResult result, GenerationContext ctx) {
        return generateServicePair(result, ctx).get(0);
    }

    public JavaSourceFile generateController(TranslationResult result, GenerationContext ctx) {
        String className = domainName(result.getSourceObject().getName());
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

        String relativePath = "src/main/java/" + pkg.replace('.', '/') + "/controller/" + className + "Controller.java";
        return new JavaSourceFile(relativePath, render("java/controller.ftl", model),
                result.getSourceObject().getName(), ArtifactType.CONTROLLER);
    }

    public List<JavaSourceFile> generateDto(TranslationResult result, GenerationContext ctx) {
        String className = domainName(result.getSourceObject().getName());
        String pkg = ctx.getTargetPackage();

        // Build DTO fields from method parameters if available, else use defaults
        List<Map<String, Object>> fields = new ArrayList<>();
        if (result.getJavaIR() != null && result.getJavaIR().getMethods() != null) {
            result.getJavaIR().getMethods().stream()
                    .flatMap(m -> m.getParameters() != null ? m.getParameters().stream() : java.util.stream.Stream.empty())
                    .distinct()
                    .forEach(param -> {
                        String[] parts = param.trim().split("\\s+");
                        if (parts.length >= 2) {
                            fields.add(Map.of("name", parts[parts.length - 1], "type", parts[0],
                                    "description", parts[parts.length - 1], "required", true, "maxLength", 0));
                        }
                    });
        }
        if (fields.isEmpty()) {
            fields.add(Map.of("name", "id", "type", "Long", "description", "Identifier", "required", true, "maxLength", 0));
            fields.add(Map.of("name", "name", "type", "String", "description", "Name", "required", true, "maxLength", 255));
        }

        Map<String, Object> reqModel = new HashMap<>();
        reqModel.put("packageName", pkg);
        reqModel.put("baseName", className);
        reqModel.put("fields", fields);
        Map<String, Object> respModel = new HashMap<>(reqModel);

        List<JavaSourceFile> dtos = new ArrayList<>();
        String base = "src/main/java/" + pkg.replace('.', '/') + "/dto/";
        dtos.add(new JavaSourceFile(base + className + "Request.java", render("java/dto-request.ftl", reqModel), className, ArtifactType.DTO));
        dtos.add(new JavaSourceFile(base + className + "Response.java", render("java/dto-response.ftl", respModel), className, ArtifactType.DTO));
        return dtos;
    }

    public JavaSourceFile generateTest(TranslationResult result, GenerationContext ctx) {
        String className = domainName(result.getSourceObject().getName());
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
        model.put("mockFields", result.getJavaIR().getFields().stream()
                .filter(f -> !f.contains("static"))
                .map(f -> f.replaceFirst("^private final ", "").replaceFirst(";", "").trim())
                .toList());
        model.put("methods", methods);

        String relativePath = "src/test/java/" + pkg.replace('.', '/') + "/service/" + className + "ServiceTest.java";
        return new JavaSourceFile(relativePath, render("java/test.ftl", model),
                result.getSourceObject().getName(), ArtifactType.TEST);
    }

    public JavaSourceFile generateApplication(GenerationContext ctx) {
        String pkg = ctx.getTargetPackage();
        Map<String, Object> model = new HashMap<>();
        model.put("packageName", pkg);
        model.put("appName", "Plsql2JavaApplication");
        String path = "src/main/java/" + pkg.replace('.', '/') + "/Plsql2JavaApplication.java";
        return new JavaSourceFile(path, render("java/application.ftl", model), ctx.getSchemaName(), ArtifactType.APPLICATION);
    }

    public JavaSourceFile generateDatabaseConfig(GenerationContext ctx) {
        String pkg = ctx.getTargetPackage();
        Map<String, Object> model = Map.of("packageName", pkg);
        String path = "src/main/java/" + pkg.replace('.', '/') + "/config/DatabaseConfig.java";
        return new JavaSourceFile(path, render("java/database-config.ftl", model), ctx.getSchemaName(), ArtifactType.CONFIG);
    }

    public JavaSourceFile generateTransactionConfig(GenerationContext ctx) {
        String pkg = ctx.getTargetPackage();
        Map<String, Object> model = Map.of("packageName", pkg);
        String path = "src/main/java/" + pkg.replace('.', '/') + "/config/TransactionConfig.java";
        return new JavaSourceFile(path, render("java/transaction-config.ftl", model), ctx.getSchemaName(), ArtifactType.CONFIG);
    }

    public JavaSourceFile generateCustomerNotFoundException(GenerationContext ctx) {
        String pkg = ctx.getTargetPackage();
        Map<String, Object> model = Map.of("packageName", pkg);
        String path = "src/main/java/" + pkg.replace('.', '/') + "/exception/CustomerNotFoundException.java";
        return new JavaSourceFile(path, render("java/customer-not-found.ftl", model), ctx.getSchemaName(), ArtifactType.EXCEPTION);
    }

    public JavaSourceFile generateGlobalExceptionHandler(GenerationContext ctx) {
        String pkg = ctx.getTargetPackage();
        Map<String, Object> model = Map.of("packageName", pkg);
        String path = "src/main/java/" + pkg.replace('.', '/') + "/exception/GlobalExceptionHandler.java";
        return new JavaSourceFile(path, render("java/global-exception-handler.ftl", model), ctx.getSchemaName(), ArtifactType.EXCEPTION);
    }

    public JavaSourceFile generateCustomerMapper(GenerationContext ctx) {
        String pkg = ctx.getTargetPackage();
        Map<String, Object> model = Map.of("packageName", pkg);
        String path = "src/main/java/" + pkg.replace('.', '/') + "/mapper/CustomerMapper.java";
        return new JavaSourceFile(path, render("java/customer-mapper.ftl", model), ctx.getSchemaName(), ArtifactType.SERVICE);
    }

    public JavaSourceFile generateCustomerDto(GenerationContext ctx) {
        String pkg = ctx.getTargetPackage();
        Map<String, Object> model = Map.of("packageName", pkg);
        String path = "src/main/java/" + pkg.replace('.', '/') + "/dto/CustomerDto.java";
        return new JavaSourceFile(path, render("java/customer-dto.ftl", model), ctx.getSchemaName(), ArtifactType.DTO);
    }

    public JavaSourceFile generateCustomerStatusDto(GenerationContext ctx) {
        String pkg = ctx.getTargetPackage();
        Map<String, Object> model = Map.of("packageName", pkg);
        String path = "src/main/java/" + pkg.replace('.', '/') + "/dto/CustomerStatusDto.java";
        return new JavaSourceFile(path, render("java/customer-status-dto.ftl", model), ctx.getSchemaName(), ArtifactType.DTO);
    }

    public JavaSourceFile generateJdbcUtil(GenerationContext ctx) {
        String pkg = ctx.getTargetPackage();
        Map<String, Object> model = Map.of("packageName", pkg);
        String path = "src/main/java/" + pkg.replace('.', '/') + "/util/JdbcUtil.java";
        return new JavaSourceFile(path, render("java/jdbc-util.ftl", model), ctx.getSchemaName(), ArtifactType.CONFIG);
    }

    public JavaSourceFile generateSqlConstants(GenerationContext ctx) {
        String pkg = ctx.getTargetPackage();
        Map<String, Object> model = Map.of("packageName", pkg);
        String path = "src/main/java/" + pkg.replace('.', '/') + "/util/SqlConstants.java";
        return new JavaSourceFile(path, render("java/sql-constants.ftl", model), ctx.getSchemaName(), ArtifactType.CONFIG);
    }

    public JavaSourceFile generateCustomerRepositoryTest(GenerationContext ctx) {
        String pkg = ctx.getTargetPackage();
        Map<String, Object> model = Map.of("packageName", pkg);
        String path = "src/test/java/" + pkg.replace('.', '/') + "/repository/CustomerRepositoryTest.java";
        return new JavaSourceFile(path, render("java/repository-test.ftl", model), ctx.getSchemaName(), ArtifactType.TEST);
    }

    public JavaSourceFile generateLogback(GenerationContext ctx) {
        Map<String, Object> model = Map.of("packageName", ctx.getTargetPackage());
        return new JavaSourceFile("src/main/resources/logback.xml",
                render("java/logback.ftl", model), ctx.getSchemaName(), ArtifactType.APP_YML);
    }

    public JavaSourceFile generateEntity(String domainName, GenerationContext ctx) {
        String pkg = ctx.getTargetPackage();
        Map<String, Object> model = new HashMap<>();
        model.put("packageName", pkg);
        model.put("className", domainName);
        model.put("tableName", domainName.toUpperCase());
        model.put("columns", buildEntityColumns(domainName));
        String path = "src/main/java/" + pkg.replace('.', '/') + "/entity/" + domainName + ".java";
        return new JavaSourceFile(path, render("java/entity.ftl", model), domainName, ArtifactType.ENTITY);
    }

    /** Returns column metadata derived from the discovered DDL for known tables. */
    private List<Map<String, Object>> buildEntityColumns(String domainName) {
        // Column descriptor: name, javaType, fieldName, capitalName, nullable, length
        return switch (domainName.toUpperCase()) {
            case "CUSTOMER" -> List.of(
                col("CUSTOMER_NAME", "String",  false, 100),
                col("EMAIL",         "String",  true,  100),
                col("PHONE",         "String",  true,  20),
                col("DOB",           "LocalDate", true, 0),
                col("STATUS",        "String",  true,  20),
                col("ANNUAL_INCOME", "java.math.BigDecimal", true, 0),
                col("CREATED_DATE",  "LocalDateTime", true, 0),
                col("UPDATED_DATE",  "LocalDateTime", true, 0)
            );
            case "KYC" -> List.of(
                col("CUSTOMER_ID",   "Long",   false, 0),
                col("PAN_NUMBER",    "String", true,  20),
                col("AADHAR_NUMBER", "String", true,  20),
                col("KYC_STATUS",    "String", true,  20),
                col("VERIFIED_DATE", "LocalDate", true, 0)
            );
            default -> List.of();
        };
    }

    private Map<String, Object> col(String name, String javaType, boolean nullable, int length) {
        String fieldName = toCamelCase(name);
        String capitalName = toPascalCase(name);
        return Map.of("name", name, "javaType", javaType, "fieldName", fieldName,
                "capitalName", capitalName, "nullable", nullable, "length", length);
    }

    private static String toCamelCase(String name) {
        if (name == null || name.isBlank()) return "unknown";
        String pascal = toPascalCase(name);
        return Character.toLowerCase(pascal.charAt(0)) + pascal.substring(1);
    }

    public JavaSourceFile generateRepository(String domainName, GenerationContext ctx) {
        String pkg = ctx.getTargetPackage();
        Map<String, Object> model = new HashMap<>();
        model.put("packageName", pkg);
        model.put("className", domainName);
        String path = "src/main/java/" + pkg.replace('.', '/') + "/repository/" + domainName + "Repository.java";
        return new JavaSourceFile(path, render("java/repository.ftl", model), domainName, ArtifactType.REPOSITORY);
    }

    public JavaSourceFile generateNotFoundException(GenerationContext ctx) {
        return generateCustomerNotFoundException(ctx);
    }

    public JavaSourceFile generateRefCursorService(GenerationContext ctx) {
        String pkg = ctx.getTargetPackage();
        Map<String, Object> model = Map.of("packageName", pkg);
        String path = "src/main/java/" + pkg.replace('.', '/') + "/cursor/CustomerCursorMapper.java";
        return new JavaSourceFile(path, render("java/cursor-mapper.ftl", model), "CustomerCursorMapper", ArtifactType.SERVICE);
    }

    public JavaSourceFile generateBulkUpdateService(GenerationContext ctx) {
        String pkg = ctx.getTargetPackage();
        Map<String, Object> model = Map.of("packageName", pkg);
        String path = "src/main/java/" + pkg.replace('.', '/') + "/batch/BulkCustomerUpdater.java";
        return new JavaSourceFile(path, render("java/bulk-updater.ftl", model), "BulkCustomerUpdater", ArtifactType.SERVICE);
    }

    public JavaSourceFile generateDynamicSqlService(GenerationContext ctx) {
        String pkg = ctx.getTargetPackage();
        Map<String, Object> model = Map.of("packageName", pkg);
        String path = "src/main/java/" + pkg.replace('.', '/') + "/dynamic/DynamicSqlExecutor.java";
        return new JavaSourceFile(path, render("java/dynamic-executor.ftl", model), "DynamicSqlExecutor", ArtifactType.SERVICE);
    }

    public JavaSourceFile generateDateUtils(GenerationContext ctx) {
        return generateJdbcUtil(ctx);
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

    /** Derives a clean domain class name from an Oracle object name.
     *  PKG_CUSTOMER -> Customer, TRG_KYC_SERVICE -> KycService, SEQ_ORDER -> Order
     */
    static String domainName(String oracleName) {
        if (oracleName == null || oracleName.isBlank()) return "Unknown";
        String upper = oracleName.toUpperCase();
        for (String prefix : STRIP_PREFIXES) {
            if (upper.startsWith(prefix)) {
                oracleName = oracleName.substring(prefix.length());
                break;
            }
        }
        return toPascalCase(oracleName);
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
