public class BasicClass {

private int radius = 2; // radius of the Class
private int diameter; // the diameter to be calculated
int noModifier;

    public BasicClass foo(BasicClass b, int j){
        int k = i;
        System.out.println("This is k " + k);
    }
    
    public BasicClass(){
        super();
        this.radius = 3;
        System.out.println("Initialized!");
    }
    
    public static void main(String[] args){
        System.out.println('Hello, World!');
        BasicClass bc = new BasicClass();
        bc.foo(new BasicClass(), 7);
    }
}

