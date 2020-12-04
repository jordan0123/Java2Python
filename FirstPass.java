import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap; // import the HashMap class
import java.util.Stack;
import java.util.ArrayList;

/*
    Class for parsing class types and method information before full parse
*/
public class FirstPass{
    String methodPattern = "([a-zA-Z]+)\\(.*\\)\\s*\\{"; // regex pattern for methods
    String classPattern = "class\\s*([a-zA-Z]+)\\s*\\{"; // regex pattern for classes
    Pattern pat; // pattern object
    Matcher matcher; // matcher object
    String source; // Java source code to parse
    HashMap<String, String[]> classes; // key: methodStartIndex value: {className, classEndIndex}
    HashMap<String, String[]> methods; // key: methodStartIndex value: {methodName, methodEndIndex}
    HashMap<String, String[]> classMethods; // stores all the method names of each class
    
    FirstPass(String source){
        this.source = source;
        this.parseClasses(); // identify class declaration locations
        this.parseMethods();  // identify method declaration locations
        this.classMethods = new HashMap<String, String[]>(); // stores list of methods (value) for each class (key)
        this.parse(0, null); // start parse of file to load classMethods data structure
        for(String key: this.classMethods.keySet()){
            System.out.println("Class " + key + " has methods:");
            for(String method: this.classMethods.get(key)){
                System.out.println("\t"+method);
            }
        }
    }
    /*
        Method to recursively parse the contents of class bodies for method declarations
    */
    int parse(int start, String clsName){
        int i = start; //start of parse
        Stack<Character> stack = new Stack<Character>(); // keeps track of open and closed parens to determine class boundaries
        boolean cont = true; // keeps loop going until it's time to exit the class
        boolean inStr = false; // determines whether a found { or } was inside a string at the time and should be ignored
        ArrayList<String> clsMethods = new ArrayList<String>(); // list of methods found in the class
        
        // Loop continues until the source has been fully parsed or the classes closing } has been reached (note the initial parse with null class will not exit under this condition)
        while(i < this.source.length() && (clsName == null || cont) ){
            //If at class declartion start parse of that class body
            if(this.classes.containsKey(Integer.toString(i))){
                i = this.parse(Integer.parseInt(this.classes.get(Integer.toString(i))[1]), this.classes.get(Integer.toString(i))[0]);
                // parse returns with the new value for i to use -> skips ahead to end of the class
            }else if(this.methods.containsKey(Integer.toString(i))){
                clsMethods.add(this.methods.get(Integer.toString(i))[0]);
                i = Integer.parseInt(this.methods.get(Integer.toString(i))[1]);
                // -> skips ahead to end of the method
            }else{
                // switch statement for determining boundaries of a class using stack to store the open parens encountered
                switch(this.source.charAt(i)){
                    case '{':
                        if(!inStr){
                            stack.push('{');
                        }else{
                            //handles case where { is inside string and should be ignored
                        }
                        break;
                    case '}':
                        if(!inStr){
                            if(stack.size() != 0){
                                stack.pop();
                            }
                            if(stack.size() == 0){
                                cont = false;
                            }
                        }else{
                            //handles case where } is inside string and should be ignored
                        }
                        break;
                    case '"':
                        // alternates string status as a " is encountered
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