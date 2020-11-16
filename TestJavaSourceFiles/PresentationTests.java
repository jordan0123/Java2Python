class PresentationTests {
    public static void main(String[] args) {
        ArrayTest arrayTest;
        ForTest forTest;
        SwitchTest switchTest;
        
        System.out.println("*** ArrayTest START ***");
        int test[] = {1, 0, 4, 2, 3};
        new ArrayTest(test);
        System.out.println("*** ArrayTest  END  ***\n");

        System.out.println("*** ForTest START ***");
        for (int i = 1; i < 11; i++) forTest = new ForTest(i);
        System.out.println("*** ForTest  END  ***\n");

        System.out.println("*** SwitchTest START ***");
        for (int i = 1; i < 11; i++) switchTest = new SwitchTest(i);
        System.out.println("*** SwitchTest  END  ***\n");
    }
}