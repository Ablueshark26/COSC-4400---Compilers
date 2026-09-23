class Test {
    public static void main(String[] args) {
        Worker w;
        w = new Worker();
    }
}

class Worker extends Thread {
    int id;
    boolean running;

    public synchronized int getId() {
        return id;
    }

    public void run() {
        int i;
        i = 0;
        while (i < 10) {
            Xinu.sleep(100);
            i = i + 1;
        }
    }
}
