import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;


public class EntryPoint {
    public static class Terminator {
        private volatile boolean isTerminated;

        public void terminate() {
            isTerminated = true;
        }

        public boolean isTerminated() {
            return isTerminated;
        }
    }

    public static void main(String[] args) throws MalformedURLException {
        if(args.length != 4) {
            System.out.println("usage: crawler homePage maxDepth maxLinks homeDir");
            System.exit(0);
        }
        URL homePage = new URL(args[0]);
        int maxDepth = Integer.parseInt(args[1]);
        int maxLinks = Integer.parseInt(args[2]);
        File homeDir = new File(args[3]);

        int crawlersCount = Runtime.getRuntime().availableProcessors() * 2;
        CrawlerQueue queue = new CrawlerQueue(maxLinks, maxDepth);
        Crawler[] crawlers = new Crawler[crawlersCount];
        Thread[] threads = new Thread[crawlersCount];
        Terminator terminator = new Terminator();
        queue.push(new CrawlerItem(homePage, 0));


        for (int i = 0; i < crawlersCount; ++i)
            crawlers[i] = new Crawler(terminator, queue, homeDir);
        for (int i = 0; i < crawlersCount; ++i) {
            threads[i] = new Thread(crawlers[i]);
            threads[i].start();
        }


        while(! terminator.isTerminated()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        for (int i = 0; i < crawlersCount; ++i)
            crawlers[i].stop();
        for (int i = 0; i < crawlersCount; ++i) {
            threads[i].interrupt();
            try {
                threads[i].join();
            } catch (InterruptedException e) {
            }
        }

    }

}
