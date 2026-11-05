// PART 1: LEXER RULES 

// Reserved words
IF:         : 'if' ;
ELSE        : 'else; ;
WHILE       : 'while' ;
FUNCTIONq   : 'function' ;
VAR         : 'var' ;
PRINT       : 'print' ;

// Literals
INT         : '0' | [1-9] [0-9]* ;
BOOL        : 'true' | 'false' ;
NULL        : 'null' ;

// Symbols
ADD         : '+' ;
SUB         : '-' ;
MUL         : '*' ;
DIV         : '/' ;
MOD         : '%' ;
GT          : '>' ;
LT          : '<' ;
GE          : '>=' ;
LE          : '<=' ;
EQ          : '==' ;
ASSIGN      : '=' ;
SEMI        : ';' ;

// Identifiers
ID : [a-zA-Z_] [a-za-Z0-9_]*

// Whitespace and comments
NEWLINE       : '\r'? '\n' -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;
LINE_COMMENT  : '//' ~[\r\n]* -> skip ;
WS            : [ \t]+ -> skip ;

// PART 2: PARSER RULES
/** starting rules **/
program: statement+ ;

statement:  expr SEMI        #ExpressionStatement
    | ifStatement           #IfStatement
    | whileStatement        #WhileStatement
    | printStatement        #PrintStatement
    | SEMI             #EmptyStatement
    ;

ifStatement : IF '(' expr ')' statement
              (ELSE statement) ?
            ;

whileStatement : WHILE '(' expr ')' statement;

printStatement : PRINT expr SEMI;

// expressions
expr: expr op=( MUL | DIV | MOD) expr #MulDivMod
    | expr op=( ADD | SUB ) expr       #AddSub
    | expr op=( LT | LE | GT | GE | EQ) expr #Comparison
    | FUNCTION '(' (ID (',' ID)*)? ')' '{' expr '}' #Function
    | ID '(' (expr (',' expr)*)? ')'                #FunctionApplication
    | VAR ID ASSIGN expr                            #VariableDeclaration
    | ID                                            #VariableReference
    | ID ASSIGN expr                                #Assignment
    | INT                                           #IntegerLiteral
    | BOOL                                          #BoolLiteral
    | NULL                                          #NullLiteral
    | '(' expr ')'                                  #ParenthesizedExpression
    ;