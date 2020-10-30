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
        
        LexScanner l = new LexScanner(source);
        Parser p = new Parser();
        Translator t = new Translator();

        p.setLexer(l);

        p.setDebug(false);
        p.setPrintTree(true);
        t.setDebug(true);
        t.setCrashOnError(false);

        System.out.print(t.translate(p.parse()).getSource());
    }
}