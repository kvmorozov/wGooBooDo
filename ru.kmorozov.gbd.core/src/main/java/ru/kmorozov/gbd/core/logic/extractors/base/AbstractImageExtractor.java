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

    protected AbstractImageExtractor(BookContext bookContext, final Class<? extends AbstractImageExtractor> extractorClass) {
        this.bookContext = bookContext;
        this.setProcessStatus(bookContext.getProgress());
        this.logger = ExecutionContext.INSTANCE.getLogger(extractorClass, bookContext);

        output = ExecutionContext.INSTANCE.getOutput();
    }

    @Override
    public BookContext getUniqueObject() {
        return this.bookContext;
    }

    protected abstract void restoreState();

    @Override
    public String toString() {
        return "Extractor:" + this.bookContext;
    }

    public final void process() {
        if (!this.bookContext.started.compareAndSet(false, true)) return;

        if (!this.preCheck()) return;

        this.prepareStorage();
        this.restoreState();

        this.initComplete.set(true);
    }

    protected abstract boolean preCheck();

    @Override
    public void run() {
        this.process();

        while (!this.initComplete.get()) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.waitingProxy.forEach(this::newProxyEvent);
    }

    protected void prepareStorage() {
        if (!GBDOptions.getStorage().isValidOrCreate()) return;

        this.logger.info(ExecutionContext.INSTANCE.isSingleMode() ? String.format("Working with %s", this.bookContext.getBookInfo().getBookData().getTitle()) : "Starting...");

        try {
            this.bookContext.setStorage(GBDOptions.getStorage().getChildStorage(this.bookContext.getBookInfo().getBookData()));
            this.bookContext.getProgress().resetMaxValue(this.bookContext.getStorage().size());
        } catch (final IOException e) {
            this.logger.error(e);
        }

        if (!this.bookContext.getStorage().isValidOrCreate())
            this.logger.severe(String.format("Invalid book title: %s", this.bookContext.getBookInfo().getBookData().getTitle()));
    }
}
