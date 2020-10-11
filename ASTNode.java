import java.util.ArrayList;

class ASTNode {
    private ASTNode parent;
    private ArrayList<ASTNode> children;
    private String type;
    private String value;
     
    ASTNode(String type, String value) 
    {
        this.type = type;
        this.value = value;
        children = new ArrayList<ASTNode>();
    }
    // add child node
    void addChild(ASTNode child) 
    {
        if(child != null)
        {
            child.setParent(this);
            children.add(child);
        }
    }
    // get child nodes
    ArrayList<ASTNode> getChildren() { return children; }
    // set parent node
    void setParent(ASTNode parent) {this.parent = parent; }
    // get parent node
    ASTNode getParent() { return parent; }
    
    
}