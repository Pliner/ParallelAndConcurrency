package test;

import main.PhilosophingRunner;
import org.junit.Test;

public class MultipleThreadTests {
    @Test
    public void doTestZeroThinkAndEatTime() {
        PhilosophingRunner.run(5, 10, 0, 0, 0);
    }
}
