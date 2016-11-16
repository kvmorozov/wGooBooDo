package ru.kmorozov.gbd.core.logic.extractors.base;

import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.output.consumers.AbstractOutput;
import ru.kmorozov.gbd.core.logic.output.events.AbstractEventSource;
import ru.kmorozov.gbd.core.utils.Logger;

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static ru.kmorozov.gbd.core.logic.context.ExecutionContext.INSTANCE;

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
public abstract class AbstractImageExtractor extends AbstractEventSource implements IUniqueRunnable<BookContext>, IImageExtractor {

    protected Logger logger;

    protected final AbstractOutput output;
    protected final BookContext bookContext;

    protected final AtomicBoolean initComplete = new AtomicBoolean(false);
    protected List<HttpHostExt> waitingProxy = new CopyOnWriteArrayList<>();

    protected AbstractImageExtractor(BookContext bookContext) {
        this.bookContext = bookContext;
        setProcessStatus(bookContext.getProgress());

        this.output = INSTANCE.getOutput();
    }


    @Override
    public BookContext getUniqueObject() {
        return bookContext;
    }

    @Override
    public String toString() {
        return "Extractor:" + bookContext.toString();
    }

    protected abstract void process();

    @Override
    public void run() {
        process();
    }

    protected void prepareDirectory() {
        String baseOutputDirPath = GBDOptions.getOutputDir();
        if (baseOutputDirPath == null) return;

        File baseOutputDir = new File(baseOutputDirPath);
        if (!baseOutputDir.exists()) if (!baseOutputDir.mkdir()) return;

        logger.info(INSTANCE.isSingleMode() ? String.format("Working with %s", bookContext.getBookInfo().getBookData().getTitle()) : "Starting...");

        bookContext.setOutputDir(new File(baseOutputDirPath + "\\" + bookContext.getBookInfo().getBookData().getTitle().replace(":", "").replace("<", "").replace(">", "").replace("/", ".") + " " + bookContext.getBookInfo().getBookData().getVolumeId()));
        File[] files = bookContext.getOutputDir().listFiles();
        bookContext.getProgress().resetMaxValue(files == null ? 0 : files.length);

        if (!bookContext.getOutputDir().exists()) {
            boolean dirResult = bookContext.getOutputDir().mkdir();
            if (!dirResult) {
                logger.severe(String.format("Invalid book title: %s", bookContext.getBookInfo().getBookData().getTitle()));
                return;
            }
        }
    }
}
