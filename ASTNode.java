import java.util.ArrayList;
import java.util.Random;

class ASTNode {
    private ASTNode parent;
    private ArrayList<ASTNode> children;
    private String type;
    private String value;
    private String key;
    private int line;
    private int depth;
    
    ASTNode(String type, String value, int line) 
    {
        this.type = type;
        this.value = value;
        this.line = line;
        this.key = null;
        this.depth = -1;
        children = new ArrayList<ASTNode>();
        genKey();
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

    // get next node with more than one or zero children
    ASTNode getNextLeafOrBranch() {
        return getNextLeafOrBranch(0, children.size());
    }

    ASTNode getNextLeafOrBranch(int fIndex) {
        return getNextLeafOrBranch(fIndex, children.size());
    }

    ASTNode getNextLeafOrBranch(int fIndex, int eIndex) {
        ASTNode tempNode = null;

        for (ASTNode node : children.subList(fIndex, eIndex)) {
            if (node.childCount() == 0 || node.childCount() > 1) {
                return node;
            } else {
                tempNode = node.getNextLeafOrBranch();
                if (tempNode != null) return tempNode;
            }
        }

        return null;
    }

    ASTNode getChild(String type) {
        ASTNode tempNode = null;

        for (ASTNode node : children) {
            if (node.getType() == type) {
                return node;
            } else {
                tempNode = node.getChild(type);
                if (tempNode != null) return tempNode;
            }
        }

        return null;
    }

    // returns true if node contains a child of type
    // 'type'
    boolean contains(String type) {
        for (ASTNode child : children) {
            if (child.getType().equals(type)) return true;
        }

        return false;
    }

    // set parent node
    void setParent(ASTNode parent) {this.parent = parent; }
    // get parent node
    ASTNode getParent() { return parent; }
    
    
    //generate key (for identifying objects in a list/maintaining uniqueness)
    void genKey(){
        Random rand = new Random();
        int key_length = 16;
        String key_alpha = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(key_length);
        for(int i = 0; i < key_length; i++){
            int j = rand.nextInt(key_alpha.length());
            char c = key_alpha.charAt(j);
            sb.append(c);
        }
        this.key = sb.toString();
    }

    String getKey(){
        return this.key;
    }

    //get the number of child nodes
    int childCount(){
        return this.children.size();
    }
    
    // for printing depth
    void setDepth(int n){
        this.depth = n;
    }

    int getDepth(){
        return this.depth;
    }
    
    String getType(){
        return this.type;
    }

    String getValue(){
        if (this.value == null) return "";
        else return this.value;
    }
    
    int getLine(){
        return this.line;
    }

    boolean isMainMethod() {
        ArrayList<ASTNode> children = this.getChild("modifiers").getChildren();
        if (children.size() != 2 || !children.get(0).getType().equals("public_kw") || !children.get(1).getType().equals("static_kw")) return false;
        children = this.getChild("method header").getChildren();
        if (!children.get(1).getValue().equals("void_kw")) return false;
        children = this.getChild("method declarator").getChildren();
        if (!children.get(0).getValue().equals("main")) return false;

        return true;
    }

    // rebuilds tree underneath node to aide the translator
    // with more complex structures (nfix operators in control conditions)
    ASTNode rebuild() {
        ASTNode child = null;
        switch (this.type) {
            case "else if statement":
            child = children.get(0);
            break;

            default:
        }

        return this;
    }
    
    void print(){
        for(int i = 0; i < this.depth; i++ ){
            System.out.print(" ");
        }
        if(this.value != null){
            System.out.println(this.value + "    <" + this.type + ">: " + this.line);
        }
        else{
            System.out.println("<" + this.type + ">: " + this.line);
        }
    }
    
}