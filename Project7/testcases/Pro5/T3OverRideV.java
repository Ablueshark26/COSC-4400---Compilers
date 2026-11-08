class T3OverRideV {
    public static void main(String[] args) {
        B b;
        int x;
        b = new B();
        x = b.giveY(5);
    }
}
 
class A {
    public int giveY(int y) {
        return y;
    }
}
 
class B extends A {
    public int giveY(int y) {
        return y;
    }
}
