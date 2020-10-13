import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

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
        //System.out.println("Current token " + curTok.tokenName());
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
        //printBuffer();
        while(n > buffer.size()){
            tok = nextPeekToken();
            buffer.add(tok);
        }
        //printBuffer();
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
    
    // optionally gets nextToken and checks to see if it matches expecting token, throws error otherwise
    void expect(String expToken, boolean next) throws Exception
    {
        if(next)
        {
            nextNonSpace();
        }
        if(curTok.tokenName() != expToken)
        {
            errorMsg(expToken, curTok.getLine(), curTok.getPos());
        }
    }
    
    // gives multiple options for expected token
    // returns tokenName if one is found
    // else returns "" 
    // next: determines if the next Token should be removed before checking
    // raiseError: determines whether an error is raised if no matches are made
    String expectOr(boolean next, boolean raiseError, String ... expTokens) throws Exception
    {
        String foundTok = "";
        if(next){
            nextNonSpace();
        }
        for(String tok : expTokens){
            if(curTok.tokenName() == tok){
                foundTok = tok;
                break;
            }
        }
        if (foundTok.length() < 1 && raiseError){
            List<String> exp = Arrays.asList(expTokens);
            System.out.println("Token "+ curTok.tokenName() + " does not match any expected tokens " + exp.toString());
            System.exit(0);
        }
        return foundTok;
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
                stmnt.addChild(expressionStatement());
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
    // stores list of valid assignment Operators
    // TODO: Generalize to provide various valid lists by key example "assignmentExpressions" as param would retrieve assOps list
    String[] getAssignmentOps()
    {
        String[] assOps = {"equals_op", "*=", "/=", "%=", "+=", "-=", "<<=", ">>="};
        return assOps;
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
     * <expression statement> ::= <statement expression> ;
     */
    ASTNode expressionStatement() throws Exception
    {
        enterNT("expressionStatement");
        ASTNode expStmnt = new ASTNode("expression statement", null);
        expStmnt.addChild(statementExpression());
        expect("semi_colon_lt", true);
        nextNonSpace(); //advance past ';'
        exitNT("expressionStatement");
        return expStmnt;
    }
    
	/*
     * <statement expression> ::= <assignment> | 
     *           <preincrement expression> | <postincrement expression>
     *            <predecrement expression> | <postdecrement expression> | 
     *           <method invocation> | <class instance creation expression>
     *  INCOMPLETE ASSUMES ASSIGNMENT
     */ 
    ASTNode statementExpression() throws Exception
    {
        enterNT("statementExpression");
        ASTNode stmntExp = new ASTNode("statement expression", null);
        stmntExp.addChild(assignment());
        exitNT("statementExpression");
        return stmntExp;
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
        String[] assOps = getAssignmentOps();
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
	 * <conditional expression> ::= <conditiongital or expression> | 
	 * 								<conditional or expression> ? <expression> : <conditional expression>
	 */
	// Hacking this so it works for a single literal. Will fix to handle simplistic expressions before expanding to more complex expressions once we've handled statements and contstructs like for and while loops
	ASTNode conditionalExpression() throws Exception
	{
        enterNT("conditionalExpression");
		ASTNode cndExpr = new ASTNode("conditional expression", null);
		expectOr(false, true, "identifier", "string_lt", "decimal_lt", "integer_lt");
        //DEBUGSystem.out.println("The current token in conditional expression is " + curTok.tokenName());
        JavaToken nextTok = lookAhead(1);
        switch(nextTok.tokenName()){
            case "+_op":
            case "-_op":
                additiveExpression();
                break;
            case "*_op":
            case "/-op":
            case "%_op":
                multiplicativeExpression();
                break;
            case ">_op":
            case "<_op":
            case "<=_op":
            case ">=_op":
            case "instanceof":
                relationalExpression();
                break;
            case "semi_colon_lt":
            case ")_op":
                break;
            default:
                //Probably just a unary expression 
                cndExpr.addChild(new ASTNode(curTok.tokenName(), curTok.getLiteral()));
        }      
		//ASTNode cndExpr = new ASTNode(curTok.tokenName(), curTok.getLiteral());
        exitNT("conditionalExpression");
        return cndExpr;
	}
    /*
     *<additive expression> ::= <multiplicative expression> | 
     *                   <unary expression> + <multiplicative expression> 
     *                   <unary expression> - <multiplicative expression>
     * TODO: Need to be able to not add the first operand if it's part of a chain of expressions
     */
    ASTNode additiveExpression() throws Exception
    {
        enterNT("additiveExpression");
        ASTNode addExp = new ASTNode("additive expression", null);
        addExp.addChild(new ASTNode(curTok.tokenName(), curTok.getLiteral()));
        expectOr(true, true, "+_op", "-_op");
        addExp.addChild(new ASTNode(curTok.tokenName(), curTok.getLiteral()));
        nextNonSpace(); // advance past operator
        addExp.addChild(conditionalExpression());
        exitNT("additiveExpression");
        return addExp;
    }

    /*
     *<multiplicative expression> ::= <unary expression> * <conditional expression> | 
     *               <unary expression> / <conditional expression> | 
     *               <unary expression> % <conditional expression> 
     */
    ASTNode multiplicativeExpression() throws Exception
    {
        enterNT("multiplicativeExpression");
        ASTNode multiExp = new ASTNode("multiplicative expression", null);
        multiExp.addChild(new ASTNode(curTok.tokenName(), curTok.getLiteral()));
        expectOr(true, true, "*_op", "/_op", "%_op");
        multiExp.addChild(new ASTNode(curTok.tokenName(), curTok.getLiteral()));
        nextNonSpace(); // advance past operator
        multiExp.addChild(conditionalExpression());
        exitNT("multiplicativeExpression");
        return multiExp;
    }
    
    /*
     *<relational expression> ::= <unary expression> < <conditional expression> | 
     *                   <unary expression> > <conditional expression> | 
     *                   <unary expression> <= <shift expression> | 
     *                   <unary expression> >= <shift expression> | 
     *                   <unary expression> instanceof <reference type>
     */
    ASTNode relationalExpression() throws Exception
    {
        enterNT("relationalExpression");
        ASTNode multiExp = new ASTNode("relational expression", null);
        multiExp.addChild(new ASTNode(curTok.tokenName(), curTok.getLiteral()));
        expectOr(true, true, "<_op", ">_op", "<=_op", ">=_op");
        multiExp.addChild(new ASTNode(curTok.tokenName(), curTok.getLiteral()));
        nextNonSpace(); // advance past operator
        multiExp.addChild(conditionalExpression());
        exitNT("relationalExpression");
        return multiExp;
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
	ASTNode assignment() throws Exception
	{
        enterNT("assignment");
		ASTNode assnmnt = new ASTNode("assignment", null);
        assnmnt.addChild(leftHandSide());
        String[] assOps = getAssignmentOps();
        expectOr(true, true, assOps);
        assnmnt.addChild(new ASTNode(curTok.tokenName(), curTok.getLiteral()));
        nextNonSpace(); //advance past assignment exp
        assnmnt.addChild(assignmentExpression());
        exitNT("assignment");
		return assnmnt;
	}
	
    /*
     *
     *<left hand side> ::= <expression name> | <field access> | <array access>
     * INCOMPLETE ONLY HANDLES EXPRESSION NAME which it assumes to be an identifier
     */
    ASTNode leftHandSide() throws Exception
    {
        enterNT("leftHandSide");
        expect("identifier", false);
        ASTNode lhs = new ASTNode(curTok.tokenName(), curTok.getLiteral());
        exitNT("leftHandSide");
        return lhs;
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
