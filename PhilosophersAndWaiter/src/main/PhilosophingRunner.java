package main;

import java.util.concurrent.TimeUnit;

public class PhilosophingRunner {
    public static void run(final int philosopherCount, final int executionTimeInSeconds, final int thinkTimeMS, final int eatTimeMS, final int debugMode) {

        Thread[] threads = new Thread[philosopherCount];
        PhilosophisingProcess[] philosophisingProcesses = new PhilosophisingProcess[philosopherCount];
        IWaiter waiter = new Waiter();
        int forId = 0;
        IFork last = new Fork(forId++);
        IFork left = last;
        for(int index = 0; index < philosopherCount; ++index){
            final int id = index;
            final IFork localLeft = left;
            final IFork localRight = (index == philosopherCount - 1) ? last : new Fork(forId++);
            IPhilosopher philosopher = new Philosopher(new IPhilosopherConfiguration() {
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
            final PhilosophisingProcess philosophisingProcess = new PhilosophisingProcess(philosopher, waiter);
            philosophisingProcesses[index] = philosophisingProcess;
            threads[index] = new Thread(philosophisingProcess);
            left = localRight;
        }
        for (int i = 0; i < philosopherCount; ++i)
            threads[i].start();
        try {
            TimeUnit.SECONDS.sleep(executionTimeInSeconds);
        } catch (InterruptedException e) {
        }

        for (PhilosophisingProcess phil : philosophisingProcesses) {
            phil.stop();
        }
        for (Thread thread : threads) {
            try {
                thread.interrupt();
                thread.join(100);
            } catch (InterruptedException e) {
                System.out.println(thread.getId());
                e.printStackTrace();
            }
        }
        for (PhilosophisingProcess phil : philosophisingProcesses) {
            System.out.println(phil.philosopher.getStatistics());
        }
    }
}
