/**
 * Define lexer rules for Cool
 */
lexer grammar CoolLexer;

/* Punctuation */
PERIOD              : '.';
COMMA               : ',';
AT                  : '@';
SEMICOLON           : ';';
COLON               : ':';
CURLY_OPEN          : '{' ;
CURLY_CLOSE         : '}' ;
PARENT_OPEN         : '(' ;
PARENT_CLOSE        : ')' ;

/* Operators */
PLUS_OPERATOR       : '+';
MINUS_OPERATOR      : '-';
MULT_OPERATOR       : '*';
DIV_OPERATOR        : '/';
INT_COMPLEMENT_OPERATOR     : '~';
LESS_OPERATOR               : '<';
GREAT_OPERATOR              : '>';
LESS_EQ_OPERATOR            : '<=';
EQ_OPERATOR                 : '=' ;
ASSIGN_OPERATOR 	        : '<-';
RIGHTARROW                  : '=>';
NOT                         : 'not';

/* Keywords */
CLASS : [cC][lL][aA][sS][sS];
INHERITS : [iI][nN][hH][eE][rR][iI][tT][sS];
MAIN : [mM][aA][iI][nN];
IF : [iI][fF];
THEN : [tT][hH][eE][nN];
ELSE : [eE][lL][sS][eE];
FI : [fF][iI];
WHILE : [wW][hH][iI][lL][eE];
LOOP : [lL][oO][oO][pP];
POOL : [pP][oO][oO][lL];
LET : [lL][eE][tT];
IN : [iI][nN];
CASE : [cC][aA][sS][eE];
OF : [oO][fF];
ESAC : [eE][sS][aA][cC];
NEW : [nN][eE][wW];
ISVOID : [iI][sS][vV][oO][iI][dD];
TRUE : [t][rR][uU][eE];
FALSE : [f][aA][lL][sS][eE];

/* Identifiers and Types */
TYPEID : [A-Z]CHAR*;
OBJECTID : [a-z]CHAR*;
fragment CHAR : [a-zA-Z0-9_];

/* Int Literals */
INT_CONST : DIGIT+;
fragment DIGIT : [0-9];

/* String Literals */
BEGIN_STRING : '"' -> pushMode(STRING_MODE), more;
mode STRING_MODE;
STR_TEXT : (~[\r\n"\u0000\\])+ -> more;
STR_ESC : ('\\' [bftnr"\\\r\n])+ -> more;
NULL_STRING : '\u0000'
{ setText("String contains null character."); }
-> type(ERROR);
UNTERMINATED_STRING : '\n'
{ setText("Unterminated string constant"); }
-> type(ERROR), popMode;

EOF_STRING : EOF
{ setText("EOF in string constant"); }
-> type(ERROR);

ESC_NULL : '\\\u0000'
{ setText("String contains escaped null character."); }
-> type(ERROR);

STR_CONST : '"' -> popMode;

mode DEFAULT_MODE;

/* Whitespace and Comments */

LINE_COMMENT  : '--' ~[\r\n]* -> skip;
BEGIN_COMMENT: '(*' -> skip, pushMode(COMMENT_MODE);
mode COMMENT_MODE;
END_COMMENT: '*)' -> skip, popMode;
COMMENT_TEXT: . -> skip;
BEGIN_INNER_COMMENT: '(*' -> skip, pushMode(COMMENT_MODE);

EOF_COMMENT : '\n'?EOF
{ setText("EOF in comment"); }
-> type(ERROR);

mode DEFAULT_MODE;

UNMATCHED_PAREN : '*)'
{ setText("Unmatched *)"); }
-> type(ERROR);

WHITESPACE : (' ' | '\n' | '\r' | '\t' | '\u000B' | '\f' )+ -> skip;

/* Catch-all for unexpected characters */
ERROR : . ;
