import java.io.IOException;

import java.util.Collections;
import java.util.Arrays;
import java.util.Set;

import java.util.Stack;
import java.util.ArrayList;
import java.util.HashMap;


public class Translator {
    private Parser parser;
    private PythonBuilder pyBuilder;

    private Set<String> idList;
    private HashTableSet<String> options;

    private ASTNode mainMethod = null;

    private boolean debug = true;
    private boolean crashOnError = false;
    private boolean errorOccurred = false;

    private String errorMessage = null;

    HashMap<String, String> litTable;

    // identifier stack for switch statements
    Stack<String> switchCmp;

    // identifier stack for classes
    Stack<String> classNames;

    // found a (pre|post)fix (inc|dec)rement
    // - in the order of pre(inc, dec), post(inc, dec)
    private boolean[] foundNfix;

    Translator(Parser parser) {
        this.parser = parser;
        pyBuilder = new PythonBuilder();
        options = new HashTableSet<String>();
        switchCmp = new Stack<String>();
        classNames = new Stack<String>();

        litTable = new HashMap<String, String>();
        litTable.put("||", "or");
        litTable.put("&&", "and");
        litTable.put("!", "not");
        litTable.put("true", "True");
        litTable.put("false", "False");
        litTable.put("this", "self");
        litTable.put("null", "None");
        litTable.put("System.out.print", "print");
        litTable.put("System.out.println", "print");

        foundNfix = new boolean[4];
        Arrays.fill(foundNfix, false);
    }

    void setDebug(boolean debug) { this.debug = debug; }
    void setCrashOnError(boolean crashOnError) { this.crashOnError = crashOnError; }

    boolean getDebug() { return this.debug; }

    boolean isErrorOccurred() { return errorOccurred; }
    String getErrorMessage() { return errorMessage; }

    void error(String callName, String message) {
        if (errorOccurred) return;
        errorOccurred = true;
        errorMessage = "Error::Translator::" + callName + ": " + message;

        if (crashOnError || debug) {
            System.out.println(errorMessage);
        }

        if (crashOnError) System.exit(0);
    }

    void notImplemented(String callName, String nodeType) {
        String message = "encountered child type '" + nodeType + "' not implemented.";
        error(callName, message);
    }

    // expandStack()
    // - Expands the top node of the stack.
    Stack<ASTNode> expandStack(Stack<ASTNode> nodeStack) {
        if (nodeStack.size() > 0) {
            ASTNode node = nodeStack.pop();
            if (node.childCount() > 0) {
                ArrayList<ASTNode> children = node.getChildren();

                for (int i = children.size()-1; i > -1; i--) {
                    nodeStack.push(children.get(i));
                }
            }
        }

        return nodeStack;
    }

    // remap()
    // - remaps a Java literal to a Python literal
    String remap(String literal) {
        // System.out.println(literal);
        if (literal != null && litTable.containsKey(literal)) {
            return litTable.get(literal);
        } else if (literal != null) return literal; else return "";
    }

    String getSource() {
        return pyBuilder.getSource();
    }

    void finalize(ArrayList<Comment> comments) {
        // make sure comments are sorted by line
        Collections.sort(comments, new CommentLineComparator());

        final String[][] nFixMethods = {
            {
                "# preincrement method (generated by Java2Python)",
                "def _preinc(name, local={}):",
                "    if name in local:",
                "        local[name] += 1",
                "        return local[name]",
                "    globals()[name] += 1",
                "    return globals()[name]"
            },

            {
                "# predecrement method (generated by Java2Python)",
                "def _predec(name, local={}):",
                "    if name in local:",
                "        local[name] -= 1",
                "        return local[name]",
                "    globals()[name] -= 1",
                "    return globals()[name]"
            },

            {
                "# postincrement method (generated by Java2Python)",
                "def _postinc(name, local={}):",
                "    if name in local:",
                "        local[name] += 1",
                "        return local[name] - 1",
                "    globals()[name] += 1",
                "    return globals()[name] - 1"
            },

            {
                "# postdecrement method (generated by Java2Python)",
                "def _postdec(name, local={}):",
                "    if name in local:",
                "        local[name] -= 1",
                "        return local[name] + 1",
                "    globals()[name] -= 1",
                "    return globals()[name] + 1"
            }
        };

        // prepare pyBuilder
        pyBuilder.setCursor(-1);

        // add required nFix methods
        for (int i = 0; i < 4; i++) {
            if (foundNfix[i]) {
                pyBuilder.addLines(nFixMethods[i]);
                pyBuilder.newLine();
            }
        }

        if (mainMethod == null) return;
        // add main method stub and invoker
        pyBuilder.setCursor(-1);
        pyBuilder.addLine("import sys");
        pyBuilder.newLine(1);
        pyBuilder.setCursor(pyBuilder.size()-1);
        options.addGlobal("translateMain");
        translate(mainMethod);
        // generate invoker
        pyBuilder.addLine("if __name__ == '__main__':");
        pyBuilder.addLine("    main(sys.argv)");
        options.remove("translateMain");
    }

    void finalize(ASTNode root, ArrayList<Comment> comments) {
        translate(root);
        finalize(comments);
    }

    void finalize(ASTNode root) {
        translate(root);
        finalize(new ArrayList<Comment>());
    }

    void translate(ASTNode root)
    {
        if (!debug && errorOccurred) return;

        int lineTicket = -1;
        int lastLine = 0;

        ArrayList<ASTNode> children = null;

        Stack<ASTNode> nodeStack = new Stack<ASTNode>();
        nodeStack.push(root);

        while ((debug || !errorOccurred) && nodeStack.size() > 0) {
            switch(nodeStack.peek().getType()) {
                case "block statement":
                pyBuilder.addSourceLine(nodeStack.peek().getLine());

                if (options.containsCurrent("addBreakCondition")) {
                    pyBuilder.append("if not _sw_break:");
                    pyBuilder.newLine();
                    pyBuilder.increaseIndent();
                    options.addGlobal("breakIndent");
                    options.clearCurrent("addBreakCondition");
                }

                translate(nodeStack.pop().getChildren().get(0));
                if (!pyBuilder.getLine().equals("")) pyBuilder.newLine();
                break;

                case "local variable declaration":
                children = nodeStack.pop().getChildren();
                translate(children.get(1));
                // should encounter a variable declarator, which would then insert
                // a new line
                break;

                case "variable declarator":
                case "assignment":
                children = nodeStack.pop().getChildren();

                if (children.size() > 2){
                    translate(children.get(0));
                    pyBuilder.append(" ");
                    pyBuilder.append(children.get(1).getValue() + " ");
                    translate(children.get(2));
                    pyBuilder.newLine();
                } else if (children.get(0).getType() == "array access") {
                    translate(children.get(0));
                }

                break;

                case "array creation expression":
                children = nodeStack.pop().getChildren();
                translate(children.get(1));
                break;

                case "dim expressions":
                children = nodeStack.pop().getChildren();
                for (ASTNode child : children) pyBuilder.append("[");
                pyBuilder.append("None]");

                Collections.reverse(children);

                if (children.size() > 1) {
                    boolean isMultiplier = true; // otherwise, range
                    for (ASTNode child : children.subList(0, children.size())) {
                        if (isMultiplier) {
                            pyBuilder.append(" * ");
                            translate(child);
                            isMultiplier = false;
                        } else {
                            pyBuilder.append(" for i in range(");
                            translate(child);
                            pyBuilder.append(")]");
                        }
                    }
                } else {
                    pyBuilder.append(" * ");
                    translate(children.get(children.size()-1));
                }

                break;

                case "array access":
                children = nodeStack.pop().getChildren();
                ASTNode lastChild = children.get(children.size()-1);

                boolean lenMethod =
                    (lastChild.getType().equals("field access")
                    &&  lastChild.getValue().equals("length")); 

                if (lenMethod) pyBuilder.append("len(");

                translate(children.get(0));
                pyBuilder.append("[");
                translate(children.get(1));
                pyBuilder.append("]");

                int endIndex = children.size() + ((lenMethod) ? -1 : 0);
                for (ASTNode child : children.subList(2, endIndex)) {
                    if (child.getType().equals("expression")) {
                        pyBuilder.append("[");
                        translate(child);
                        pyBuilder.append("]");
                    } else {
                        pyBuilder.append(".");
                        translate(child);
                    }
                }

                if (lenMethod) pyBuilder.append(")");
                break;

                case "parenthesized expression":
                pyBuilder.append("(");
                lineTicket = pyBuilder.tabLine();
                translate(nodeStack.pop().getChildren().get(0));
                pyBuilder.setCursor(pyBuilder.getLineTab(lineTicket));
                pyBuilder.append(")");
                break;

                /** Class Declaration Methods
                 * TODO:
                 *  - refer to fields internally with 'self' keyword
                 *  - resolve scoping conflict between method parameters and fields of the same name
                 */
                case "class declaration":
                children = nodeStack.pop().getChildren();
                String cName = children.get(1).getValue();
                classNames.push(cName);
                pyBuilder.append("class ");

                if (!(children.get(0).childCount() > 0) || !children.get(0).getChildren().get(0).getType().equals("public_kw")) {
                    // prepend the identifier of the class with an underscore,
                    // which doesn't mean anything to the python interpreter but
                    // is good naming convention nonetheless
                    //pyBuilder.append("_");
                }
                
                pyBuilder.append(cName + ":");
                pyBuilder.newLine();
                pyBuilder.increaseIndent();

                if (!parser.classHasMethod(cName, cName)) {
                    pyBuilder.append("def __init__(self):");
                    pyBuilder.newLine();
                    pyBuilder.increaseIndent();
                    pyBuilder.append("pass");
                    pyBuilder.newLine();
                    pyBuilder.decreaseIndent();
                }

                classNames.pop();
                translate(children.get(2));
                pyBuilder.decreaseIndent();
                break;

                case "field declaration":
                children = nodeStack.pop().getChildren();

                if (!(children.get(0).childCount() > 0) || !children.get(0).getChildren().get(0).getType().equals("public_kw")) {
                    // prepend the identifier of the field with an underscore,
                    // which doesn't mean anything to the python interpreter but
                    // is good naming convention nonetheless
                    //pyBuilder.append("_");
                }

                translate(children.get(2));
                if (pyBuilder.getLine().equals("_")) pyBuilder.backspace();
                break;

                case "method declaration":
                if (options.contains("translateMain") || !nodeStack.peek().isMainMethod()) {
                    children = nodeStack.pop().getChildren();
                    pyBuilder.append("def ");
                    if (options.contains("translateMain")) pyBuilder.append("main(args)");
                    else translate(children.get(0));
                    pyBuilder.append(":");
                    pyBuilder.newLine();
                    pyBuilder.increaseIndent();
                    translate(children.get(1));
                    pyBuilder.decreaseIndent();
                } else if (mainMethod == null) {
                    mainMethod = nodeStack.pop();
                } else error("translate", "Multiple main methods defined.");
                break;

                case "method header":
                children = nodeStack.pop().getChildren();
                if (!(children.get(0).childCount() > 0) || !children.get(0).getChildren().get(0).getType().equals("public_kw")) {
                    // prepend the identifier of the method with an underscore,
                    // which doesn't mean anything to the python interpreter but
                    // is good naming convention nonetheless
                    //pyBuilder.append("_");
                }
                
                translate(children.get(2));
                break;

                case "method declarator":
                children = nodeStack.pop().getChildren();
                pyBuilder.append(children.get(0).getValue() + "(self");

                for (ASTNode fp : children.get(1).getChildren()) {
                    pyBuilder.append(", " + fp.getChildren().get(1).getValue());
                }

                pyBuilder.append(")");
                break;

                case "constructor declaration":
                children = nodeStack.pop().getChildren();
                pyBuilder.append("def ");
                translate(children.get(1));
                pyBuilder.append(":");
                pyBuilder.newLine();
                pyBuilder.increaseIndent();
                translate(children.get(2));
                pyBuilder.decreaseIndent();
                break;

                case "constructor declarator":
                children = nodeStack.pop().getChildren();
                pyBuilder.append("__init__(self");

                for (ASTNode fp : children.get(1).getChildren()) {
                    pyBuilder.append(", " + fp.getChildren().get(1).getValue());
                }

                pyBuilder.append(")");
                break;

                case "explicit constructor invocation":
                children = nodeStack.pop().getChildren();
                pyBuilder.append("super().__init__(");

                if (children.get(1).childCount() > 0) {
                    for (ASTNode arg : children.get(1).getChildren()) {
                        pyBuilder.append(arg.getValue() + ", ");
                    }

                    pyBuilder.backspace(2);
                }

                pyBuilder.append(")");
                pyBuilder.newLine();
                break;

                /** END of Class Declaration Methods */

                /** Switch Statement Methods
                 * TODO:
                 *  - remove trailing _sw_cond toggle after a lone break statement
                */
                case "switch statement":
                // NOTE: still technically unfinished as no tests have been done.
                // notImplemented("translate", "switch statement");

                options.increaseScope();
                options.addStack("inSwitch");
                children = nodeStack.pop().getChildren();
                String ident = children.get(0).getChild("identifier").getValue();

                // TODO: check if identifier was previously defined and rename accordingly
                switchCmp.push("_sw_" + ident); // add identifier to be tested against the stack

                pyBuilder.append("# switch statement for ");
                translate(children.get(0));
                pyBuilder.append(" (generated by Java2Python)");
                pyBuilder.addLine(switchCmp.peek() + " = ");
                translate(children.get(0));
                pyBuilder.addLine("_sw_dflt = True    # condition for default");
                pyBuilder.addLine("_sw_cond = False   # condition stating previous condition passed");
                pyBuilder.newLine();
                translate(children.get(1));
                pyBuilder.append("_sw_dflt = False");
                pyBuilder.addLine("_sw_cond = False");
                pyBuilder.addLine("_sw_break = False");
                pyBuilder.addLine("# end of switch statement for ");
                translate(children.get(0));

                options.removeStack("inSwitch");
                options.decreaseScope();
                switchCmp.pop(); // remove ASTNode from the stack
                break;

                case "switch block statement group":
                children = nodeStack.pop().getChildren();
                options.increaseScope();
                pyBuilder.append("if _sw_cond or ");

                if (children.get(0).getChildren().get(0).childCount() > 0) {
                    if (children.get(0).childCount() > 1) {
                        pyBuilder.append(switchCmp.peek() + " in {");
                        translate(children.get(0));
                        pyBuilder.append("}");
                    } else {
                        pyBuilder.append(switchCmp.peek() + " == ");
                        translate(children.get(0));
                    }
                } else {
                    // is default case
                    pyBuilder.append("_sw_dflt");
                }

                pyBuilder.append(":");
                pyBuilder.newLine();
                pyBuilder.increaseIndent();
                pyBuilder.append("_sw_dflt = False");
                pyBuilder.addLine("_sw_cond = False");
                pyBuilder.addLine("_sw_break = False");
                pyBuilder.newLine();
                translate(children.get(1));

                pyBuilder.append("_sw_cond = ");
                if (options.containsCurrent("addBreakCondition")) {
                    pyBuilder.append("not _sw_break");
                    options.clearCurrent("addBreakCondition");
                } else pyBuilder.append("True");

                pyBuilder.newLine();
                pyBuilder.decreaseIndent(1 + options.getGlobal("breakIndent"));
                options.clear("breakIndent");
                options.decreaseScope();
                break;

                case "switch labels":
                children = nodeStack.pop().getChildren();

                if (children.size() > 0) {
                    for (ASTNode child : children) {
                        translate(child);
                        pyBuilder.append(", ");
                    }

                    pyBuilder.backspace(2);
                }

                break;
                /** END of Switch Statement Methods */

                case "conditional expression":
                options.add("inConditional");
                boolean stringUpcast = nodeStack.peek().contains("string_lt");
                boolean exempt = false;     // exempt from upcasting (is string or operator)?
                boolean isPostfix = false;  // is this a postfix expression?
                children = nodeStack.pop().getChildren();

                for (int i = 0; i < children.size(); i++) {
                    ASTNode child = children.get(i);
                    ASTNode pChild = null;

                    if (!child.getType().matches("postfix expr(.*)")) {
                        exempt = (child.getType().equals("string_lt"))
                              || (child.getType().matches("(.*)expression"))
                              && (!child.getType().matches("parenthesized(.*)"))
                              && (!child.getType().matches("prefix expr(.*)"));
                        
                        isPostfix = (i + 1 < children.size())
                                 && (children.get(i+1).getType().matches("postfix expr(.*)"));

                        if (isPostfix) {
                            pChild = children.get(i+1);
                        }

                        if (stringUpcast && !exempt)
                            pyBuilder.append("str(");

                        if (child.getType().matches("prefix expr(.*)")) {
                            if (child.getChildren().get(0).getType().equals("++_op")) {
                                foundNfix[0] = true; // found prefix increment
                                pyBuilder.append("_preinc('");
                            } else {
                                foundNfix[1] = true; // found prefix decrement
                                pyBuilder.append("_predec('");
                            }

                            translate(children.get(++i));
                            pyBuilder.append("')");
                        } else if (isPostfix) {
                            if (pChild.getChildren().get(0).getType().equals("++_op")) {
                                foundNfix[2] = true; // found postfix increment
                                pyBuilder.append("_postinc('");
                            } else {
                                foundNfix[3] = true; // found postfix decrement
                                pyBuilder.append("_postdec('");
                            }

                            translate(child);
                            pyBuilder.append("')");
                        } else translate(child);

                        if (stringUpcast && !exempt) pyBuilder.append(")");
                        // don't add a space if current token is a unary expression
                        if (!child.getType().equals("unary expression")) pyBuilder.append(" ");
                    }
                }

                // remove trailing space...
                pyBuilder.backspace();
                options.remove("inConditional");
                break;

                case "method invocation":
                children = nodeStack.pop().getChildren();
                String methodName = children.get(0).getValue().replaceFirst("^this.", "self.");
                if (!classNames.empty() && parser.classHasMethod(classNames.peek(), methodName)) pyBuilder.append("self.");
                translate(children.get(0));
                pyBuilder.append("(");
                translate(children.get(1));

                if (debug) System.out.println("[" + methodName + "]");
                if (methodName.equals("System.out.print")) {
                    pyBuilder.append(", end=\"\"");
                }

                pyBuilder.append(")");

                for (ASTNode child : children.subList(2, children.size())) {
                    if (!child.getType().equals("array access")) pyBuilder.append(".");
                    translate(child);
                }

                break;

                case "class instance creation expression":
                children = nodeStack.pop().getChildren();
                String className = children.get(0).getValue();
                translate(children.get(0));
                pyBuilder.append("(");
                translate(children.get(1));
                pyBuilder.append(")");
                break;
                
                case "argument list":
                children = nodeStack.pop().getChildren();

                if (children.size() > 0) {
                    for (ASTNode node : children) {
                        translate(node);
                        pyBuilder.append(", ");
                    }

                    pyBuilder.backspace(2);
                }

                break;

                case "array initializer":
                children = nodeStack.pop().getChildren();
                pyBuilder.append("[");

                for (ASTNode child : children) {
                    translate(child);
                    pyBuilder.append(", ");
                }

                pyBuilder.backspace(2);
                pyBuilder.append("]");
                break;

                case "if statement":
                children = nodeStack.pop().getChildren();
                options.increaseScope();
                options.add("inIf");

                if (!options.contains("switchBreak")) {
                    pyBuilder.append("if ");
                } else pyBuilder.append("if not _sw_break and ");

                translate(children.get(0));
                pyBuilder.append(":");

                pyBuilder.newLine();
                pyBuilder.increaseIndent();
                translate(children.get(1));
                pyBuilder.decreaseIndent(1 + options.getGlobal("breakIndent"));
                options.clear("breakIndent");

                if (children.size() > 2) {
                    for (ASTNode els : children.subList(2, children.size())) {
                        translate(els);
                    }
                }

                options.remove("inIf");
                options.decreaseScope();
                break;

                case "else if statement":
                children = nodeStack.pop().getChildren();
                options.clearCurrent("addBreakCondition");

                pyBuilder.append("elif ");
                translate(children.get(0));
                pyBuilder.append(":");

                pyBuilder.newLine();
                pyBuilder.increaseIndent();
                translate(children.get(1));
                pyBuilder.decreaseIndent(1 + options.getGlobal("breakIndent"));
                options.clear("breakIndent");

                if (children.size() > 2) translate(children.get(2));
                break;
                
                case "else statement":
                children = nodeStack.pop().getChildren();
                options.clearCurrent("addBreakCondition");

                pyBuilder.append("else:");
                pyBuilder.newLine();
                pyBuilder.increaseIndent();
                translate(children.get(0));
                pyBuilder.decreaseIndent(1 + options.getGlobal("breakIndent"));
                options.clear("breakIndent");
                break;

                /* START of loop cases */

                case "while statement":
                children = nodeStack.pop().getChildren();
                options.increaseScope();
                options.addStack("inLoop");

                pyBuilder.append("while ");
                translate(children.get(0));
                pyBuilder.append(":");
                pyBuilder.newLine();

                pyBuilder.increaseIndent();
                translate(children.get(1));
                pyBuilder.decreaseIndent();
                options.removeStack("inLoop");
                options.decreaseScope();
                break;

                case "do statement":
                children = nodeStack.pop().getChildren();
                options.increaseScope();
                options.addStack("inLoop");

                pyBuilder.append("while True:");
                pyBuilder.newLine();
                pyBuilder.increaseIndent();
                translate(children.get(0));
                pyBuilder.append("if not (");
                translate(children.get(1));
                pyBuilder.append("): break");
                pyBuilder.addLine();
                pyBuilder.decreaseIndent();

                options.removeStack("inLoop");
                options.decreaseScope();
                break;

                case "for statement":
                // it would be nice to use python for-statements eventually,
                // though this is much simpler
                children = nodeStack.pop().getChildren();
                options.increaseScope();
                options.addStack("inLoop");

                translate(children.get(0));
                if (!pyBuilder.getLine().equals("")) pyBuilder.newLine();
                pyBuilder.append("while ");
                translate(children.get(1));
                pyBuilder.append(":");
                pyBuilder.newLine();
                pyBuilder.increaseIndent();
                translate(children.get(3));
                translate(children.get(2));
                pyBuilder.newLine();
                pyBuilder.decreaseIndent();

                options.removeStack("inLoop");
                options.decreaseScope();
                break;

                case "foreach statement":
                children = nodeStack.pop().getChildren();
                options.increaseScope();
                options.addStack("inLoop");

                pyBuilder.append("for ");
                translate(children.get(0));
                pyBuilder.append(" in ");
                translate(children.get(1));
                pyBuilder.append(":");
                pyBuilder.newLine();
                pyBuilder.increaseIndent();
                translate(children.get(2));
                pyBuilder.decreaseIndent();

                options.removeStack("inLoop");
                options.decreaseScope();
                break;

                /* END of loop cases */

                case "try statement":
                children = nodeStack.pop().getChildren();
                pyBuilder.append("try:");
                pyBuilder.newLine();
                pyBuilder.increaseIndent();
                translate(children.get(0));
                pyBuilder.decreaseIndent();

                for (ASTNode child : children.subList(1, children.size())) {
                    translate(child);
                }

                break;

                case "catch clause":
                children = nodeStack.pop().getChildren();
                pyBuilder.append("except:");
                pyBuilder.newLine();
                pyBuilder.increaseIndent();
                translate(children.get(1));
                pyBuilder.decreaseIndent();
                break;

                case "try finally":
                pyBuilder.append("finally:");
                pyBuilder.newLine();
                pyBuilder.increaseIndent();
                translate(nodeStack.pop().getChildren().get(0));
                pyBuilder.decreaseIndent();
                break;

                case "return statement":
                case "continue statement":
                case "throws statement":
                String kw = "";
                if(nodeStack.peek().getType() == "throws statement"){
                    kw = "raise"; //throws is only one that needs to be translated
                }else{
                    kw = nodeStack.peek().getValue();
                }
                pyBuilder.append(kw + " ");
                if(nodeStack.peek().childCount()>0){
                    children = nodeStack.pop().getChildren();
                    translate(children.get(0));
                }else{
                    nodeStack.pop();
                }
                break;

                case "prefix expression":
                children = nodeStack.pop().getChildren();

                if (options.contains("inConditional")) {
                    if (children.get(0).getType().equals("++_op")) {
                        foundNfix[0] = true; // found a preincrement expression requiring a function
                        pyBuilder.append("_preinc(");
                    } else {
                        foundNfix[1] = true; // found a predecrement expression requiring a function
                        pyBuilder.append("_predec(");
                    }

                    translate(children.get(1));
                    pyBuilder.append(")");
                } else {
                    translate(children.get(1));
                    pyBuilder.append(
                            ((children.get(0).getType().equals("++_op"))
                            ? " += " : " -= ") + "1");
                }

                break;

                case "postfix expression":
                children = nodeStack.pop().getChildren();

                if (options.contains("inConditional")) {
                    if (children.get(1).getType().equals("++_op")) {
                        foundNfix[2] = true; // found a postincrement expression requiring a function
                        pyBuilder.append("_postinc(");
                    } else {
                        foundNfix[3] = true; // found a postdecrement expression requiring a function
                        pyBuilder.append("_postdec(");
                    }

                    translate(children.get(0));
                    pyBuilder.append(")");
                } else {
                    translate(children.get(0));
                    pyBuilder.append(
                            ((children.get(1).getType().equals("++_op"))
                            ? " += " : " -= ") + "1");
                }

                break;

                case "field access":
                ArrayList<String> field = new ArrayList<String>(Arrays.asList(nodeStack.pop().getValue().split("\\.")));
                if (debug) System.out.println("Field: " + field);

                boolean firstElement = true;
                if (field.size() > 1) {
                    for (String idnt : field.subList(0, field.size()-2)) {
                        if (firstElement && idnt.equals("this")) idnt = "self";
                        pyBuilder.append(idnt + ".");
                        firstElement = false;
                    }

                    if (field.get(field.size()-1).equals("length")) {
                        pyBuilder.append("len(" + field.get(field.size()-2) + ")");
                    } else pyBuilder.append(field.get(field.size()-2) + "." + field.get(field.size()-1));
                } else if (field.size() > 0) pyBuilder.append(field.get(0).replace("this", "self"));

                break;


                case "break statement":
                if (options.peek().equals("inSwitch")) {
                    if (!options.contains("switchBreak")) options.add("switchBreak");
                    options.add("addBreakCondition");       // add break condition to current scope
                    options.add("addBreakCondition", -1);   // add break condition to previous scope
                    pyBuilder.append("_sw_break = True");
                    nodeStack.pop();
                    break;
                } else if (!options.peek().equals("inLoop")) {
                    error("translate", "Break statement not inside loop or switch.");
                }

                default:
                if (nodeStack.peek().getValue() != null && !nodeStack.peek().getValue().equals("")) {
                    pyBuilder.append(remap(nodeStack.pop().getValue().replaceFirst("^this.", "self.")));
                } else nodeStack = expandStack(nodeStack);
            }
        }

        if (lineTicket != -1) {
            pyBuilder.destroyLineTab(lineTicket);
        }
    }
}