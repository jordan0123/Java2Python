// test
    int str = 1;
    int j = 2;
    j = 1;
    j = 3 / (2 - 1);
    j = i - 3 * 1;
    boolean h = i > j;
    boolean x = k + j;
    boolean b = h || x;
    int k;
    int l = j - 3 * 2;
	
    h = !h;
	
    while(i > 2)
    {
        while(i>4)
        {
            i = i - 1;
        }
        i = i - 1;
        taco = i;
    }
    if(i < 3)
    {
     j = 4;
     h = 3;
    }else if(--i < 3)
    {
        j = 5;
    }else
    {
        j = 6;
        if(j == 6)
        {
            k = 8;
        }else if(j++ == 7)
        {
            k = 9;
        }else
        {
            k = 10;
        }
    }
    do{
        k = k - 1;
    }while(k > 0);

    for(int k = 0; k < 10; ++k){
        j = 5;
    }

// whole line comment
i = 3 + -j;
int [] y = new int[3]; // end line comment
y[2] = 2;
diameter = 3.0;
unit = "metric";
circle.x = radius(diameter, math.PI).unit("km").toString();
circle.x = here();
i *= 3;

++i;
i--;
i++;
--i;

System.out.println("Part A" + "Part B" + (2 + 3) + y[2]);
System.out.print("One" + "Two");

int [] intArr = {1,2,3};

for (int i : intArr) {
    i++;
}

a = n || b && a || c && (++a == 2 || ++b == a++) && d;

switch (i) {
    case 1:
    System.out.println(i);
    case y[2]:
    System.out.println(i+1);
    break;

    case 3:
    switch (y[2]) {
        case 2:
        System.out.println(2);
        case i--:
        System.out.println(3);
        break;

        default:
        i = 2;
    }
    break;

    default:
    System.out.print("Error::");
    System.out.println("\"not found\"");
}

boolean a = true;
boolean b = false;
a = false;
b = true;
a = true || false || b;

System.out.println("Done!");