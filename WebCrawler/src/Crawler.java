import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.UUID;

public class Crawler implements Runnable {

    private EntryPoint.Terminator terminator;
    private CrawlerQueue queue;
    private File baseDirectory;
    private volatile boolean canContinue = true;

    public Crawler(EntryPoint.Terminator terminator, CrawlerQueue queue, File baseDirectory) {
        this.terminator = terminator;
        this.queue = queue;
        this.baseDirectory = baseDirectory;
    }

    @Override
    public void run() {
        while (canContinue) {
            CrawlerItem queueItem = queue.pop();
            if (queueItem == null) {
                try {
                    Thread.sleep(100);
                    continue;
                } catch (InterruptedException e) {
                    break;
                }
            }

            URL url = queueItem.Url;
            String content = CrawlerUtils.getContent(url);
            if (content != null) {
                Set<URL> links = CrawlerUtils.getLinks(url, content);
                for (URL link : links) {

                    if (!queue.push(new CrawlerItem(link, queueItem.Depth + 1))) {
                        canContinue = false;
                        terminator.terminate();
                        break;
                    }
                }
                File file = new File(baseDirectory, UUID.randomUUID().toString());
                try (FileWriter writer = new FileWriter(file)) {
                    writer.append(content);
                } catch (IOException e) {
                    break;
                }
                queue.incrementProcessedFiles();
            }
        }
    }

    public void stop() {
        canContinue = false;
    }
}
