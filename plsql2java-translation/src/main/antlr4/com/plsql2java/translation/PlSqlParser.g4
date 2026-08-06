// PlSqlParser.g4 — Oracle PL/SQL Parser Grammar (subset for plsql2java)
// Source: adapted from antlr/grammars-v4 plsql grammar (Apache 2.0)
// Version: pinned to antlr4-maven-plugin 4.13.1

parser grammar PlSqlParser;

options { tokenVocab = PlSqlLexer; }

// Top-level unit
compilationUnit
    : (packageSpec | packageBody | procedureDecl | functionDecl)* EOF
    ;

packageSpec
    : PACKAGE ID IS packageSpecItem* END ID? SEMI
    ;

packageBody
    : PACKAGE BODY ID IS (procedureDecl | functionDecl | variableDecl)* END ID? SEMI
    ;

procedureDecl
    : PROCEDURE ID LPAREN paramList? RPAREN (IS | AS) declareSection? block
    ;

functionDecl
    : FUNCTION ID LPAREN paramList? RPAREN RETURN dataType (IS | AS) declareSection? block
    ;

paramList
    : param (COMMA param)*
    ;

param
    : ID (IN | OUT | IN OUT)? dataType (ASSIGN expr)?
    ;

declareSection
    : (variableDecl | cursorDecl)*
    ;

variableDecl
    : ID dataType (ASSIGN expr)? SEMI                                   # varDeclSimple
    | ID dataType DEFAULT expr SEMI                                     # varDeclDefault
    ;

cursorDecl
    : CURSOR ID IS selectStmt SEMI
    ;

dataType
    : VARCHAR2 LPAREN NUMBER_LIT RPAREN
    | NUMBER_KW (LPAREN NUMBER_LIT (COMMA NUMBER_LIT)? RPAREN)?
    | DATE_KW
    | BOOLEAN_KW
    | INTEGER_KW
    | ID (DOT ID)? (PERCENT (TYPE | ROWTYPE))?
    ;

block
    : BEGIN statement* exceptionBlock? END ID? SEMI
    ;

exceptionBlock
    : EXCEPTION exceptionHandler+
    ;

exceptionHandler
    : WHEN exceptionName (OR exceptionName)* THEN statement+
    ;

exceptionName
    : OTHERS
    | ID
    ;

packageSpecItem
    : procedureSpec
    | functionSpec
    | variableDecl
    ;

procedureSpec
    : PROCEDURE ID LPAREN paramList? RPAREN SEMI
    ;

functionSpec
    : FUNCTION ID LPAREN paramList? RPAREN RETURN dataType SEMI
    ;

statement
    : ifStatement
    | caseStatement
    | loopStatement
    | whileStatement
    | forStatement
    | cursorForStatement
    | openStatement
    | fetchStatement
    | closeStatement
    | bulkCollectStatement
    | forallStatement
    | raiseStatement
    | gotoStatement
    | dbmsOutputStatement
    | assignStatement
    | returnStatement
    | callStatement
    | nullStatement
    ;

ifStatement
    : IF condition THEN statement+
      (ELSIF condition THEN statement+)*
      (ELSE statement+)?
      END IF SEMI
    ;

caseStatement
    : CASE expr? (WHEN expr THEN statement+)+ (ELSE statement+)? END CASE SEMI
    ;

loopStatement
    : LOOP statement+ END LOOP SEMI
    ;

whileStatement
    : WHILE condition LOOP statement+ END LOOP SEMI
    ;

forStatement
    : FOR ID IN expr DOTDOT expr LOOP statement+ END LOOP SEMI
    ;

cursorForStatement
    : FOR ID IN (ID | LPAREN selectStmt RPAREN) LOOP statement+ END LOOP SEMI
    ;

openStatement
    : OPEN ID SEMI
    ;

fetchStatement
    : FETCH ID INTO idList SEMI
    ;

closeStatement
    : CLOSE ID SEMI
    ;

bulkCollectStatement
    : (SELECT expr INTO BULK COLLECT INTO ID (LIMIT expr)? SEMI)
    | (FETCH ID BULK COLLECT INTO ID (LIMIT expr)? SEMI)
    ;

forallStatement
    : FORALL ID IN expr DOTDOT expr (SAVE EXCEPTIONS)? dmlStatement SEMI
    ;

dmlStatement
    : insertStmt
    | updateStmt
    | deleteStmt
    ;

insertStmt
    : INSERT INTO ID LPAREN idList RPAREN selectStmt
    | INSERT INTO ID LPAREN idList RPAREN VALUES LPAREN exprList RPAREN
    ;

updateStmt
    : UPDATE ID SET ID EQ expr (COMMA ID EQ expr)* (WHERE condition)?
    ;

deleteStmt
    : DELETE FROM ID (WHERE condition)?
    ;

raiseStatement
    : RAISE ID? SEMI
    ;

gotoStatement
    : GOTO ID SEMI
    ;

dbmsOutputStatement
    : DBMS_OUTPUT DOT PUT_LINE LPAREN expr RPAREN SEMI
    ;

assignStatement
    : ID (DOT ID)? ASSIGN expr SEMI
    ;

returnStatement
    : RETURN expr? SEMI
    ;

callStatement
    : ID (DOT ID)? LPAREN exprList? RPAREN SEMI
    ;

nullStatement
    : NULL_ SEMI
    ;

selectStmt
    : SELECT exprList FROM ID (WHERE condition)?
    ;

condition
    : expr ((EQ | NEQ | LT | GT | LE | GE) expr)?
    | condition AND condition
    | condition OR condition
    | NOT condition
    ;

expr
    : NUMBER_LIT
    | STRING_LIT
    | NULL_
    | TRUE_
    | FALSE_
    | ID (DOT ID)? (LPAREN exprList? RPAREN)?
    | expr (PLUS | MINUS | STAR | SLASH) expr
    | LPAREN expr RPAREN
    ;

exprList
    : expr (COMMA expr)*
    ;

idList
    : ID (COMMA ID)*
    ;


