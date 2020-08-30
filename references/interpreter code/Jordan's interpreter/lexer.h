#pragma once

#include <fstream>
#include <string>

enum CHAR_TYPE
{
	LETTER,
	DIGIT,
	END,
	OTHER
};

enum TOKEN
{
	EOF_TOKEN, // represents EOF but isn't -1

	LIT_INT,

	IDENT,

	ASSIGN_OP,
	ADD_OP,
	SUB_OP,
	LESS_OP,
	GREATER_OP,

	KW_IF,
	KW_THEN,
	KW_DIM,
	KW_AS,
	KW_LET,
	KW_END,
	KW_GOTO,
	KW_PRINT,

	TYPE_INTEGER

};

class Lexer
{
	CHAR_TYPE char_type;
	char lexeme[100];
	char next_char;
	int lex_len;
	TOKEN next_token;

	void getChar();
	void getNonSpace();
	void addChar();
	TOKEN lookup_char(char c);
	TOKEN lookup_string(std::string str);

public:

	Lexer();

	char* input;
	int char_index;

	TOKEN getNextToken() { return next_token; }
	char* getNextLexeme() { return lexeme; }

	void lex();
};

// returns a string describing the token
std::string tokenToString(TOKEN t);
