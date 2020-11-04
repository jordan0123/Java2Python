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
        t.setDebug(true);
        t.setCrashOnError(false);
        // Translate Java Code
        String response = t.translate(p.parse()).getSource();
        // Load python code to return map
        Map<String, String> retMap = new HashMap<String,String>();
        retMap.put("statusCode", "200");
        retMap.put("body", response);
        return retMap;
    }
}