package main;

public final class PhilosopherActionsLogger implements IPhilosopherActionsLogger {
    private final IPhilosopherActionsLoggerConfiguration configuration;
    private int eatCount = 0;
    private long waitTimeMs = 0;
    private long startWaitMs;

    public PhilosopherActionsLogger(IPhilosopherActionsLoggerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void eating() {
        waitTimeMs += System.currentTimeMillis() - startWaitMs;
        eatCount++;
        if (configuration.isDebugMode())
            System.out.println("[" + configuration.getId() + "] eating");
    }

    @Override
    public void thinking() {
        if (configuration.isDebugMode())
            System.out.println("[" + configuration.getId() + "] thinking");
    }

    @Override
    public void hungry() {
        if (configuration.isDebugMode())
            System.out.println("[" + configuration.getId() + "] hungry");
        startWaitMs = System.currentTimeMillis();
    }

    @Override
    public void takeForks() {
        if (configuration.isDebugMode()) {
            System.out.println("[" + configuration.getId() + "] took left fork");
            System.out.println("[" + configuration.getId() + "] took right fork");
        }
    }

    @Override
    public void putForks() {
        if (configuration.isDebugMode()) {
            System.out.println("[" + configuration.getId() + "] put right fork");
            System.out.println("[" + configuration.getId() + "] put left fork");
        }
    }

    @Override
    public synchronized String getStatistics() {
        return "[" + configuration.getId() + "] " + eatCount + " " + waitTimeMs;
    }
}
