package main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class Waiter implements IWaiter {

    private final Lock lock;
    private final List<ICateredPhilosopher> orderedPhilosophers;
    private final List<IFork> usedForks;

    public Waiter() {
        lock = new ReentrantLock();
        orderedPhilosophers =  new ArrayList<>();
        usedForks  = new ArrayList<>();
    }

    @Override
    public void makeOrder(ICateredPhilosopher cateredPhilosopher) {
        lock.lock();
        try {
            orderedPhilosophers.add(cateredPhilosopher);
            processOrder();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void thanks(ICateredPhilosopher cateredPhilosopher) {
        lock.lock();
        try {
            orderedPhilosophers.remove(cateredPhilosopher);
            makeForksAsFree(cateredPhilosopher);
        } finally {
            lock.unlock();
        }
    }

    private void processOrder() {
        for (ICateredPhilosopher cateredPhilosopher : orderedPhilosophers) {
            if (areForksFree(cateredPhilosopher)) {
                markForksAsUsed(cateredPhilosopher);
                try (ICateredAction action = cateredPhilosopher.getAction()) {
                    action.signalEat();
                }
            }
        }
    }

    private boolean areForksFree(ICateredPhilosopher cateredPhilosopher) {
        return !usedForks.contains(cateredPhilosopher.getLeft()) && !usedForks.contains(cateredPhilosopher.getRight());
    }

    private void markForksAsUsed(ICateredPhilosopher cateredPhilosopher) {
        usedForks.add(cateredPhilosopher.getLeft());
        usedForks.add(cateredPhilosopher.getRight());
    }

    private void makeForksAsFree(ICateredPhilosopher cateredPhilosopher) {
        usedForks.remove(cateredPhilosopher.getLeft());
        usedForks.remove(cateredPhilosopher.getRight());
    }
}
