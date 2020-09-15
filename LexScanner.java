import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Lexical Scanner class
class LexScanner{
    private SourceArray sa; //array of lexemes for source code
    private Map<String,JavaToken> tokType; // keyword table
    private String curLex; // current lexeme
    private String curTok; // current token
    private JavaToken curJavaToken; // current java token
    private int curTokCode; // current token code
    private int lastPos; //last position

    // abstracts process of getting the next lexeme
    class SourceArray {
        private int line; // current line
        private int pos; // current position
        private int nextPos; //next position (if correction necessary stores next lexeme position)
        private String source; // java source code
        private String lex; //current lexeme
        private boolean skip; //used for skipping calling nextLex when curLex already set
        private int sourceIndex; // current char in the string
        private String lexRegex; // regex for lexeme
        private Pattern lexPattern; // pattern for a lexeme
        private Matcher matcher; // matcher for lexemes

        // Constructor. Init line and pos #'s in source code and creates a buffered reader object with source file
        SourceArray(String source) {
            this.sourceIndex = 0;
            this.line = this.pos = 1;
            this.source = source;
            this.lexRegex = "[a-zA-Z_0-9]+|[^a-zA-Z_0-9]";
            this.skip = false; 
            this.lexPattern = Pattern.compile(this.lexRegex);
            this.matcher = lexPattern.matcher(source);
        }

        // Returns current line 1...n in source code
        int currentLine(){
            return line;
        }

        // Returns current position 1...n in current line
        int currentPos(){
            return pos;
        }
        
        // Returns the position of the start of the previous lexeme
        
        void haltNext(int lastPos){
            //System.out.println("Halting next!");
            this.skip = true;
            this.nextPos = this.pos;
            this.pos = lastPos;
        }

        // Returns next lexeme in source file. Returns "EOF" if at end of file.
        String nextLex(){
            String lex;
            if(!(this.skip)){
                while(matcher.find()){
                    if(matcher.group().matches("\\n")){
                        line++;
                        sourceIndex++;
                        lastPos = pos;
                        pos = 0;
                    }else{
                        lastPos = pos;
                        pos = pos + matcher.start() - sourceIndex;
                        sourceIndex = matcher.start();
                        this.lex = matcher.group();
                        return this.lex;
                    }
                }
                return "EOF";
            }else{
                this.skip = false;
                this.pos = nextPos;
                return this.lex;
            }
        }

        // Prints all lexemes in the source file
        void printAll() {
            String s;
            while((s = this.nextLex()) != "EOF")
                System.out.println(s + "#\tLine - " + line + " Pos - " + pos);
        }
    }
    
    // Constructor: Feeds source code to SourceArray 
    LexScanner(String source) throws Exception{
        sa = new SourceArray(source); //initialize data structure to store source code
        tokType = initTokenTypes(); //initialize keyword/operator table
    }

    // Initializes the keyword table for tokens that have a single literal representation ex "while", ")", "+" not <integer> or <id>
    Map<String, JavaToken> initTokenTypes(){
        Map<String,JavaToken> tokType = new HashMap<String, JavaToken>();
        // Keywords
        tokType.put("abstract", new JavaToken("abstract", "abstract_kw", 1001, true));
        tokType.put("continue", new JavaToken("continue", "continue_kw", 1002, true));
        tokType.put("for", new JavaToken("for", "for_kw", 1003, true));
        tokType.put("new", new JavaToken("new", "new_kw", 1004, true));
        tokType.put("switch", new JavaToken("switch", "switch_kw", 1005, true));
        tokType.put("assert", new JavaToken("assert", "assert_kw", 1006, true));
        tokType.put("default", new JavaToken("default", "default_kw", 1007, true));
        tokType.put("goto", new JavaToken("goto", "goto_kw", 1008, true));
        tokType.put("package", new JavaToken("package", "package_kw", 1009, true));
        tokType.put("synchronized", new JavaToken("synchronized", "synchronized_kw", 1010, true));
        tokType.put("boolean", new JavaToken("boolean", "boolean_kw", 1011, true));
        tokType.put("do", new JavaToken("do", "do_kw", 1012, true));
        tokType.put("if", new JavaToken("if", "if_kw", 1013, true));
        tokType.put("private", new JavaToken("private", "private_kw", 1014, true));
        tokType.put("this", new JavaToken("this", "this_kw", 1015, true));
        tokType.put("break", new JavaToken("break", "break_kw", 1016, true));
        tokType.put("double", new JavaToken("double", "double_kw", 1017, true));
        tokType.put("implements", new JavaToken("implements", "implements_kw", 1018, true));
        tokType.put("protected", new JavaToken("protected", "protected_kw", 1019, true));
        tokType.put("throw", new JavaToken("throw", "throw_kw", 1020, true));
        tokType.put("byte", new JavaToken("byte", "byte_kw", 1021, true));
        tokType.put("else", new JavaToken("else", "else_kw", 1022, true));
        tokType.put("import", new JavaToken("import", "import_kw", 1023, true));
        tokType.put("public", new JavaToken("public", "public_kw", 1024, true));
        tokType.put("throws", new JavaToken("throws", "throws_kw", 1025, true));
        tokType.put("case", new JavaToken("case", "case_kw", 1026, true));
        tokType.put("enum", new JavaToken("enum", "enum_kw", 1027, true));
        tokType.put("instanceof", new JavaToken("instanceof", "instanceof_kw", 1028, true));
        tokType.put("return", new JavaToken("return", "return_kw", 1029, true));
        tokType.put("transient", new JavaToken("transient", "transient_kw", 1030, true));
        tokType.put("catch", new JavaToken("catch", "catch_kw", 1031, true));
        tokType.put("extends", new JavaToken("extends", "extends_kw", 1032, true));
        tokType.put("int", new JavaToken("int", "int_kw", 1033, true));
        tokType.put("short", new JavaToken("short", "short_kw", 1034, true));
        tokType.put("try", new JavaToken("try", "try_kw", 1035, true));
        tokType.put("char", new JavaToken("char", "char_kw", 1036, true));
        tokType.put("final", new JavaToken("final", "final_kw", 1037, true));
        tokType.put("interface", new JavaToken("interface", "interface_kw", 1038, true));
        tokType.put("static", new JavaToken("static", "static_kw", 1039, true));
        tokType.put("void", new JavaToken("void", "void_kw", 1040, true));
        tokType.put("class", new JavaToken("class", "class_kw", 1041, true));
        tokType.put("finally", new JavaToken("finally", "finally_kw", 1042, true));
        tokType.put("long", new JavaToken("long", "long_kw", 1043, true));
        tokType.put("strictfp", new JavaToken("strictfp", "strictfp_kw", 1044, true));
        tokType.put("volatile", new JavaToken("volatile", "volatile_kw", 1045, true));
        tokType.put("const", new JavaToken("const", "const_kw", 1046, true));
        tokType.put("float", new JavaToken("float", "float_kw", 1047, true));
        tokType.put("native", new JavaToken("native", "native_kw", 1048, true));
        tokType.put("super", new JavaToken("super", "super_kw", 1049, true));
        tokType.put("while", new JavaToken("while", "while_kw", 1050, true));
        // Operators
        tokType.put("(", new JavaToken("(", "(_op", 2001, false));
        tokType.put(")", new JavaToken(")", ")_op", 2002, false));
        tokType.put("[", new JavaToken("[", "[_op", 2003, false));
        tokType.put("]", new JavaToken("]", "]_op", 2004, false));
        tokType.put(".", new JavaToken(".", "._op", 2005, false));
        tokType.put("~", new JavaToken("~", "~_op", 2006, false));
        tokType.put("!", new JavaToken("!", "!_op", 2007, false));
        tokType.put("^", new JavaToken("^", "^_op", 2008, false));
        tokType.put("++", new JavaToken("++", "++_op", 2009, false));
        tokType.put("--", new JavaToken("--", "--_op", 2010, false));
        tokType.put(">>>", new JavaToken(">>>", ">>>_op", 2011, false));
        tokType.put("<=", new JavaToken("<=", "<=_op", 2012, false));
        tokType.put(">=", new JavaToken(">=", ">=_op", 2013, false));
        tokType.put("==", new JavaToken("==", "==_op", 2014, false));
        tokType.put("!=", new JavaToken("!=", "!=_op", 2015, false));
        tokType.put("&&", new JavaToken("&&", "&&_op", 2016, false));
        tokType.put("?", new JavaToken("?", "?_op", 2017, false));
        tokType.put("+=", new JavaToken("+=", "+=_op", 2018, false));
        tokType.put("-=", new JavaToken("-=", "-=_op", 2019, false));
        tokType.put("*=", new JavaToken("*=", "*=_op", 2020, false));
        tokType.put("/=", new JavaToken("/=", "/=_op", 2021, false));
        tokType.put("%=", new JavaToken("%=", "%=_op", 2022, false));
        tokType.put("+", new JavaToken("+", "+_op", 2023, true));
        tokType.put("-", new JavaToken("-", "-_op", 2024, true));
        tokType.put("*", new JavaToken("*", "*_op", 2025, true));
        tokType.put("/", new JavaToken("/", "/_op", 2026, true));
        tokType.put("%", new JavaToken("%", "%_op", 2027, true));
        tokType.put("<", new JavaToken("<", "<_op", 2028, true));
        tokType.put(">", new JavaToken(">", ">_op", 2029, true));
        tokType.put("<<", new JavaToken("<<", "<<_op", 2030, true));
        tokType.put(">>", new JavaToken(">>", ">>_op", 2031, true));
        tokType.put("=", new JavaToken("=", "=_op", 2032, true));
        tokType.put("&", new JavaToken("&", "&_op", 2033, true));
        tokType.put("|", new JavaToken("|", "|_op", 2034, true));
        // Special characters
        tokType.put("{", new JavaToken("{", "open_bracket_lt", 3003, false));
        tokType.put("}", new JavaToken("}", "close_bracket_lt", 3004, false));
        tokType.put(":", new JavaToken(":", "colon_lt", 3005, false));
        tokType.put(";", new JavaToken(";", "semi_colon_lt", 3006, false));
        tokType.put(",", new JavaToken(",", "comma_lt", 3007, false));
        tokType.put(".", new JavaToken(".", "period_lt", 3008, false));
        tokType.put(" ", new JavaToken(" ", "space_lt", 3009, true));
        tokType.put("\"", new JavaToken("\"", "double_quote_lt", 3010, true));
        tokType.put("'", new JavaToken("'", "single_quote_lt", 3011, true));
        // For reference but not stored in this data structure
        // id, "identifier:3001"
        // integer, "integer_lt:3002"
        // EOF: "EOF:4001"
        // DNE: "DNE:5001"
        return tokType;
    }
    
    
    // Returns the token type and token code in one string ex integer_lt:3002
    JavaToken getTokenType(String lexeme) throws Exception{
        // Check if lexeme in keyword table, else determine if one of other possible options
        if(tokType.containsKey(lexeme)){
            this.curJavaToken = tokType.get(lexeme);
            if(this.curJavaToken.possibleMulti()){
                if(lexeme.equals(" ")){
                    handleSpaces(lexeme);
                }else if(lexeme.equals("\"") || lexeme.equals("'")){
                    handleString(lexeme);
                }else{
                    advance(lexeme);
                }
            }
            return this.curJavaToken;//tokType.get(this.curLex);
        }else if(isNumeric(lexeme)){
            getNumberToken(lexeme);
            return this.curJavaToken;
        }else if(lexeme.equals("EOF")){
            return new JavaToken("EOF","EOF", 4001);
        }else if(isID(lexeme)){
            return new JavaToken(lexeme, "identifier", 3001);
        }else{
            return new JavaToken("DNE", "DNE", 5001); // Does not exist
        }
    }
    
    void advance(String lexeme){
        int startPos = getPosition();
        while(tokType.containsKey(lexeme)){
            this.curLex = lexeme;
            this.curJavaToken = tokType.get(lexeme);
            lexeme += sa.nextLex();
        }
        //stops sa from advancing on the next lexeme 
        sa.haltNext(lastPos);
    }
    void handleSpaces(String lexeme){
        int startPos = getPosition();
        while(lexeme.replace(" ", "").length() == 0){
            this.curLex = lexeme;
            lexeme += sa.nextLex();
        }
        //stops sa from advancing on the next lexeme 
        sa.haltNext(startPos);
    }
    
    void handleString(String lexeme) throws Exception{
        String next;
        int startPos = getPosition();
        this.curJavaToken = new JavaToken("string_lt", "string_lt", 3012);
        while(!(next = sa.nextLex()).equals(lexeme)){
            if(next.equals("EOF")){
                throw new Exception("String starting at line " + getLine() + " pos " + startPos + " is unterminated.");
            }else{
                this.curLex += next;
            }
        }
        this.curLex += next;
    }
    
    // Returns next token
    String nextToken() throws IOException, Exception{
        this.curLex = sa.nextLex();
        JavaToken tokenType = this.getTokenType(this.curLex);
        // If lemexe in token table assign values to class variables
        if(tokenType.tokenName() != "DNE"){
            this.curTok =tokenType.tokenName();
            this.curTokCode = tokenType.tokenCode();
        }else{
            this.curTok = "DNE";
            this.curTokCode = 5001;
        }
        return this.curTok;
    }
    // returns true if lexeme is a number
    boolean isNumeric(String lexeme){
        if(lexeme == null){
            return false;
        }
        try {
            double d = Double.parseDouble(lexeme);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
    
    void getNumberToken(String lexeme){
        String decPat = "[0-9]+([.,][0-9]+)?";
        String intPat = "([0-9]+[,]*)+";
        String partDecPat = "([0-9]+[,]*)+[,.]+";
        boolean numeric = true;
        boolean partial = false;
        String next = "";
        int startPos = getPosition();
        
        while(numeric){
            if(lexeme.matches(intPat)){
                this.curJavaToken = new JavaToken(lexeme, "integer_lt", 3002, true);
                partial = false;
            }else if(lexeme.matches(decPat)){
                this.curJavaToken = new JavaToken(lexeme, "decimal_lt", 3003, true);
                partial = false;
            }else if(lexeme.matches(partDecPat)){
                this.curJavaToken = new JavaToken(lexeme, "DNE", 5001);
                partial = true;
            }
            else {
                this.curJavaToken = new JavaToken(lexeme, "DNE", 5001);
                partial = false;
                System.exit(1);
            }
            this.curLex = lexeme;
            next = sa.nextLex();
            if(tokType.containsKey(next) && !(next.equals(".")) && !(next.equals(","))){
                //break 
                numeric = false;
            }else if(!(isNumeric(lexeme)) &&  !(partial)){
                numeric = false;
            }
            lexeme += next;
        }
        sa.haltNext(startPos);
    }
    // Determines if lexeme is an identifier
    boolean isID(String lexeme){
        // Checks if one character letter
        if(lexeme.length() > 0 && lexeme.matches("[a-zA-Z_$][a-zA-Z_$0-9]*")){
            return true;
        }else{
            return false;
        }
    }
    
    // Returns current token - slightly different than next token
    String getToken(){
        return this.curTok;
    }

    // Returns current token code 
    int getTokenCode(){
        return this.curTokCode;
    }

    // Returns current lexeme
    String getLexeme(){
        return this.curLex;
    }

    // Returns line number of current lexeme 1...n
    int getLine(){
        return sa.currentLine();
    }

    // Returns position (lexeme count) of current lexeme in line 1...n
    int getPosition(){
        return sa.currentPos();
    }
}