package ru.kmorozov.App.Network;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by km on 21.11.2015.
 */
public class ImageExtractor {

    private static Logger logger = Logger.getLogger(ImageExtractor.class.getName());

    private static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.143 Safari/537.36";

    private String url;

    public ImageExtractor(String url) {
        this.url = url;
    }

    public void process() {
        try {
            Document doc = Jsoup
                    .connect(url)
                    .userAgent(USER_AGENT)
                    .get();
            Elements media = doc.select("[src]");

            for (Element src : media) {
                if (src.tagName().equals("img"))
                    print(" * %s: <%s> %sx%s (%s)",
                            src.tagName(), src.attr("abs:src"), src.attr("width"), src.attr("height"),
                            trim(src.attr("alt"), 20));
                else
                    print(" * %s: <%s>", src.tagName(), src.attr("abs:src"));
            }
        } catch (HttpStatusException hse) {
            logger.log(Level.SEVERE, "Cannot process images: " + hse.getMessage() + " (" + hse.getStatusCode() + ")");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Cannot process images!");
        }
    }

    private static void print(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }

    private static String trim(String s, int width) {
        if (s.length() > width)
            return s.substring(0, width - 1) + ".";
        else
            return s;
    }


}
