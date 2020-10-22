import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        boolean winRLast = false;
        if(!(this.skip)){
            while(matcher.find()){
                // handles unix and windows newlines
                if(matcher.group().matches("\\r")){
                    winRLast=true;
                    line++;
                    sourceIndex = sourceIndex++;
                    pos = 0;
                }
                else if(matcher.group().matches("\\n")){
                    if(!winRLast){
                        line++;
                    }
                    winRLast=false;
                    sourceIndex++;
                    pos = 0;
                }
                else{
                    winRLast=false;
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