import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.FileWriter;
import java.io.BufferedWriter;

public class ParserTest {
    public static void main(String[] args) throws Exception{
        if (args.length <= 0) {
            throw new Exception("Argument not provided");
        }
        String source = "";
        String filename = args[0];
        //BufferedWriter bw = null; // writes to output file
        try {
            source = new String (Files.readAllBytes(Paths.get(filename)));
            System.out.println(source);
        }catch (IOException e){
            e.printStackTrace();
        }
        
        //String outFile = source + "-scanner_trace_file.txt";
        //bw = new BufferedWriter((new FileWriter(outFile)));
        // Initialize LexScanner with filename of source code
        LexScanner l = new LexScanner(source);
        Parser p = new Parser();
        p.setLexer(l);
        p.parse();
        if(p.getErrorMsg() != null)
        {
            System.out.println(p.getErrorMsg());
        }
//        String pStr = "".format("\n%-15s%-17s%-15s%-10s%-10s\n", "Lexeme", "Token", "Token Code", "Line", "Position");
//        System.out.print(pStr);
//        bw.write(pStr);
//        while(!(l.nextToken()).equals("EOF")){
//            //pStr = pStr.format("%-15s%-17s%-15s%-10s%-10s\n", l.getLexeme(), l.getToken(), l.getTokenCode(), l.getLine(), l.getPosition());
//            //System.out.print(pStr);
//            bw.write(pStr);
//        }
//        bw.close();
    }
}