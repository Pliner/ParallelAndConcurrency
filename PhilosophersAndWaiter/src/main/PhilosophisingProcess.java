package main;

public class PhilosophisingProcess implements Runnable{
    public final IPhilosopher philosopher;
    public final IWaiter waiter;

    private volatile boolean canContinue = true;

    public PhilosophisingProcess(IPhilosopher philosopher, IWaiter waiter) {
        this.philosopher = philosopher;
        this.waiter = waiter;
    }

    @Override
    public void run() {
        while(canContinue) {
            philosopher.think();
            try {
                philosopher.eat(waiter);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void stop() {
        canContinue = false;
    }
}
