class sweep {
    public static void main(String[] a) {
        Xinu.print(new Fac().compute(10));
    }
}

class Fac {
    int count;

    public int compute(int num) {
        int r;
        int i;
        r = 1;
        i = num;
        while (0 < i) {
            r = r * i;
            i = i - 1;
            count = count + 1;
        }
        if (r < 100 && !(r == 7)) r = r / 4;
        else r = r - 4096;
        return r + this.many(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    public int many(int a, int b, int c, int d, int e, int f, int g,
                    int h, int i, int j) {
        return a + j * 8 - i;
    }
}