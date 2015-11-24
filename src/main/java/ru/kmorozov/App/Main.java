package ru.kmorozov.App;

import ru.kmorozov.App.Logic.Proxy.ProxyListProvider;
import ru.kmorozov.App.Logic.Runtime.ImageExtractor;

import java.util.logging.Logger;

public class Main {

    private static Logger logger = Logger.getLogger(Main.class.getName());

    private static final String TEST_BOOK_URL = "https://books.google.ru/books?id=BEvEV9OVzacC";

    public static void main(String[] args) {
        if (ProxyListProvider.INSTANCE.getProxyList() != null && ProxyListProvider.INSTANCE.getProxyList().size() > 0)
            logger.info(String.format("Starting with %s proxies.", ProxyListProvider.INSTANCE.getProxyList().size()));
        ImageExtractor extractor = new ImageExtractor(TEST_BOOK_URL);

        extractor.process();
    }
}
