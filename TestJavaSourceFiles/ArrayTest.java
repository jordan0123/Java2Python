class ArrayTest {
    public ArrayTest(int[] i) {
        int[][] j = new int[5][3];
        int[][][] k = new int[5][9][9];

        // call to .length doesn't translate properly to python, would
        // have to use len() instead.
        int index = 0;
        for (int n : i) {
            for (int l = 0; l < 3; l++) { j[index][l] = index + n + l; }
            for (int l = 0; l < 9; l++) { 
                for (int m = 0; m < 9; m++) { k[index][l][m] = index + n + l + m; }
            }

            index++;
        }

        // print single-dim i
        System.out.print("i: [");
        for (int l = 0; l < 5; l++) {
            System.out.print(i[l]);
            if (l+1 < 5) { System.out.print(", "); }
        } System.out.println("]");

        // print double-dim j
        System.out.print("j: [");
        for (int l = 0; l < 5; l++) {
            System.out.print("[");
            for (int m = 0; m < 3; m++) {
                System.out.print(j[l][m]);
                if (m+1 < 3) { System.out.print(", "); }
            } System.out.print("]");

            if (l+1 < 5) { System.out.print(", "); }
        } System.out.println("]");

        // print triple-dim k
        System.out.print("k: [");
        for (int l = 0; l < 5; l++) {
            System.out.print("[");
            for (int m = 0; m < 9; m++) {
                System.out.print("[");
                for (int o = 0; o < 9; o++) {
                    System.out.print(k[l][m][o]);
                    if (o+1 < 9) { System.out.print(", "); }
                } System.out.print("]");
                if (m+1 < 9) { System.out.print(", "); }
            } System.out.print("]");
            if (l+1 < 5) { System.out.print(", "); }
        } System.out.println("]");
    }
}