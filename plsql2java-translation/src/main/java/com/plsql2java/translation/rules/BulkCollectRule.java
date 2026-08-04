package com.plsql2java.translation.rules;

import com.plsql2java.translation.engine.TranslationRule;
import com.plsql2java.translation.model.AstNode;
import com.plsql2java.translation.model.ConstructType;
import com.plsql2java.translation.model.TranslationContext;
import com.plsql2java.translation.model.TranslationOutcome;
import org.springframework.stereotype.Component;

@Component
public class BulkCollectRule implements TranslationRule {

    @Override
    public ConstructType getConstructType() {
        return ConstructType.BULK_COLLECT;
    }

    @Override
    public TranslationOutcome apply(AstNode node, TranslationContext context) {
        String rawText = node.getText().toUpperCase();
        boolean hasLimit = rawText.contains("LIMIT");
        String snippet;
        if (hasLimit) {
            snippet = "// BULK COLLECT with LIMIT translated to paginated fetch\n"
                    + "        // TODO: replace pageSize with actual LIMIT value\n"
                    + "        List<Object> results = repository.findAll(PageRequest.of(0, pageSize)).getContent();"
                    + " // import org.springframework.data.domain.PageRequest";
        } else {
            snippet = "// BULK COLLECT translated to List fetch\n"
                    + "        List<Object> results = repository.findAll(); // TODO: replace with typed repository method";
        }
        return TranslationOutcome.translated(snippet);
    }
}
