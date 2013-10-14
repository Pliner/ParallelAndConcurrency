import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Philosopher implements Runnable {
    int position;
    int eatCount = 0;
    long waitTime = 0;
    long startWait;
    Random rnd = new Random();

    public final Condition contidion;
    public final Lock lock;
    public final Fork LeftFork;
    public final Fork RightFork;
    private int thinkTime;
    private int eatTime;
    private final Waiter waiter;

    Philosopher(int position, Waiter waiter, Fork leftFork, Fork rightFork, int thinkTime, int eatTime) {
        this.position = position;
        this.waiter = waiter;
        this.LeftFork = leftFork;
        this.RightFork = rightFork;
        this.thinkTime = thinkTime;
        this.eatTime = eatTime;
        this.lock = new ReentrantLock();
        this.contidion = lock.newCondition();
    }

    private volatile boolean canEat;
    private volatile boolean canContinue = true;

    public void run() {
        while (canContinue) {
            think();
            lock.lock();
            try {
                waiter.makeOrder(this);
                while (!canEat)
                    contidion.await();
                eat();
                canEat = false;
                waiter.thanks(this);
            } catch (InterruptedException e) {
                break;
            } finally {
                lock.unlock();
            }
        }
    }

    public void stop() {
        canContinue = false;
    }

    public void canEat() {
        canEat = true;
    }

    private void think() {
        System.out.println("[" + position + "] is thinking");
        try {
            TimeUnit.MILLISECONDS.sleep(rnd.nextInt(thinkTime));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("[" + position + "] is hungry");
        startWait = System.currentTimeMillis();
    }

    private void eat() {
        waitTime += System.currentTimeMillis() - startWait;
        System.out.println("[" + position + "] is eating");
        try {
            TimeUnit.MILLISECONDS.sleep(rnd.nextInt(eatTime));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        eatCount++;
        System.out.println("[" + position + "] finished eating");
    }


}
