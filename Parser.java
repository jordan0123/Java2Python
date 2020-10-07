public class Parser {
	private LexScanner lexer;
	private static int depth = 0;
	
	
	void setLexer(LexScanner l)
	{
		this.lexer = l;
		
	}
	
	String nextNonSpace() throws Exception
	{
		lexer.nextToken();
		
		while (lexer.getTokenCode() == 3009)
		{
			lexer.nextToken();
		}
		
		return lexer.getToken();
	}
	
	void parse() throws Exception
	{
		System.out.println("**BEGIN PARSE**");
		
		block();
		
		System.out.println("**FINISHED PARSE**");
	}
	
	/*
	 * <block> ::= { <block statements>? }
	 */
	void block() throws Exception
	{
		enterNT("block");
		
		String s = nextNonSpace(); // get the token
		System.out.println("next token: " + s);
		
		blockStatements(); // parse next block
		
		if (lexer.getTokenCode() != 4001) // if not EOF
		{
			block(); // recurse
		}
		
		exitNT("block");
	}
	
	/*
	 * <block statements> ::= <block statement> | <block statements> <block statement>
	 */
	void blockStatements() throws Exception
	{
		enterNT("blockStatements");
		blockStatement();
		exitNT("blockStatements");
		
	}
	
	/*
	 * <block statement> ::= <local variable declaration statement> | <statement>
	 */
	void blockStatement() throws Exception
	{
		enterNT("blockStatement");
		localVariableDeclarationStatement();
		exitNT("blockStatement");
		
	}
	
	// <local variable declaration statement> ::= <local variable declaration> ;
	void localVariableDeclarationStatement() throws Exception
	{
		enterNT("localVariableDeclarationStatement");
		localVariableDeclaration();
		exitNT("localVariableDeclarationStatement");
	}
	
	// <local variable declaration> ::= <type> <variable declarators>
	void localVariableDeclaration() throws Exception
	{
		enterNT("localVariableDeclaration");
		
		if(type()) 
		{
			variableDeclarators();
		}

		exitNT("localVariableDeclaration");
	}
	
	// incomplete
	boolean type() throws Exception
	{
		enterNT("type");
		switch (lexer.getTokenCode())
		{
		case 1033: // int_kw
			break;
		case 1034: // short_kw
			break;
		case 1036: // char_kw
			break;
		case 1043: // long_kw
			break;
		case 1047: // float_kw
			break;
		default:
			System.out.println("BACKTRACK: type() failed.");
			exitNT("type");
			return false;
		}
		// valid token type if it makes it past the switch
		System.out.println("type: " + lexer.getLexeme());	
		
		exitNT("type");
		
		String s = nextNonSpace(); // get the token
		System.out.println("next token: " + s);
		
		return true;
	}
	
	/*
	 * <variable declarators> ::= <variable declarator> 
	 * 							| <variable declarators> , <variable declarator>
	 */
	void variableDeclarators()
	{
		enterNT("variableDeclarators");
		variableDeclarator();
		exitNT("variableDeclarators");
	}
	
	/*
	 * <variable declarator> ::= <variable declarator id> 
	 * 						   | <variable declarator id> = <variable initializer>
	 */
	boolean variableDeclarator() throws Exception
	{
		enterNT("variableDeclarator");
		if(variableDeclaratorID())
		{
			if (lexer.getTokenCode() == 2032) // '='
			{
				//System.out.println("next token: " + nextNonSpace());
				variableInitializer();
			}
		}
		exitNT("variableDeclarator");
	}
	
	// Incomplete
	/*
	 * <variable declarator id> ::= <identifier> | <variable declarator id> [ ]
	 */
	boolean variableDeclaratorID() throws Exception
	{
		enterNT("variableDeclaratorID");
		
		if (lexer.getTokenCode() == 3001) // identifier
		{
			exitNT("variableDeclaratorID");
			System.out.println("next token: " + nextNonSpace());
			return true;
		}
		
		exitNT("variableDeclaratorID");
		return false;
	}
	
	/*
	 * <variable initializer> ::= <expression> | <array initializer>
	 */
	boolean variableInitializer()
	{
		expression();
	}
	
	/*
	 * <expression> ::= <assignment expression>
	 */
	boolean expression()
	{
		assignmentExpression();
	}
	
	/*
	 * <assignment expression> ::= <conditional expression> | <assignment>
	 */
	boolean assignmentExpression()
	{
		
	}
	// print out the non-terminal being entered
	void enterNT(String s)
	{
		for(int i = 0; i < depth; i++)
		{
			System.out.print(" ");
		}
		System.out.println("-> enter <" + s + ">");
		depth++;
	}
	// print out the non-terminal being exited
	void exitNT(String s)
	{
		for(int i = 0; i < depth; i++)
		{
			System.out.print(" ");
		}
		System.out.println("<- exit <" + s + ">");
		depth--;
	}
	
}
