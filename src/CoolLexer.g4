lexer grammar CoolLexer;

@members {
    int commentLevel = 0;
    int stringLength = 0;
    StringBuilder buf;
}

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
LESS_EQ_OPERATOR            : '<=';
EQ_OPERATOR                 : '=' ;
ASSIGN_OPERATOR 	        : '<-';
RIGHTARROW                  : '=>';

/* Keywords */
CLASS : [cC][lL][aA][sS][sS];
INHERITS : [iI][nN][hH][eE][rR][iI][tT][sS];
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
NOT : [nN][oO][tT];
TRUE : 't'[rR][uU][eE];
FALSE : 'f'[aA][lL][sS][eE];

/* Identifiers and Types */
TYPEID : [A-Z]IDENTIFIER*;
OBJECTID : [a-z]IDENTIFIER*;
fragment IDENTIFIER : [a-zA-Z0-9_];

/* WHITESPACE */
WHITESPACE : (' ' | '\n' | '\f' | '\r' | '\t' | '\u000B')+ -> skip;

/* Int Literals */
INT_CONST : [0-9]+;

/* String Literals */
BEGIN_STRING : '"' {
    stringLength = 0;
    buf = new StringBuilder();
    buf.append(getText());
    } -> pushMode(STRING_MODE), more;

mode STRING_MODE;

STR_TEXT : ~[\r\n"\u0000\\] { stringLength++; buf.append(getText()); } -> more;

STR_ESC:
    '\\'
    (
        'n'             { buf.append("\\n"); }
      | 't'             { buf.append("\\t"); }
      | 'b'             { buf.append("\\b"); }
      | 'f'             { buf.append("\\f"); }
      | '\\'            { buf.append("\\\\"); }
      | '"'             { buf.append("\\\""); }
      | '\\' [0-9]+     { buf.append(getText()); }
      | '\n'            { buf.append("\\n"); }
      | .               { buf.append(getText().substring(1)); } // Non-standard escapes
    )
    { stringLength++; }
    -> more;

UNTERMINATED_STRING : '\n'
{ setText("Unterminated string constant"); }
-> type(ERROR), popMode;

NULL_STRING : '\u0000' .*? ('"' | '\n')
{ setText("String contains null character."); }
-> type(ERROR), popMode;

ESC_NULL : '\\\u0000' .*? ('"' | '\n')
{ setText("String contains escaped null character."); }
-> type(ERROR), popMode;

EOF_STRING : EOF
{ setText("EOF in string constant"); }
-> type(ERROR), popMode;

STR_CONST : '"' {
    buf.append(getText());
    if (stringLength > 1024) {
        setText("String constant too long");
        setType(ERROR);
    } else {
        setText(buf.toString());
    }
}-> popMode;

mode DEFAULT_MODE;

/* Line and Block Comments */
LINE_COMMENT  : '--' ~[\r\n]* -> skip;
BEGIN_COMMENT: '(*' { commentLevel++; } -> pushMode(COMMENT_MODE), skip;

mode COMMENT_MODE;

EOF_COMMENT : . EOF
{ setText("EOF in comment"); }
-> type(ERROR);

END_COMMENT:
    '*)' {
        commentLevel--;
    } -> popMode, skip;

COMMENT_TEXT: . -> skip;

BEGIN_INNER_COMMENT:
    '(*' {
        commentLevel++;
    } -> skip, pushMode(COMMENT_MODE);

ERROR_UNCLOSED_COMMENT:
    '*)' EOF {
        commentLevel--;
        if (commentLevel > 0) {
            setText("EOF in comment");
            setType(ERROR);
        } else {
            skip();
        }
    };

mode DEFAULT_MODE;

UNMATCHED_PAREN : '*)'
{ setText("Unmatched *)"); }
-> type(ERROR);

/* Catch-all for unexpected characters */
ERROR : . ;
