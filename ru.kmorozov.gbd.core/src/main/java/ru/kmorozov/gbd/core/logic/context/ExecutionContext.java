package ru.kmorozov.gbd.core.logic.context;

import ru.kmorozov.gbd.core.logic.Proxy.AbstractProxyListProvider;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.extractors.IPostProcessor;
import ru.kmorozov.gbd.core.logic.extractors.ImageExtractor;
import ru.kmorozov.gbd.core.logic.output.consumers.AbstractOutput;
import ru.kmorozov.gbd.core.logic.progress.IProgress;
import ru.kmorozov.gbd.core.utils.Logger;
import ru.kmorozov.gbd.core.utils.QueuedThreadPoolExecutor;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by km on 22.11.2015.
 */
public class ExecutionContext {

    public static ExecutionContext INSTANCE;

    private static final String EMPTY = "";

    public final QueuedThreadPoolExecutor bookExecutor = new QueuedThreadPoolExecutor(150);
    public final QueuedThreadPoolExecutor pdfExecutor = new QueuedThreadPoolExecutor(5);

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

    public void addBookContext(IBookListProducer idsProducer, IProgress progress, IPostProcessor postProcessor) {
        for (String bookId : idsProducer.getBookIds()) {
            BookContext bookContext = bookContextMap.get(bookId);
            if (bookContext == null) {
                bookContext = new BookContext(bookId, progress, postProcessor);
                bookContextMap.put(bookId, bookContext);
            }
        }
    }

    public List<BookContext> getContexts(boolean shuffle) {
        List<BookContext> contexts = Arrays.asList(bookContextMap.values().toArray(new BookContext[bookContextMap.values().size()]));
        if (shuffle) Collections.shuffle(contexts);
        return contexts;
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

    public synchronized void updateBlacklist() {
        AbstractProxyListProvider.updateBlacklist();
    }

    public void execute() {
        for (BookContext bookContext : getContexts(true)) {
            ImageExtractor extractor = new ImageExtractor(bookContext);
            bookExecutor.execute(extractor);
            extractor.newProxyEvent(HttpHostExt.NO_PROXY);
        }

        AbstractProxyListProvider.getInstance().processProxyList();

        bookExecutor.terminate(1, TimeUnit.HOURS);
        pdfExecutor.terminate(1, TimeUnit.HOURS);
    }

    public void newProxyEvent(HttpHostExt proxy) {
        for (BookContext bookContext : getContexts(true))
            bookContext.getExtractor().newProxyEvent(proxy);
    }

    public void postProcessBook(BookContext bookContext) {
        pdfExecutor.execute(bookContext.getPostProcessor());
    }

    public boolean isSingleMode() {
        return singleMode;
    }
}
