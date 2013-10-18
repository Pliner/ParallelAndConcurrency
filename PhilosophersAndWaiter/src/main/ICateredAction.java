package main;

public interface ICateredAction extends AutoCloseable{
    void signalEat();
    void close();
}
