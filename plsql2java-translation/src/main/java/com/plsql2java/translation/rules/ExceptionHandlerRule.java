package com.plsql2java.translation.rules;

import com.plsql2java.translation.engine.TranslationMappingLoader;
import com.plsql2java.translation.engine.TranslationRule;
import com.plsql2java.translation.model.AstNode;
import com.plsql2java.translation.model.ConstructType;
import com.plsql2java.translation.model.OracleExceptionMapping;
import com.plsql2java.translation.model.TranslationContext;
import com.plsql2java.translation.model.TranslationOutcome;
import org.springframework.stereotype.Component;

@Component
public class ExceptionHandlerRule implements TranslationRule {

    private final TranslationMappingLoader mappingLoader;

    public ExceptionHandlerRule(TranslationMappingLoader mappingLoader) {
        this.mappingLoader = mappingLoader;
    }

    @Override
    public ConstructType getConstructType() {
        return ConstructType.EXCEPTION_HANDLER;
    }

    @Override
    public TranslationOutcome apply(AstNode node, TranslationContext context) {
        String exceptionName = node.getAttribute("exceptionName");
        if (exceptionName == null) exceptionName = "OTHERS";

        OracleExceptionMapping mapping = mappingLoader.getExceptionMapping(exceptionName);
        String javaException = mapping != null ? mapping.getJavaException() : "Exception";

        String snippet;
        if ("OTHERS".equalsIgnoreCase(exceptionName)) {
            snippet = "} catch (Exception e) {\n"
                    + "            log.warn(\"Unhandled exception in " + context.getObjectName() + "\", e);\n"
                    + "        }";
        } else {
            snippet = "} catch (" + javaException + " e) {\n"
                    + "            // TODO: handle " + exceptionName + "\n"
                    + "        }";
        }
        return TranslationOutcome.translated(snippet);
    }
}
