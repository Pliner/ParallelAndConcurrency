import java.util.concurrent.locks.ReentrantLock;

public class Fork extends ReentrantLock{
    public final int Id;

    public Fork(int id) {
        this.Id = id;
    }
}
