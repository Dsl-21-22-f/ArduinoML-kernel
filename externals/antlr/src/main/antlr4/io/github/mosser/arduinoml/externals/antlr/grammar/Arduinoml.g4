grammar Arduinoml;


/******************
 ** Parser rules **
 ******************/

root            :   declaration bricks states EOF;

declaration     :   'application' name=IDENTIFIER;

bricks          :   (sensor|actuator)+;
    sensor      :   'sensor'   location ;
    actuator    :   'actuator' location ;
    location    :   id=IDENTIFIER ':' port=PORT_NUMBER;

states          :   state+;
    state       :   initial? name=IDENTIFIER '{'  action+ transition '}';
    action      :   receiver=IDENTIFIER '<=' value=SIGNAL;
    transition  :   expression=expr '=>' next=IDENTIFIER;
    initial     :   '->';

expr            :   (unaryexpr|binaryexpr);
    unaryexpr   :   condition;
    binaryexpr  :   expr1=unaryexpr operator=OPERATOR expr2=unaryexpr;
condition       :   (timecondition|sensorcondition);
    timecondition:  'after' trigger=NUMBER 'ms';
    sensorcondition: trigger=IDENTIFIER 'is' value=(BUTTONSTATE|SIGNAL);

/*****************
 ** Lexer rules **
 *****************/
PORT_NUMBER     :   [1-9] | '11' | '12';
NUMBER          :   [0-9]+;
IDENTIFIER      :   LOWERCASE (LOWERCASE|UPPERCASE)+;
BUTTONSTATE     :   'PUSHED';
SIGNAL          :   'HIGH' | 'LOW';
OPERATOR        :     'OR' | 'AND';
/*************
 ** Helpers **
 *************/

fragment LOWERCASE  : [a-z];                                 // abstract rule, does not really exists
fragment UPPERCASE  : [A-Z];
NEWLINE             : ('\r'? '\n' | '\r')+      -> skip;
WS                  : ((' ' | '\t')+)           -> skip;     // who cares about whitespaces?
COMMENT             : '#' ~( '\r' | '\n' )*     -> skip;     // Single line comments, starting with a #
