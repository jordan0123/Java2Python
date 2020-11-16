class ArrayTest {
    public ArrayTest(int[] i) {
        int[][] j = new int[5][3];
        int[][][] k = new int[5][4][2];

        // call to .length doesn't translate properly to python, would
        // have to use len() instead.
        int index = 0;
        for (int n : i) {
            for (int l = 0; l < 3; l++) { j[index][l] = index + n + l; }
            for (int l = 0; l < 4; l++) { 
                for (int m = 0; m < 2; m++) { k[index][l][m] = index + n + l + m; }
            }

            index++;
        }

        System.out.print("i: [");
        for (int l = 0; l < 5; l++) {
            System.out.print(i[l]);
            if (l+1 < 5) { System.out.print(", "); }
        } System.out.println("]");

        System.out.print("j: [");
        for (int l = 0; l < 5; l++) {
            System.out.print("[");
            for (int m = 0; m < 3; m++) {
                System.out.print(j[l][m]);
                if (m+1 < 3) { System.out.print(", "); }
            } System.out.print("]");

            if (l+1 < 5) { System.out.print(", "); }
        } System.out.println("]");
    }
}