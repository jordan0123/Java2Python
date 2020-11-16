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