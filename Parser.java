import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class Parser {
	private LexScanner lexer;
	private static int depth = 0;
    private ArrayList<JavaToken> buffer;
    private JavaToken curTok;
    private JavaToken lastTok;
    private Map<String,String[]> modifiers;

    private boolean debug = true;
    private boolean printTree = true;
    private String errorMsg = null;
    private ArrayList<Comment> comments;
    private ArrayList<String> references;
    
    Parser()
    {
        this.buffer = new ArrayList<JavaToken>();
        this.comments = new ArrayList<Comment>();
        this.references = new ArrayList<String>();
        initModifiers();
    }
	
	void setLexer(LexScanner l)
	{
		this.lexer = l;
		
	}
	
    String getErrorMsg()
    {
        return this.errorMsg;
    }
    
    ArrayList<Comment> getComments()
    {
        return comments;
    }
    
    String nextToken() throws IOException, Exception
    {
        if (buffer.size() > 0)
        {
            if(this.curTok != null){
                this.lastTok = this.curTok;
            }
            this.curTok = buffer.remove(0);
        }
        else
        {
            lexer.nextToken();
            if(this.curTok != null && curTok.tokenCode() != 3009 && curTok.tokenCode() != 3014)
            {
                // if not a space store current token as last token before updating
                this.lastTok = this.curTok;
            }
            this.curTok = lexer.getJavaToken();
        }
        return this.curTok.tokenName();
    }
    
    
	String nextNonSpace() throws Exception
	{   
		nextToken();
		while (curTok.tokenCode() == 3009 || curTok.tokenCode() == 3014)
		{
            if(curTok.tokenCode() == 3014)
            {
                comments.add(new Comment(curTok.getLiteral(), curTok.getLine()));
            }
            nextToken();
		}
        if (debug) System.out.println("Current token " + curTok.tokenName() + " token line " + curTok.getLine());
		return curTok.tokenName();
	}
    
    JavaToken nextPeekToken() throws IOException, Exception
    {
        lexer.nextToken();
        JavaToken retVal = lexer.getJavaToken();
        while(retVal.tokenCode() == 3009 || curTok.tokenCode() == 3014)
        {
            if(retVal.tokenCode() == 3014)
            {
                comments.add(new Comment(retVal.getLiteral(), retVal.getLine()));
                lexer.nextToken();
                retVal = lexer.getJavaToken();
            }
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
            // copy by value to avoid multiple occur of one reference with dupe values
            tok = nextPeekToken().getCopy();
            buffer.add(tok);
        }
        //printBuffer();
        return buffer.get(n - 1);
    }
    
    // print current buffer contents
    void printBuffer(){
        if (debug) {
            JavaToken print;
            for(int i = 0; i < buffer.size(); i++){
                print = buffer.get(i);
                System.out.print("Index: " + i + " Literal: " + print.getLiteral() + " Line: " + print.getLine());
            }
            System.out.println("");
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
    
    String lookAheadToFind(ArrayList<String> find) throws Exception
    {
        enterNT("lookAheadToFind");
        int open_par = 0; // open parenthesis encountered without matching close
        int open_bra = 0; // open brackets encountered without matching close
        boolean cont = true;
        int n = 0; //lookahead value
        // if parse just started need to initialize curTok
        if(this.curTok == null){
            nextNonSpace(); 
        }
        JavaToken tok = curTok;
        String fToken = "";
        while(cont)
        {
            if(find.contains(tok.tokenName()))
            {
                if(open_par == 0 && open_bra == 0)
                {
                    fToken = tok.tokenName();
                    cont = false;
                }
            }
            if(tok.tokenName() == "EOF")
            {
                fToken = tok.tokenName();
                cont = false;
            }
            switch(tok.tokenName())
            {
                case "(_op":
                    open_par++;
                    break;
                case "[":
                    open_bra++;
                    break;
                case ")_op":
                    open_par--;
                    break;
                case "]":
                    open_bra--;
                    break;
                case "EOF":
                    fToken = tok.tokenName();
                    cont = false;
                    break;
            }
            n++; //advance lookahead index
            tok = lookAhead(n);
        }
        exitNT("lookAheadToFind");
        return fToken;
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
    
    void errorMsg(String expToken, int line, int pos) throws Exception
    {
        errorMsg = "Error (line " + line + " position " + pos + ") Expecting " + expToken +" Current Token " + curTok.tokenName() + " Literal " + curTok.getLiteral();
        throw new Exception("Syntax error");
    }
    //
    void customErrorMsg(String msg, int line, int pos) throws Exception
    {
        errorMsg = "Error (line " + line + " position " + pos + ")" + msg;
        throw new Exception("Syntax error");
    }

    boolean isLiteral(String token)
    {
        ArrayList<String> lits = new ArrayList<String>(Arrays.asList("string_lt", "integer_lt","decimal_lt", "null_lt"));
        return lits.contains(token);
    }
    
    /* Handles both a full java program and as well as a code block without class
     * Goes to typeDeclarations() when class_kw is found before ; else uses <block>
     *<compilation unit> ::= <package declaration>? <import declarations>? <type declarations>?
     */
	ASTNode parse() throws Exception
	{
		if (debug) System.out.println("**BEGIN PARSE**");
        ASTNode program = new ASTNode("program",null, 1);
        try{
            ArrayList<String> find = new ArrayList<String>();
            find.add("EOF");
            find.add("semi_colon_lt");
            find.add("class_kw");
            String fToken = lookAheadToFind(find);
            if(fToken == "class_kw")
            {
                program.addChild(typeDeclarations());
            }else{
                program.addChild(blockStatements());
            }
            if (debug) {
                System.out.println("**FINISHED PARSE**");
                System.out.println("Current token: " + curTok.tokenName() + "Current line" + curTok.getLine());
            }
            if (printTree) printTree(program);
        }catch(Exception e)
        {
            if(errorMsg == null){
                errorMsg = "A system error has occured";
                if(debug) e.printStackTrace();
            }
        }
        return program;
	}
	
	/*
	 * <block> ::= { <block statements>? }
	 */
	ASTNode block() throws Exception
	{
		enterNT("block");
        ASTNode block = new ASTNode("block",null, curTok.getLine());
        String s = nextNonSpace();
        // if not } then contains block statements
        if(s != "close_bracket_lt"){
            block.addChild(blockStatements());
        } 
        // checks that current token is a }
        expect("close_bracket_lt", false);
        nextNonSpace(); //advance past the close bracket
		exitNT("block");
        return block;
	}
	
	/*
	 * <block statements> ::= <block statement> | <block statements> <block statement>
	 */
	ASTNode blockStatements() throws Exception
	{
		enterNT("blockStatements");
        ASTNode blockStmnts = new ASTNode("block statements",null, curTok.getLine());
//        //to handle Java code without {}. Python doesn't need class info so we don't need to require it
//        if(this.curTok == null){
//            nextNonSpace();
//        }
        while(curTok.tokenCode() != 3004 && curTok.tokenCode() != 4001 && curTok.tokenCode != 1026 && curTok.tokenCode != 1007) // close_bracket_lt, 4001 = EOF 1026 = case_kw, 1007 = default_kw
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
        ASTNode blockStmnt = new ASTNode("block statement",null, curTok.getLine());
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
        ASTNode stmnt = new ASTNode("statement",null, curTok.getLine());
        switch(curTok.tokenName())
        {
            case "open_bracket_lt": // {
                stmnt.addChild(block());
                break;
            case "semi_colon_lt": // ;
                stmnt.addChild(new ASTNode("empty statement",null, curTok.getLine()));
                break;
            case "if_kw":
                stmnt.addChild(ifStatement());
                break;
            case "for_kw":
                stmnt.addChild(forStatement());
                break;
            case "switch_kw":
            	stmnt.addChild(switchStatement());
                break;
            case "do_kw":
                stmnt.addChild(doStatement());
                break;
            case "while_kw":
                stmnt.addChild(whileStatement());
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
        exitNT("statement");
        return stmnt;
    }
    
	// <local variable declaration statement> ::= <local variable declaration> ;
	ASTNode localVariableDeclarationStatement() throws Exception
	{
		enterNT("localVariableDeclarationStatement");
		ASTNode localVarDecStmnt = new ASTNode("local variable declaration statement",null, curTok.getLine()); 
        localVarDecStmnt.addChild(localVariableDeclaration());
        expect("semi_colon_lt", false);
        nextNonSpace(); //advance past ';'
		exitNT("localVariableDeclarationStatement");
        return localVarDecStmnt;
	}
	
	// <local variable declaration> ::= <type> <variable declarators>
	ASTNode localVariableDeclaration() throws Exception
	{
		enterNT("localVariableDeclaration");
		ASTNode localVarDec = new ASTNode("local variable declaration",null, curTok.getLine());
        // check first element is type then add
		if(!isType()) 
		{
			errorMsg("type", curTok.getLine(), curTok.getPos());
        }
        localVarDec.addChild(type());
        localVarDec.addChild(variableDeclarators());
		exitNT("localVariableDeclaration");
        return localVarDec;
	}
	
	// returns if current token is a type i.e. int, short, double keywords in java
	boolean isType() throws Exception
	{
        if(references.contains(curTok.getLiteral())){
            return true;
        }else{
            switch (curTok.tokenCode())
            {
                case 1011: // boolean_kw
                case 1017: // double_kw
                case 1033: // int_kw
                case 1034: // short_kw
                case 1036: // char_kw
                case 1043: // long_kw
                case 1047: // float_kw
                case 1051: // string_kw
                    return true;
              default:
                return false;
            }
        }
	}
    // stores list of valid assignment Operators
    // TODO: Generalize to provide various valid lists by key example "assignmentExpressions" as param would retrieve assOps list
    static String[] getAssignmentOps()
    {
        String[] assOps = {"equals_op", "*=_op", "/=_op", "%=_op", "+=_op", "-=_op", "<<=_op", ">>=_op"};
        return assOps;
    }
    
	// returns a node of <primitive type> or <reference type> or <primative type array><reference type array>
    ASTNode type() throws Exception
    {
        enterNT("type");
        if(!isType() && !references.contains(curTok.getLiteral())) 
		{
			errorMsg("type", curTok.getLine(), curTok.getPos());
        }
        ASTNode retVal = null;
        String typeLit = curTok.getLiteral(); // saves type info
        String ASTName = ""; // to save name of the node
        if(references.contains(typeLit)){
            ASTName = "reference type";
        }else{
            ASTName = "primative type";
        }
        nextNonSpace(); //advance past type
        if(curTok.tokenCode() == 2003) // open [
        {
            expect("]", true); // advances again checking for ]
            ASTName = ASTName + " array";
            retVal = new ASTNode(ASTName,typeLit, curTok.getLine());
            nextNonSpace(); //advance past ]
        }
        else{
            retVal = new ASTNode(ASTName,typeLit, curTok.getLine());
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
        ASTNode varDecs = new ASTNode("variable declarators",null, curTok.getLine());
        boolean moreDecs = true;
        while(moreDecs)
        {
            varDecs.addChild(variableDeclarator());
            if(curTok.tokenCode() == 3007) // comma_lt
            {
                nextNonSpace(); // increment ahead of ,
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
		ASTNode varDec = new ASTNode("variable declarator",null, curTok.getLine());
        // check if identifier else throw error
        expect("identifier", false);
        varDec.addChild(variableDeclaratorID());
        // check if variable initialization <followed by equal_op ('=')>
        if (curTok.tokenCode() == 2032) // = 
        {
            varDec.addChild(new ASTNode(curTok.tokenName(),curTok.getLiteral(), curTok.getLine()));
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
        nextNonSpace(); // advance past id
        // check if array identifier looking for '['
		if(curTok.tokenCode() == 2003 ) // [
        {
            expect("]", true); // advances again checking for ]
            varDecID = new ASTNode("array identifier",id, curTok.getLine());
            nextNonSpace(); //advance past ]
        }
        else
        {
            varDecID = new ASTNode("identifier",id, curTok.getLine());
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
        ASTNode varInit = new ASTNode("variable initializer",null, curTok.getLine());
        //check for array initializer start symbol '{'
        if(curTok.tokenCode() == 3003)
        {
            System.out.println(curTok.getLiteral());
            System.out.println(curTok.tokenCode());
            System.out.println("Array initialier not supported yet!");
            varInit.addChild(arrayInitializer());
        }
        else
        {
            varInit.addChild(expression());
        }
        exitNT("variableInitializer");
        return varInit;
	}
    
    ASTNode arrayInitializer() throws Exception
    {
        enterNT("arrayInitializer");
        ASTNode arrInit = new ASTNode("array initializer",null, curTok.getLine());
        expect("open_bracket_lt", false);
        nextNonSpace(); //advance past {
        boolean moreElems = true;
        if(curTok.tokenCode() == 3004)
        {
            moreElems = false; // no elems to add to array
        }
        while(moreElems)
        {
            arrInit.addChild(conditionalExpression("close_bracket_lt"));
            if(curTok.tokenCode() == 3007) // comma_lt
            {
                nextNonSpace(); // increment ahead of ,
            }
            else
            {
                moreElems = false;
            }
        }
        expect("close_bracket_lt", false);
        nextNonSpace(); // advance past }
        exitNT("arrayInitializer");
        return arrInit;
    }
    
    /*
     * <expression statement> ::= <statement expression> ;
     */
    ASTNode expressionStatement() throws Exception
    {
        enterNT("expressionStatement");
        ASTNode expStmnt = new ASTNode("expression statement",null, curTok.getLine());
        expStmnt.addChild(statementExpression());
        expect("semi_colon_lt", false);
        nextNonSpace(); //advance past ';'
        exitNT("expressionStatement");
        return expStmnt;
    }
    
	/*
     * <statement expression> ::= <assignment> | 
     *           <preincrement expression> | <postincrement expression>
     *            <predecrement expression> | <postdecrement expression> | 
     *           <method invocation> | <class instance creation expression>
     *
     */
    ASTNode statementExpression() throws Exception
    {
        enterNT("statementExpression");
        ASTNode stmntExp = new ASTNode("statement expression",null, curTok.getLine());
        
        switch(curTok.tokenName())
        {
            case "--_op":
                stmntExp.addChild(prefixExpression("--_op"));
                break;
            case "++_op":
                stmntExp.addChild(prefixExpression("++_op"));
                break;
            case "new_kw":
                stmntExp.addChild(classInstanceCreationExpression());
                break;
            default:
                //either assignment, post(increment|decrement), or method invocation
                //use lookahead to find assignment or -- ++ else it's a method invocation
                
                String[]assOps = getAssignmentOps();
                ArrayList<String> find = new ArrayList<String>(Arrays.asList(assOps));
                find.add("--_op");
                find.add("++_op");
                find.add("EOF");
                find.add("semi_colon_lt");
                String fToken = lookAheadToFind(find);
                if(debug) System.out.println("The fToken is " + fToken);
                if(Arrays.asList(assOps).contains(fToken))
                {
                    stmntExp.addChild(assignment());
                }else if(fToken == "--_op"){
                    stmntExp.addChild(postfixExpression("--_op"));
                }else if(fToken == "++_op"){
                    stmntExp.addChild(postfixExpression("++_op"));
                }else if(fToken == "semi_colon_lt" || fToken == "EOF")
                {
                    stmntExp.addChild(handleIdentifier());
                }
        }

        exitNT("statementExpression");
        return stmntExp;
    }
    
	/*
	 * <expression> ::= <assignment expression>
	 */
	ASTNode expression() throws Exception
	{
        enterNT("expression");
        ASTNode exp = new ASTNode("expression",null, curTok.getLine());
        exp.addChild(assignmentExpression());
        exitNT("expression");
        return exp;
	}
	
    /*
	 * <parenthesized expression> ::= (<expression>)
	 */
	ASTNode parenthesizedExpression() throws Exception
	{
        enterNT("parenthesized expression");
        ASTNode parExp = new ASTNode("parenthesized expression",null, curTok.getLine());
        nextNonSpace(); //advance past (
        parExp.addChild(expression());
        nextNonSpace(); // advance past )
        exitNT("parenthesized expression");
        return parExp;
	}
    
	/*
	 * <assignment expression> ::= <conditional expression> | <assignment>
	 */
	ASTNode assignmentExpression() throws Exception
	{
		//look ahead until it finds a token that indicates either an <assignment operator> or <conditional expression>
        
        enterNT("assignmentExpression");
        ASTNode assExp = new ASTNode("assignment expression",null, curTok.getLine());
        // these indicated assignment expression
        String[] assOps = getAssignmentOps();
        ArrayList<String> find = new ArrayList<String>(Arrays.asList(assOps));
        // these indicate conditional expression
        find.add("+_op");
        find.add("-_op");
        find.add("~_op");
        find.add("!_op");
        find.add("||_op");
        find.add("&&_op");
        find.add("^_op");
        find.add("&_op");
        find.add("==_op");
        find.add("<_op");
        find.add(">_op");
        find.add(">=_op");
        find.add("<=_op");
        find.add("instanceof_kw");
        find.add("<<_op");
        find.add(">>_op");
        find.add("*_op");
        find.add("/_op");
        find.add("%_op");
        find.add("++_op");
        find.add("--_op");
        find.add("EOF");
        find.add("semi_colon_lt");
        String fToken = lookAheadToFind(find);
        if(debug) System.out.println("The fToken is " + fToken);
        if(Arrays.asList(assOps).contains(fToken))
        {
            assExp.addChild(assignment());
        }else
        {
            assExp.addChild(conditionalExpression(null));
        }
        exitNT("assignmentExpression");
        return assExp;
	}
	
    /*
	 * <conditional expression> ::= <conditiongital or expression> | 
	 * 								<conditional or expression> ? <expression> : <conditional expression>
	 */
	ASTNode conditionalExpression(String endToken) throws Exception
	{
        enterNT("conditionalExpression");
		ASTNode cndExpr = new ASTNode("conditional expression",null, curTok.getLine());
		//expectOr(false, true, "identifier", "string_lt", "decimal_lt", "integer_lt", "(_op");
        
        //JavaToken nextTok = lookAhead(1);
        
        boolean endExp = false;
        boolean validExp = false;
        String lastPart = "";
        ASTNode lastChild;
        while(!endExp){
            if (debug) System.out.println("The current token in conditional expression is " + curTok.tokenName());
            if(cndExpr.childCount() > 0)
            {
                lastChild = cndExpr.getChildren().get(cndExpr.childCount() - 1);
            }
            else{
                lastChild = null;
            }
            if(curTok.tokenName() == endToken)
            {
                if(!validExp)
                {
                    customErrorMsg("Error: Illegal end of expression", curTok.getLine(), curTok.getPos());
                }
                endExp = true;
                break;
            }else{
                switch(curTok.tokenName()){
                    case "(_op":
                        //TODO: Check for cast before calling parenthizedExp
                        cndExpr.addChild(parenthesizedExpression());
                        validExp=true;
                        lastPart = "operand";
                        break;
                    case "+_op":
                    case "-_op":
                        if(cndExpr.childCount() > 0){
                            int retVal = binaryOrUnary(lastChild.getType());
                            if(retVal == 2)
                            {
                                cndExpr.addChild(binaryExpression());
                            }
                            else if(retVal == 1)
                            {
                                cndExpr.addChild(unaryExpression());
                            }
                        }
                        else{
                            cndExpr.addChild(unaryExpression());
                        }
                        validExp=false;
                        lastPart = "operator";
                        break;
                    case "~_op":
                    case "!_op":
                        cndExpr.addChild(unaryExpression());
                        validExp=false;
                        lastPart = "operator";
                        break;
                    case "++_op":
                    case "--_op":
                        // if(cndExpr.childCount() > 0){
                        //     cndExpr.addChild(postfixExpressionOp());
                        //     validExp=true;
                        //     lastPart = "operand";
                        // }else{
                        //     cndExpr.addChild(prefixExpressionOp());
                        //     validExp=false;
                        //     lastPart = "operator";
                        // }
                        
                        if (lookAhead(1).tokenName() == "identifier") {
                            cndExpr.addChild(prefixExpression(curTok.tokenName()));
                            validExp = true;
                            lastPart = "operand";
                        } else {
                            // operator is not paired with an identifier, which is invalid
                            validExp = false;
                            lastPart = "operator";
                        }

                        break;
                    case "*_op":
                    case "/_op":
                    case "%_op":
                    case ">_op":
                    case "<_op":
                    case "<=_op":
                    case ">=_op":
                    case "instanceof":
                    case "||_op":
                    case "&&_op":
                    case "|_op":
                    case "^_op":
                    case "&_op":
                    case "==_op":
                    case "<<_op":
                    case ">>_op":
                        cndExpr.addChild(binaryExpression());
                        validExp=false; //waiting on its second operand
                        lastPart = "operator";
                        break;
                    case "semi_colon_lt":
                    case ")_op":
                    case "colon_lt":
                    case "]":
                    case "comma_lt":
                        // end of expression
                        if(lastPart == "operator")
                        {
                            customErrorMsg("Error: Illegal end of expression", curTok.getLine(), curTok.getPos());
                        }else if(lastPart == "")
                        {
                            customErrorMsg("Error: Expecting expression", curTok.getLine(), curTok.getPos());
                        }
                        endExp = true;
                        break;
                    
                    case "identifier":
                    if (lookAhead(1).tokenName() == "++_op" || lookAhead(1).tokenName() == "--_op") {
                        cndExpr.addChild(postfixExpression(lookAhead(1).tokenName()));
                        lastPart = "operand";
                        validExp=true;
                        break;
                    }

                    default:
                        //Some primary (field access, array access, method, literal, etc)
                        // can't have two operands in a row
                        if(lastPart == "operand"){
                            //will fail
                            expect("semi_colon_lt", false);
                        }
                        cndExpr.addChild(primary());
                        validExp = true;
                        lastPart = "operand";
                }
            }
            if(endToken == "ONE"){
                endExp = true;
            }
        }
        exitNT("conditionalExpression");
        return cndExpr;
	}
    
    ASTNode primary() throws Exception
    {
        enterNT("primary");
        ASTNode prim = null;
        if(curTok.tokenName() == "new_kw")
        {
            //check if class or array creation
            ArrayList<String> find = new ArrayList<String>();
            find.add("(_op");
            find.add("[");
            find.add("EOF");
            find.add("semi_colon_lt");
            String fToken = lookAheadToFind(find);
            if(fToken == "[")
            {
                prim = arrayCreationExpression();
            }else
            {
                //class creation
                prim = primaryNoNewArray();
            }
        }
        else
        {
            prim = primaryNoNewArray();
        }
        exitNT("primary");
        return prim;
    }
    
    ASTNode primaryNoNewArray() throws Exception
    {
        enterNT("primaryNoNewArray");
        ASTNode primNoNew;
        if(isLiteral(curTok.tokenName()))
        {
            primNoNew = new ASTNode(curTok.tokenName(),curTok.getLiteral(), curTok.getLine());
            nextNonSpace(); //advance to next token
        }
        else{
            switch(curTok.tokenName()){
                case "new_kw":
                    primNoNew = classInstanceCreationExpression();
                    break;
                case "this_kw":
                case "super_kw":
                case "identifier":
                    //determine if method, field access, array access, or var
                    //keep checking for . and '[]' vs '()' vs ''\
                    primNoNew = handleIdentifier();
                    break;
                default:
                    notImplemented("The default for switch case in PrimaryNoNewArray");
                    primNoNew = new ASTNode(curTok.tokenName(),curTok.getLiteral(), curTok.getLine());
            }
        }
        exitNT("primaryNoNewArray");
        return primNoNew;
    }
    
    ASTNode unaryExpression() throws Exception
    {
        enterNT("unaryExpression");
        ASTNode unExp = new ASTNode("unary expression",null, curTok.getLine());
        unExp.addChild(new ASTNode(curTok.tokenName(),curTok.getLiteral(), curTok.getLine())); // add operator
        nextNonSpace(); //advance past operator
        exitNT("unaryExpression");
        return unExp;
    }
    
    ASTNode binaryExpression() throws Exception
    {
        enterNT("binaryExpression");
        HashMap<String, String> binOps = new HashMap<String, String>();
        binOps.put("+_op", "additive expression");
        binOps.put("-_op", "additive expression");
        binOps.put("*_op", "multiplicative expression");
        binOps.put("/_op", "multiplicative expression");
        binOps.put("%_op", "multiplicative expression");
        binOps.put(">_op", "relational expression");
        binOps.put("<_op", "relational expression");
        binOps.put("<=_op", "relational expression");
        binOps.put(">=_op", "relational expression");
        binOps.put("instanceof", "relational expression");
        binOps.put("||_op", "conditional or expression");
        binOps.put("&&_op", "conditional and expression");
        binOps.put("|_op", "inclusive or expression");
        binOps.put("^_op", "exclusive or expression");
        binOps.put("&_op", "and expression");
        binOps.put("==_op", "equality expression");
        binOps.put("<<_op", "shift expression");
        binOps.put(">>_op", "shift expression");
        String binaryType = binOps.get(curTok.tokenName());
        ASTNode binExp = new ASTNode(binaryType,null, curTok.getLine());
        binExp.addChild(new ASTNode(curTok.tokenName(),curTok.getLiteral(), curTok.getLine())); // add operator
        nextNonSpace(); //advance past operator
        exitNT("binaryExpression");
        return binExp;
    }
    // checks last node and determines if it's a valid operand for the binaryExp
    // binary exp types
    //  - "parenthesized expression"
    //  - literal
    // unary exp
    //  - "binary operators"
    int binaryOrUnary(String lastNode) throws Exception
    {
        
        int retVal = 0;
        if(isLiteral(lastNode)){
            retVal = 2;
        }else{
            switch(lastNode){
                case "parenthesized expression":
                case "identifier":
                case "prefix expression":
                case "postfix expression":
                case "postfix expression operator":
                    retVal = 2;
                    break;
                case "prefix expression operator": // case where exp after prefix has not been handled yet
                case "conditional or expression":
                case "conditional and expression":
                case "inclusive or expression":
                case "exclusive or expression":
                case "and expression":
                case "equality expression":
                case "relational expression":
                case "shift expression":
                case "additive expression":
                case "multiplicative expression":
                    retVal = 1;
                    break;
                case "unary expression":
                    customErrorMsg("Error: Illegal start of expression", curTok.getLine(), curTok.getPos());
                    break;
                default:
                    if (debug) System.out.println("Found another ASTNode in binaryOrUnary " + lastNode);
                    System.exit(0);
            }
        }
        return retVal;
    }
    
    ASTNode handleIdentifier() throws Exception
    {
        enterNT("handleIdentifier");
        ASTNode id = null;
        boolean cont = true;
//        ArrayList<String> names = new ArrayList<String>();
//        names.add(curTok.getLiteral());
        String name = curTok.getLiteral();
        boolean periodEnd = false;
        String idType = "identifier";
        while(cont)
        {
            nextNonSpace();
            switch(curTok.tokenName()){
                case "period_lt":
                    //consume and add previous to qualified name
                    name = name + ".";
                    idType="field access";
                    periodEnd = true;
                    break;
                case "(_op":
                    //method
                    idType="method";
                    cont = false;
                    break;
                case "[":
                    //array access
                    idType="array access";
                    cont = false;
                    break;
                case "identifier":
                    if(name.charAt(name.length() - 1) == '.'){
                        name = name + curTok.getLiteral();
                        idType="field access";
                        periodEnd = false;
                        break;
                    }else{
                        expect("semi_colon_lt", false);
                    }
                default:
                    if (debug) System.out.println("The default for this is " + curTok.tokenName());
                    cont=false;
            }
        }
        if(periodEnd){
            customErrorMsg("Error: Expecting identifier at", curTok.getLine(), curTok.getPos());
        }
        if (debug) System.out.println("The id type is " + idType);
        switch(idType)
        {
            case "identifier":
                id = new ASTNode("identifier",name, curTok.getLine());
                break;
            case "method":
                id = methodInvocation(name);
                break;
            case "field access":
                id = new ASTNode("field access",name, curTok.getLine());
                break;
            case "array access":
                id = arrayAccess(name);
                break;
            default:
                if (debug) System.out.println("Somethings wrong with idType ###" + idType + "###");
                System.exit(0);
        }
        exitNT("handleIdentifier");
        return id;
    }
    
    ASTNode arrayAccess(String name) throws Exception
    {
        enterNT("arrayAccess");
        ASTNode arrAcc = new ASTNode("array access",null, curTok.getLine());
        arrAcc.addChild(new ASTNode("identifier",name, curTok.getLine()));
        nextNonSpace(); // move past [
        arrAcc.addChild(expression());
        expect("]", false);
        nextNonSpace();
        exitNT("arrayAccess");
        return arrAcc;
    }
    
    ASTNode methodInvocation(String name) throws Exception
    {
        enterNT("methodInvocation");
        ASTNode methInv = new ASTNode("method invocation",null, curTok.getLine());
        methInv.addChild(new ASTNode("method name",name, curTok.getLine()));
        expect("(_op", false);
        nextNonSpace(); // move past (
        if(curTok.tokenName() == ")_op")
        {
            methInv.addChild(new ASTNode("argument list",null, curTok.getLine()));
        }else
        {
            methInv.addChild(argumentList());
        }
        expect(")_op", false);
        nextNonSpace(); // advance past )
        if(curTok.tokenName() == "period_lt"){
            // another method invocation
            expect("identifier", true);
            String name1 = curTok.getLiteral();
            nextNonSpace(); // advance past id
            methInv.addChild(methodInvocation(name1));
        }
        exitNT("methodInvocation");
        return methInv;
    }
        
    ASTNode postfixExpression(String operator) throws Exception
    {
        enterNT("postfixExpression");
        ASTNode postfix = new ASTNode("postfix expression",null, curTok.getLine());
        expect("identifier", false);
        postfix.addChild(new ASTNode("identifier",curTok.getLiteral(), curTok.getLine()));
        nextNonSpace();
        expect(operator, false);
        postfix.addChild(new ASTNode(curTok.tokenName(),curTok.getLiteral(), curTok.getLine()));
        nextNonSpace(); //move past operator
        exitNT("postfixExpression");
        return postfix;
    }
    
    //handles just the postfix operator (like unary expression)
    ASTNode postfixExpressionOp() throws Exception
    {
        enterNT("postfixExpressionOp");
        ASTNode postfixExpOp = new ASTNode("postfix expression operator",null, curTok.getLine());
        postfixExpOp.addChild(new ASTNode(curTok.tokenName(),curTok.getLiteral(), curTok.getLine())); // add operator
        nextNonSpace(); //advance past operator
        exitNT("postfixExpressionOp");
        return postfixExpOp;
    }
    
    ASTNode prefixExpression(String operator) throws Exception
    {
        enterNT("prefixExpression");
        ASTNode prefix = new ASTNode("prefix expression",null, curTok.getLine());
        expect(operator, false);
        prefix.addChild(new ASTNode(curTok.tokenName(),curTok.getLiteral(), curTok.getLine()));
        nextNonSpace(); // advance past operator
        expect("identifier", false);
        prefix.addChild(new ASTNode("identifier",curTok.getLiteral(), curTok.getLine()));
        nextNonSpace();
        exitNT("prefixExpression");
        return prefix;
    }
    //handles just the prefix operator (like unary expression)
    ASTNode prefixExpressionOp() throws Exception
    {
        enterNT("prefixExpressionOp");
        ASTNode prefixExpOp = new ASTNode("prefix expression operator",null, curTok.getLine());
        prefixExpOp.addChild(new ASTNode(curTok.tokenName(),curTok.getLiteral(), curTok.getLine())); // add operator
        nextNonSpace(); //advance past operator
        exitNT("prefixExpressionOp");
        return prefixExpOp;
    }
    
    // new <type> <dim exprs> <dims>?
    ASTNode arrayCreationExpression() throws Exception
    {
        enterNT("arrayCreationExpression");
        ASTNode arrCreate = new ASTNode("array creation expression",null, curTok.getLine());
        nextNonSpace();
        if(!isType() && !references.contains(curTok.getLiteral()))
		{
			errorMsg("type", curTok.getLine(), curTok.getPos());
        }
        arrCreate.addChild(new ASTNode("array type",curTok.getLiteral(), curTok.getLine()));
        nextNonSpace(); // advance past type
        expect("[", false);
        arrCreate.addChild(dimExprs());
        if(curTok.tokenName() == "[")
        {
            arrCreate.addChild(dims());
        }
        exitNT("arrayCreationExpression");
        return arrCreate;
    }
    
    ASTNode dimExprs() throws Exception
    {
        enterNT("dimExprs");
        ASTNode dimExps = new ASTNode("dim expressions",null, curTok.getLine());
        boolean cont = false;
        do{
            dimExps.addChild(dimExpr());
            JavaToken nextTok = lookAhead(1);
            // check for an additional dim expr (lookahead 1 to verify it's not a dim)
            if(curTok.tokenName() == "[" && nextTok.tokenName() != "]")
            {
                cont = true;
            }else
            {
                cont = false;
            }
        }while(cont);
        exitNT("dimExprs");
        return dimExps;
    }
    
    ASTNode dimExpr() throws Exception
    {
        enterNT("dimExpr");
        expect("[", false);
        nextNonSpace(); // advance past [
        ASTNode dimExp = expression();
        expect("]", false);
        nextNonSpace(); // advance past ]
        exitNT("dimExpr");
        return dimExp;
    }
    
    ASTNode dims() throws Exception
    {
        enterNT("dims");
        ASTNode dms = new ASTNode("dims",null, curTok.getLine());
        boolean cont = false;
        do{
            dms.addChild(dim());
            if(curTok.tokenName() == "[")
            {
                cont = true;
            }else
            {
                cont = false;
            }
        }while(cont);
        exitNT("dims");
        return dms;
    }
    ASTNode dim() throws Exception
    {
        enterNT("dim");
        expect("[", false);
        ASTNode dm = new ASTNode("dim", "[]", curTok.getLine());
        expect("]", true);
        nextNonSpace(); // advance past ]
        exitNT("dim");
        return dm;
    }
	
    ASTNode argumentList() throws Exception
    {
        enterNT("argumentList");
        ASTNode argList = new ASTNode("argument list",null, curTok.getLine());
        boolean cont = false;
        do{
            argList.addChild(expression());;
            // check for an additional arg
            if(curTok.tokenName() == "comma_lt")
            {
                nextNonSpace(); //advance past ,
                cont = true;
            }else
            {
                cont = false;
            }
        }while(cont);
        exitNT("argumentList");
        return argList;
    }
                        
	/*
	 * <assignment> ::= <left hand side> <assignment operator> <assignment expression>
	 */
	ASTNode assignment() throws Exception
	{
        enterNT("assignment");
		ASTNode assnmnt = new ASTNode("assignment",null, curTok.getLine());
        assnmnt.addChild(leftHandSide());
        String[] assOps = getAssignmentOps();
        expectOr(false, true, assOps);
        assnmnt.addChild(new ASTNode(curTok.tokenName(),curTok.getLiteral(), curTok.getLine()));
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
        ASTNode lhs = handleIdentifier();
        exitNT("leftHandSide");
        return lhs;
    }
    
    ASTNode switchStatement() throws Exception
    {
    	enterNT("switchStatement");
    	expect("switch_kw", false);
    	ASTNode switchStmnt = new ASTNode("switch statement",null, curTok.getLine());
    	expect("(_op", true);
    	nextNonSpace(); // move past (
    	switchStmnt.addChild(expression());
    	expect(")_op", false);
    	nextNonSpace(); // move past )
    	switchStmnt.addChild(switchBlock());
        expect("close_bracket_lt", false);
    	nextNonSpace(); //move past 
    	exitNT("switchStatement");
    	return switchStmnt;
    }
    
    ASTNode switchBlock() throws Exception
    {
    	enterNT("switchBlock");
    	expect("open_bracket_lt", false);
    	nextNonSpace(); // move past {
    	ASTNode switchBlk = new ASTNode("switch block",null, curTok.getLine());
    	
    	// check if there is one before
    	switchBlk.addChild(switchBlockStatementGroups());
        exitNT("switchBlock");
    	return switchBlk;
    }
    
    ASTNode switchBlockStatementGroups() throws Exception
    {
    	enterNT("switchBlockStatementGroups");
    	ASTNode switchBlkStmntGroups = new ASTNode("switch block statement groups",null, curTok.getLine());
    	
    	switchBlkStmntGroups.addChild(switchBlockStatementGroup());
    	// recurse if multiple statement groups
    	if (curTok.tokenCode() == 1026 || curTok.tokenCode() == 1007) switchBlockStatementGroups(); // 1026 = case_kw, 1007 = default_kw
    	
    	exitNT("switchBlockStatementGroups");
    	return switchBlkStmntGroups;
    }
    
    ASTNode switchBlockStatementGroup() throws Exception
    {
    	enterNT("switchBlockStatementGroup");
    	ASTNode switchBlkStmntGroup = new ASTNode("switch block statement group",null, curTok.getLine());
    	switchBlkStmntGroup.addChild(switchLabels());
    	switchBlkStmntGroup.addChild(blockStatements());
    	
    	exitNT("switchBlockStatementGroup");
    	
    	return switchBlkStmntGroup;
    }
    
    ASTNode switchLabels() throws Exception
    {
    	enterNT("switchLabels");
    	ASTNode switchLbls = new ASTNode("switch labels",null, curTok.getLine());
    	
    	switchLbls.addChild(switchLabel());
    	if (curTok.tokenCode() == 1026 || curTok.tokenCode() == 1007) switchLabels(); // case or default, recurse if multiple switch labels
    	
    	exitNT("switchLabels");
    	
    	return switchLbls;
    }
    
    ASTNode switchLabel() throws Exception
    {
    	enterNT("switchLabel");
    	ASTNode switchLbl = new ASTNode("switch label",null, curTok.getLine());
    	if (curTok.tokenCode() == 1026) // 'case'
    	{
	    	nextNonSpace(); // move past "case"
	    	switchLbl.addChild(expression());
    	}
    	else
    	{
    		expect("default_kw", false);
    		nextNonSpace();
    	}
    	
    	expect("colon_lt", false);
    	nextNonSpace(); // move past :
    	
    	exitNT("switchLabel");
    	
    	return switchLbl;
    }
    
    ASTNode doStatement() throws Exception
    {
        enterNT("doStatement");
        expect("do_kw", false);
        ASTNode doStmnt = new ASTNode("do statement",null, curTok.getLine());
        expect("open_bracket_lt", true);
        nextNonSpace(); // move past {
        doStmnt.addChild(statement());
        expect("close_bracket_lt", false);
        expect("while_kw", true);
        expect("(_op", true);
        nextNonSpace(); // move past (
        doStmnt.addChild(expression());
        expect(")_op", false);
        expect("semi_colon_lt", true);
        exitNT("doStatement");
        nextNonSpace();
        return doStmnt;
    }
    
    ASTNode whileStatement() throws Exception
    {
        enterNT("whileStatement");
        expect("while_kw", false);
        ASTNode whileStmnt = new ASTNode("while statement",null, curTok.getLine());
        expect("(_op", true);
        nextNonSpace(); // move past (
        whileStmnt.addChild(expression());
        expect(")_op", false);
        expect("open_bracket_lt", true);
        whileStmnt.addChild(statement());
        exitNT("whileStatement");
        return whileStmnt;
    }

    ASTNode elseStatement() throws Exception
    {
        enterNT("elseStatement");
        expect("else_kw", false);
        ASTNode elseStmnt = null;
        boolean elseFound = false;
        nextNonSpace();

        if(curTok.tokenName() == "if_kw")
        {
            elseStmnt = new ASTNode("else if statement",null, curTok.getLine());
            elseStmnt.addChild(ifHeaders());
        } else {
            elseStmnt = new ASTNode("else statement",null, curTok.getLine());
            elseFound = true;
        }

        elseStmnt.addChild(statement());

        if (curTok.tokenName() == "else_kw") {
            if (!elseFound) {
                elseStmnt.addChild(elseStatement());
            } else customErrorMsg("Else without if", curTok.getLine(), curTok.getPos());
        }

        exitNT("elseStatement");
        return elseStmnt;
    }
    
    ASTNode ifStatement() throws Exception
    {
        enterNT("ifStatement");
        expect("if_kw", false);
        ASTNode ifStmnt = new ASTNode("if statement",null, curTok.getLine());
        ifStmnt.addChild(ifHeaders());
        ifStmnt.addChild(statement());

        if (curTok.tokenName() == "else_kw") ifStmnt.addChild(elseStatement());

        exitNT("ifStatement");
        return ifStmnt;
    }
    
    ASTNode ifHeaders() throws Exception
    {
        expect("(_op", true);
        nextNonSpace(); // move past (
        ASTNode exp = expression();
        expect(")_op", false);
        expect("open_bracket_lt", true);
        return exp;
    }

    ASTNode forStatement() throws Exception
    {
        enterNT("forStatement");
        expect("for_kw", false);
        ASTNode forStmnt = null;
        expect("(_op", true);
        nextNonSpace(); //move past (

        // **BEGIN checking for colon token, which will determine whether
        //         loop is a foreach loop
        // NOTE: very hacky, will likely break under certain circumstances
        boolean isForEach = false;
        boolean identFound = false;

        if (isType()) {
            isForEach = lookAhead(1).tokenName() == "identifier"
                        && lookAhead(2).tokenName() == "colon_lt";
        }
        // END check for colon token**

        if (isForEach) {
            forStmnt = new ASTNode("foreach statement",null, curTok.getLine());
            nextNonSpace(); // skip type
            forStmnt.addChild(primary());
            nextNonSpace(); // skip colon
            forStmnt.addChild(expression());
        } else {
            forStmnt = new ASTNode("for statement",null, curTok.getLine());
            if(curTok.tokenName() == "semi_colon_lt")
            {
                forStmnt.addChild(new ASTNode("for init",null, curTok.getLine()));
                nextNonSpace(); //move past ';'
            }
            else
            {
                forStmnt.addChild(forInit());
            }
            if(curTok.tokenName() == "semi_colon_lt")
            {
                forStmnt.addChild(new ASTNode("expression",null, curTok.getLine()));
                nextNonSpace(); //move past ';'
            }
            else
            {
                forStmnt.addChild(expression());
                expect("semi_colon_lt", false);
                nextNonSpace(); // move past ;
            }
            if(curTok.tokenName() == "semi_colon_lt")
            {
                forStmnt.addChild(new ASTNode("for update",null, curTok.getLine()));
                nextNonSpace(); //move past ';'
            }
            else
            {
                forStmnt.addChild(forUpdate());
            }
        }

        expect(")_op", false);
        expect("open_bracket_lt", true);
        forStmnt.addChild(statement());
        exitNT("for statement");
        return forStmnt;
    }
    
ASTNode forInit() throws Exception
    {
        enterNT("forInit");
        ASTNode forIn = new ASTNode("for init",null, curTok.getLine());
        do
        {
            //TODO: ADD modifiers as possible indicators
            if(isType())
            {
                forIn.addChild(localVariableDeclarationStatement());
            }
            else
            {
                forIn.addChild(statementExpressionList());
                //expect("semi_colon_lt", false);
                nextNonSpace(); //advance past ;
            }
            System.out.println(curTok.getLiteral());
        }
        while(lastTok.tokenName() != "semi_colon_lt");
        exitNT("forInit");
        return forIn;
    }
    
    ASTNode forUpdate() throws Exception
    {
        enterNT("forUpdate");
        ASTNode forUp = new ASTNode("for update",null, curTok.getLine());
        forUp.addChild(statementExpressionList());
        exitNT("forUpdate");
        return forUp;
    }
    
    ASTNode statementExpressionList() throws Exception
    {
        enterNT("statementExpressionList");
        ASTNode stmntExpList = new ASTNode("statement expression list",null, curTok.getLine());
        boolean moreStmnts = true;
        while(moreStmnts)
        {
            stmntExpList.addChild(statementExpression());
            if(curTok.tokenCode() == 3007) // comma_lt
            {
                nextNonSpace(); // increment ahead of ,
                moreStmnts = true;
            }
            else
            {
                moreStmnts = false;
            }
        }
        exitNT("statementExpressionList");
        return stmntExpList;
    }
    
    ASTNode typeDeclarations() throws Exception
	{
		enterNT("typeDeclarations");
        ASTNode typeDecs = new ASTNode("type declarations",null, curTok.getLine());
        while(curTok.tokenCode() != 4001) // EOF
        {
            typeDecs.addChild(classDeclaration());
        }
        
        exitNT("typeDeclarations");
		return typeDecs;
	}
    
    
    ASTNode classInstanceCreationExpression() throws Exception
    {
        enterNT("classInstanceCreationExpression");
        ASTNode clsInst = new ASTNode("class instance creation expression",null, curTok.getLine());
        expect("new_kw", false);
        nextNonSpace(); //advance past new
        if(!references.contains(curTok.getLiteral()))
        {
            errorMsg("reference type", curTok.getLine(), curTok.getPos());
        }
        clsInst.addChild(new ASTNode("reference type",curTok.getLiteral(), curTok.getLine()));
        expect("(_op", true);
        nextNonSpace(); // move past (
        if(curTok.tokenName() == ")_op")
        {
            clsInst.addChild(new ASTNode("argument list",null, curTok.getLine()));
        }else
        {
            clsInst.addChild(argumentList());
        }
        expect(")_op", false);
        nextNonSpace(); // advance past )
        exitNT("classInstanceCreationExpression");
        return clsInst;
    }
    
    ASTNode classDeclaration() throws Exception
    {
        enterNT("classDeclaration");
        ASTNode classDec = new ASTNode("class declaration",null, curTok.getLine());
        if(isModifier(null))
        {
            classDec.addChild(handleModifiers("class"));
        }else{
            // create null place holder
            classDec.addChild(new ASTNode("modifiers",null, curTok.getLine()));
        }
        
        expect("class_kw", false);
        expect("identifier", true);
        classDec.addChild(new ASTNode("identifier",curTok.getLiteral(), curTok.getLine()));
        if(references.contains(curTok.getLiteral()))
        {
            customErrorMsg(curTok.getLiteral() + " has already been declared", curTok.getLine(), curTok.getPos());
        }
        references.add(curTok.getLiteral());
        nextNonSpace(); // advance past identifier
        classDec.addChild(classBody());
        exitNT("classDeclaration");
        return classDec;
    }
    
    void initModifiers(){
        modifiers = new HashMap<String,String[]>();
        String[] all = {"class","field","method", "constructor"};
        String[] allButConstr = {"class", "field", "method"};
        String[] allButClass = {"field", "method", "constructor"};
        String[] fieldAndMethod = {"field","method"};
        String[] classAndMethod = {"class","method"};
        String[] field = {"field"};
        String[] method = {"method"};
        
        modifiers.put("public_kw", all);
        modifiers.put("final_kw", allButConstr);
        modifiers.put("protected_kw", allButClass);
        modifiers.put("private_kw", allButClass);
        modifiers.put("static_kw", fieldAndMethod);
        modifiers.put("abstract_kw", classAndMethod);
        modifiers.put("transient_kw", field);
        modifiers.put("volatile_kw", field);
        modifiers.put("synchronized_kw", method);
        modifiers.put("native_kw", method);
    }
    
    boolean isModifier(String type) throws Exception
    {
        boolean retVal = false;
        if(modifiers.containsKey(curTok.tokenName())){
            if(type == null)
            {
                retVal = true;
            }else
            {
                String[] types = modifiers.get(curTok.tokenName());
                for(String mType: types){
                    if(type == mType){
                        retVal = true;
                    }
                }
            }
        }
        return retVal;
    }
    
    ASTNode handleModifiers(String type) throws Exception
    {
        enterNT("handleModifiers");
        ASTNode mod = new ASTNode("modifiers",null, curTok.getLine());
        while(isModifier(null))
        {
            if(!isModifier(type))
            {
                customErrorMsg(curTok.getLiteral() + " is not a " + type + " modifier", curTok.getLine(), curTok.getPos());
            }
            mod.addChild(new ASTNode(curTok.tokenName(),curTok.getLiteral(), curTok.getLine()));
            nextNonSpace(); // advance past modifier
        }
        exitNT("handleModifiers");
        return mod;
    }
    
    ASTNode classBody() throws Exception
    {
        enterNT("classBody");
        ASTNode clsBody = new ASTNode("class body",null, curTok.getLine());
        expect("open_bracket_lt", false);
        String s = nextNonSpace();
        // if not } then contains block statements
        if(s != "close_bracket_lt"){
            clsBody.addChild(classBodyDeclarations());
        } 
        // checks that current token is a }
        expect("close_bracket_lt", false);
        nextNonSpace(); //advance past the close bracket
        exitNT("classBody");
        return clsBody;
    }
    
    ASTNode classBodyDeclarations() throws Exception
    {
        enterNT("classBodyDeclarations");
        ASTNode clsBodyDecs = new ASTNode("class body declarations",null, curTok.getLine());
        while(curTok.tokenCode() != 3004 && curTok.tokenCode != 1026 && curTok.tokenCode != 1007) // close_bracket_lt, 4001 = EOF 1026 = case_kw, 1007 = default_kw
        {
            // error msg if reach EOF while parsing
            if(curTok.tokenCode() == 4001)
            {
                errorMsg("}",curTok.getLine(), curTok.getPos());
            }
            //check if constructor or method/field declaration
            if(isConstructor()){
                clsBodyDecs.addChild(constructorDeclaration());
            }else{
                clsBodyDecs.addChild(classMemberDeclaration());
            }
        }
        exitNT("classBodyDeclarations");
        return clsBodyDecs;
    }
    //Looks ahead to see if the current statement is a constructor declaration or a method/field declartion
    boolean isConstructor() throws Exception
    {
        JavaToken tok = curTok;
        int n = 0;
        //if modifier but not constructor mod then not constructor 
        if(isModifier(null)){
            if(!isModifier("constructor")){
                return false;
            }
            else{
                tok = lookAhead(++n);
            }
        }
        // if not a class type then it can't be a contructor declaration
        if(!references.contains(tok.getLiteral())){
            return false;
        }else{
            tok = lookAhead(++n);
        }
        // need to have an ( here to fully indicate a constructor vs a class result type for a method
        if(tok.tokenName() != "(_op"){
            return false;
        }
        return true;
    }
    
    ASTNode constructorDeclaration() throws Exception
    {
        enterNT("constructorDeclaration");
        ASTNode conDec = new ASTNode("constructor declaration", null, curTok.getLine());
        if(isModifier(null))
        {
            conDec.addChild(handleModifiers("method"));
        }else{
            conDec.addChild(new ASTNode("modifiers",null, curTok.getLine()));
        }
        conDec.addChild(constructorDeclarator());
        // TODO handle throws
        conDec.addChild(constructorBody());
        exitNT("constructorDeclaration");
        return conDec;
    }
    
    ASTNode constructorDeclarator() throws Exception
    {
        enterNT("constructorDeclarator");
        ASTNode conDec = new ASTNode("constructor declarator", null, curTok.getLine());
        if(!references.contains(curTok.getLiteral())){
			errorMsg("reference type", curTok.getLine(), curTok.getPos());
        }
        conDec.addChild(new ASTNode("identifier", curTok.getLiteral(), curTok.getLine()));
        expect("(_op", true);
        nextNonSpace(); // move past (
        if(curTok.tokenName() == ")_op")
        {
            conDec.addChild(new ASTNode("formal parameter list",null, curTok.getLine()));
        }else
        {
            conDec.addChild(formalParameterList());
        }
        expect(")_op", false);
        nextNonSpace(); // advance past )
        exitNT("constructorDeclarator");
        return conDec;
    }
    
    ASTNode constructorBody() throws Exception
    {
        enterNT("constructorBody");
        ASTNode conBody = new ASTNode("constructor body", null, curTok.getLine());
        expect("open_bracket_lt", false);
        String s = nextNonSpace();
        // if not } then contains explicit constructor statements or block statements
        if(s != "close_bracket_lt"){
            if(s == "super_kw" || s == "this_kw"){
                conBody.addChild(explicitConstructorInvocation());
            }
            conBody.addChild(blockStatements());
        } 
        // checks that current token is a }
        expect("close_bracket_lt", false);
        nextNonSpace(); //advance past the close bracket
        exitNT("constructorBody");
        return conBody;
    }
    
    ASTNode explicitConstructorInvocation() throws Exception
    {
        enterNT("explicitConstructorInvocation");
        ASTNode expConInv = new ASTNode("explicit constructor invocation", null, curTok.getLine());
        // add super or this
        expConInv.addChild(new ASTNode(curTok.tokenName(),curTok.getLiteral(), curTok.getLine()));
        expect("(_op", true); 
        nextNonSpace(); // move past (
        if(curTok.tokenName() == ")_op")
        {
            expConInv.addChild(new ASTNode("argument list",null, curTok.getLine()));
        }else
        {
            expConInv.addChild(argumentList());
        }
        expect(")_op", false);
        expect("semi_colon_lt", true);
        nextNonSpace(); // advance past ;
        exitNT("explicitConstructorInvocation");
        return expConInv;
    }
    
    ASTNode classMemberDeclaration() throws Exception
    {
        enterNT("classMemberDeclaration");
        ASTNode clsMemDec = null;
        ArrayList<String> find = new ArrayList<String>();
        find.add("EOF");
        find.add("semi_colon_lt");
        find.add("(_op");
        String fToken = lookAheadToFind(find);
        if(fToken == "(_op"){
            clsMemDec = methodDeclaration();
        }else{
            clsMemDec = fieldDeclaration();
        }
        exitNT("classMemeberDeclaration");
        return clsMemDec;
    }
    
    ASTNode fieldDeclaration() throws Exception
    {
        enterNT("fieldDeclaration");
        ASTNode fieldDec = new ASTNode("field declaration",null, curTok.getLine());
        if(isModifier(null))
        {
            fieldDec.addChild(handleModifiers("field"));
        }else{
            //create null placeholder
            fieldDec.addChild(new ASTNode("modifiers",null, curTok.getLine()));
        }
		if(!isType()) 
		{
			errorMsg("type", curTok.getLine(), curTok.getPos());
        }
        fieldDec.addChild(type());
        fieldDec.addChild(variableDeclarators());
        expect("semi_colon_lt", false);
        nextNonSpace(); //advance past ;
		exitNT("fieldDeclaration");
        return fieldDec;
    }
    
    ASTNode methodDeclaration() throws Exception
    {
        enterNT("methodDeclaration");
        ASTNode methDec = new ASTNode("method declaration", null, curTok.getLine());
        methDec.addChild(methodHeader());
        methDec.addChild(block());
        exitNT("methodDeclaration");
        return methDec;
    }
    
    ASTNode methodHeader() throws Exception
    {
        enterNT("method header");
        ASTNode methHeader = new ASTNode("method header", null, curTok.getLine());
        if(isModifier(null))
        {
            methHeader.addChild(handleModifiers("method"));
        }else{
            methHeader.addChild(new ASTNode("modifiers",null, curTok.getLine()));
        }
        if(!isType() && curTok.tokenName() != "void_kw")
		{
			errorMsg("type", curTok.getLine(), curTok.getPos());
        }
        if(curTok.tokenName() == "void_kw")
        {
            methHeader.addChild(new ASTNode("primative type", "void_kw", curTok.getLine()));
            nextNonSpace(); //advance past void
        }else{
            methHeader.addChild(type());
        }
        methHeader.addChild(methodDeclarator());
        // TODO: Handle throws
        exitNT("method header");
        return methHeader;
    }
    
    ASTNode methodDeclarator() throws Exception
    {
        enterNT("method declarator");
        ASTNode methDec = new ASTNode("method declarator", null, curTok.getLine());
        expect("identifier", false);
        methDec.addChild(new ASTNode("identifier", curTok.getLiteral(), curTok.getLine()));
        expect("(_op", true);
        nextNonSpace(); // move past (
        if(curTok.tokenName() == ")_op")
        {
            methDec.addChild(new ASTNode("formal parameter list",null, curTok.getLine()));
        }else
        {
            methDec.addChild(formalParameterList());
        }
        expect(")_op", false);
        nextNonSpace(); // advance past )
        exitNT("method declarator");
        return methDec;
    }
    
    ASTNode formalParameterList() throws Exception
    {
        enterNT("formalParameterList");
        ASTNode paramList = new ASTNode("formal parameter list",null, curTok.getLine());
        boolean cont = false;
        do{
            paramList.addChild(formalParameter());
            // check for an additional param
            if(curTok.tokenName() == "comma_lt")
            {
                nextNonSpace(); //advance past ,
                cont = true;
            }else
            {
                cont = false;
            }
        }while(cont);
        exitNT("formalParameterList");
        return paramList;
    }
    
    ASTNode formalParameter() throws Exception
    {
        enterNT("formalParameter");
        ASTNode formalParam = new ASTNode("formal parameter",null, curTok.getLine());
        formalParam.addChild(type());
        formalParam.addChild(variableDeclaratorID());
        exitNT("formalParameter");
        return formalParam;
    }
    
	// print out the non-terminal being entered
	void enterNT(String s)
	{
        if (debug) {
            for(int i = 0; i < depth; i++)
            {
                System.out.print(" ");
            }
            System.out.println("-> enter <" + s + ">");
            depth++;
        }
    }

	// print out the non-terminal being exited
	void exitNT(String s)
	{
        if (debug) {
            for(int i = 0; i < depth; i++)
            {
                System.out.print(" ");
            }
            System.out.println("<- exit <" + s + ">");
            depth--;
        }
    }

    void printTree(ASTNode root){
        ArrayList<ASTNode> stack = new ArrayList<ASTNode>();
        ArrayList<String> visited = new ArrayList<String>();
        ASTNode parentNode = new ASTNode("start",null, curTok.getLine());
        parentNode.setDepth(0);
        stack.add(root);
        visited.add(root.getKey());
        while (stack.size() > 0)
        {
            boolean keepParent = false;
            // don't need childless nodes
            if(stack.get(stack.size() -1).childCount() == 0){
                stack.remove(stack.size() -1);
            }
            else{
                parentNode = stack.get(stack.size() - 1);
            }
            ArrayList<ASTNode> children = parentNode.getChildren();
            for(ASTNode child : children){
                if(!visited.contains(child.getKey())){
                    keepParent = true;
                    child.setDepth(parentNode.getDepth()+1);
                    stack.add(child);
                    child.print(); // print ASTNODE
                    visited.add(child.getKey());
                    break;
                }
            }
            if (!keepParent)
            {
                stack.remove(stack.size() - 1);
            }
        }
    }
    
    void setDebug(boolean debug) {
        this.debug = debug;
    }

    void setPrintTree(boolean printTree) {
        this.printTree = printTree;
    }
}