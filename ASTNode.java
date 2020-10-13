import java.util.ArrayList;
import java.util.Random;

class ASTNode {
    private ASTNode parent;
    private ArrayList<ASTNode> children;
    private String type;
    private String value;
    private String key;
    private int depth;
    
    ASTNode(String type, String value) 
    {
        this.type = type;
        this.value = value;
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
    
    void print(){
        for(int i = 0; i < this.depth; i++ ){
            System.out.print(" ");
        }
        if(this.value != null){
            System.out.println(this.value + "    <" + this.type + ">");
        }
        else{
            System.out.println("<" + this.type + ">");
        }
    }
    
}