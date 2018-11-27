package ru.kmorozov.gbd.core.logic.context;

import ru.kmorozov.db.core.config.IContextLoader;
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

    private ExecutionContext(AbstractOutputReceiver output, boolean singleMode) {
        this.output = output;
        this.singleMode = singleMode;
    }

    public static synchronized void initContext(AbstractOutputReceiver output, boolean singleMode) {
        ExecutionContext.INSTANCE = new ExecutionContext(output, singleMode);
    }

    public Logger getLogger(Class<?> clazz, BookContext bookContext) {
        return Logger.getLogger(this.output, clazz.getName(), this.singleMode || null == bookContext ? ExecutionContext.EMPTY : bookContext.getBookInfo().getBookData().getTitle() + ": ");
    }

    public Logger getLogger(Class<?> clazz) {
        return this.getLogger(clazz, null);
    }

    public Logger getLogger(String location) {
        return Logger.getLogger(this.output, location, ExecutionContext.EMPTY);
    }

    public void addBookContext(IBookListProducer idsProducer, IProgress progress, IPostProcessor postProcessor) {
        for (String bookId : idsProducer.getBookIds()) {
            try {
                this.bookContextMap.computeIfAbsent(bookId, k -> new BookContext(bookId, progress, postProcessor));
            } catch (Exception ex) {
                ExecutionContext.logger.severe("Cannot add book " + bookId + " because of " + ex.getMessage());
            }
        }
    }

    public List<BookContext> getContexts(boolean shuffle) {
        List<BookContext> contexts = Arrays.asList(this.bookContextMap.values().toArray(new BookContext[0]));
        if (shuffle) Collections.shuffle(contexts);
        return contexts;
    }

    public AbstractOutputReceiver getOutput() {
        return this.output;
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
        this.bookExecutor = new QueuedThreadPoolExecutor<>((long) this.bookContextMap.size(), 5, BookContext::isImgStarted, "bookExecutor");
        this.pdfExecutor = new QueuedThreadPoolExecutor<>((long) this.bookContextMap.size(), 5, BookContext::isPdfCompleted, "pdfExecutor");

        for (BookContext bookContext : this.getContexts(true)) {
            IImageExtractor extractor = bookContext.getExtractor();
            extractor.newProxyEvent(HttpHostExt.NO_PROXY);
            this.bookExecutor.execute(extractor);
        }

        AbstractProxyListProvider.getInstance().processProxyList(UrlType.GOOGLE_BOOKS);

        this.bookExecutor.terminate(10L, TimeUnit.MINUTES);
        this.pdfExecutor.terminate(30L, TimeUnit.MINUTES);

        long totalProcessed = this.getContexts(false).stream().mapToLong(BookContext::getPagesProcessed).sum();
        this.getLogger("Total").info("Total pages processed: " + totalProcessed);

        final IContextLoader contextProvider = ContextProvider.getContextProvider();

        contextProvider.updateIndex();
        contextProvider.updateContext();
        this.updateBlacklist();
        AbstractHttpProcessor.close();
    }

    public static void sendProxyEvent(HttpHostExt proxy) {
        if (ExecutionContext.INSTANCE != null)
            ExecutionContext.INSTANCE.newProxyEvent(proxy);
    }

    private void newProxyEvent(HttpHostExt proxy) {
        for (BookContext bookContext : this.getContexts(true))
            bookContext.getExtractor().newProxyEvent(proxy);
    }

    public void postProcessBook(BookContext bookContext) {
        this.pdfExecutor.execute(bookContext.getPostProcessor());
    }

    public boolean isSingleMode() {
        return this.singleMode;
    }

    public Iterable<String> getBookIds() {
        return this.bookContextMap.keySet();
    }
}