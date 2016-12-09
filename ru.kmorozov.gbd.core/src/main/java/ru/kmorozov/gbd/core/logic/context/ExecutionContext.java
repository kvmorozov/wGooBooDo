package ru.kmorozov.gbd.core.logic.context;

import ru.kmorozov.gbd.core.config.storage.AbstractContextProvider;
import ru.kmorozov.gbd.core.logic.Proxy.AbstractProxyListProvider;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.extractors.base.IImageExtractor;
import ru.kmorozov.gbd.core.logic.extractors.base.IPostProcessor;
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

    public QueuedThreadPoolExecutor bookExecutor;
    public QueuedThreadPoolExecutor pdfExecutor;

    private final boolean singleMode;
    private final AbstractOutput output;
    private final Map<String, BookContext> bookContextMap = new HashMap<>();
    private final static AbstractContextProvider contextProvider = AbstractContextProvider.getContextProvider();

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
            bookContextMap.computeIfAbsent(bookId, k -> new BookContext(bookId, progress, postProcessor));
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
        bookExecutor = new QueuedThreadPoolExecutor<>(bookContextMap.size(), 5, BookContext::isImgStarted, "bookExecutor");
        pdfExecutor = new QueuedThreadPoolExecutor<>(bookContextMap.size(), 5, BookContext::isPdfCompleted, "pdfExecutor");

        for (BookContext bookContext : getContexts(true)) {
            IImageExtractor extractor = bookContext.getExtractor();
            extractor.newProxyEvent(HttpHostExt.NO_PROXY);
            bookExecutor.execute(extractor);
        }

        AbstractProxyListProvider.getInstance().processProxyList();

        bookExecutor.terminate(10, TimeUnit.MINUTES);
        pdfExecutor.terminate(30, TimeUnit.MINUTES);

        contextProvider.updateIndex();
        contextProvider.updateContext();
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

    public Collection<String> getBookIds() {
        return bookContextMap.keySet();
    }
}
