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
    
    JavaToken nextPeekToken() throws IOException, Exception
    {
        lexer.nextToken();
        JavaToken retVal = lexer.getJavaToken();
        while(retVal.tokenCode() == 3009)
        {
            lexer.nextToken();
            retVal = lexer.getJavaToken();
        }
        return retVal;
    }
    
    // get JavaToken n spots ahead (adds to buffer that will be removed from over pulling from Lexer)
    JavaToken lookAhead(int n) throws Exception
    {
        JavaToken tok;
        while(n > buffer.size()){
            tok = nextPeekToken();
            buffer.add(tok);
        }
        return buffer.get(n - 1);
    }
    
    // print current buffer contents
    void printBuffer(){
        JavaToken print;
        for(int i = 0; i < buffer.size(); i++){
            print = buffer.get(i);
            System.out.println("Index: " + i + " Token: " + print.tokenName());
        }
    }
    
    // Identifies when a non-implemented function would be called during parse and exit
    // used for tagging parts of code that are not implement
    // i.e. need to call statement but it's not implemented yet
    // notImplemented("statement");
    void notImplemented(String funcName)
    {
        System.out.println("This is a " + funcName + ". Not implemented yet.");
        System.exit(0);
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
        // checks that current token is a }
        expect("close_bracket_lt", false);
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
        while(curTok.tokenCode() != 3004) // close_bracket_lt
        {
            // error msg if reach EOF while parsing
            if(curTok.tokenCode() == 4001) // EOF
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
            blockStmnt.addChild(statement());
        }
        
        exitNT("blockStatement");
		return blockStmnt;
	}
	
    ASTNode statement() throws Exception
    {
        enterNT("statement");
        ASTNode stmnt = new ASTNode("statement", null);
        switch(curTok.tokenName())
        {
            case "open_bracket_lt": // {
                stmnt.addChild(block());
                break;
            case "semi_colon_lt": // ;
                stmnt.addChild(new ASTNode("empty statement", null));
                break;
            case "if_kw":
                notImplemented("if statement");
                break;
            case "for_kw":
                notImplemented("for statement");
                break;
            case "switch_kw":
                notImplemented("switch statement");
                break;
            case "do_kw":
                notImplemented("do statement");
                break;
            case "while_kw":
                notImplemented("while statement");
                break;
            case "break_kw":
                notImplemented("break statement");
                break;
            case "continue_kw":
                notImplemented("continue statement");
                break;
            case "return_kw":
                notImplemented("return statement");
                break;
            case "throw_kw":
                // for some reason 'throw' is for <throws statement> and 'throws' is for <throws> 
                notImplemented("throws statement");
                break;
            case "synchronized_kw":
                // TODO: Check if this has a python analogue
                notImplemented("synchronized statement");
                break;
            case "try_kw":
                notImplemented("try statement");
                break;
            default:
                //with everything else weeded out. It's either an <expression statement> or a <labeled statement>. Let's worry about <labeled statement> some other time.
                notImplemented("expression statement");
        }
        return stmnt;
    }
    
    
    
	// <local variable declaration statement> ::= <local variable declaration> ;
	ASTNode localVariableDeclarationStatement() throws Exception
	{
		enterNT("localVariableDeclarationStatement");
		ASTNode localVarDecStmnt = new ASTNode("local variable declaration statement", null); 
        localVarDecStmnt.addChild(localVariableDeclaration());
        // checks that next token is ;. Assumes localVariableDeclaration ends with last token of Declaration still the curTok
        expect("semi_colon_lt", true);
        nextNonSpace(); //advance past ';'
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
        enterNT("type");
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
        exitNT("type");
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
            if(nextTok.tokenCode() == 3007) // comma_lt
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
            nextNonSpace(); // advance to =
            nextNonSpace(); // advance to next token
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
		if(nextTok.tokenCode() == 2003 ) // [
        {   
            nextNonSpace(); // advance to [
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
	ASTNode variableInitializer() throws Exception
	{
        enterNT("variableInitializer");
        ASTNode varInit = new ASTNode("variable initializer", null);
        //check for array initializer start symbol '{'
        if(curTok.tokenCode() == 3003)
        {
            System.out.println("Array initialier not supported yet!");
            //varInit.addChild(arrayInitializer());
        }
        else
        {
            varInit.addChild(expression());
        }
        exitNT("variableInitializer");
        return varInit;
	}
	
	/*
	 * <expression> ::= <assignment expression>
	 */
	ASTNode expression() throws Exception
	{
        enterNT("expression");
        ASTNode exp = new ASTNode("expression", null);
        exp.addChild(assignmentExpression());
        exitNT("expression");
        return exp;
	}
	
	/*
	 * <assignment expression> ::= <conditional expression> | <assignment>
	 */
	ASTNode assignmentExpression() throws Exception
	{
		//look ahead to see if next token is an <assignment operator>
        //TODO: Assumes <left hand side> is one token. Won't work for field access or array access now but could extend to handle by ignoring [ and ] in lookahead
        
        enterNT("assignmentExpression");
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
        exitNT("assignmentExpression");
        return assExp;
	}
	
	/*
	 * <conditional expression> ::= <conditional or expression> | 
	 * 								<conditional or expression> ? <expression> : <conditional expression>
	 */
	// Hacking this so it works for a single literal. Will fix to handle simplistic expressions before expanding to more complex expressions once we've handled statements and contstructs like for and while loops
	ASTNode conditionalExpression()
	{
        enterNT("conditionalExpression");
		//ASTNode cndExpr = new ASTNode("conditional expression", null);
		//cndExpr.addChild(conditionalOrExpression());
		ASTNode cndExpr = new ASTNode(curTok.tokenName(), curTok.getLiteral());
        exitNT("conditionalExpression");
        return cndExpr;
	}
	
//	/*
//	 * <conditional or expression> ::= <conditional and expression> | 
//	 * 									<conditional or expression> || <conditional and expression>
//	 */
//	ASTNode conditionalOrExpression()
//	{
//
//	}
	
	/*
	 * <assignment> ::= <left hand side> <assignment operator> <assignment expression>
	 */
	// INCOMPLETE
	ASTNode assignment() throws Exception
	{
		ASTNode assnmnt = new ASTNode("assignment", null);
		//JavaToken nextTok = lookAhead(1);
		
		return assnmnt;
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
