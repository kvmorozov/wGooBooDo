package ru.kmorozov.gbd.core.logic.context;

import ru.kmorozov.gbd.core.config.IBaseLoader;
import ru.kmorozov.gbd.core.logic.Proxy.AbstractProxyListProvider;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.Proxy.UrlType;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractHttpProcessor;
import ru.kmorozov.gbd.core.logic.extractors.base.IImageExtractor;
import ru.kmorozov.gbd.core.logic.extractors.base.IPostProcessor;
import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.gbd.logger.consumers.AbstractOutputReceiver;
import ru.kmorozov.gbd.logger.progress.IProgress;
import ru.kmorozov.gbd.utils.QueuedThreadPoolExecutor;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by km on 22.11.2015.
 */
public final class ExecutionContext {

    static final Logger logger = Logger.getLogger(ExecutionContext.class);

    private static final String EMPTY = "";
    public static ExecutionContext INSTANCE;
    private final boolean singleMode;
    private final AbstractOutputReceiver output;
    private final Map<String, BookContext> bookContextMap = new HashMap<>();
    public QueuedThreadPoolExecutor<BookContext> bookExecutor;
    public QueuedThreadPoolExecutor<BookContext> pdfExecutor;

    private ExecutionContext(final AbstractOutputReceiver output, final boolean singleMode) {
        this.output = output;
        this.singleMode = singleMode;
    }

    public static synchronized void initContext(final AbstractOutputReceiver output, final boolean singleMode) {
        INSTANCE = new ExecutionContext(output, singleMode);
    }

    public Logger getLogger(final Class<?> clazz, final BookContext bookContext) {
        return Logger.getLogger(output, clazz.getName(), singleMode || null == bookContext ? EMPTY : bookContext.getBookInfo().getBookData().getTitle() + ": ");
    }

    public Logger getLogger(final Class<?> clazz) {
        return getLogger(clazz, null);
    }

    public Logger getLogger(final String location) {
        return Logger.getLogger(output, location, EMPTY);
    }

    public void addBookContext(final IBookListProducer idsProducer, final IProgress progress, final IPostProcessor postProcessor) {
        for (final String bookId : idsProducer.getBookIds()) {
            try {
                bookContextMap.computeIfAbsent(bookId, k -> new BookContext(bookId, progress, postProcessor));
            } catch (final Exception ex) {
                logger.severe("Cannot add book " + bookId + " because of " + ex.getMessage());
            }
        }
    }

    public List<BookContext> getContexts(final boolean shuffle) {
        final List<BookContext> contexts = Arrays.asList(bookContextMap.values().toArray(new BookContext[bookContextMap.values().size()]));
        if (shuffle) Collections.shuffle(contexts);
        return contexts;
    }

    public AbstractOutputReceiver getOutput() {
        return output;
    }

    public static int getProxyCount() {
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

        for (final BookContext bookContext : getContexts(true)) {
            final IImageExtractor extractor = bookContext.getExtractor();
            extractor.newProxyEvent(HttpHostExt.NO_PROXY);
            bookExecutor.execute(extractor);
        }

        AbstractProxyListProvider.getInstance().processProxyList(UrlType.GOOGLE_BOOKS);

        bookExecutor.terminate(10, TimeUnit.MINUTES);
        pdfExecutor.terminate(30, TimeUnit.MINUTES);

        final long totalProcessed = getContexts(false).stream().mapToLong(BookContext::getPagesProcessed).sum();
        getLogger("Total").info("Total pages processed: " + totalProcessed);

        IBaseLoader contextProvider = ContextProvider.getContextProvider();

        contextProvider.updateIndex();
        contextProvider.updateContext();
        updateBlacklist();
        AbstractHttpProcessor.close();
    }

    public static void sendProxyEvent(final HttpHostExt proxy) {
        if (INSTANCE != null)
            INSTANCE.newProxyEvent(proxy);
    }

    private void newProxyEvent(final HttpHostExt proxy) {
        for (final BookContext bookContext : getContexts(true))
            bookContext.getExtractor().newProxyEvent(proxy);
    }

    public void postProcessBook(final BookContext bookContext) {
        pdfExecutor.execute(bookContext.getPostProcessor());
    }

    public boolean isSingleMode() {
        return singleMode;
    }

    public Iterable<String> getBookIds() {
        return bookContextMap.keySet();
    }
}