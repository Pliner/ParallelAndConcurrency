package main;

public class EntryPoint {
    public static void main(String[] args) {
        args = new String[] {"5", "60", "100", "100", "1"};

        if (args.length != 5) {
            System.out.println("Usage: philosophers philCount executionTimeSeconds thinkTimeMs eatTimeMs debugMode");
        }

        int philosopherCount = Integer.parseInt(args[0]);
        int executionTimeInSeconds = Integer.parseInt(args[1]);
        int thinkTimeMS = Integer.parseInt(args[2]);
        int eatTimeMS = Integer.parseInt(args[3]);
        int debugMode = Integer.parseInt(args[4]);

        PhilosophingRunner.run(philosopherCount, executionTimeInSeconds, thinkTimeMS, eatTimeMS, debugMode);
    }
}
