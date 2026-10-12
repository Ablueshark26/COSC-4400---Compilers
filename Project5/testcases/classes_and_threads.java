class Test {
    public static void main(String[] args) {
        Counter c;
        Ticker t;
        int result;
        c = new Counter();
        t = new Ticker();
        c.increment();
        c.increment();
        result = c.getValue();
        t.start();
    }
}

class Counter {
    int value;

    public int getValue() {
        return value;
    }

    public int increment() {
        value = value + 1;
        return value;
    }
}

class Ticker extends Thread {
    int ticks;

    public synchronized int getTicks() {
        return ticks;
    }

    public void run() {
        int i;
        i = 0;
        while (i < 100) {
            Xinu.sleep(10);
            ticks = ticks + 1;
            i = i + 1;
        }
    }
}
