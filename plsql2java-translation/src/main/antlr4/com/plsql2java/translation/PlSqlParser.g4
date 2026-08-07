// PlSqlParser.g4 — Oracle PL/SQL Parser Grammar (subset for plsql2java)
// Source: adapted from antlr/grammars-v4 plsql grammar (Apache 2.0)
// Version: pinned to antlr4-maven-plugin 4.13.1

parser grammar PlSqlParser;

options { tokenVocab = PlSqlLexer; }

// Top-level unit
compilationUnit
    : (createOrReplace? (packageSpec | packageBody | procedureDecl | functionDecl) | triggerDecl | anonymousBlock)* EOF
    ;

createOrReplace
    : CREATE (OR REPLACE)?
    ;

packageSpec
    : PACKAGE ID (IS | AS) packageSpecItem* END ID? SEMI
    ;

typeDecl
    : TYPE ID IS (RECORD LPAREN typedIdList RPAREN | TABLE OF dataType (INDEX BY dataType)?) SEMI
    ;

packageBody
    : PACKAGE BODY ID (IS | AS) (procedureDecl | functionDecl | variableDecl)* END ID? SEMI
    ;

procedureDecl
    : PROCEDURE ID LPAREN paramList RPAREN (IS | AS) declareSection? block
    | PROCEDURE ID (IS | AS) declareSection? block
    ;

functionDecl
    : FUNCTION ID LPAREN paramList RPAREN RETURN dataType (IS | AS) declareSection? block
    | FUNCTION ID RETURN dataType (IS | AS) declareSection? block
    ;

triggerDecl
    : TRIGGER ID triggerEvent? (IS | AS)? declareSection? block
    ;

triggerEvent
    : ~(BEGIN | IS | AS | DECLARE)+
    ;

anonymousBlock
    : DECLARE? declareSection? block
    ;

paramList
    : param (COMMA param)*
    ;

param
    : ID (IN | OUT | IN OUT)? dataType (ASSIGN expr)?
    | ID (IN | OUT | IN OUT)? SYS_REFCURSOR
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
    : VARCHAR2 (LPAREN NUMBER_LIT RPAREN)?
    | NUMBER_KW (LPAREN NUMBER_LIT (COMMA NUMBER_LIT)? RPAREN)?
    | DATE_KW
    | BOOLEAN_KW
    | INTEGER_KW
    | SYS_REFCURSOR
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
    | typeDecl
    | variableDecl
    ;

procedureSpec
    : PROCEDURE ID LPAREN paramList RPAREN SEMI
    | PROCEDURE ID SEMI
    ;

functionSpec
    : FUNCTION ID LPAREN paramList RPAREN RETURN dataType SEMI
    | FUNCTION ID RETURN dataType SEMI
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
    | selectIntoStatement
    | insertStatement
    | updateStatement
    | deleteStatement
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
    : OPEN ID FOR selectStmt SEMI
    | OPEN ID SEMI
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

selectIntoStatement
    : SELECT selectExprList INTO idList FROM ID (WHERE condition)? SEMI
    ;

insertStatement
    : INSERT INTO ID (LPAREN idList RPAREN)? VALUES LPAREN exprList RPAREN SEMI
    ;

updateStatement
    : UPDATE ID SET ID EQ expr (COMMA ID EQ expr)* (WHERE condition)? SEMI
    ;

deleteStatement
    : DELETE FROM ID (WHERE condition)? SEMI
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
    : (COLON (NEW | OLD | ID) DOT)? ID (DOT ID)? ASSIGN expr SEMI
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
    : SELECT selectExprList FROM ID (WHERE condition)?
    ;

selectExprList
    : STAR
    | exprList
    ;

condition
    : expr IS NOT? NULL_
    | expr ((EQ | NEQ | LT | GT | LE | GE) expr)
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
    | COLON (NEW | OLD | ID) DOT ID
    | ID (DOT ID)? (LPAREN (exprList | STAR)? RPAREN)?
    | expr (PLUS | MINUS | STAR | SLASH | CONCAT) expr
    | LPAREN expr RPAREN
    ;

exprList
    : expr (COMMA expr)*
    ;

idList
    : ID (COMMA ID)*
    ;

typedIdList
    : ID dataType (COMMA ID dataType)*
    ;


