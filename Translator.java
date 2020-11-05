import java.io.IOException;

import java.util.Set;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Hashtable;

public class Translator {
    private PythonBuilder pyBuilder;

    private Set<String> idList;

    private boolean debug = true;
    private boolean crashOnError = true;

    private boolean errorOccured = false;

    Hashtable<String, String> litTable;

    Translator() {
        pyBuilder = new PythonBuilder();
        litTable = new Hashtable<String, String>();

        litTable.put("||", "or");
        litTable.put("&&", "and");
        litTable.put("!", "not");
        litTable.put("true", "True");
        litTable.put("false", "False");
        litTable.put("System.out.print", "print");
        litTable.put("System.out.println", "print");
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

    String getSource() {
        return pyBuilder.getSource();
    }

    void translate(ASTNode root)
    {
        int lineTicket = -1;
        int lastLine = 0;

        ArrayList<ASTNode> children = null;

        Stack<ASTNode> nodeStack = new Stack<ASTNode>();
        nodeStack.push(root);

        while (nodeStack.size() > 0) {
            switch(nodeStack.peek().getType()) {
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
                    pyBuilder.append(children.get(0).getValue() + " = ");
                    translate(children.get(2));
                    pyBuilder.newLine();
                }

                break;

                case "parenthesized expression":
                pyBuilder.append("(");
                lineTicket = pyBuilder.tabLine();
                translate(nodeStack.pop().getChildren().get(0));
                pyBuilder.setCursor(pyBuilder.getLineTab(lineTicket));
                pyBuilder.append(")");
                break;

                case "conditional expression":
                children = nodeStack.pop().getChildren();

                for (ASTNode child : children) {
                    if (child.getValue() != null && child.getValue() != "")
                        pyBuilder.append(child.getValue() + ' ');
                    else {
                        translate(child);
                        pyBuilder.append(" ");
                    }
                }

                // remove trailing space...
                pyBuilder.backspace();

                break;

                case "while statement":
                children = nodeStack.pop().getChildren();
                pyBuilder.clearCurrent();

                pyBuilder.append("while ");
                translate(children.get(0));
                pyBuilder.append(":");
                pyBuilder.addCurrent();

                pyBuilder.increaseIndent();
                translate(children.get(1));
                pyBuilder.decreaseIndent();
                break;

                case "if statement":
                children = nodeStack.pop().getChildren();
                pyBuilder.clearCurrent();

                pyBuilder.append("if ");
                translate(children.get(0));
                pyBuilder.append(":");

                pyBuilder.newLine();
                pyBuilder.increaseIndent();
                translate(children.get(1));
                pyBuilder.decreaseIndent();

                if (children.size() > 2) {
                    for (ASTNode els : children.subList(2, children.size())) {
                        translate(els);
                    }
                }

                break;

                case "else if statement":
                children = nodeStack.pop().getChildren();

                pyBuilder.append("elif ");
                translate(children.get(0));
                pyBuilder.append(":");

                pyBuilder.newLine();
                pyBuilder.increaseIndent();
                translate(children.get(1));
                pyBuilder.decreaseIndent();

                if (children.size() > 2) {
                    translate(children.get(2));
                }

                break;
                
                case "else statement":
                children = nodeStack.pop().getChildren();
                pyBuilder.append("else:");

                pyBuilder.newLine();
                pyBuilder.increaseIndent();
                translate(children.get(0));
                pyBuilder.decreaseIndent();
                break;

                case "do statement":
                children = nodeStack.pop().getChildren();
                pyBuilder.clearCurrent();

                pyBuilder.append("while True:");
                pyBuilder.addCurrent();

                pyBuilder.increaseIndent();
                translate(children.get(0));
                pyBuilder.append("if ");
                translate(children.get(1));
                pyBuilder.append(": break");
                pyBuilder.addLine();
                pyBuilder.decreaseIndent();
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
                pyBuilder.append(nodeStack.pop().getValue());
                break;
                */

                default:
                if (nodeStack.peek().getValue() != null && nodeStack.peek().getValue() != "") {
                    pyBuilder.append(remap(nodeStack.pop().getValue()));
                } else nodeStack = expandStack(nodeStack);
            }
        }

        if (lineTicket != -1) {
            pyBuilder.destroyLineTab(lineTicket);
        }
    }
}