package main;

public final class Fork implements IFork {

    private int id;

    public Fork(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "[" + id + "]";
    }
}
