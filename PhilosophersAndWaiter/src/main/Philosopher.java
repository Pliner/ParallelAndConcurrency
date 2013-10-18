package main;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class Philosopher implements IPhilosopher, ICateredPhilosopher {
    private final Lock lock;
    private final Condition eatCondition;
    private volatile boolean canEat = false;
    private volatile boolean canContinue = true;
    private final IWaiter waiter;
    private final IPhilosopherConfiguration configuration;
    private final IPhilosopherActionsLogger actionsLogger;
    private final Random random = new Random();

    public Philosopher(IWaiter waiter, IPhilosopherConfiguration configuration, IPhilosopherActionsLogger philosopherActionLogger) {
        this.waiter = waiter;
        this.configuration = configuration;
        this.actionsLogger = philosopherActionLogger;
        this.lock = new ReentrantLock();
        this.eatCondition = lock.newCondition();
    }

    private void eat() {
        actionsLogger.takeForks();
        actionsLogger.eating();
        try {
            TimeUnit.MILLISECONDS.sleep(random.nextInt(configuration.getEatTimeMs()));
        } catch (InterruptedException e) {
        }
        actionsLogger.putForks();
    }

    private void eatDone() {
        canEat = false;
    }

    private boolean canEat() {
        return canEat;
    }

    private void think() {
        actionsLogger.thinking();
        try {
            TimeUnit.MILLISECONDS.sleep(random.nextInt(configuration.getThinkTimeMs()));
        } catch (InterruptedException e) {
        }
        actionsLogger.hungry();
    }

    @Override
    public String getStatistics() {
        return actionsLogger.getStatistics();
    }

    @Override
    public void stop() {
        canContinue = false;
    }

    private void lock() {
        lock.lock();
    }

    private void unlock() {
        lock.unlock();
    }

    private void signalEat() {
        canEat = true;
        eatCondition.signal();
    }

    private void awaitEat() throws InterruptedException {
        eatCondition.await();
    }

    @Override
    public ICateredAction getAction() {
        return new PhilosopherAction();
    }

    @Override
    public IFork getLeft() {
        return configuration.getLeft();
    }

    @Override
    public IFork getRight() {
        return configuration.getRight();
    }

    @Override
    public void run() {
        while (canContinue) {
            think();
            lock();
            try {
                waiter.makeOrder(this);
                while (!canEat()) {
                    if (canContinue)
                        awaitEat();
                    else
                        return;
                }
                eat();
                eatDone();
                waiter.thanks(this);
            } catch (InterruptedException e) {
                return;
            } finally {
                unlock();
            }
        }
    }

    private class PhilosopherAction implements ICateredAction {

        private PhilosopherAction() {
            Philosopher.this.lock();
        }

        @Override
        public void signalEat() {
            Philosopher.this.signalEat();
        }

        @Override
        public void close() {
            Philosopher.this.unlock();
        }
    }

    @Override
    public String toString() {
        return "[" + configuration.getId() + "] - " + getLeft() + " " + getRight() + "";
    }
}
