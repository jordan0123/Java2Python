public class BasicClass {

private int radius = 2; // radius of the Class
private int k = 3;
private int diameter; // the diameter to be calculated
int noModifier;

    public BasicClass foo(BasicClass b, int i){
        int k = i;
        System.out.println("This is k " + this.k);
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

