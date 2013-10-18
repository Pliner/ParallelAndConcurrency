import java.net.URL;

public class CrawlerItem {
    public final URL Url;
    public final int Depth;

    public CrawlerItem(URL url, int depth) {
        Url = url;
        Depth = depth;
    }
}
