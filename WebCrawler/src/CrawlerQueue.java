import java.net.URL;
import java.util.*;

public class CrawlerQueue {
    private final Set<URL> used = new HashSet<>();

    private final ArrayList<Queue<CrawlerItem>> queues;
    private volatile int totalItems = 0;
    private final int maxTotalItems;
    private final int maxDepth;

    public CrawlerQueue(int maxTotalItems, int maxDepth) {
        this.maxTotalItems = maxTotalItems;
        this.maxDepth = maxDepth;

        this.queues = new ArrayList<>(maxDepth);
        for (int i = 0; i < maxDepth; ++i)
            queues.add(new LinkedList<CrawlerItem>());
    }

    public synchronized boolean push(CrawlerItem queueItem) {
       if (maxTotalItems <= totalItems)
            return false;
        if (queueItem.Depth == maxDepth)
            return false;
        if(used.contains(queueItem.Url))
            return true;
        used.add(queueItem.Url);
        queues.get(queueItem.Depth).offer(queueItem);
        return true;
    }

    public synchronized CrawlerItem pop() {
        for (int i = 0; i < maxDepth; ++i)
            if(!queues.get(i).isEmpty())
                return queues.get(i).poll();
        return null;
    }

    public synchronized void incrementProcessedFiles() {
        ++totalItems;
    }
}
