#include "lexer.h"
#include <iostream>

// create lexer and open the program file
Lexer::Lexer()
{
	char_index = 0;
}

// gets the next character from the file and determines the type
void Lexer::getChar()
{
	next_char = EOF;

	if (next_char != 0)
	{
		next_char = input[char_index++];

		if (isalpha(next_char))
		{
			char_type = LETTER;
		}
		else if (isdigit(next_char))
		{
			char_type = DIGIT;
		}
		else
		{
			char_type = OTHER;
		}
	}
	else
	{
		char_type = END;
	}
}

// gets the next non-space character
void Lexer::getNonSpace()
{
	getChar();

	while (isspace(next_char))
	{
		getChar();
	}
}

// adds next_char to lexeme
void Lexer::addChar()
{
	if (lex_len < 99)
	{
		lexeme[lex_len++] = next_char;
		lexeme[lex_len] = 0; // null terminator
	}
	else
	{
		std::cout << "Error: lexeme too long" << std::endl;
		exit(1);
	}
}

// returns a token given a character
TOKEN Lexer::lookup_char(char c)
{
	switch (c)
	{
	case '=':
		addChar();
		next_token = ASSIGN_OP;
		break;

	case '+':
		addChar();
		next_token = ADD_OP;
		break;

	case '-':
		addChar();
		next_token = SUB_OP;
		break;

	case '<':
		addChar();
		next_token = LESS_OP;
		break;

	case '>':
		addChar();
		next_token = GREATER_OP;
		break;

	default:
		addChar();
		next_token = EOF_TOKEN;
	}

	return next_token;
}

// returns a token given a string
TOKEN Lexer::lookup_string(std::string str)
{
	if (str == "IF")
	{
		return KW_IF;
	}
	if (str == "THEN")
	{
		return KW_THEN;
	}
	if (str == "DIM")
	{
		return KW_DIM;
	}
	if (str == "AS")
	{
		return KW_AS;
	}
	if (str == "LET")
	{
		return KW_LET;
	}
	if (str == "END")
	{
		return KW_END;
	}
	if (str == "GOTO")
	{
		return KW_GOTO;
	}
	if (str == "PRINT")
	{
		return KW_PRINT;
	}

	if (str == "INTEGER")
	{
		return TYPE_INTEGER;
	}

	return IDENT;
}

// returns the next token from the source file
void Lexer::lex()
{
	lex_len = 0;
	getNonSpace();

	switch (char_type)
	{
		// either an identifier, type, or keyword
	case LETTER:
		addChar();
		getChar();

		while (char_type == LETTER || char_type == DIGIT)
		{
			addChar();
			getChar();
		}

		next_token = lookup_string(lexeme);
		break;

		// literal integer
	case DIGIT:
		addChar();
		getChar();

		while (char_type == DIGIT)
		{
			addChar();
			getChar();
		}

		next_token = LIT_INT;
		break;

		// operators and such
	case OTHER:
		lookup_char(next_char);
		getChar();
		break;

	case END:
		next_token = EOF_TOKEN;

		lexeme[0] = 'E';
		lexeme[1] = 'O';
		lexeme[2] = 'F';
		lexeme[3] = 0;
		break;
	}

	//std::cout << "Next token: " << tokenToString(next_token) << " (lexeme: " << lexeme << ")" << std::endl;
	//return next_token;
}

std::string tokenToString(TOKEN t)
{
	switch (t)
	{
	case EOF_TOKEN:
		return "EOF_TOKEN";
	case LIT_INT:
		return "LIT_INT";
	case IDENT:
		return "IDENT";
	case ASSIGN_OP:
		return "ASSIGN_OP";
	case ADD_OP:
		return "ADD_OP";
	case SUB_OP:
		return "SUB_OP";
	case LESS_OP:
		return "LESS_OP";
	case GREATER_OP:
		return "GREATER_OP";
	case KW_IF:
		return "KW_IF";
	case KW_THEN:
		return "KW_THEN";
	case KW_DIM:
		return "KW_DIM";
	case KW_AS:
		return "KW_AS";
	case KW_LET:
		return "KW_LET";
	case KW_END:
		return "KW_END";
	case KW_GOTO:
		return "KW_GOTO";
	case KW_PRINT:
		return "KW_PRINT";
	case TYPE_INTEGER:
		return "TYPE_INTEGER";
	}
}
