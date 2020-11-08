import java.io.IOException;

import java.util.Arrays;

import java.util.Set;
import java.util.HashSet;

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

    void translate(ASTNode root) {
        translate(root, new HashSet<String>());
    }

    void translate(ASTNode root, Set<String> options)
    {
        int lineTicket = -1;
        int lastLine = 0;

        ArrayList<ASTNode> children = null;

        Stack<ASTNode> nodeStack = new Stack<ASTNode>();
        nodeStack.push(root);

        while (nodeStack.size() > 0) {
            switch(nodeStack.peek().getType()) {
                case "block statement":
                translate(nodeStack.pop().getChildren().get(0));
                if (pyBuilder.getLine() != "") pyBuilder.newLine();
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
                pyBuilder.append("[None] * ");
                translate(children.get(1));
                break;

                case "array access":
                children = nodeStack.pop().getChildren();
                translate(children.get(0));
                pyBuilder.append("[");
                translate(children.get(1));
                pyBuilder.append("]");
                break;

                case "parenthesized expression":
                pyBuilder.append("(");
                lineTicket = pyBuilder.tabLine();
                translate(nodeStack.pop().getChildren().get(0));
                pyBuilder.setCursor(pyBuilder.getLineTab(lineTicket));
                pyBuilder.append(")");
                break;

                case "conditional expression":
                boolean stringUpcast = nodeStack.peek().contains("string_lt");
                boolean exempt = false; // exempt from upcasting (is string or operator)?
                children = nodeStack.pop().getChildren();

                for (ASTNode child : children) {
                    exempt = (child.getType().equals("string_lt"))
                          || (child.getType().matches("(.*)expression"))
                          && (!child.getType().matches("parenthesized(.*)"));

                    if (stringUpcast && !exempt)
                        pyBuilder.append("str(");
                    if (child.getValue() != null && !child.getValue().equals(""))
                        pyBuilder.append(child.getValue());
                    else translate(child);
                    if (stringUpcast && !exempt)
                        pyBuilder.append(")");

                    pyBuilder.append(" ");
                }

                // remove trailing space...
                pyBuilder.backspace();

                break;

                case "method invocation":
                children = nodeStack.pop().getChildren();
                String methodName = children.get(0).getValue();
                translate(children.get(0));
                pyBuilder.append("(");
                translate(children.get(1));

                if (debug) System.out.println("[" + methodName + "]");
                if (methodName.equals("System.out.print")) {
                    pyBuilder.append(", end=\"\"");
                }

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

                case "while statement":
                children = nodeStack.pop().getChildren();

                pyBuilder.append("while ");
                translate(children.get(0));
                pyBuilder.append(":");
                pyBuilder.newLine();

                pyBuilder.increaseIndent();
                translate(children.get(1));
                pyBuilder.decreaseIndent();
                break;

                case "if statement":
                children = nodeStack.pop().getChildren();

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

                if (children.size() > 2) translate(children.get(2));
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

                pyBuilder.append("while True:");
                pyBuilder.newLine();

                pyBuilder.increaseIndent();
                translate(children.get(0));
                pyBuilder.append("if ");
                translate(children.get(1));
                pyBuilder.append(": break");
                pyBuilder.addLine();
                pyBuilder.decreaseIndent();
                break;

                case "foreach statement":
                children = nodeStack.pop().getChildren();
                pyBuilder.append("for ");
                translate(children.get(0));
                pyBuilder.append(" in ");
                translate(children.get(1));
                pyBuilder.append(":");
                pyBuilder.newLine();
                pyBuilder.increaseIndent();
                translate(children.get(2));
                pyBuilder.decreaseIndent();
                break;

                case "for statement":
                // it would be nice to use python for-statements eventually,
                // though this is much simpler
                children = nodeStack.pop().getChildren();
                translate(children.get(0));
                if (pyBuilder.getLine() != "") pyBuilder.newLine();
                pyBuilder.append("while ");
                translate(children.get(1));
                pyBuilder.append(":");
                pyBuilder.newLine();
                pyBuilder.increaseIndent();
                translate(children.get(2));
                pyBuilder.newLine();
                translate(children.get(3));
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