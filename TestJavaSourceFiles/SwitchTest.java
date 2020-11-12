int a = 2;
int b = 4;

int c[] = new int[4];
c[0] = 2;
c[1] = 6;

switch (a) {
    case 1:
    System.out.println("Hello!");
    case 2:
    a = 5;
    break;
    case 4:
    switch (b) {
        case 4:
        a = 6;
        break;
        case c[2]:
        a = 2;
        if (c[2] == 7) { c[2] = 3; }
        case 6:
        if (a == 2) { break; break; }
        if (a == 3) {
            if (b == 2) {
                break;
            }
        } else if (a == 4) {
            if (b == 4) {
                break;
            } else {
                b = 3;
            }
        } else {
            a -= 3;
            if (a == 6) {
                break;
            }
        }
        b = 2;
        break;
        default:
        a = 9;
    }
    a = 2;
    case 5:
    a = 3;
}

System.out.println(a);