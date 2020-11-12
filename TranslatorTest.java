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
            if (t.isErrorOccurred()) {
                statusCode = "502";
                response = t.getErrorMessage();
            } else response = t.getSource();
        }
        System.out.println("Response:\n\n" + response + "\nStatus Code: " + statusCode);

        if (t.isErrorOccurred() && t.getDebug()) {
            System.out.println("Debug:\n\n" + t.getSource());
        }

        if (p.getErrorMsg() != null || t.isErrorOccurred()) return;

        // write python source to an executable python file
        try {
            String[] fArray = filename.split("\\\\");
            filename = fArray[fArray.length-1];
            FileWriter writer = new FileWriter(filename + ".py");
            writer.write(t.getSource());
            writer.close();
            System.out.println("Python source written to: " + filename + ".py");
        } catch (IOException e) {
            System.out.println("An error occurred while writing to file.");
            e.printStackTrace();
        }
    }
}
