boolean a = true;
boolean b = false;

int c[] = new int[2];
c[0] = 11;
c[1] = c[0];

System.out.print("condition ");
if (a || (b || --c[0] == 10) && c[1]++ - 1 == 12 - 2) {
    System.out.println("passed!");
} else { System.out.println("failed!"); }

System.out.println(c + ", " + d);