package ru.simpleGBD.App;

import ru.simpleGBD.App.Logic.Proxy.IProxyListProvider;
import ru.simpleGBD.App.Logic.Proxy.WebProxyListProvider;
import ru.simpleGBD.App.Logic.Runtime.ImageExtractor;

import java.util.logging.Logger;

public class Main {

    private static Logger logger = Logger.getLogger(Main.class.getName());

    private static final String TEST_BOOK_URL = "https://books.google.ru/books?id=BEvEV9OVzacC";

    public static void main(String[] args) {
        if (IProxyListProvider.getInstance().getProxyList() != null && IProxyListProvider.getInstance().getProxyList().size() > 0)
            logger.info(String.format("Starting with %s proxies.", WebProxyListProvider.INSTANCE.getProxyList().size()));
        ImageExtractor extractor = new ImageExtractor(TEST_BOOK_URL);

        extractor.process();
    }
}
