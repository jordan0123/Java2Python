import java.io.IOException;

import java.util.Set;
import java.util.Stack;
import java.util.ArrayList;

public class Translator {
    // private PythonBuilder pBuilder;

    // private Stack<ASTNode> nodeStack;
    private Set<String> idList;

    private boolean debug = true;
    private boolean crashOnError = true;

    private boolean errorOccured = false;

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

    // expandNode()
    // - Expands given node by adding children back into the stack
/*
    void expandNode(ASTNode node)
    {
        if (node.childCount() > 0) {
            ArrayList<ASTNode> children = node.getChildren();

            for (int i = children.size()-1; i > -1; i--) {
                nodeStack.push(children.get(i));
            }
        }
    }
*/

    /** START OF TRANSLATOR HANDLERS
     * 
     *  of the template:
     *      ArrayList<String> <nodeType>(ASTNode node)
     */

/*
    ArrayList<String> conditionalExpression(ASTNode node, ArrayList<String> lines) {
        return lines;
    }

    ArrayList<String> assignmentExpression(ASTNode node, ArrayList<String> lines) {
        ASTNode child = node.getChildren().get(0);

        switch(child.getType()) {
            case "conditional expression":
            lines = conditionalExpression(child, lines);
            break;

            default:
            notImplemented("assignmentExpression", child.getType());
        }

        return lines;
    }

    ArrayList<String> localVariableDeclaration(ASTNode node, ArrayList<String> lines) {
        // get children of variable declarator
        ArrayList<ASTNode> vd = node.getChild("variable declarator").getChildren();

        if (vd.size() > 1) {
            lines.add(vd.get(0) + " = " + assignmentExpression(vd.get(2)));
        }

        return lines;
    }
*/

    /** END OF TRANSLATOR HANDLERS */

    PythonBuilder translate(ASTNode root)
    {
        PythonBuilder  pBuilder = new PythonBuilder();
        PythonBuilder tpBuilder = null; // temporary PythonBuilder

        ArrayList<ASTNode> children = null;

        Stack<ASTNode> nodeStack = new Stack<ASTNode>();
        nodeStack.push(root);

        nodeStack = expandStack(nodeStack);

        while (nodeStack.size() > 0) {
            tpBuilder = null;

            switch(nodeStack.peek().getType()) {
                case "variable declarator":
                case "assignment":
                children = nodeStack.pop().getChildren();

                if (children.size() > 2){
                    pBuilder.append(children.get(0).getValue() + " = ");
                    tpBuilder = translate(children.get(2));

                    pBuilder.append(tpBuilder.getCurrent());
                    pBuilder.addLines(tpBuilder);
                    pBuilder.addCurrent();
                }

                break;

                case "parenthesized expression":
                pBuilder.append("(");
                tpBuilder = translate(nodeStack.pop().getChildren().get(0));
                pBuilder.append(tpBuilder.getCurrent());
                pBuilder.append(")");
                pBuilder.addLines(tpBuilder);
                break;

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

                default:
                nodeStack = expandStack(nodeStack);
            }
        }

        return pBuilder;
    }
}