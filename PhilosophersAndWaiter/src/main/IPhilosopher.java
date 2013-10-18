package main;

public interface IPhilosopher {
    void eat(IWaiter waiter) throws InterruptedException;
    void think();
    String getStatistics();
}


