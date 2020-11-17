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
        for (int i = 1; i < 11; i++) {forTest = new ForTest(i);}
        System.out.println("*** ForTest  END  ***\n");

        System.out.println("*** SwitchTest START ***");
        for (int i = 1; i < 11; i++) {switchTest = new SwitchTest(i);}
        System.out.println("*** SwitchTest  END  ***\n");
    }
}

class ArrayTest {
    public ArrayTest(int[] i) {
        int[][] j = new int[5][3];
        int[][][] k = new int[5][9][9];

        // call to .length doesn't translate properly to python, would
        // have to use len() instead.
        int index = 0;
        for (int n : i) {
            for (int l = 0; l < j[0].length; l++) { j[index][l] = index + n + l; }
            for (int l = 0; l < k[0].length; l++) { 
                for (int m = 0; m < k[0][0].length; m++) { k[index][l][m] = index + n + l + m; }
            }

            index++;
        }

        // print single-dim i
        System.out.print("i: [");
        for (int l = 0; l < i.length; l++) {
            System.out.print(i[l]);
            if (l+1 < 5) { System.out.print(", "); }
        } System.out.println("]");

        // print double-dim j
        System.out.print("j: [");
        for (int l = 0; l < j.length; l++) {
            System.out.print("[");
            for (int m = 0; m < j[0].length; m++) {
                System.out.print(j[l][m]);
                if (m+1 < j[0].length) { System.out.print(", "); }
            } System.out.print("]");
            if (l+1 < j.length) { System.out.print(", "); }
        } System.out.println("]");

        // print triple-dim k
        System.out.print("k: [");
        for (int l = 0; l < k.length; l++) {
            System.out.print("[");
            for (int m = 0; m < k[0].length; m++) {
                System.out.print("[");
                for (int o = 0; o < k[0][0].length; o++) {
                    System.out.print(k[l][m][o]);
                    if (o+1 < k[0][0].length) { System.out.print(", "); }
                } System.out.print("]");
                if (m+1 < k[0].length) { System.out.print(", "); }
            } System.out.print("]");
            if (l+1 < k.length) { System.out.print(", "); }
        } System.out.println("]");
    }
}


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

class ForTest {
    public ForTest(int num) {
        int arr[] = new int[num];
        for (int i = 0; i < num; i++) {
            arr[i] = i;
            System.out.print("Hello ");
        } System.out.println();

        for (int i : arr) { // seems to fail here due to assignmentExpression() finding an =_op
            for (int j = 0; j < 1; j++) { arr[j] = i; }
            System.out.print(i + " ");
        } System.out.println();
    }
}