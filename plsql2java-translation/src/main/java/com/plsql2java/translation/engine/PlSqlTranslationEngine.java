package com.plsql2java.translation.engine;

import com.plsql2java.common.ProgressEvent;
import com.plsql2java.common.ProgressListener;
import com.plsql2java.common.ProgressStage;
import com.plsql2java.model.OracleObject;
import com.plsql2java.translation.PlSqlLexer;
import com.plsql2java.translation.PlSqlParser;
import com.plsql2java.translation.ast.AstBuilder;
import com.plsql2java.translation.ast.PlSqlErrorListener;
import com.plsql2java.translation.model.AstNode;
import com.plsql2java.translation.model.ConstructTranslationResult;
import com.plsql2java.translation.model.ConstructType;
import com.plsql2java.translation.model.FlaggedConstruct;
import com.plsql2java.translation.model.JavaIR;
import com.plsql2java.translation.model.TranslationContext;
import com.plsql2java.translation.model.TranslationOutcome;
import com.plsql2java.translation.model.TranslationResult;
import com.plsql2java.translation.model.TranslationStatus;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlSqlTranslationEngine {

    private static final Logger log = LoggerFactory.getLogger(PlSqlTranslationEngine.class);
    private static final String DEFAULT_TARGET_PACKAGE = "com.example.service";

    private final TranslationRuleRegistry registry;
    private final JavaIRAssembler assembler;

    public PlSqlTranslationEngine(TranslationRuleRegistry registry, JavaIRAssembler assembler) {
        this.registry = registry;
        this.assembler = assembler;
    }

    public TranslationResult translate(OracleObject object) {
        return translate(object, DEFAULT_TARGET_PACKAGE);
    }

    public TranslationResult translate(OracleObject object, String targetPackage) {
        MDC.put("objectName", object.getName());
        try {
            TranslationContext context = new TranslationContext(
                    object.getName(), object.getType(), object.getSchema());

            String source = buildSource(object);
            List<AstNode> nodes = parse(source, object.getName(), context);

            for (AstNode node : nodes) {
                TranslationOutcome outcome = applyRule(node, context);
                context.addConstructResult(new ConstructTranslationResult(
                        node.getConstructType(), node.getLineNumber(), outcome));
            }

            JavaIR javaIR = assembler.assemble(object, context, targetPackage);
            List<FlaggedConstruct> flagged = buildFlaggedConstructs(object.getName(), context);

            return new TranslationResult(object, javaIR, context.getConstructResults(), flagged);

        } catch (Exception e) {
            log.error("Translation failed for object: {}", object.getName());
            TranslationOutcome failOutcome = TranslationOutcome.flagged(
                    "Translation failed: " + e.getMessage(),
                    "Review PL/SQL source manually",
                    100
            );
            ConstructTranslationResult failResult = new ConstructTranslationResult(
                    ConstructType.UNKNOWN, 0, failOutcome);
            FlaggedConstruct flagged = new FlaggedConstruct(
                    object.getName(), ConstructType.UNKNOWN, 0,
                    "Translation failed: " + e.getMessage(), "Review PL/SQL source manually");
            return new TranslationResult(object, null, List.of(failResult), List.of(flagged));
        } finally {
            MDC.remove("objectName");
        }
    }

    public List<TranslationResult> translateAll(List<OracleObject> objects, ProgressListener listener) {
        List<TranslationResult> results = new ArrayList<>();
        int total = objects.size();
        for (int i = 0; i < total; i++) {
            OracleObject object = objects.get(i);
            listener.onProgress(ProgressEvent.builder("translation", ProgressStage.TRANSLATION)
                    .objectName(object.getName())
                    .processed(i, total)
                    .message("Translating: " + object.getName())
                    .build());
            results.add(translate(object));
        }
        listener.onProgress(ProgressEvent.builder("translation", ProgressStage.TRANSLATION)
                .processed(total, total)
                .message("Translation complete")
                .build());
        return results;
    }

    private String buildSource(OracleObject object) {
        if (object.getSourceBody() != null && !object.getSourceBody().isBlank()) {
            return object.getSourceSpec() + "\n" + object.getSourceBody();
        }
        return object.getSourceSpec();
    }

    private List<AstNode> parse(String source, String objectName, TranslationContext context) {
        PlSqlErrorListener errorListener = new PlSqlErrorListener();

        PlSqlLexer lexer = new PlSqlLexer(CharStreams.fromString(source));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PlSqlParser parser = new PlSqlParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        PlSqlParser.CompilationUnitContext tree = parser.compilationUnit();

        if (errorListener.hasErrors()) {
            log.warn("Parse errors in object {}: {} error(s)", objectName, errorListener.getErrors().size());
            List<AstNode> errorNodes = new ArrayList<>();
            for (PlSqlErrorListener.ParseError error : errorListener.getErrors()) {
                AstNode errorNode = new AstNode(ConstructType.UNKNOWN,
                        "Parse error: " + error.message(), error.line());
                errorNodes.add(errorNode);
            }
            return errorNodes;
        }

        AstBuilder builder = new AstBuilder();
        ParseTreeWalker.DEFAULT.walk(builder, tree);
        return builder.getNodes();
    }

    private TranslationOutcome applyRule(AstNode node, TranslationContext context) {
        List<TranslationRule> rules = registry.getRulesForConstruct(node.getConstructType());
        if (rules.isEmpty()) {
            return TranslationOutcome.flagged(
                    "No translation rule for construct: " + node.getConstructType(),
                    "Review this construct manually",
                    30
            );
        }
        return rules.get(0).apply(node, context);
    }

    private List<FlaggedConstruct> buildFlaggedConstructs(String objectName, TranslationContext context) {
        List<FlaggedConstruct> flagged = new ArrayList<>();
        for (ConstructTranslationResult result : context.getConstructResults()) {
            if (result.getStatus() == TranslationStatus.FLAGGED) {
                flagged.add(new FlaggedConstruct(
                        objectName,
                        result.getConstructType(),
                        result.getLineNumber(),
                        result.getFlagReason(),
                        result.getRecommendation()
                ));
            }
        }
        return flagged;
    }
}
