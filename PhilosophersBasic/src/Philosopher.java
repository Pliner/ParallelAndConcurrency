public class Philosopher extends PhilosopherBase implements Runnable {

    volatile boolean stopFlag = false;

    private final Fork lockFirst;
    private final Fork lockLast;

    public Philosopher(int position, Fork left, Fork right) {
        super(position);
        if (left.Id < right.Id) {
            lockFirst = left;
            lockLast = right;
        } else {
            lockFirst = right;
            lockLast = left;
        }
    }

    public void run() {
        //TODO подчинить вывод на консоль
        while (!stopFlag) {
            think();
            synchronized (lockFirst) {
                System.out.println("[" + position + "] took left fork");
                synchronized (lockLast) {
                    System.out.println("[" + position + "] took right fork");
                    eat();
                }
            }
        }
        System.out.println("[" + position + "] stopped");
    }
}