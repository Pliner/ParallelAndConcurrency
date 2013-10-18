package main;

import java.util.concurrent.TimeUnit;

public class PhilosophingRunner {
    public static void run(final int philosopherCount, final int executionTimeInSeconds, final int thinkTimeMS, final int eatTimeMS, final int debugMode) {

        Thread[] threads = new Thread[philosopherCount];
        IPhilosopher[] philosophers = new Philosopher[philosopherCount];
        IFork[] forks = new Fork[philosopherCount];
        IWaiter waiter = new Waiter();
        for(int index = 0; index < philosopherCount; ++index)
            forks[index] = new Fork(index);


        for(int index = 0; index < philosopherCount; ++index){
            final int id = index;
            final IFork localLeft = forks[index];
            final IFork localRight = forks[(index + 1) % philosopherCount];
            IPhilosopher philosopher = new Philosopher(waiter, new IPhilosopherConfiguration() {
                @Override
                public int getId() {
                    return id;
                }

                @Override
                public IFork getLeft() {
                    return localLeft;
                }

                @Override
                public IFork getRight() {
                    return localRight;
                }

                @Override
                public int getEatTimeMs() {
                    return eatTimeMS;
                }

                @Override
                public int getThinkTimeMs() {
                    return thinkTimeMS;
                }
            }, new PhilosopherActionsLogger(new IPhilosopherActionsLoggerConfiguration() {
                @Override
                public int getId() {
                    return id;
                }

                @Override
                public boolean isDebugMode() {
                    return debugMode == 1;
                }
            }));
            philosophers[index] = philosopher;
        }
        for (int index = 0; index < philosopherCount; ++index) {
            threads[index] = new Thread(philosophers[index]);
            threads[index].start();
        }


        try {
            TimeUnit.SECONDS.sleep(executionTimeInSeconds);
        } catch (InterruptedException e) {
        }

        for (IPhilosopher phil : philosophers) {
            phil.stop();
        }
        for (Thread thread : threads) {
            try {
                thread.interrupt();
                thread.join();
            } catch (InterruptedException e) {
            }
        }
        for (IPhilosopher phil : philosophers) {
            System.out.println(phil.getStatistics());
        }
    }
}
