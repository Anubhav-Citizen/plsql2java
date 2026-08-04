package com.plsql2java.translation.rules;

import com.plsql2java.translation.engine.TranslationRule;
import com.plsql2java.translation.model.AstNode;
import com.plsql2java.translation.model.ConstructType;
import com.plsql2java.translation.model.TranslationContext;
import com.plsql2java.translation.model.TranslationOutcome;
import org.springframework.stereotype.Component;

@Component
public class LoopRule implements TranslationRule {

    @Override
    public ConstructType getConstructType() {
        return ConstructType.LOOP;
    }

    @Override
    public TranslationOutcome apply(AstNode node, TranslationContext context) {
        ConstructType type = node.getConstructType();
        String snippet = switch (type) {
            case WHILE_LOOP -> "// WHILE loop translated from PL/SQL\n"
                    + "        while (/* condition */) {\n"
                    + "            // TODO: implement loop body\n"
                    + "        }";
            case FOR_LOOP -> "// FOR loop translated from PL/SQL\n"
                    + "        for (int i = /* low */; i <= /* high */; i++) {\n"
                    + "            // TODO: implement loop body\n"
                    + "        }";
            default -> "// LOOP translated from PL/SQL (EXIT WHEN → break)\n"
                    + "        while (true) {\n"
                    + "            // TODO: implement loop body\n"
                    + "            // if (exitCondition) break;\n"
                    + "        }";
        };
        return TranslationOutcome.translated(snippet);
    }
}
