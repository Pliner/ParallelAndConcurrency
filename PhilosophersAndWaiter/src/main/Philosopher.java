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
    private final IPhilosopherConfiguration configuration;
    private final IPhilosopherActionsLogger actionsLogger;
    private final Random random = new Random();

    public Philosopher(IPhilosopherConfiguration configuration, IPhilosopherActionsLogger philosopherActionLogger) {
        this.configuration = configuration;
        this.actionsLogger = philosopherActionLogger;
        this.lock = new ReentrantLock();
        this.eatCondition = lock.newCondition();
    }

    @Override
    public void eat(IWaiter waiter) throws InterruptedException {
        lock();
        try {
            waiter.makeOrder(this);
            while (!canEat())
                awaitEat();
            doEat();
            setEatDone();
            waiter.thanks(this);
        } finally {
            unlock();
        }
    }

    private void doEat() {
        actionsLogger.takeForks();
        actionsLogger.eating();
        try {
            TimeUnit.MILLISECONDS.sleep(random.nextInt(configuration.getEatTimeMs()));
        } catch (InterruptedException e) {
        }
        actionsLogger.putForks();
    }

    private void setEatDone() {
        canEat = false;
    }

    private boolean canEat() {
        return canEat;
    }

    @Override
    public void think() {
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
