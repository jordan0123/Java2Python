import java.util.ArrayList;
import java.io.IOException;
import java.util.Arrays;

public class Parser {
	private LexScanner lexer;
	private static int depth = 0;
    private ArrayList<JavaToken> buffer;
    private JavaToken curTok;
    
    Parser()
    {
        this.buffer = new ArrayList<JavaToken>();
    }
	
	void setLexer(LexScanner l)
	{
		this.lexer = l;
		
	}
	
    String nextToken() throws IOException, Exception
    {
        if (buffer.size() > 0)
        {
            this.curTok = buffer.remove(0);
        }
        else
        {
            lexer.nextToken();
            this.curTok = lexer.getJavaToken();
        }
        return this.curTok.tokenName();
    }
    
    
	String nextNonSpace() throws Exception
	{
        
		nextToken();
		
		while (curTok.tokenCode() == 3009)
		{
			nextToken();
		}
		
		return curTok.tokenName();
	}
    // get JavaToken n spots ahead (adds to buffer that will be removed from over pulling from Lexer)
    JavaToken lookAhead(int n) throws Exception
    {
        while(n > buffer.size()){
            nextNonSpace();
            buffer.add(curTok);
        }
        return buffer.get(buffer.size() - 1);
    }
    // gets nextToken and checks to see if it matches expecting token, throws error otherwise
    void expect(String expToken, boolean next) throws Exception
    {
        if(next)
        {
            nextNonSpace();
        }
        if(curTok.tokenName != expToken)
        {
            errorMsg(expToken, curTok.getLine(), curTok.getPos());
        }
    }
    
    void errorMsg(String expToken, int line, int pos)
    {
        System.out.println("Error (line " + line + " position " + pos + ") Expecting " + expToken);
        System.exit(0);
    }
        

    /* TODO: Currently set up to parse a single block { } of java statements
     * Will eventually go to typeDeclarations() when <block> works and assume <package declaration> and <import declarations> are unnecessary
     *
     *<compilation unit> ::= <package declaration>? <import declarations>? <type declarations>?
     */    
	ASTNode parse() throws Exception
	{
		System.out.println("**BEGIN PARSE**");
        ASTNode program = new ASTNode("program", null);
        // TODO: Replace with call to typeDeclarations() when ready
		expect("open_bracket_lt", true);
        program.addChild(block());
        System.out.println("**FINISHED PARSE**");
        return program;
	}
	
	/*
	 * <block> ::= { <block statements>? }
	 */
	ASTNode block() throws Exception
	{
		enterNT("block");
        ASTNode block = new ASTNode("block", null);
        String s = nextNonSpace();
        // if not } then contains block statements
        if(s != "close_bracket_lt"){
            block.addChild(blockStatements());
        } 
        // TODO: Check for closed bracket. Not sure on ordering of things if I should expect next token or check current token. Printing for now.
        System.out.println("The current token which should be } is " + curTok.tokenName());
		exitNT("block");
        return block;
	}
	
	/*
	 * <block statements> ::= <block statement> | <block statements> <block statement>
	 */
	ASTNode blockStatements() throws Exception
	{
		enterNT("blockStatements");
        ASTNode blockStmnts = new ASTNode("block statements", null);
        while(curTok.tokenCode() != 3004)
        {
            // error msg if reach EOF while parsing
            if(curTok.tokenCode() == 4001)
            {
                errorMsg("}",curTok.getLine(), curTok.getPos());    
            }
            blockStmnts.addChild(blockStatement());
        }
        exitNT("blockStatements");
		return blockStmnts;
	}
	
	/*
	 * <block statement> ::= <local variable declaration statement> | <statement>
	 */
	ASTNode blockStatement() throws Exception
	{
		enterNT("blockStatement");
        ASTNode blockStmnt = new ASTNode("block statement", null);
        //check if local variable declaration by checking if current token is a type else it's a statement
        if(isType())
        {
            blockStmnt.addChild(localVariableDeclarationStatement());
        }
        else
        {
            System.out.println("This is a statement. Not implemented yet.");
            //blockStmnt.addChild(statement());
        }
        
        exitNT("blockStatement");
		return blockStmnt;
	}
	
	// <local variable declaration statement> ::= <local variable declaration> ;
	ASTNode localVariableDeclarationStatement() throws Exception
	{
		enterNT("localVariableDeclarationStatement");
		ASTNode localVarDecStmnt = new ASTNode("local variable declaration statement",null); 
        localVarDecStmnt.addChild(localVariableDeclaration());
        // TODO: Check for ;. Not sure on ordering of things if I should expect next token or check current token. Printing for now.
        System.out.println("The current token which should be ; is " + curTok.tokenName());
		exitNT("localVariableDeclarationStatement");
        return localVarDecStmnt;
	}
	
	// <local variable declaration> ::= <type> <variable declarators>
	ASTNode localVariableDeclaration() throws Exception
	{
		enterNT("localVariableDeclaration");
		ASTNode localVarDec = new ASTNode("local variable declaration", null);
        // check first element is type then add
		if(!isType()) 
		{
			errorMsg("type", curTok.getLine(), curTok.getPos());
        }
        localVarDec.addChild(type());
        nextNonSpace();
        localVarDec.addChild(variableDeclarators());
		exitNT("localVariableDeclaration");
        return localVarDec;
	}
	
	// returns if current token is a type i.e. int, short, double keywords in java
    // TODO: Handle <reference type>
	boolean isType() throws Exception
	{
		switch (curTok.tokenCode())
		{
            case 1011: // boolean_kw
            case 1017: // double_kw
            case 1033: // int_kw
            case 1034: // short_kw
            case 1036: // char_kw
            case 1043: // long_kw
            case 1047: // float_kw
                return true;
		  default:  
            return false;
		}
	}
	// returns a node of <primitive type> or <array type>
    // TODO: Handle <reference type>
    ASTNode type() throws Exception
    {   
        ASTNode retVal;
        JavaToken nextTok = lookAhead(1);
        if(nextTok.tokenCode() == 2003) // open [
        {
            String typeLit = curTok.getLiteral(); // saves type info
            nextNonSpace(); // advances to the [
            expect("]", true); // advances again checking for ]
            retVal = new ASTNode("array type", typeLit);
        }
        else{
            retVal = new ASTNode("primitive type", curTok.getLiteral());
        }
        return retVal;
    }
    
	/*
	 * <variable declarators> ::= <variable declarator> 
	 * 							| <variable declarators> , <variable declarator>
	 */
	ASTNode variableDeclarators() throws Exception
	{
		enterNT("variableDeclarators");
        ASTNode varDecs = new ASTNode("variable declarators", null);
        boolean moreDecs = true;
        while(moreDecs)
        {
            varDecs.addChild(variableDeclarator());
            JavaToken nextTok = lookAhead(1);
            if(nextTok.tokenCode() == 3007)
            {
                // increment ahead of ,
                nextNonSpace();
                nextNonSpace();
                moreDecs = true;
            }
            else
            {
                moreDecs = false;    
            }
        }
        // while , is found after variableDeclarator() call
        while(curTok.tokenCode() != 3007); 
        exitNT("variableDeclarators");
        return varDecs;
	}
	
	/*
	 * <variable declarator> ::= <variable declarator id> 
	 * 						   | <variable declarator id> = <variable initializer>
	 */
	ASTNode variableDeclarator() throws Exception
	{
		enterNT("variableDeclarator");
		ASTNode varDec = new ASTNode("variable declarator", null);
        // check if identifier else throw error
        expect("identifier", false);
        varDec.addChild(variableDeclaratorID());
        // check if variable initialization <followed by equal_op ('=')>
        JavaToken nextTok = lookAhead(1);
        if (nextTok.tokenCode() == 2032) // = 
        {
            nextNonSpace(); //advance to =
            nextNonSpace(); //advance to next token
            varDec.addChild(variableInitializer());
        }
		exitNT("variableDeclarator");
        return varDec;
	}
	
	/*
	 * <variable declarator id> ::= <identifier> | <variable declarator id> [ ]
	 */
	ASTNode variableDeclaratorID() throws Exception
	{
		enterNT("variableDeclaratorID");
		ASTNode varDecID;
        // check if identifier else throw error
        expect("identifier", false);
        String id = curTok.getLiteral();
        // check if array identifier looking for '['
        JavaToken nextTok = lookAhead(1);
		if(nextTok.tokenCode() == 2003 )
        {   
            nextNonSpace(); //advance to [
            expect("]", true); // advances again checking for ]
            varDecID = new ASTNode("array identifier", id);    
        }
        else
        {
            varDecID = new ASTNode("identifier", id);
        }
		exitNT("variableDeclaratorID");
		return varDecID;
	}
	
	/*
	 * <variable initializer> ::= <expression> | <array initializer>
	 */
	ASTNode variableInitializer()
	{
        ASTNode varInit = new ASTNode("variable initializer", null);
        //check for array initializer start symbol '{'
        if(curTok.tokenCode() == 3003)
        {
            varInit.addChild(arrayInitializer());
        }
        else
        {
            varInit.addChild(expression());
        }
        return varInit;
	}
	
	/*
	 * <expression> ::= <assignment expression>
	 */
	ASTNode expression()
	{
        ASTNode exp = new ASTNode("expression", null);
        exp.addChild(assignmentExpression());
        return exp;
	}
	
	/*
	 * <assignment expression> ::= <conditional expression> | <assignment>
	 */
	ASTNode assignmentExpression()
	{
		//look ahead to see if next token is an <assignment operator>
        //TODO: Assumes <left hand side> is one token. Won't work for field access or array access now but could extend to handle by ignoring [ and ] in lookahead
        ASTNode assExp = new ASTNode("assignment expression", null);
        JavaToken nextTok = lookAhead(1);
        String[] assOps = {"=", "*=", "/=", "%=", "+=", "-=", "<<=", ">>="};
        if(Arrays.asList(assOps).contains(nextTok.tokenName()))
        {
            assExp.addChild(assignment());
        }
        else
        {
            assExp.addChild(conditionalExpression());
        }
        return assExp;
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
