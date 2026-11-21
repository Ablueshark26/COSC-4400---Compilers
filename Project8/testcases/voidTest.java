class voidTest {
    public static void main(String[] a) { Xinu.print("start"); }
}

class Helper extends Thread {
    public void foo() { Xinu.print("hi"); }
}
