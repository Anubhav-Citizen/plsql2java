// PlSqlLexer.g4 — Oracle PL/SQL Lexer Grammar (subset for plsql2java)
// Source: adapted from antlr/grammars-v4 plsql grammar (Apache 2.0)
// Version: pinned to antlr4-maven-plugin 4.13.1

lexer grammar PlSqlLexer;

// Keywords
IF          : [Ii][Ff];
ELSIF       : [Ee][Ll][Ss][Ii][Ff];
ELSE        : [Ee][Ll][Ss][Ee];
END         : [Ee][Nn][Dd];
THEN        : [Tt][Hh][Ee][Nn];
CASE        : [Cc][Aa][Ss][Ee];
WHEN        : [Ww][Hh][Ee][Nn];
LOOP        : [Ll][Oo][Oo][Pp];
WHILE       : [Ww][Hh][Ii][Ll][Ee];
FOR         : [Ff][Oo][Rr];
IN          : [Ii][Nn];
EXIT        : [Ee][Xx][Ii][Tt];
EXCEPTION   : [Ee][Xx][Cc][Ee][Pp][Tt][Ii][Oo][Nn];
RAISE       : [Rr][Aa][Ii][Ss][Ee];
GOTO        : [Gg][Oo][Tt][Oo];
CURSOR      : [Cc][Uu][Rr][Ss][Oo][Rr];
OPEN        : [Oo][Pp][Ee][Nn];
FETCH       : [Ff][Ee][Tt][Cc][Hh];
CLOSE       : [Cc][Ll][Oo][Ss][Ee];
INTO        : [Ii][Nn][Tt][Oo];
BULK        : [Bb][Uu][Ll][Kk];
COLLECT     : [Cc][Oo][Ll][Ll][Ee][Cc][Tt];
FORALL      : [Ff][Oo][Rr][Aa][Ll][Ll];
DECLARE     : [Dd][Ee][Cc][Ll][Aa][Rr][Ee];
BEGIN       : [Bb][Ee][Gg][Ii][Nn];
PROCEDURE   : [Pp][Rr][Oo][Cc][Ee][Dd][Uu][Rr][Ee];
FUNCTION    : [Ff][Uu][Nn][Cc][Tt][Ii][Oo][Nn];
PACKAGE     : [Pp][Aa][Cc][Kk][Aa][Gg][Ee];
RETURN      : [Rr][Ee][Tt][Uu][Rr][Nn];
IS          : [Ii][Ss];
AS          : [Aa][Ss];
CREATE      : [Cc][Rr][Ee][Aa][Tt][Ee];
OR          : [Oo][Rr];
REPLACE     : [Rr][Ee][Pp][Ll][Aa][Cc][Ee];
TRIGGER     : [Tt][Rr][Ii][Gg][Gg][Ee][Rr];
BEFORE      : [Bb][Ee][Ff][Oo][Rr][Ee];
AFTER       : [Aa][Ff][Tt][Ee][Rr];
EACH        : [Ee][Aa][Cc][Hh];
ROW         : [Rr][Oo][Ww];
NEW         : [Nn][Ee][Ww];
OLD         : [Oo][Ll][Dd];
NULL_       : [Nn][Uu][Ll][Ll];
TRUE_       : [Tt][Rr][Uu][Ee];
FALSE_      : [Ff][Aa][Ll][Ss][Ee];
AND         : [Aa][Nn][Dd];
NOT         : [Nn][Oo][Tt];
OTHERS      : [Oo][Tt][Hh][Ee][Rr][Ss];
LIMIT       : [Ll][Ii][Mm][Ii][Tt];
SAVE        : [Ss][Aa][Vv][Ee];
EXCEPTIONS  : [Ee][Xx][Cc][Ee][Pp][Tt][Ii][Oo][Nn][Ss];
SELECT      : [Ss][Ee][Ll][Ee][Cc][Tt];
INSERT      : [Ii][Nn][Ss][Ee][Rr][Tt];
UPDATE      : [Uu][Pp][Dd][Aa][Tt][Ee];
DELETE      : [Dd][Ee][Ll][Ee][Tt][Ee];
TYPE        : [Tt][Yy][Pp][Ee];
ROWTYPE     : [Rr][Oo][Ww][Tt][Yy][Pp][Ee];
BODY        : [Bb][Oo][Dd][Yy];
DEFAULT     : [Dd][Ee][Ff][Aa][Uu][Ll][Tt];
OUT         : [Oo][Uu][Tt];
VALUES      : [Vv][Aa][Ll][Uu][Ee][Ss];
FROM        : [Ff][Rr][Oo][Mm];
WHERE       : [Ww][Hh][Ee][Rr][Ee];
SET         : [Ss][Ee][Tt];
RECORD      : [Rr][Ee][Cc][Oo][Rr][Dd];
INDEX       : [Ii][Nn][Dd][Ee][Xx];
TABLE       : [Tt][Aa][Bb][Ll][Ee];
OF          : [Oo][Ff];
BY          : [Bb][Yy];
SYS_REFCURSOR : [Ss][Yy][Ss][_][Rr][Ee][Ff][Cc][Uu][Rr][Ss][Oo][Rr];
DBMS_OUTPUT : [Dd][Bb][Mm][Ss][_][Oo][Uu][Tt][Pp][Uu][Tt];
PUT_LINE    : [Pp][Uu][Tt][_][Ll][Ii][Nn][Ee];
VARCHAR2    : [Vv][Aa][Rr][Cc][Hh][Aa][Rr]'2';
NUMBER_KW   : [Nn][Uu][Mm][Bb][Ee][Rr];
DATE_KW     : [Dd][Aa][Tt][Ee];
BOOLEAN_KW  : [Bb][Oo][Oo][Ll][Ee][Aa][Nn];
INTEGER_KW  : [Ii][Nn][Tt][Ee][Gg][Ee][Rr];

// Identifiers and literals
ID          : [A-Za-z_][A-Za-z0-9_$#]*;
NUMBER_LIT  : [0-9]+ ('.' [0-9]+)?;
STRING_LIT  : '\'' (~'\'' | '\'\'')*  '\'';

// Operators and punctuation
DOT         : '.';
COMMA       : ',';
SEMI        : ';';
COLON       : ':';
ASSIGN      : ':=';
LPAREN      : '(';
RPAREN      : ')';
EQ          : '=';
NEQ         : '<>' | '!=';
LT          : '<';
GT          : '>';
LE          : '<=';
GE          : '>=';
PLUS        : '+';
MINUS       : '-';
STAR        : '*';
SLASH       : '/';
CONCAT      : '||';
PERCENT     : '%';
AT          : '@';
DOTDOT      : '..';

// Whitespace and comments
WS          : [ \t\r\n]+ -> skip;
LINE_COMMENT: '--' ~[\r\n]* -> skip;
BLOCK_COMMENT: '/*' .*? '*/' -> skip;
