class T1TypeIndex{
    public static void main(String[] args) {
        Helper h;
        int[] arr;
        h = new Helper();
        arr = new int[5];
        Xinu.printint(arr.length);
        Xinu.printint(arr[0]);
        Xinu.printint(h.add(2, 3));
    }
}
 
class Helper {
    public int add(int x, int y) {
        return x + y;
    }
}
 
