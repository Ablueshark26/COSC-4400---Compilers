class Test {
    public static void main(String[] args) {
        boolean t;
        boolean f;
        boolean isNull;
        Widget w;
        t = true;
        f = false;
        w = new Id();
        isNull = (w == null);
    }
}

class Id {
    int id;
}
