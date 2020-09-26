public class Parser {
	private LexScanner lexer;
	
	void setLexer(LexScanner l)
	{
		this.lexer = l;
	}
	
	void parse() throws Exception
	{
		System.out.println("**BEGIN PARSE**");
		
		lexer.nextToken(); // get the first token
		
		System.out.println("**FINISHED PARSE**");
	}
	
	/* 
	<Blocks>		-><Block><Blocks>
					| <Block>
	*/
	void blocks()
	{
		enterNT("blocks");
		
		block(); // parse next block
		
		if (lexer.getTokenCode() != 4001) // if not EOF
		{
			blocks(); // recurse
		}
		
		exitNT("blocks");
	}
	
	/*
	 * <block> ::= { <block statements>? }
	 */
	void block()
	{
		enterNT("block");
		blockStatements();
		exitNT("block");
	}
	
	/*
	 * <block statements> ::= <block statement> | <block statements> <block statement>
	 */
	void blockStatements()
	{
		enterNT("blockStatements");
		blockStatement();
		exitNT("blockStatements");
		
	}
	
	/*
	 * <block statement> ::= <local variable declaration statement> | <statement>
	 */
	void blockStatement()
	{
		enterNT("blockStatement");
		//
		exitNT("blockStatement");
		
	}
	
	// print out the non-terminal being entered
	void enterNT(String s)
	{
		System.out.println("-> enter <" + s + ">");
	}
	// print out the non-terminal being exited
	void exitNT(String s)
	{
		System.out.println("<- exit <" + s + ">");
	}
	
}
