package ru.kmorozov.gbd.core.logic.extractors.base;

import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.gbd.logger.consumers.AbstractOutputReceiver;
import ru.kmorozov.gbd.logger.events.AbstractEventSource;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
public abstract class AbstractImageExtractor extends AbstractEventSource implements IUniqueRunnable<BookContext>, IImageExtractor {

    protected final AbstractOutputReceiver output;
    protected final BookContext bookContext;
    protected final AtomicBoolean initComplete = new AtomicBoolean(false);
    protected final Logger logger;
    protected Collection<HttpHostExt> waitingProxy = new CopyOnWriteArrayList<>();

    protected AbstractImageExtractor(final BookContext bookContext, Class<? extends AbstractImageExtractor> extractorClass) {
        this.bookContext = bookContext;
        setProcessStatus(bookContext.getProgress());
        logger = ExecutionContext.INSTANCE.getLogger(extractorClass, bookContext);

        this.output = ExecutionContext.INSTANCE.getOutput();
    }

    @Override
    public BookContext getUniqueObject() {
        return bookContext;
    }

    protected abstract void scanDir();

    @Override
    public String toString() {
        return "Extractor:" + bookContext;
    }

    public final void process() {
        if (!bookContext.started.compareAndSet(false, true)) return;

        if (!preCheck()) return;

        prepareStorage();
        scanDir();

        initComplete.set(true);
    }

    protected abstract boolean preCheck();

    @Override
    public void run() {
        process();

        while (!initComplete.get()) {
            try {
                Thread.sleep(1000L);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
        waitingProxy.forEach(this::newProxyEvent);
    }

    protected void prepareStorage() {
        if (!GBDOptions.getStorage().isValidOrCreate()) return;

        logger.info(ExecutionContext.INSTANCE.isSingleMode() ? String.format("Working with %s", bookContext.getBookInfo().getBookData().getTitle()) : "Starting...");

        try {
            bookContext.setStorage(GBDOptions.getStorage().getChildStorage(bookContext.getBookInfo().getBookData()));
            bookContext.getProgress().resetMaxValue(bookContext.getStorage().size());
        } catch (IOException e) {
            logger.error(e);
        }

        if (!bookContext.getStorage().isValidOrCreate())
            logger.severe(String.format("Invalid book title: %s", bookContext.getBookInfo().getBookData().getTitle()));
    }
}
