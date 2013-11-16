import java.util.concurrent.TimeUnit;

public class Philosopher extends PhilosopherBase implements Runnable {

    private boolean canThink;
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
        canThink = true;
    }

    public void run() {
        while (!stopFlag) {
            if (canThink)
                think();

            if (isReverseOrder) {
                try {
                    if (right.tryLock(rnd.nextInt(total), TimeUnit.MICROSECONDS)) {

                        System.out.println("[" + position + "] took right fork");
                        try {
                            if (left.tryLock(rnd.nextInt(total), TimeUnit.MICROSECONDS)) {
                                System.out.println("[" + position + "] took left fork");
                                try {
                                    eat();
                                } finally {
                                    left.unlock();
                                }
                                canThink = true;
                            } else {
                                canThink = false;
                            }
                        } finally {
                            right.unlock();
                        }
                    } else
                        canThink = false;
                } catch (InterruptedException e) {
                    break;
                }
                isReverseOrder = !isReverseOrder;
            } else {
                try {
                    if (left.tryLock(rnd.nextInt(total), TimeUnit.MICROSECONDS)) {

                        System.out.println("[" + position + "] took left fork");
                        try {
                            if (right.tryLock(rnd.nextInt(total), TimeUnit.MICROSECONDS)) {

                                System.out.println("[" + position + "] took right fork");
                                try {
                                    eat();
                                } finally {
                                    System.out.println("[" + position + "] put right fork");
                                    right.unlock();
                                }
                                canThink = true;
                            } else
                                canThink = false;
                        } finally {
                            System.out.println("[" + position + "] put left fork");
                            left.unlock();
                        }
                    } else
                        canThink = false;
                } catch (InterruptedException e) {
                    break;
                }
                isReverseOrder = !isReverseOrder;
            }
            try {
                TimeUnit.MICROSECONDS.sleep(rnd.nextInt(total));
            } catch (InterruptedException e) {
            }
        }
    }
}