public class MyPhilosopher extends Philosopher implements Runnable {

    volatile boolean stopFlag = false;

    public MyPhilosopher(int position, Fork left, Fork right) {
        super(position, left, right);
    }

    public void run() {
        while (!stopFlag) {
            think();
            synchronized (left) {
                System.out.println("[Philosopher " + position + "] took left fork");
                synchronized (right) {
                    System.out.println("[Philosopher " + position + "] took right fork");
                    eat();
                }
            }
        }
        System.out.println("[Philosopher " + position + "] stopped");
    }
}