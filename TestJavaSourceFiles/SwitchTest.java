class SwitchTest {
    public SwitchTest(int a) {
        int b = 2; // do not change
        int c = 3; // do not change

        System.out.println("a = " + a);

        switch (a) {
            case 1:
            b = 5;
            case 2:
            case 3:
            c = 10;
            if (b == 5) { break; }

            case 9:
            b = 2;
            switch (c) {
                case 10:
                a += 3; // a should be 5 or 6 here
                if (a == 6) { break; }
                b = 1;
                break;
                case 20:
                if (a == 5) {
                    b = 5;
                    c = 70;
                    while (true) {
                        break;
                    }
                } else if (a == 6) {
                    b = 20;
                    c = 30;
                    break;
                } else {
                    c = 22;
                }

                b = 10;
            }
            break;

            default:
            a = 7; b = 7; c = 7;
        }

        System.out.println(a + ", " + b + ", " + c);
        // a = 1 (1, 5, 10)
        // a = 2 (5, 2, 10)
        // a = 3 (6, 1, 10)
    }
}