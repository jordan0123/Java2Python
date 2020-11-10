package senior;

import com.amazonaws.services.lambda.runtime.Context;
import java.util.Map;
import java.util.HashMap;

public class RequestHandler {
    public Map<String,String> handleRequest(Map<String,String> event, Context context) throws Exception{ 
    	// Get source from the event
        String source = event.get("code");
        
    	// Set up translator
        LexScanner l = new LexScanner(source);
        Parser p = new Parser();
        Translator t = new Translator();
        p.setLexer(l);
        p.setDebug(false);
        p.setPrintTree(false);
        t.setDebug(false);
        t.setCrashOnError(false);
        // Check for errors in parse then translate Java Code
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
        // Load python code to return map
        Map<String, String> retMap = new HashMap<String,String>();
        retMap.put("statusCode", statusCode);
        retMap.put("body", response);
        return retMap;
    }
}