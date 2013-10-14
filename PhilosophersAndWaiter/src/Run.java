import java.util.concurrent.TimeUnit;

public class Run {
    public static void main(String[] args) throws Exception {
        args = new String[]{"10", "60", "100", "100", "1"};
        if (args.length != 5) {
            System.out.println("usage: *.jar philosopherCount runningTime thinkMaxTime eatMaxTime debugMode");
            System.exit(0);
        }
        int philosopherCount = Integer.parseInt(args[0]);
        int runningTime = Integer.parseInt(args[1]);
        int thinkMaxTime = Integer.parseInt(args[2]);
        int eatMaxTime = Integer.parseInt(args[3]);
        int debugMode = Integer.parseInt(args[4]);

        Philosopher[] phils = new Philosopher[philosopherCount];

        int lastId = 0;
        Fork[] forks = new Fork[philosopherCount];
        for (int i = 0; i < philosopherCount; ++i) {
            forks[i] = new Fork(++lastId);
        }
        Waiter waiter = new Waiter(forks);
        Fork last = forks[0];
        Fork left = last;
        for (int i = 0; i < philosopherCount; i++) {
            Fork right = (i == philosopherCount - 1) ? last : forks[i + 1];
            forks[i] = left;
            phils[i] = new Philosopher(i, waiter, left, right, thinkMaxTime, eatMaxTime);
            left = right;
        }


        Thread[] threads = new Thread[philosopherCount];
        for (int i = 0; i < philosopherCount; i++) {
            threads[i] = new Thread(phils[i]);
            threads[i].start();
        }

        TimeUnit.SECONDS.sleep(runningTime);

        for (Philosopher phil : phils) {
            phil.stop();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        int totalEatCount = 0;
        for (Philosopher phil : phils) {
            totalEatCount += phil.eatCount;
            System.out.println("[" + phil.position + "] ate "
                    + phil.eatCount + " times and waited " + phil.waitTime + " ms");
        }
        System.out.println("[Total] ate " + totalEatCount + " times");
    }

}
