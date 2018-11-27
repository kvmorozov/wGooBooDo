package ru.kmorozov.gbd.core.logic.context;

import ru.kmorozov.gbd.core.config.IStorage;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractHttpProcessor;
import ru.kmorozov.gbd.core.logic.extractors.base.IImageExtractor;
import ru.kmorozov.gbd.core.logic.extractors.base.IPostProcessor;
import ru.kmorozov.gbd.core.logic.library.ILibraryMetadata;
import ru.kmorozov.gbd.core.logic.library.LibraryFactory;
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage;
import ru.kmorozov.db.core.logic.model.book.BookInfo;
import ru.kmorozov.gbd.core.logic.model.book.base.IPage;
import ru.kmorozov.gbd.logger.progress.IProgress;
import ru.kmorozov.gbd.utils.QueuedThreadPoolExecutor;

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

    BookContext(String bookId, IProgress progress, IPostProcessor postProcessor) {
        bookInfo = LibraryFactory.getMetadata(bookId).getBookExtractor(bookId).getBookInfo();
        this.progress = progress;
        this.postProcessor = postProcessor;
        metadata = LibraryFactory.getMetadata(bookId);

        this.pagesBefore = this.getPagesStream().filter(pageInfo -> pageInfo.isFileExists()).count();
        this.sigExecutor = new QueuedThreadPoolExecutor<>(1L, QueuedThreadPoolExecutor.THREAD_POOL_SIZE, x -> true, "Sig_" + bookId);
        this.imgExecutor = new QueuedThreadPoolExecutor<>(0L, QueuedThreadPoolExecutor.THREAD_POOL_SIZE, BookContext.pagePredicate, "Img_" + bookId);
    }

    public String getBookId() {
        return this.bookInfo.getBookId();
    }

    public BookInfo getBookInfo() {
        return this.bookInfo;
    }

    public IStorage getStorage() {
        return this.storage;
    }

    public void setStorage(IStorage storage) {
        this.storage = storage;
    }

    public IProgress getProgress() {
        return this.progress;
    }

    public Runnable getPostProcessor() {
        return this.postProcessor.getPostProcessor(this);
    }

    public boolean isPdfCompleted() {
        return this.pdfCompleted.get();
    }

    public boolean isImgStarted() {
        return this.started.get();
    }

    public long getPagesBefore() {
        return this.pagesBefore;
    }

    public void setPagesBefore(long pagesBefore) {
        this.pagesBefore = pagesBefore;
    }

    public Stream<IPage> getPagesStream() {
        return Arrays.stream(this.bookInfo.getPages().getPages());
    }

    public long getPagesProcessed() {
        return this.pagesProcessed;
    }

    public void setPagesProcessed(long pagesProcessed) {
        this.pagesProcessed = pagesProcessed;
    }

    @Override
    public String toString() {
        return this.bookInfo.getBookId() + ' ' + this.bookInfo.getBookData().getTitle();
    }

    public IImageExtractor getExtractor() {
        if (null == this.extractor) this.extractor = this.metadata.getExtractor(this);

        return this.extractor;
    }
}
