public class Philosopher extends PhilosopherBase implements Runnable {

    private boolean isReverseOrder;
    volatile boolean stopFlag = false;

    private int total;
    private final Fork left;
    private final Fork right;

    public Philosopher(int position, int total, Fork left, Fork right) {
        super(position);
        this.total = total;
        this.left = left;
        this.right = right;
        isReverseOrder = false;
    }

    public void run() {
        while (!stopFlag) {
            think();
            if (isReverseOrder) {
                if (right.tryLock()) {

                    System.out.println("[" + position + "] took right fork");
                    try {
                        if (left.tryLock()) {
                            System.out.println("[" + position + "] took left fork");
                            try {
                                eat();
                            } finally {
                                left.unlock();
                            }
                        }
                    } finally {
                        right.unlock();
                    }
                }
                isReverseOrder = !isReverseOrder;
            } else {
                if (left.tryLock()) {

                    System.out.println("[" + position + "] took left fork");
                    try {
                        if (right.tryLock()) {

                            System.out.println("[" + position + "] took right fork");
                            try {
                                eat();
                            } finally {
                                System.out.println("[" + position+ "] put right fork");
                                right.unlock();
                            }
                        }
                    } finally {
                        System.out.println("[" + position + "] put left fork");
                        left.unlock();
                    }
                }
                isReverseOrder = !isReverseOrder;
            }
            try {
                Thread.sleep(rnd.nextInt(total));
            } catch (InterruptedException e) {
            }


        }
    }
}