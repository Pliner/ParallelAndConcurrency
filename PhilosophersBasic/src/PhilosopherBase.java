import java.util.Random;

public abstract class PhilosopherBase {

    int position;
    int eatCount = 0;
    long waitTime = 0;
    long startWait;
    Random rnd = new Random();

    protected PhilosopherBase(int position) {
        this.position = position;
    }

    protected void eat() {
        waitTime += System.currentTimeMillis() - startWait;
        System.out.println("[" + position + "] is eating");
        try {
            Thread.sleep(rnd.nextInt(100));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        eatCount++;
        System.out.println("[" + position + "] finished eating");
    }

    protected void think() {
        System.out.println("[" + position + "] is thinking");
        try {
            Thread.sleep(rnd.nextInt(100));
        } catch (InterruptedException e) {
        }
        System.out.println("[" + position + "] is hungry");
        startWait = System.currentTimeMillis();
    }

}
