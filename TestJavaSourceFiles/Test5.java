class PythonBuild {
    private int hello = 2;
    private boolean test = false;
    private int world;
    
    PythonBuild(int hello) {
        this.hello = 3;
    }

    int getHello() { return this.hello; }
}

class HelloWorld {
    PythonBuild pythonBuild = null;
    public static void main(String[] args) {
        pythonBuild = new PythonBuild(5);
        System.out.println("Hello, world! " + pythonBuild.getHello());
    }
}