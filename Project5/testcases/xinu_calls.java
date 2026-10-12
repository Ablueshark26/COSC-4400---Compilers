class Test {
    public static void main(String[] args) {
        int tid;
        /* Xinu call as a standalone statement */
        Xinu.create(0, 0, 0);
        /* Xinu call used as an expression (assigned to a variable) */
        tid = Xinu.getid();
        Xinu.resume(tid);
        Xinu.sleep(50);
    }
}
