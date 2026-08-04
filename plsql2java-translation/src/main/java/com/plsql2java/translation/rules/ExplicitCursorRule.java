package com.plsql2java.translation.rules;

import com.plsql2java.translation.engine.TranslationRule;
import com.plsql2java.translation.model.AstNode;
import com.plsql2java.translation.model.ConstructType;
import com.plsql2java.translation.model.TranslationContext;
import com.plsql2java.translation.model.TranslationOutcome;
import org.springframework.stereotype.Component;

@Component
public class ExplicitCursorRule implements TranslationRule {

    @Override
    public ConstructType getConstructType() {
        return ConstructType.EXPLICIT_CURSOR;
    }

    @Override
    public TranslationOutcome apply(AstNode node, TranslationContext context) {
        String cursorName = node.getAttribute("cursorName");
        String query = node.getAttribute("query");
        if (cursorName != null && query != null) {
            context.registerCursor(cursorName, query);
        }
        String snippet = "// Cursor " + (cursorName != null ? cursorName : "UNKNOWN")
                + " translated to repository query\n"
                + "        // @Query(nativeQuery = true, value = \"" + sanitize(query) + "\")\n"
                + "        // List<Object[]> " + (cursorName != null ? cursorName.toLowerCase() : "results")
                + " = repository.findByCursor(); // TODO: define repository method";
        return TranslationOutcome.translated(snippet);
    }

    private String sanitize(String query) {
        if (query == null) return "";
        return query.replaceAll("\\s+", " ").trim();
    }
}
