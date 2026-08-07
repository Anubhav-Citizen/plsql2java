// Generated from com/plsql2java/translation/PlSqlParser.g4 by ANTLR 4.13.1
package com.plsql2java.translation;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link PlSqlParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface PlSqlParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#compilationUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompilationUnit(PlSqlParser.CompilationUnitContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#createOrReplace}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateOrReplace(PlSqlParser.CreateOrReplaceContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#packageSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPackageSpec(PlSqlParser.PackageSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#typeDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeDecl(PlSqlParser.TypeDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#packageBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPackageBody(PlSqlParser.PackageBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#procedureDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProcedureDecl(PlSqlParser.ProcedureDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#functionDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionDecl(PlSqlParser.FunctionDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#triggerDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerDecl(PlSqlParser.TriggerDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#triggerEvent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerEvent(PlSqlParser.TriggerEventContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#anonymousBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnonymousBlock(PlSqlParser.AnonymousBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#paramList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParamList(PlSqlParser.ParamListContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#param}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam(PlSqlParser.ParamContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#declareSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclareSection(PlSqlParser.DeclareSectionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code varDeclSimple}
	 * labeled alternative in {@link PlSqlParser#variableDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarDeclSimple(PlSqlParser.VarDeclSimpleContext ctx);
	/**
	 * Visit a parse tree produced by the {@code varDeclDefault}
	 * labeled alternative in {@link PlSqlParser#variableDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarDeclDefault(PlSqlParser.VarDeclDefaultContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#cursorDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCursorDecl(PlSqlParser.CursorDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataType(PlSqlParser.DataTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock(PlSqlParser.BlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#exceptionBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExceptionBlock(PlSqlParser.ExceptionBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#exceptionHandler}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExceptionHandler(PlSqlParser.ExceptionHandlerContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#exceptionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExceptionName(PlSqlParser.ExceptionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#packageSpecItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPackageSpecItem(PlSqlParser.PackageSpecItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#procedureSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProcedureSpec(PlSqlParser.ProcedureSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#functionSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionSpec(PlSqlParser.FunctionSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(PlSqlParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#ifStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfStatement(PlSqlParser.IfStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#caseStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseStatement(PlSqlParser.CaseStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#loopStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoopStatement(PlSqlParser.LoopStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#whileStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhileStatement(PlSqlParser.WhileStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#forStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForStatement(PlSqlParser.ForStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#cursorForStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCursorForStatement(PlSqlParser.CursorForStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#openStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpenStatement(PlSqlParser.OpenStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#fetchStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFetchStatement(PlSqlParser.FetchStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#closeStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCloseStatement(PlSqlParser.CloseStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#bulkCollectStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBulkCollectStatement(PlSqlParser.BulkCollectStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#forallStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForallStatement(PlSqlParser.ForallStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#dmlStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDmlStatement(PlSqlParser.DmlStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#selectIntoStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectIntoStatement(PlSqlParser.SelectIntoStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#insertStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertStatement(PlSqlParser.InsertStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#updateStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdateStatement(PlSqlParser.UpdateStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#deleteStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeleteStatement(PlSqlParser.DeleteStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#insertStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertStmt(PlSqlParser.InsertStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#updateStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdateStmt(PlSqlParser.UpdateStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#deleteStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeleteStmt(PlSqlParser.DeleteStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#raiseStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRaiseStatement(PlSqlParser.RaiseStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#gotoStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGotoStatement(PlSqlParser.GotoStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#dbmsOutputStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDbmsOutputStatement(PlSqlParser.DbmsOutputStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#assignStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignStatement(PlSqlParser.AssignStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#returnStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnStatement(PlSqlParser.ReturnStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#callStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCallStatement(PlSqlParser.CallStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#nullStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullStatement(PlSqlParser.NullStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#selectStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectStmt(PlSqlParser.SelectStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#selectExprList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectExprList(PlSqlParser.SelectExprListContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCondition(PlSqlParser.ConditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(PlSqlParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#exprList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprList(PlSqlParser.ExprListContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#idList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdList(PlSqlParser.IdListContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlSqlParser#typedIdList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypedIdList(PlSqlParser.TypedIdListContext ctx);
}