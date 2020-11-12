public class Circle {

private double pi = 3.14;

    public Circle(double diameter){
        this.diameter = diameter;
        this.radius = diameter / 2;
        System.out.println("The radius is " + this.radius);
    }
    
    double area(){
        return (pi * this.radius**2);
    }
    
    public static void main(String[] args){
        System.out.println('Hello, World!');
        Circle c = new Circle(5);
        System.out.println("The circles area is " + c.area());
    }
}

