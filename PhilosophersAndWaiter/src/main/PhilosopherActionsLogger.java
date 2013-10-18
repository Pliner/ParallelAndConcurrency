package main;

public final class PhilosopherActionsLogger implements IPhilosopherActionsLogger {
    private final IPhilosopherActionsLoggerConfiguration configuration;
    private int eatCount = 0;

    public PhilosopherActionsLogger(IPhilosopherActionsLoggerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public synchronized void eating() {
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
        return "[" + configuration.getId() + "] " + eatCount;
    }
}
