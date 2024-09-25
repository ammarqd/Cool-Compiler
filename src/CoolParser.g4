/**
 * Define a grammar for Cool
 */
parser grammar CoolParser;

options { tokenVocab = CoolLexer; }


/*  Starting point for parsing a Cool file  */

program 
	: (coolClass SEMICOLON)+ EOF
	;

coolClass :
	CLASS TYPEID (INHERITS TYPEID)? CURLY_OPEN CURLY_CLOSE;
