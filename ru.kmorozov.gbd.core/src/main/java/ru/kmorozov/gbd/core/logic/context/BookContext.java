package ru.kmorozov.gbd.core.logic.context;

import ru.kmorozov.db.core.logic.model.book.BookInfo;
import ru.kmorozov.gbd.core.config.IStorage;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractHttpProcessor;
import ru.kmorozov.gbd.core.logic.extractors.base.IImageExtractor;
import ru.kmorozov.gbd.core.logic.extractors.base.IPostProcessor;
import ru.kmorozov.gbd.core.logic.library.ILibraryMetadata;
import ru.kmorozov.gbd.core.logic.library.LibraryFactory;
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage;
import ru.kmorozov.gbd.core.logic.model.book.base.IPage;
import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.gbd.logger.progress.IProgress;
import ru.kmorozov.gbd.utils.QueuedThreadPoolExecutor;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Created by km on 08.11.2016.
 */
public class BookContext {

    private static final Predicate<AbstractPage> pagePredicate = AbstractPage::isDataProcessed;
    public final QueuedThreadPoolExecutor<? extends AbstractHttpProcessor> sigExecutor;
    public final QueuedThreadPoolExecutor<AbstractPage> imgExecutor;
    private final BookInfo bookInfo;
    private final IProgress progress;
    private final IPostProcessor postProcessor;
    private final ILibraryMetadata metadata;
    public AtomicBoolean started = new AtomicBoolean(false);
    public AtomicBoolean pdfCompleted = new AtomicBoolean(false);
    private IStorage storage;
    private IImageExtractor extractor;
    private long pagesBefore, pagesProcessed;
    private final Logger logger;

    BookContext(final String bookId, final IProgress progress, final IPostProcessor postProcessor) {
        this.bookInfo = LibraryFactory.getMetadata(bookId).getBookExtractor(bookId).getBookInfo();
        this.progress = progress;
        this.postProcessor = postProcessor;
        this.metadata = LibraryFactory.getMetadata(bookId);

        pagesBefore = getPagesStream().filter(pageInfo -> pageInfo.isFileExists()).count();
        sigExecutor = new QueuedThreadPoolExecutor<>(1L, QueuedThreadPoolExecutor.THREAD_POOL_SIZE, x -> true, "Sig_" + bookId);
        imgExecutor = new QueuedThreadPoolExecutor<>(0L, QueuedThreadPoolExecutor.THREAD_POOL_SIZE, pagePredicate, "Img_" + bookId);

        logger = ExecutionContext.INSTANCE.getLogger(BookContext.class, this);
    }

    public String getBookId() {
        return bookInfo.getBookId();
    }

    public BookInfo getBookInfo() {
        return bookInfo;
    }

    public IStorage getStorage() {
        return storage;
    }

    public void setStorage(final IStorage storage) {
        this.storage = storage;
    }

    public IProgress getProgress() {
        return progress;
    }

    public Runnable getPostProcessor() {
        return postProcessor.getPostProcessor(this);
    }

    public boolean isPdfCompleted() {
        return pdfCompleted.get();
    }

    public boolean isImgStarted() {
        return started.get();
    }

    public long getPagesBefore() {
        return pagesBefore;
    }

    public void setPagesBefore(final long pagesBefore) {
        this.pagesBefore = pagesBefore;
    }

    public Stream<IPage> getPagesStream() {
        return Arrays.stream(bookInfo.getPages().getPages());
    }

    public long getPagesProcessed() {
        return pagesProcessed;
    }

    public void setPagesProcessed(final long pagesProcessed) {
        this.pagesProcessed = pagesProcessed;
    }

    @Override
    public String toString() {
        return bookInfo.getBookId() + ' ' + bookInfo.getBookData().getTitle();
    }

    public IImageExtractor getExtractor() {
        if (null == extractor) extractor = metadata.getExtractor(this);

        return extractor;
    }

    public void restoreState() {
        try {
            getPagesStream().filter(IPage::isFileExists).forEach(page -> {
                try {
                    if (!storage.isPageExists(page)) {
                        logger.severe(String.format("Page %s not found in storage!", page.getPid()));
                        ((AbstractPage) page).setDataProcessed(false);
                        ((AbstractPage) page).setFileExists(false);
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            });

            storage.restoreState(bookInfo);
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            if (null != getProgress()) getProgress().finish();
        }

        setPagesBefore(getPagesStream().filter(IPage::isFileExists).count());
    }
}
