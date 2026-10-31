// PART 1: LEXER RULES 

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