import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap; // import the HashMap class
import java.util.Stack;
import java.util.ArrayList;


public class FirstPass{
    String methodPattern = "([a-zA-Z]+)\\(.*\\)\\s*\\{";
    String classPattern = "class\\s*([a-zA-Z]+)\\s*\\{";
    Pattern pat;
    Matcher matcher;
    String source;
    HashMap<String, String[]> classes; // key: methodStartIndex value: {className, classEndIndex}
    HashMap<String, String[]> methods; // key: methodStartIndex value: {methodName, methodEndIndex}
    HashMap<String, String[]> classMethods; // stores all the method names of each class
    
    FirstPass(String source){
        this.source = source;
        this.parseClasses();
        this.parseMethods();
        this.classMethods = new HashMap<String, String[]>();
        this.parse(0, null);
        for(String key: this.classMethods.keySet()){
            System.out.println("Class " + key + " has methods:");
            for(String method: this.classMethods.get(key)){
                System.out.println("\t"+method);
            }
        }
    }
    
    int parse(int start, String clsName){
        int i = start; //start of parse
        Stack<Character> stack = new Stack<Character>(); // keeps track of open and closed parens to determine class boundaries
        boolean cont = true; // keeps loop going until it's time to exit the class
        boolean inStr = false; // determines whether a found { or } was inside a string at the time and should be ignored
        ArrayList<String> clsMethods = new ArrayList<String>(); // list of methods found in the class
        while(i < this.source.length() && (clsName == null || cont) ){
            if(this.classes.containsKey(Integer.toString(i))){
                i = this.parse(Integer.parseInt(this.classes.get(Integer.toString(i))[1]), this.classes.get(Integer.toString(i))[0]);
            }else if(this.methods.containsKey(Integer.toString(i))){
                clsMethods.add(this.methods.get(Integer.toString(i))[0]);
                i = Integer.parseInt(this.methods.get(Integer.toString(i))[1]);
            }else{
                switch(this.source.charAt(i)){
                    case '{':
                        if(!inStr){
                            stack.push('{');
                        }else{
                            System.out.print("Ignoring { in str");
                        }
                        break;
                    case '}':
                        if(!inStr){
                            stack.pop();
                            if(stack.size() == 0){
                                cont = false;
                            }
                        }else{
                            System.out.print("Ignoring } in str");
                        }
                        break;
                    case '"':
                        inStr = !inStr;
                        break;
                }
                i++;
            }
        }
        if(clsName != null){
            String[] arr = new String[clsMethods.size()];
            classMethods.put(clsName, clsMethods.toArray(arr));
        }
        
        return i;
    }
    // finds names and start and end indexes for class declarations
    void parseClasses(){
        this.classes = new HashMap<String, String[]>();
        this.pat = Pattern.compile(this.methodPattern);
        this.pat = Pattern.compile(this.classPattern);
        this.matcher = pat.matcher(this.source);
        while(matcher.find()){
            String [] match = {matcher.group(1), Integer.toString(matcher.end() - 1)};
            this.classes.put(Integer.toString(matcher.start()), match);
        }
    }
    // finds names and start and end indexes for method declarations
    void parseMethods(){
        methods = new HashMap<String, String[]>();
        this.pat = Pattern.compile(this.methodPattern);
        this.matcher = pat.matcher(this.source);
        while(matcher.find()){
            String [] match = {matcher.group(1), Integer.toString(matcher.end() - 1)};
            this.methods.put(Integer.toString(matcher.start()), match);
        }
    }
    // returns a map of classes and their respective methods
    HashMap<String, String[]> getClassMethods(){
        return this.classMethods;
    }
}


//String methodPattern = "[a-zA-Z]+\\s*(?=\\()(?:(?=.*?\\((?!.*?\1)(.*\\)(?!.*\2).*))(?=.*?\\)(?!.*?\2)(.*)).)+?.*?(?=\1)[^(]*(?=\2$)\\s*\\{";
//String methodPattern = "(?=\\()(?:(?=.*?\\((?!.*?\1)(.*\\)(?!.*\2).*))(?=.*?\\)(?!.*?\2)(.*)).)+?.*?(?=\1)[^(]*(?=\2$)\\{";
//        for(String key: this.classes.keySet()){
//            System.out.println("Here's a key " + key);
//            System.out.println(this.classes.get(key)[0]);
//        }
//        for(String key: this.methods.keySet()){
//            System.out.println("Here's a key " + key);
//            System.out.println(this.methods.get(key)[0]);
//        }