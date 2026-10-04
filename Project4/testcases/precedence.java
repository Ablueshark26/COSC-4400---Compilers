class Test {
    public static void main(String[] args) {
        int a;
        int b;
        int c;
        boolean r1;
        boolean r2;
        boolean r3;
        a = 2 + 3 * 4;
        b = (2 + 3) * 4;
        c = a - b / 2;
        r1 = a > b && b < c;
        r2 = a == b || c != a;
        r3 = !r1 && (r2 || a > 0);
    }
}
