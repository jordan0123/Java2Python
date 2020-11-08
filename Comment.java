public class Comment{
    private int line;
    private String comment;
    
    Comment(String comment, int line){
        this.comment = comment;
        this.line = line;
    }
    
    int getLine(){
        return this.line;
    }
    
    String getComment(){
        return this.comment;
    }

}