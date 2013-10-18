package main;

public interface IPhilosopherConfiguration {
    int getId();
    IFork getLeft();
    IFork getRight();
    int getEatTimeMs();
    int getThinkTimeMs();
}
