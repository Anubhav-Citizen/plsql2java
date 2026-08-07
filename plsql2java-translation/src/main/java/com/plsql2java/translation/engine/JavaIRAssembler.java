package com.plsql2java.translation.engine;

import com.plsql2java.model.OracleObject;
import com.plsql2java.model.OracleObjectType;
import com.plsql2java.translation.PlSqlParser;
import com.plsql2java.translation.model.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class JavaIRAssembler {

    public JavaIR assemble(OracleObject object, TranslationContext context, String targetPackage) {
        String className = toPascalCase(object.getName()) + "Service";
        List<String> injectedFields = new ArrayList<>();
        List<String> staticFields = new ArrayList<>();
        List<String> imports = new ArrayList<>();
        List<JavaMethodIR> methods = new ArrayList<>();

        imports.add("java.math.BigDecimal");
        imports.add("org.slf4j.Logger");
        imports.add("org.slf4j.LoggerFactory");
        imports.add("org.springframework.stereotype.Service");
        imports.add("org.springframework.transaction.annotation.Transactional");

        staticFields.add("private static final Logger log = LoggerFactory.getLogger(" + className + ".class);");

        List<ConstructTranslationResult> procResults = context.getConstructResults().stream()
                .filter(r -> r.getConstructType() == ConstructType.PROCEDURE_DEF
                          || r.getConstructType() == ConstructType.FUNCTION_DEF)
                .toList();

        if (!procResults.isEmpty()) {
            for (ConstructTranslationResult result : procResults) {
                methods.add(buildMethodFromProcResult(result, object, context, imports, injectedFields));
            }
        } else {
            methods.add(buildFallbackMethod(object, context));
        }

        boolean hasDml = context.getConstructResults().stream()
                .anyMatch(r -> r.getConstructType() == ConstructType.INSERT_STMT
                            || r.getConstructType() == ConstructType.UPDATE_STMT
                            || r.getConstructType() == ConstructType.DELETE_STMT
                            || r.getConstructType() == ConstructType.SELECT_INTO);
        if (hasDml) {
            imports.add("org.springframework.jdbc.core.JdbcTemplate");
            injectedFields.add("JdbcTemplate jdbcTemplate");
        }

        List<String> allFields = new ArrayList<>(staticFields);
        injectedFields.forEach(f -> allFields.add("private final " + f + ";"));

        List<String> sortedImports = deduplicateAndSort(imports);
        String rawSource = buildRawSource(targetPackage, sortedImports, className, staticFields,
                injectedFields, methods, object.isHasCompilationErrors());

        return new JavaIR(object.getName(), targetPackage, className,
                sortedImports, allFields, methods, rawSource);
    }

    private JavaMethodIR buildMethodFromProcResult(ConstructTranslationResult result,
                                                    OracleObject object,
                                                    TranslationContext context,
                                                    List<String> imports,
                                                    List<String> fields) {
        Object nodeObj = result.getAstNode();
        if (!(nodeObj instanceof AstNode node)) {
            return buildFallbackMethod(object, context);
        }

        String procName = node.getAttribute("name");
        String methodName = procName != null ? toCamelCase(procName) : "execute";
        boolean isFunction = result.getConstructType() == ConstructType.FUNCTION_DEF;
        String returnType = isFunction ? mapOracleReturnType(node.getAttribute("returnType")) : "void";

        List<String> javaParams = buildJavaParams(node);
        String body = buildMethodBody(node, object, context, imports, returnType);

        String javadoc = "/** Translated from PL/SQL: " + object.getSchema() + "."
                + object.getName() + "." + (procName != null ? procName : "execute") + " */";

        List<String> annotations = new ArrayList<>();
        annotations.add("@Transactional");

        return new JavaMethodIR(methodName, returnType, javaParams, body, annotations, javadoc,
                List.of(result));
    }

    private String buildMethodBody(AstNode node, OracleObject object,
                                    TranslationContext context, List<String> imports,
                                    String returnType) {
        Object blockCtxObj = node.getObjectAttribute("blockCtx");
        if (!(blockCtxObj instanceof PlSqlParser.BlockContext blockCtx)) {
            return "// TODO: implement - PL/SQL block not parsed";
        }

        StringBuilder body = new StringBuilder();
        for (PlSqlParser.StatementContext stmt : blockCtx.statement()) {
            String line = translateStatement(stmt, object, imports);
            if (line != null) body.append(line).append("\n        ");
        }

        if (blockCtx.exceptionBlock() != null) {
            for (PlSqlParser.ExceptionHandlerContext handler : blockCtx.exceptionBlock().exceptionHandler()) {
                String exName = handler.exceptionName(0).getText().toUpperCase();
                body.append("// EXCEPTION: ").append(exName).append(" -> ")
                    .append(mapException(exName)).append("\n        ");
            }
        }

        if (!returnType.equals("void") && !body.toString().contains("return ")) {
            body.append("return null; // TODO: map return value");
        }

        return body.toString().trim();
    }

    private String translateStatement(PlSqlParser.StatementContext stmt, OracleObject object,
                                       List<String> imports) {
        if (stmt.insertStatement() != null) {
            return translateInsert(stmt.insertStatement(), object);
        } else if (stmt.updateStatement() != null) {
            return translateUpdate(stmt.updateStatement(), object);
        } else if (stmt.deleteStatement() != null) {
            return translateDelete(stmt.deleteStatement(), object);
        } else if (stmt.selectIntoStatement() != null) {
            return translateSelectInto(stmt.selectIntoStatement(), object);
        } else if (stmt.ifStatement() != null) {
            return translateIf(stmt.ifStatement(), object, imports);
        } else if (stmt.returnStatement() != null) {
            return translateReturn(stmt.returnStatement());
        } else if (stmt.dbmsOutputStatement() != null) {
            return "log.debug(\"" + stmt.dbmsOutputStatement().expr().getText() + "\");";
        } else if (stmt.assignStatement() != null) {
            return translateAssign(stmt.assignStatement());
        } else if (stmt.callStatement() != null) {
            return translateCall(stmt.callStatement());
        } else if (stmt.openStatement() != null) {
            return "// REF CURSOR: see RefCursorService.getCustomers()";
        } else if (stmt.nullStatement() != null) {
            return "// NULL statement";
        }
        return "// TODO: " + stmt.getText().substring(0, Math.min(60, stmt.getText().length()));
    }

    private String translateInsert(PlSqlParser.InsertStatementContext ctx, OracleObject object) {
        String table = ctx.ID().getText();
        List<org.antlr.v4.runtime.tree.TerminalNode> cols = ctx.idList() != null ? ctx.idList().ID() : List.of();
        List<PlSqlParser.ExprContext> exprs = ctx.exprList() != null ? ctx.exprList().expr() : List.of();
        int count = Math.min(cols.size(), exprs.size());
        if (count == 0) {
            return "jdbcTemplate.update(\"INSERT INTO " + table + " VALUES (?)\", /* TODO: params */);";
        }
        List<String> colNames = new ArrayList<>();
        List<String> args = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            colNames.add(cols.get(i).getText());
            args.add(mapExprToJava(exprs.get(i).getText()));
        }
        String colList = String.join(", ", colNames);
        String placeholders = String.join(", ", java.util.Collections.nCopies(count, "?"));
        String argList = String.join(", ", args);
        return "jdbcTemplate.update(\"INSERT INTO " + table + " (" + colList + ") VALUES (" + placeholders + ")\", " + argList + ");";
    }

    private String translateUpdate(PlSqlParser.UpdateStatementContext ctx, OracleObject object) {
        String table = ctx.ID(0).getText();
        List<org.antlr.v4.runtime.tree.TerminalNode> ids = ctx.ID();
        List<PlSqlParser.ExprContext> exprs = ctx.expr();
        List<String> setClauses = new ArrayList<>();
        List<String> setArgs = new ArrayList<>();
        int setCnt = ctx.condition() != null ? exprs.size() - 1 : exprs.size();
        for (int i = 1; i < ids.size() && i - 1 < setCnt; i++) {
            setClauses.add(ids.get(i).getText() + "=?");
            setArgs.add(mapExprToJava(exprs.get(i - 1).getText()));
        }
        String setClause = String.join(", ", setClauses);
        String args = String.join(", ", setArgs);
        if (ctx.condition() != null) {
            // WHERE condition bind param: extract from condition text, not from exprs list
            String condText = ctx.condition().getText();
            String whereClause = condText.replaceAll("=[^,)]+", "=?");
            String whereArg = toCamelCase(condText.replaceAll(".*=", ""));
            return "jdbcTemplate.update(\"UPDATE " + table + " SET " + setClause
                    + " WHERE " + whereClause + "\", "
                    + (args.isEmpty() ? whereArg : args + ", " + whereArg) + ");";
        }
        return "jdbcTemplate.update(\"UPDATE " + table + " SET " + setClause + "\", " + args + ");";
    }

    private String translateDelete(PlSqlParser.DeleteStatementContext ctx, OracleObject object) {
        String table = ctx.ID().getText();
        if (ctx.condition() != null) {
            return "jdbcTemplate.update(\"DELETE FROM " + table + " WHERE "
                    + ctx.condition().getText() + "\");";
        }
        return toCamelCase(table) + "Repository.deleteAll();";
    }

    private String translateSelectInto(PlSqlParser.SelectIntoStatementContext ctx, OracleObject object) {
        String table = ctx.ID().getText();
        String intoVar = toCamelCase(ctx.idList().getText());
        String cols = ctx.selectExprList().getText();
        String javaType = mapJdbcReturnType(cols);
        if (ctx.condition() != null) {
            String whereText = ctx.condition().getText();
            String bindParam = toCamelCase(whereText.replaceAll(".*=", ""));
            String whereClause = whereText.replaceAll("=[^,)]+", "=?");
            return javaType + " " + intoVar + " = jdbcTemplate.queryForObject(\n            "
                    + "\"SELECT " + cols + " FROM " + table + " WHERE " + whereClause + "\",\n            "
                    + javaType + ".class, " + bindParam + ");";
        }
        return javaType + " " + intoVar + " = jdbcTemplate.queryForObject(\"SELECT " + cols
                + " FROM " + table + "\", " + javaType + ".class);";
    }

    private String translateIf(PlSqlParser.IfStatementContext ctx, OracleObject object,
                                 List<String> imports) {
        String cond = translateCondition(ctx.condition(0).getText());
        StringBuilder sb = new StringBuilder();
        sb.append("if (").append(cond).append(") {\n        ");
        for (PlSqlParser.StatementContext s : ctx.statement()) {
            String line = translateStatement(s, object, imports);
            if (line != null) sb.append("    ").append(line).append("\n        ");
        }
        sb.append("}");
        return sb.toString();
    }

    private String translateReturn(PlSqlParser.ReturnStatementContext ctx) {
        if (ctx.expr() != null) {
            return "return " + mapExprToJava(ctx.expr().getText()) + ";";
        }
        return "return;";
    }

    private String translateAssign(PlSqlParser.AssignStatementContext ctx) {
        String varName = toCamelCase(ctx.ID(0).getText());
        String val = mapExprToJava(ctx.expr().getText());
        return varName + " = " + val + ";";
    }

    private String translateCall(PlSqlParser.CallStatementContext ctx) {
        String name = ctx.ID(0).getText();
        String args = ctx.exprList() != null ? ctx.exprList().getText() : "";
        return toCamelCase(name) + "(" + args + ");";
    }

    private JavaMethodIR buildFallbackMethod(OracleObject object, TranslationContext context) {
        List<String> bodyLines = new ArrayList<>();
        for (ConstructTranslationResult result : context.getConstructResults()) {
            if (result.getJavaSnippet() != null) bodyLines.add(result.getJavaSnippet());
            if (result.getStatus() == TranslationStatus.FLAGGED && result.getFlagReason() != null) {
                bodyLines.add("// TODO [FLAGGED - " + result.getConstructType() + " line "
                        + result.getLineNumber() + "]: " + result.getFlagReason());
                if (result.getRecommendation() != null)
                    bodyLines.add("// Recommendation: " + result.getRecommendation());
            }
        }
        if (bodyLines.isEmpty()) bodyLines.add("// TODO: implement");
        String javadoc = "/** Translated from PL/SQL: " + object.getSchema() + "." + object.getName() + " */";
        return new JavaMethodIR("execute", "void", List.of(),
                String.join("\n        ", bodyLines), List.of("@Transactional"), javadoc,
                context.getConstructResults());
    }

    private List<String> buildJavaParams(AstNode node) {
        Object paramCtxObj = node.getObjectAttribute("paramCtx");
        if (!(paramCtxObj instanceof PlSqlParser.ParamListContext paramCtx)) return List.of();
        List<String> params = new ArrayList<>();
        for (PlSqlParser.ParamContext p : paramCtx.param()) {
            String javaType = mapOracleType(p.dataType().getText());
            String paramName = toCamelCase(p.ID().getText());
            params.add(javaType + " " + paramName);
        }
        return params;
    }

    private String mapOracleType(String oracleType) {
        if (oracleType == null) return "Object";
        String upper = oracleType.toUpperCase();
        if (upper.startsWith("VARCHAR2") || upper.startsWith("VARCHAR") || upper.contains("CHAR")) return "String";
        if (upper.startsWith("NUMBER") || upper.startsWith("INTEGER") || upper.startsWith("INT")) return "BigDecimal";
        if (upper.startsWith("DATE")) return "java.time.LocalDate";
        if (upper.startsWith("BOOLEAN")) return "boolean";
        if (upper.contains("%TYPE") || upper.contains("%ROWTYPE")) return "Object";
        return "Object";
    }

    private String mapOracleReturnType(String oracleType) {
        if (oracleType == null) return "Object";
        String upper = oracleType.toUpperCase();
        if (upper.startsWith("VARCHAR2") || upper.startsWith("VARCHAR") || upper.contains("CHAR")) return "String";
        if (upper.startsWith("NUMBER") || upper.startsWith("INTEGER") || upper.startsWith("INT")) return "Long";
        if (upper.startsWith("DATE")) return "java.time.LocalDate";
        if (upper.startsWith("BOOLEAN")) return "boolean";
        if (upper.contains("%TYPE") || upper.contains("%ROWTYPE")) return "Object";
        return "Object";
    }

    private String mapJdbcReturnType(String cols) {
        if (cols.toUpperCase().contains("COUNT")) return "Long";
        if (cols.toUpperCase().contains("STATUS") || cols.toUpperCase().contains("NAME")) return "String";
        return "BigDecimal";
    }

    private String mapExprToJava(String expr) {
        if (expr == null) return "null";
        if (expr.startsWith(":NEW.")) return "entity.get" + capitalize(toCamelCase(expr.substring(5))) + "()";
        if (expr.startsWith(":OLD.")) return "entity.get" + capitalize(toCamelCase(expr.substring(5))) + "()";
        if (expr.equalsIgnoreCase("SYSDATE")) return "java.time.LocalDateTime.now()";
        if (expr.toUpperCase().contains(".NEXTVAL")) return "null /* @GeneratedValue handles this */";
        if (expr.startsWith("'") && expr.endsWith("'")) return "\"" + expr.substring(1, expr.length() - 1) + "\"";
        return toCamelCase(expr);
    }

    private String translateCondition(String condition) {
        if (condition == null) return "true";
        return condition
                .replace(":NEW.", "entity.get")
                .replace(":OLD.", "entity.get")
                .replace("IS NULL", "== null")
                .replace("IS NOT NULL", "!= null")
                .replace("<>", "!=")
                .replace("SYSDATE", "java.time.LocalDateTime.now()");
    }

    private String mapException(String oracleException) {
        return switch (oracleException) {
            case "NO_DATA_FOUND" -> "throw new ResourceNotFoundException(\"Record not found\");";
            case "TOO_MANY_ROWS" -> "throw new IllegalStateException(\"Too many rows returned\");";
            case "DUP_VAL_ON_INDEX" -> "throw new IllegalArgumentException(\"Duplicate value\");";
            default -> "log.warn(\"Unhandled exception: " + oracleException + "\");";
        };
    }

    private List<String> deduplicateAndSort(List<String> imports) {
        Set<String> seen = new LinkedHashSet<>(imports);
        return seen.stream().sorted().collect(Collectors.toList());
    }

    private String buildRawSource(String packageName, List<String> imports, String className,
                                   List<String> staticFields, List<String> injectedFields,
                                   List<JavaMethodIR> methods, boolean hasCompilationErrors) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(packageName).append(";\n\n");
        imports.forEach(i -> sb.append("import ").append(i).append(";\n"));
        sb.append("\n");
        if (hasCompilationErrors) sb.append("// WARNING: Source PL/SQL had compilation errors.\n");
        sb.append("@Service\n");
        sb.append("public class ").append(className).append(" {\n\n");
        staticFields.forEach(f -> sb.append("    ").append(f).append("\n"));
        injectedFields.forEach(f -> sb.append("    private final ").append(f).append(";\n"));
        if (!injectedFields.isEmpty()) {
            sb.append("\n    public ").append(className).append("(");
            sb.append(String.join(", ", injectedFields));
            sb.append(") {\n");
            injectedFields.forEach(f -> {
                String name = f.trim().split("\\s+")[f.trim().split("\\s+").length - 1];
                sb.append("        this.").append(name).append(" = ").append(name).append(";\n");
            });
            sb.append("    }\n");
        }
        sb.append("\n");
        for (JavaMethodIR m : methods) {
            sb.append("    ").append(m.getJavadoc()).append("\n");
            m.getAnnotations().forEach(a -> sb.append("    ").append(a).append("\n"));
            sb.append("    public ").append(m.getReturnType()).append(" ").append(m.getMethodName())
              .append("(").append(String.join(", ", m.getParameters())).append(") {\n");
            if (m.getBody() != null && !m.getBody().isBlank()) {
                Arrays.stream(m.getBody().split("\n"))
                      .forEach(line -> sb.append("        ").append(line).append("\n"));
            }
            sb.append("    }\n\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    public static String toPascalCase(String name) {
        if (name == null || name.isBlank()) return "Unknown";
        String[] parts = name.split("[_\\s]+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                sb.append(part.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

    static String toCamelCase(String name) {
        if (name == null || name.isBlank()) return "unknown";
        String pascal = toPascalCase(name);
        return Character.toLowerCase(pascal.charAt(0)) + pascal.substring(1);
    }

    static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
