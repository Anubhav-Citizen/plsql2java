// Generated from com/plsql2java/translation/PlSqlParser.g4 by ANTLR 4.13.1
package com.plsql2java.translation;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link PlSqlParser}.
 */
public interface PlSqlParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void enterCompilationUnit(PlSqlParser.CompilationUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void exitCompilationUnit(PlSqlParser.CompilationUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#packageSpec}.
	 * @param ctx the parse tree
	 */
	void enterPackageSpec(PlSqlParser.PackageSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#packageSpec}.
	 * @param ctx the parse tree
	 */
	void exitPackageSpec(PlSqlParser.PackageSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#packageBody}.
	 * @param ctx the parse tree
	 */
	void enterPackageBody(PlSqlParser.PackageBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#packageBody}.
	 * @param ctx the parse tree
	 */
	void exitPackageBody(PlSqlParser.PackageBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#procedureDecl}.
	 * @param ctx the parse tree
	 */
	void enterProcedureDecl(PlSqlParser.ProcedureDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#procedureDecl}.
	 * @param ctx the parse tree
	 */
	void exitProcedureDecl(PlSqlParser.ProcedureDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#functionDecl}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDecl(PlSqlParser.FunctionDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#functionDecl}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDecl(PlSqlParser.FunctionDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#paramList}.
	 * @param ctx the parse tree
	 */
	void enterParamList(PlSqlParser.ParamListContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#paramList}.
	 * @param ctx the parse tree
	 */
	void exitParamList(PlSqlParser.ParamListContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#param}.
	 * @param ctx the parse tree
	 */
	void enterParam(PlSqlParser.ParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#param}.
	 * @param ctx the parse tree
	 */
	void exitParam(PlSqlParser.ParamContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#declareSection}.
	 * @param ctx the parse tree
	 */
	void enterDeclareSection(PlSqlParser.DeclareSectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#declareSection}.
	 * @param ctx the parse tree
	 */
	void exitDeclareSection(PlSqlParser.DeclareSectionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code varDeclSimple}
	 * labeled alternative in {@link PlSqlParser#variableDecl}.
	 * @param ctx the parse tree
	 */
	void enterVarDeclSimple(PlSqlParser.VarDeclSimpleContext ctx);
	/**
	 * Exit a parse tree produced by the {@code varDeclSimple}
	 * labeled alternative in {@link PlSqlParser#variableDecl}.
	 * @param ctx the parse tree
	 */
	void exitVarDeclSimple(PlSqlParser.VarDeclSimpleContext ctx);
	/**
	 * Enter a parse tree produced by the {@code varDeclDefault}
	 * labeled alternative in {@link PlSqlParser#variableDecl}.
	 * @param ctx the parse tree
	 */
	void enterVarDeclDefault(PlSqlParser.VarDeclDefaultContext ctx);
	/**
	 * Exit a parse tree produced by the {@code varDeclDefault}
	 * labeled alternative in {@link PlSqlParser#variableDecl}.
	 * @param ctx the parse tree
	 */
	void exitVarDeclDefault(PlSqlParser.VarDeclDefaultContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#cursorDecl}.
	 * @param ctx the parse tree
	 */
	void enterCursorDecl(PlSqlParser.CursorDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#cursorDecl}.
	 * @param ctx the parse tree
	 */
	void exitCursorDecl(PlSqlParser.CursorDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterDataType(PlSqlParser.DataTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitDataType(PlSqlParser.DataTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#block}.
	 * @param ctx the parse tree
	 */
	void enterBlock(PlSqlParser.BlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#block}.
	 * @param ctx the parse tree
	 */
	void exitBlock(PlSqlParser.BlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#exceptionBlock}.
	 * @param ctx the parse tree
	 */
	void enterExceptionBlock(PlSqlParser.ExceptionBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#exceptionBlock}.
	 * @param ctx the parse tree
	 */
	void exitExceptionBlock(PlSqlParser.ExceptionBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#exceptionHandler}.
	 * @param ctx the parse tree
	 */
	void enterExceptionHandler(PlSqlParser.ExceptionHandlerContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#exceptionHandler}.
	 * @param ctx the parse tree
	 */
	void exitExceptionHandler(PlSqlParser.ExceptionHandlerContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#exceptionName}.
	 * @param ctx the parse tree
	 */
	void enterExceptionName(PlSqlParser.ExceptionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#exceptionName}.
	 * @param ctx the parse tree
	 */
	void exitExceptionName(PlSqlParser.ExceptionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#packageSpecItem}.
	 * @param ctx the parse tree
	 */
	void enterPackageSpecItem(PlSqlParser.PackageSpecItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#packageSpecItem}.
	 * @param ctx the parse tree
	 */
	void exitPackageSpecItem(PlSqlParser.PackageSpecItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#procedureSpec}.
	 * @param ctx the parse tree
	 */
	void enterProcedureSpec(PlSqlParser.ProcedureSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#procedureSpec}.
	 * @param ctx the parse tree
	 */
	void exitProcedureSpec(PlSqlParser.ProcedureSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#functionSpec}.
	 * @param ctx the parse tree
	 */
	void enterFunctionSpec(PlSqlParser.FunctionSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#functionSpec}.
	 * @param ctx the parse tree
	 */
	void exitFunctionSpec(PlSqlParser.FunctionSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(PlSqlParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(PlSqlParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#ifStatement}.
	 * @param ctx the parse tree
	 */
	void enterIfStatement(PlSqlParser.IfStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#ifStatement}.
	 * @param ctx the parse tree
	 */
	void exitIfStatement(PlSqlParser.IfStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#caseStatement}.
	 * @param ctx the parse tree
	 */
	void enterCaseStatement(PlSqlParser.CaseStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#caseStatement}.
	 * @param ctx the parse tree
	 */
	void exitCaseStatement(PlSqlParser.CaseStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#loopStatement}.
	 * @param ctx the parse tree
	 */
	void enterLoopStatement(PlSqlParser.LoopStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#loopStatement}.
	 * @param ctx the parse tree
	 */
	void exitLoopStatement(PlSqlParser.LoopStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#whileStatement}.
	 * @param ctx the parse tree
	 */
	void enterWhileStatement(PlSqlParser.WhileStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#whileStatement}.
	 * @param ctx the parse tree
	 */
	void exitWhileStatement(PlSqlParser.WhileStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#forStatement}.
	 * @param ctx the parse tree
	 */
	void enterForStatement(PlSqlParser.ForStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#forStatement}.
	 * @param ctx the parse tree
	 */
	void exitForStatement(PlSqlParser.ForStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#cursorForStatement}.
	 * @param ctx the parse tree
	 */
	void enterCursorForStatement(PlSqlParser.CursorForStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#cursorForStatement}.
	 * @param ctx the parse tree
	 */
	void exitCursorForStatement(PlSqlParser.CursorForStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#openStatement}.
	 * @param ctx the parse tree
	 */
	void enterOpenStatement(PlSqlParser.OpenStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#openStatement}.
	 * @param ctx the parse tree
	 */
	void exitOpenStatement(PlSqlParser.OpenStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#fetchStatement}.
	 * @param ctx the parse tree
	 */
	void enterFetchStatement(PlSqlParser.FetchStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#fetchStatement}.
	 * @param ctx the parse tree
	 */
	void exitFetchStatement(PlSqlParser.FetchStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#closeStatement}.
	 * @param ctx the parse tree
	 */
	void enterCloseStatement(PlSqlParser.CloseStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#closeStatement}.
	 * @param ctx the parse tree
	 */
	void exitCloseStatement(PlSqlParser.CloseStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#bulkCollectStatement}.
	 * @param ctx the parse tree
	 */
	void enterBulkCollectStatement(PlSqlParser.BulkCollectStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#bulkCollectStatement}.
	 * @param ctx the parse tree
	 */
	void exitBulkCollectStatement(PlSqlParser.BulkCollectStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#forallStatement}.
	 * @param ctx the parse tree
	 */
	void enterForallStatement(PlSqlParser.ForallStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#forallStatement}.
	 * @param ctx the parse tree
	 */
	void exitForallStatement(PlSqlParser.ForallStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#dmlStatement}.
	 * @param ctx the parse tree
	 */
	void enterDmlStatement(PlSqlParser.DmlStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#dmlStatement}.
	 * @param ctx the parse tree
	 */
	void exitDmlStatement(PlSqlParser.DmlStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#insertStmt}.
	 * @param ctx the parse tree
	 */
	void enterInsertStmt(PlSqlParser.InsertStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#insertStmt}.
	 * @param ctx the parse tree
	 */
	void exitInsertStmt(PlSqlParser.InsertStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#updateStmt}.
	 * @param ctx the parse tree
	 */
	void enterUpdateStmt(PlSqlParser.UpdateStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#updateStmt}.
	 * @param ctx the parse tree
	 */
	void exitUpdateStmt(PlSqlParser.UpdateStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#deleteStmt}.
	 * @param ctx the parse tree
	 */
	void enterDeleteStmt(PlSqlParser.DeleteStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#deleteStmt}.
	 * @param ctx the parse tree
	 */
	void exitDeleteStmt(PlSqlParser.DeleteStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#raiseStatement}.
	 * @param ctx the parse tree
	 */
	void enterRaiseStatement(PlSqlParser.RaiseStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#raiseStatement}.
	 * @param ctx the parse tree
	 */
	void exitRaiseStatement(PlSqlParser.RaiseStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#gotoStatement}.
	 * @param ctx the parse tree
	 */
	void enterGotoStatement(PlSqlParser.GotoStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#gotoStatement}.
	 * @param ctx the parse tree
	 */
	void exitGotoStatement(PlSqlParser.GotoStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#dbmsOutputStatement}.
	 * @param ctx the parse tree
	 */
	void enterDbmsOutputStatement(PlSqlParser.DbmsOutputStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#dbmsOutputStatement}.
	 * @param ctx the parse tree
	 */
	void exitDbmsOutputStatement(PlSqlParser.DbmsOutputStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#assignStatement}.
	 * @param ctx the parse tree
	 */
	void enterAssignStatement(PlSqlParser.AssignStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#assignStatement}.
	 * @param ctx the parse tree
	 */
	void exitAssignStatement(PlSqlParser.AssignStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#returnStatement}.
	 * @param ctx the parse tree
	 */
	void enterReturnStatement(PlSqlParser.ReturnStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#returnStatement}.
	 * @param ctx the parse tree
	 */
	void exitReturnStatement(PlSqlParser.ReturnStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#callStatement}.
	 * @param ctx the parse tree
	 */
	void enterCallStatement(PlSqlParser.CallStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#callStatement}.
	 * @param ctx the parse tree
	 */
	void exitCallStatement(PlSqlParser.CallStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#nullStatement}.
	 * @param ctx the parse tree
	 */
	void enterNullStatement(PlSqlParser.NullStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#nullStatement}.
	 * @param ctx the parse tree
	 */
	void exitNullStatement(PlSqlParser.NullStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#selectStmt}.
	 * @param ctx the parse tree
	 */
	void enterSelectStmt(PlSqlParser.SelectStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#selectStmt}.
	 * @param ctx the parse tree
	 */
	void exitSelectStmt(PlSqlParser.SelectStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#condition}.
	 * @param ctx the parse tree
	 */
	void enterCondition(PlSqlParser.ConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#condition}.
	 * @param ctx the parse tree
	 */
	void exitCondition(PlSqlParser.ConditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(PlSqlParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(PlSqlParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#exprList}.
	 * @param ctx the parse tree
	 */
	void enterExprList(PlSqlParser.ExprListContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#exprList}.
	 * @param ctx the parse tree
	 */
	void exitExprList(PlSqlParser.ExprListContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlSqlParser#idList}.
	 * @param ctx the parse tree
	 */
	void enterIdList(PlSqlParser.IdListContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlSqlParser#idList}.
	 * @param ctx the parse tree
	 */
	void exitIdList(PlSqlParser.IdListContext ctx);
}