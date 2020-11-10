import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.FileWriter;
import java.io.BufferedWriter;

public class TranslatorTest {
    public static void main(String[] args) throws Exception{
        if (args.length <= 0) {
            throw new Exception("Argument not provided");
        }

        String source = "";
        String filename = args[0];

        try {
            source = new String (Files.readAllBytes(Paths.get(filename)));
            System.out.println(source);
        } catch (IOException e){
            e.printStackTrace();
        }
        
        // Set up translator
        LexScanner l = new LexScanner(source);
        Parser p = new Parser();
        Translator t = new Translator();
        p.setLexer(l);
        p.setDebug(false);
        p.setPrintTree(false);
        t.setDebug(true);
        t.setCrashOnError(false);
        // Translate Java Code
        ASTNode program = p.parse();
        String response = "";
        String statusCode = "200";
        if(p.getErrorMsg() != null)
        {
        	statusCode = "501";
            response = p.getErrorMsg();
        }else
        {
        	t.finalize(program);
        	response = t.getSource();
        }
        System.out.print("Response " + response + "\nStatus Code: " + statusCode);
    }
}