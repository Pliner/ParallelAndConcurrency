import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Waiter {
    private final Fork[] forks;
    private final Lock waiterLock;
    private final List<Philosopher> orderedPhilosophers = new ArrayList<Philosopher>();
    private final List<Fork> freeForks = new ArrayList<Fork>();

    public Waiter(Fork[] forks) {
        this.forks = forks;
        this.waiterLock = new ReentrantLock();
        for (Fork fork : forks)
            freeForks.add(fork);
    }

    public void makeOrder(Philosopher philosopher) {
        waiterLock.lock();
        try {
            orderedPhilosophers.add(philosopher);
            checkCanSomebodyEat();
        } finally {
            waiterLock.unlock();
        }
    }

    private void checkCanSomebodyEat() {
        for (Philosopher philosopher : orderedPhilosophers) {
            if (freeForks.contains(philosopher.LeftFork) && freeForks.contains(philosopher.RightFork)) {
                philosopher.lock.lock();
                try {
                    freeForks.remove(philosopher.LeftFork);
                    freeForks.remove(philosopher.RightFork);
                    philosopher.canEat();
                    philosopher.contidion.signal();
                } finally {
                    philosopher.lock.unlock();
                }
            }
        }
    }

    public void thanks(Philosopher philosopher) {
        waiterLock.lock();
        try {
            orderedPhilosophers.remove(philosopher);
            freeForks.add(philosopher.LeftFork);
            freeForks.add(philosopher.RightFork);
            checkCanSomebodyEat();
        } finally {
            waiterLock.unlock();
        }
    }
}
