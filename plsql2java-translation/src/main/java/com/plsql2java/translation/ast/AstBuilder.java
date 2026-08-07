package com.plsql2java.translation.ast;

import com.plsql2java.translation.PlSqlParserBaseListener;
import com.plsql2java.translation.PlSqlParser;
import com.plsql2java.translation.model.AstNode;
import com.plsql2java.translation.model.ConstructType;

import java.util.ArrayList;
import java.util.List;

public class AstBuilder extends PlSqlParserBaseListener {

    private final List<AstNode> nodes = new ArrayList<>();

    @Override
    public void enterVarDeclSimple(PlSqlParser.VarDeclSimpleContext ctx) {
        AstNode node = new AstNode(ConstructType.VARIABLE_DECLARATION, ctx.getText(), ctx.start.getLine());
        node.setAttribute("varName", ctx.ID().getText());
        node.setAttribute("dataType", ctx.dataType().getText());
        nodes.add(node);
    }

    @Override
    public void enterIfStatement(PlSqlParser.IfStatementContext ctx) {
        nodes.add(new AstNode(ConstructType.IF_ELSIF_ELSE, ctx.getText(), ctx.start.getLine()));
    }

    @Override
    public void enterCaseStatement(PlSqlParser.CaseStatementContext ctx) {
        nodes.add(new AstNode(ConstructType.CASE_STATEMENT, ctx.getText(), ctx.start.getLine()));
    }

    @Override
    public void enterLoopStatement(PlSqlParser.LoopStatementContext ctx) {
        nodes.add(new AstNode(ConstructType.LOOP, ctx.getText(), ctx.start.getLine()));
    }

    @Override
    public void enterWhileStatement(PlSqlParser.WhileStatementContext ctx) {
        nodes.add(new AstNode(ConstructType.WHILE_LOOP, ctx.getText(), ctx.start.getLine()));
    }

    @Override
    public void enterForStatement(PlSqlParser.ForStatementContext ctx) {
        nodes.add(new AstNode(ConstructType.FOR_LOOP, ctx.getText(), ctx.start.getLine()));
    }

    @Override
    public void enterCursorForStatement(PlSqlParser.CursorForStatementContext ctx) {
        nodes.add(new AstNode(ConstructType.IMPLICIT_CURSOR_FOR, ctx.getText(), ctx.start.getLine()));
    }

    @Override
    public void enterExceptionHandler(PlSqlParser.ExceptionHandlerContext ctx) {
        AstNode node = new AstNode(ConstructType.EXCEPTION_HANDLER, ctx.getText(), ctx.start.getLine());
        node.setAttribute("exceptionName", ctx.exceptionName(0).getText().toUpperCase());
        nodes.add(node);
    }

    @Override
    public void enterCursorDecl(PlSqlParser.CursorDeclContext ctx) {
        AstNode node = new AstNode(ConstructType.EXPLICIT_CURSOR, ctx.getText(), ctx.start.getLine());
        node.setAttribute("cursorName", ctx.ID().getText());
        node.setAttribute("query", ctx.selectStmt().getText());
        nodes.add(node);
    }

    @Override
    public void enterBulkCollectStatement(PlSqlParser.BulkCollectStatementContext ctx) {
        nodes.add(new AstNode(ConstructType.BULK_COLLECT, ctx.getText(), ctx.start.getLine()));
    }

    @Override
    public void enterForallStatement(PlSqlParser.ForallStatementContext ctx) {
        boolean hasSaveExceptions = ctx.SAVE() != null;
        ConstructType type = hasSaveExceptions ? ConstructType.FORALL_SAVE_EXCEPTIONS : ConstructType.FORALL;
        nodes.add(new AstNode(type, ctx.getText(), ctx.start.getLine()));
    }

    @Override
    public void enterRaiseStatement(PlSqlParser.RaiseStatementContext ctx) {
        nodes.add(new AstNode(ConstructType.RAISE, ctx.getText(), ctx.start.getLine()));
    }

    @Override
    public void enterGotoStatement(PlSqlParser.GotoStatementContext ctx) {
        nodes.add(new AstNode(ConstructType.GOTO, ctx.getText(), ctx.start.getLine()));
    }

    @Override
    public void enterDbmsOutputStatement(PlSqlParser.DbmsOutputStatementContext ctx) {
        AstNode node = new AstNode(ConstructType.DBMS_OUTPUT, ctx.getText(), ctx.start.getLine());
        node.setAttribute("message", ctx.expr().getText());
        nodes.add(node);
    }

    public List<AstNode> getNodes() {
        return nodes;
    }
}
