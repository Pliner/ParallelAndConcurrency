public class Run {

    static final int count = 10;

    public static void main(String[] args) throws Exception {
        Philosopher[] phils = new Philosopher[count];

        int lastId = 0;
        Fork last = new Fork(++lastId);
        Fork left = last;
        for (int i = 0; i < count; i++) {
            Fork right = (i == count - 1) ? last : new Fork(++lastId);
            phils[i] = new Philosopher(i, left, right);
            left = right;
        }

        Thread[] threads = new Thread[count];
        for (int i = 0; i < count; i++) {
            threads[i] = new Thread(phils[i]);
            threads[i].start();
        }

        Thread.sleep(60000);

        for (Philosopher phil : phils) {
            phil.stopFlag = true;
        }
        for (Thread thread : threads) {
            thread.join();
        }

        for (Philosopher phil : phils) {
            System.out.println("[PhilosopherBase " + phil.position + "] ate "
                    + phil.eatCount + " times and waited " + phil.waitTime + " ms");
        }
    }

}
