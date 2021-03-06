import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrawlerUtils {

    public static Pattern linkPattern = Pattern.compile("<a href=\"([^\"#]+)\"");

    public static String getContent(URL url) {
        StringBuilder page = new StringBuilder ();
        BufferedReader in = null;
        try {
            URLConnection conn = url.openConnection();
            String contentType = conn.getContentType();
            if (contentType != null && contentType.startsWith("text/html")) {
                if (contentType.indexOf("charset=") == -1) {
                    in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"), 8192 * 100);
                } else {
                    String encoding = contentType.substring(contentType.indexOf("charset=") + 8);
                    in = new BufferedReader(new InputStreamReader(conn.getInputStream(), encoding), 8192 * 100);
                }
                String str;
                while ((str = in.readLine()) != null) {
                    page.append(str);
                }
                return page.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(in != null)
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return null;
    }

    public static Set<URL> getLinks(URL url, String content) {
        Set<URL> links = new HashSet<>();
        Matcher matcher = linkPattern.matcher(content);
        while (matcher.find()) {
            try {
                URL link = new URL(url, matcher.group(1));
                links.add(link);
            } catch (MalformedURLException e) {
            }
        }
        return links;
    }

    public static void main(String[] args) {
        args = new String[] {"in", "http://www.yandex.ru"};
        BufferedWriter output = null;
        try {

            File dataFile = new File(args[0]);
            output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataFile), "UTF-8"));

            URL page = new URL(args[1]);
            
            String content = getContent(page);
            if (content != null) { // HTML content found!                
                output.write(page + "\t" + content + "\n");

                Set<URL> links = getLinks(page, content);                
                System.out.println("Found " + links.size() + " links:");
                for (URL link : links) {
                    System.out.println(link);
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
