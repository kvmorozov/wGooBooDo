package ru.kmorozov.gbd.core.logic.extractors.base;

import org.apache.commons.lang3.StringUtils;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.output.consumers.AbstractOutput;
import ru.kmorozov.gbd.core.logic.output.events.AbstractEventSource;
import ru.kmorozov.gbd.core.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static ru.kmorozov.gbd.core.logic.context.ExecutionContext.INSTANCE;

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
public abstract class AbstractImageExtractor extends AbstractEventSource implements IUniqueRunnable<BookContext>, IImageExtractor {

    protected final AbstractOutput output;
    protected final BookContext bookContext;
    protected final AtomicBoolean initComplete = new AtomicBoolean(false);
    protected Logger logger;
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

    protected abstract void scanDir();

    @Override
    public String toString() {
        return "Extractor:" + bookContext.toString();
    }

    public final void process() {
        if (!bookContext.started.compareAndSet(false, true)) return;

        if (!preCheck()) return;

        prepareDirectory();
        scanDir();

        initComplete.set(true);
    }

    protected abstract boolean preCheck();

    @Override
    public void run() {
        process();

        while (!initComplete.get()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        waitingProxy.forEach(this::newProxyEvent);
    }

    protected void prepareDirectory() {
        String baseOutputDirPath = GBDOptions.getOutputDir();
        if (baseOutputDirPath == null) return;

        File baseOutputDir = new File(baseOutputDirPath);
        if (!baseOutputDir.exists()) if (!baseOutputDir.mkdir()) return;

        logger.info(INSTANCE.isSingleMode() ? String.format("Working with %s", bookContext.getBookInfo().getBookData().getTitle()) : "Starting...");

        bookContext.setOutputDir(new File(getDirectoryName(baseOutputDirPath)));
        File[] files = bookContext.getOutputDir().listFiles();
        bookContext.getProgress().resetMaxValue(files == null ? 0 : files.length);

        if (!bookContext.getOutputDir().exists()) {
            boolean dirResult = bookContext.getOutputDir().mkdir();
            if (!dirResult) {
                logger.severe(String.format("Invalid book title: %s", bookContext.getBookInfo().getBookData().getTitle()));
            }
        }
    }

    private String getDirectoryName(String baseOutputDirPath) {
        try {
            Optional<Path> optPath = Files.find(Paths.get(baseOutputDirPath), 1, (path, basicFileAttributes) -> path.toString().contains(bookContext.getBookInfo().getBookData()
                                                                                                                                                    .getVolumeId())).findAny();
            if (optPath.isPresent()) return optPath.get().toString();
        } catch (IOException ignored) {
        }

        String directoryName = baseOutputDirPath + "\\" + bookContext.getBookInfo().getBookData().getTitle()
                                                                     .replace(":", "")
                                                                     .replace("<", "")
                                                                     .replace(">", "")
                                                                     .replace("/", ".");
        String volumeId = bookContext.getBookInfo().getBookData().getVolumeId();
        return StringUtils.isEmpty(volumeId) ? directoryName : directoryName + " " + bookContext.getBookInfo().getBookData().getVolumeId();
    }
}
