package ru.kmorozov.gbd.core.logic.context;

import ru.kmorozov.gbd.core.logic.Proxy.AbstractProxyListProvider;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.extractors.IPostProcessor;
import ru.kmorozov.gbd.core.logic.extractors.ImageExtractor;
import ru.kmorozov.gbd.core.logic.output.consumers.AbstractOutput;
import ru.kmorozov.gbd.core.logic.progress.IProgress;
import ru.kmorozov.gbd.core.utils.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by km on 22.11.2015.
 */
public class ExecutionContext {

    public static ExecutionContext INSTANCE;

    private static final String EMPTY = "";

    private final boolean singleMode;
    private final AbstractOutput output;
    private final Map<String, BookContext> bookContextMap = new HashMap<>();
    private String baseUrl;

    private ExecutionContext(AbstractOutput output, boolean singleMode) {
        this.output = output;
        this.singleMode = singleMode;
    }

    public synchronized static void initContext(AbstractOutput output, boolean singleMode) {
        INSTANCE = new ExecutionContext(output, singleMode);
    }

    public Logger getLogger(Class clazz, BookContext bookContext) {
        return Logger.getLogger(output, clazz.getName(), singleMode || bookContext == null ? EMPTY : bookContext.getBookInfo().getBookData().getTitle() + ": ");
    }

    public Logger getLogger(Class clazz) {
        return getLogger(clazz, null);
    }

    public Logger getLogger(String location) {
        return Logger.getLogger(output, location, EMPTY);
    }

    public BookContext getBookContext(String bookId) {
        BookContext bookContext = bookContextMap.get(bookId);
        if (bookContext == null) {
            bookContext = new BookContext(bookId);
            bookContextMap.put(bookId, bookContext);
        }

        return bookContext;
    }

    public AbstractOutput getOutput() {
        return output;
    }

    public String getBaseUrl() {
        if (baseUrl == null) baseUrl = bookContextMap.values().stream().findAny().get().getBaseUrl();

        return baseUrl;
    }

    public int getProxyCount() {
        return AbstractProxyListProvider.getInstance().getProxyCount();
    }

    public synchronized void updateProxyList() {
        AbstractProxyListProvider.getInstance().updateProxyList();
    }

    public void execute(IProgress progress, IPostProcessor postProcessor) {
        for (BookContext bookContext : bookContextMap.values()) {
            ImageExtractor extractor = new ImageExtractor(bookContext, progress, postProcessor);
            extractor.process();
            extractor.newProxyEvent(HttpHostExt.NO_PROXY);
        }

        Iterator<HttpHostExt> proxyIterator = AbstractProxyListProvider.getInstance().getProxyList();
        while (proxyIterator.hasNext()) for (BookContext bookContext : bookContextMap.values())
            bookContext.getExtractor().newProxyEvent(proxyIterator.next());
    }
}
