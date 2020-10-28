import java.io.IOException;

import java.util.Stack;
import java.util.ArrayList;

public class Translator {
    private Stack<ASTNode> nodeStack;

    // expandStack()
    // - Expands the top node of the stack.
    void expandStack() {
        if (nodeStack.size() > 0) {
            expandNode(nodeStack.pop());
        }
    }

    // expandNode()
    // - Expands given node by adding children back into the stack
    void expandNode(ASTNode node)
    {
        if (node.childCount() > 0) {
            ArrayList<ASTNode> children = node.getChildren();

            for (int i = children.size()-1; i > -1; i--) {
                nodeStack.push(children.get(i));
            }
        }
    }

    /** START OF TRANSLATOR HANDLERS
     * 
     *  of the template:
     *      ArrayList<String> <nodeType>(ASTNode node)
     */

    ArrayList<String> assignmentExpression(ASTNode node) {
        ArrayList<String> strList = new ArrayList<String>();

        ArrayList<ASTNode> aE = node.getChildren();
        switch(aE.get(0).getType()) {
            case "conditional expression":
            strList.add(aE.get(0).getChildren().get(0).getValue());
            break;

            default:
        }

        return strList;
    }

    ArrayList<String> expression(ASTNode node) {
        ArrayList<String> strList = new ArrayList<String>();

        ArrayList<ASTNode> e = node.getChildren();
        switch (e.get(0).getType()) {
            case "assignment expression":
            strList = assignmentExpression(e.get(0));
            break;

            default:
        }

        return strList;
    }

    ArrayList<String> localVariableDeclaration(ASTNode node) {
        ArrayList<String> strList = new ArrayList<String>();

        ArrayList<ASTNode> lVD = node.getChildren();
        // get children of VD node
        ArrayList<ASTNode> vD = lVD.get(1).getChild("variable declarator").getChildren();

        // append identifier
        strList.add(vD.get(0).getValue());

        // if the size of the children of VD is larger than 2, we assume variable is being initialized
        if (vD.size() > 2) {
            strList.set(0, strList.get(0) + " = " + expression(vD.get(2).getChildren().get(0)).get(0));
        }

        return strList;
    }

    /** END OF TRANSLATOR HANDLERS */

    void translate(ASTNode root)
    {
        ArrayList<String> strList = new ArrayList<String>();

        nodeStack = new Stack<ASTNode>();
        nodeStack.push(root);

        while (nodeStack.size() > 0) {
            switch(nodeStack.peek().getType()) {
                case "local variable declaration":
                strList.add(localVariableDeclaration(nodeStack.pop()).get(0));
                break;

                default:
                expandStack();
            }
        }

        for (String e : strList) {
            System.out.println(e);
        }
    }
}