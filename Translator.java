import java.io.IOException;

import java.util.Set;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Hashtable;

public class Translator {
    // private PythonBuilder pBuilder;

    // private Stack<ASTNode> nodeStack;
    private Set<String> idList;

    private boolean debug = true;
    private boolean crashOnError = true;

    private boolean errorOccured = false;

    Hashtable<String, String> litTable;

    Translator() {
        litTable = new Hashtable<String, String>();

        litTable.put("||", "or");
        litTable.put("&&", "and");
        litTable.put("!", "not");
        // litTable.put("System.out.print", "print");
        // litTable.put("System.out.println", "print");
    }

    void setDebug(boolean debug) {
        this.debug = debug;
    }

    void setCrashOnError(boolean crashOnError) {
        this.crashOnError = crashOnError;
    }

    void notImplemented(String callName, String nodeType) {
        errorOccured = true;

        if (crashOnError || debug) {
            System.out.println("Error::Translator::" + callName + ": encountered child type '" + nodeType + "' not implemented.");
        }

        if (crashOnError) System.exit(0);
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

    PythonBuilder translate(ASTNode root)
    {
        PythonBuilder  pBuilder = new PythonBuilder();
        PythonBuilder tpBuilder = null; // temporary PythonBuilder

        ArrayList<ASTNode> children = null;

        Stack<ASTNode> nodeStack = new Stack<ASTNode>();
        nodeStack.push(root);

        // nodeStack = expandStack(nodeStack);

        while (nodeStack.size() > 0) {
            tpBuilder = null;

            switch(nodeStack.peek().getType()) {
                case "variable declarator":
                pBuilder.clearCurrent(); // TODO: cleaner solution for ignoring type tokens
                case "assignment":
                children = nodeStack.pop().getChildren();

                if (children.size() > 2){
                    pBuilder.append(children.get(0).getValue() + " = ");
                    tpBuilder = translate(children.get(2));

                    pBuilder.append(tpBuilder.getCurrent());
                    pBuilder.addLines(tpBuilder);
                    pBuilder.addCurrent(); // commit changes being made to source
                }

                break;

                case "parenthesized expression":
                pBuilder.append("(");
                tpBuilder = translate(nodeStack.pop().getChildren().get(0));
                pBuilder.append(tpBuilder.getCurrent());
                pBuilder.append(")");
                pBuilder.addLines(tpBuilder);
                break;

                case "conditional expression":
                children = nodeStack.pop().getChildren();
                for (ASTNode child : children) {
                    if (child.getValue() != null && child.getValue() != "")
                        pBuilder.append(child.getValue() + ' ');
                    else {
                        tpBuilder = translate(child);
                        pBuilder.append(tpBuilder.getCurrent() + ' ');
                        pBuilder.addLines(tpBuilder);
                    }
                }

                //pBuilder.addCurrent();
                break;

                case "while statement":
                children = nodeStack.pop().getChildren();
                pBuilder.clearCurrent();

                pBuilder.append("while ");
                tpBuilder = translate(children.get(0));
                pBuilder.append(tpBuilder.getCurrent() + ':');
                pBuilder.addLines(tpBuilder);
                pBuilder.addCurrent();

                pBuilder.increaseIndent();
                tpBuilder = translate(children.get(1));
                pBuilder.addLines(tpBuilder);
                pBuilder.decreaseIndent();
                break;

                case "if statement":
                children = nodeStack.pop().getChildren();
                pBuilder.clearCurrent();

                pBuilder.append("if ");
                tpBuilder = translate(children.get(0));
                pBuilder.append(tpBuilder.getCurrent() + ':');
                pBuilder.addLines(tpBuilder);
                pBuilder.addCurrent();

                pBuilder.increaseIndent();
                tpBuilder = translate(children.get(1));
                pBuilder.addLines(tpBuilder);
                pBuilder.decreaseIndent();

                if (children.size() > 2) {
                    for (ASTNode els : children.subList(2, children.size())) {
                        tpBuilder = translate(els);
                        pBuilder.addLines(tpBuilder);
                    }
                }

                break;

                case "else if statement":
                children = nodeStack.pop().getChildren();
                pBuilder.clearCurrent();

                pBuilder.append("elif ");
                tpBuilder = translate(children.get(0));
                pBuilder.append(tpBuilder.getCurrent() + ':');
                pBuilder.addLines(tpBuilder);
                pBuilder.addCurrent();

                pBuilder.increaseIndent();
                tpBuilder = translate(children.get(1));
                pBuilder.addLines(tpBuilder);
                pBuilder.decreaseIndent();
                break;
                
                case "else statement":
                children = nodeStack.pop().getChildren();
                pBuilder.clearCurrent();

                pBuilder.append("else:");
                pBuilder.addCurrent();

                pBuilder.increaseIndent();
                tpBuilder = translate(children.get(0));
                pBuilder.addLines(tpBuilder);
                pBuilder.decreaseIndent();
                break;

                case "do statement":
                children = nodeStack.pop().getChildren();
                pBuilder.clearCurrent();

                pBuilder.append("while True:");
                pBuilder.addCurrent();

                pBuilder.increaseIndent();
                tpBuilder = translate(children.get(0));
                pBuilder.addLines(tpBuilder);
                tpBuilder = translate(children.get(1));
                pBuilder.append("if " + tpBuilder.getCurrent() + ": break");
                pBuilder.addCurrent();
                pBuilder.decreaseIndent();
                break;

                /* 
                case "identifier":
                case "string_lt":
                case "+_op":
                case "-_op":
                case "*_op":
                case "/_op":
                case "integer_lt":
                case "decimal_lt":
                pBuilder.append(nodeStack.pop().getValue());
                break;
                */

                default:
                if (nodeStack.peek().getValue() != null && nodeStack.peek().getValue() != "") {
                    pBuilder.append(remap(nodeStack.pop().getValue()));
                } else nodeStack = expandStack(nodeStack);
            }
        }

        return pBuilder;
    }
}