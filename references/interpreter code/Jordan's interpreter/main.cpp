/*
* Name: Jordan Hasty
* Course: CS4308
* Section: W01
* Assignment: Project Deliverable 3
* Date: 5/6/20
*/

#include <iostream>
#include <fstream>
#include <string>
#include <map>
#include <stack>
#include "lexer.h"

// exits the program loop when set to true
bool quit = false;

std::ifstream in_file; // source file to read from
Lexer lexer;

std::map<std::string, int> int_symbols; // symbol table
std::stack<int> int_stack; // stack to store integers

int line_num; // line number
char cur_line[500]; // current line string

// interprets a statement
void statement();

// executes the code in cur_line
void executeLine();

// prints error and exits program
void error(const char* str)
{
	std::cout << "ERROR: " << str << std::endl;
	exit(1);
}

// interprets data type keywords
// <Type>			->INTEGER
void type()
{
	if (lexer.getNextToken() == TYPE_INTEGER)
	{
		lexer.lex();
	}
	else
	{
		error("expected type specifier");
	}
}

// interprets numbers
/*
<Number>		-><Identifier>
				| INT_LIT
*/
void number()
{
	if (lexer.getNextToken() == IDENT || lexer.getNextToken() == LIT_INT)
	{

		if (lexer.getNextToken() == IDENT)
		{
			// check if valid identifier
			bool exists = int_symbols.count(lexer.getNextLexeme());
			if (!exists) error("unknown identifier");

			// push number onto stack
			int_stack.push(int_symbols[lexer.getNextLexeme()]);
		}

		if (lexer.getNextToken() == LIT_INT)
		{
			// push number onto stack
			int_stack.push(std::stoi(lexer.getNextLexeme()));
		}

		lexer.lex();
	}
	else
	{
		error("expected number");
	}

}

// interprets term
/*
<Term>			-><Number> '+' <Term>
				| <Number> '-' <Term>
				| <Number>
*/
void term()
{

	number(); // pushes the next number onto the stack

	// check if an operation is performed on the number
	if (lexer.getNextToken() == ADD_OP || lexer.getNextToken() == SUB_OP)
	{
		TOKEN op = lexer.getNextToken();

		lexer.lex();
		term(); // recurse

		// read two operands from stack
		int b = int_stack.top(); int_stack.pop();
		int a = int_stack.top(); int_stack.pop();

		// perform operation and push result onto stack
		if (op == ADD_OP)
			int_stack.push(a + b);
		
		if (op == SUB_OP)
			int_stack.push(a - b);
	}

}

// interprets expressions
/*
<Expression> 	-><Term> '<' <Expression>
				| <Term> '>' <Expression>
				| <Term>
*/
void expression()
{
	term(); // push next term onto the stack

	// check if an operation is performed
	if (lexer.getNextToken() == LESS_OP || lexer.getNextToken() == GREATER_OP)
	{
		TOKEN op = lexer.getNextToken();

		lexer.lex();
		expression(); // recurse

		// read two operands from stack
		int b = int_stack.top(); int_stack.pop();
		int a = int_stack.top(); int_stack.pop();

		// perform operation and push result onto stack
		if (op == LESS_OP)
			int_stack.push(a < b);

		if (op == GREATER_OP)
			int_stack.push(a > b);
	}

}

// interprets dim statements which declare variables
// <DimStatement>	->DIM <Identifier> AS <Type>
void dimStmnt()
{

	if (lexer.getNextToken() == IDENT)
	{ 
		// get the name of the identifier
		std::string ident_str = lexer.getNextLexeme();

		lexer.lex();

		if (lexer.getNextToken() == KW_AS)
		{
			lexer.lex();
			type();

			// insert the identifier into the symbol table with a default value of 0
			int_symbols.insert({ident_str, 0 });
		}
		else
		{
			error("expected AS keyword");
		}

	}
	else
	{
		error("expected identifier");
	}

}

// interprets if statements
// <IfStatement>	->IF <Expression> THEN <Statement>
void ifStmnt()
{
	// evaluate the condition
	expression(); // pushes the expression result onto the stack

	bool eval = int_stack.top(); int_stack.pop();

	if (eval == 0)
		return;

	if (lexer.getNextToken() == KW_THEN)
	{
		lexer.lex();
		statement(); // interpret statement
	}
	else
	{
		error("expected THEN keyword");
	}

}

// interprets let statements which define variables
// <LetStatement>	->LET <Identifier> '=' <Expression>
void letStmnt()
{

	if (lexer.getNextToken() == IDENT)
	{
		// get the name of the identifier
		std::string ident = lexer.getNextLexeme();
		// check if its valid
		bool exists = int_symbols.count(ident);

		if (!exists)
		{
			error("unknown identifier");
		}

		lexer.lex();

		if (lexer.getNextToken() == ASSIGN_OP)
		{
			lexer.lex();
			expression(); // evaluate expression and push onto the stack
			 
			// update the symbol with the new value from the stack
			int_symbols[ident] = int_stack.top(); int_stack.pop();
		}
		else
		{
			error("expected assignment operator '='");
		}
	}
	else
	{
		error("expected identifier");
	}
}

// interprets goto statements by going to the line and executing it when called
// <GotoStatement>	->GOTO <Expression>
void gotoStmnt()
{
	// evaluate the expression and push onto stack
	expression();

	// get the line number from the stack
	line_num = int_stack.top(); int_stack.pop();

	in_file.seekg(0); // go back to beginning

	int line = -1;

	// search file for the line number
	while (line != line_num)
	{
		in_file.getline(cur_line, 500);
		lexer.input = cur_line;
		lexer.char_index = 0;
		lexer.lex();

		line = std::stoi(lexer.getNextLexeme());
	}

	// execute the line when found
	executeLine();

}

// interprets print statements by printing the expression to the console
// <PrintStatement>->PRINT <Expression>
void printStmnt()
{
	expression(); // evaluate expression and push onto stack
	std::cout << int_stack.top() << std::endl; // print the result
	int_stack.pop();
}

// interprets end statement which terminates the interpreted program
// <EndStatement>	->END
void endStmnt()
{
	lexer.lex();
	std::cout << "END" << std::endl;
	quit = true;
}


// interprets a statement
/*
<Statement>		-><IfStatement>
				| <DimStatement>
				| <LetStatement>
				| <EndStatement>
				| <GotoStatement>
				| <PrintStatement>
*/
void statement()
{

	// determine type of statement
	switch (lexer.getNextToken())
	{
	case KW_IF:
		lexer.lex();
		ifStmnt();
		break;

	case KW_DIM:
		lexer.lex();
		dimStmnt();
		break;

	case KW_LET:
		lexer.lex();
		letStmnt();
		break;

	case KW_END:
		lexer.lex();
		endStmnt();
		break;

	case KW_GOTO:
		lexer.lex();
		gotoStmnt();
		break;

	case KW_PRINT:
		lexer.lex();
		printStmnt();
		break;

	default:
		error("expected statement");
		break;
	}
}


// executes the current line
// <Line>			->INT_LIT <Statement>
void executeLine()
{
	// line must begin with integer literal
	if (lexer.getNextToken() == LIT_INT)
	{
		line_num = std::stoi(lexer.getNextLexeme());
		lexer.lex();
		statement(); // interpret statement
	}
	else
	{
		error("expected line to begin with integer literal");
	}
}


int main()
{
    in_file.open("myprogram.bas"); // open the source file for reading
	lexer = Lexer();

	// main program loop
	while (!quit)
	{
		in_file.getline(cur_line, 500); // reads next line into current line from source file
		lexer.input = cur_line; // sets the lexer input to the current line
		lexer.char_index = 0; // sets the lexer's current char index to the beginning
		lexer.lex(); // perform lexical analysis on the first lexeme
		executeLine(); // execute the line
	}

	return 0;
}
