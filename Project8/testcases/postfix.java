class Test {
    public static void main(String[] args) {
        Node n;
        int v;
        n = new Node();
        n.left = new Node();
        n.left.value = 42;
        v = n.left.getValue();
        v = n.getChild(0).getValue();
    }
}

class Node {
    Node left;
    Node right;
    int value;

    public int getValue() {
        return value;
    }

    public Node getChild(int i) {
        return left;
    }
}
