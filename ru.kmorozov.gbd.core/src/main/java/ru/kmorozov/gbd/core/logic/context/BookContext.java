package ru.kmorozov.gbd.core.logic.context;

import ru.kmorozov.gbd.core.logic.extractors.base.AbstractHttpProcessor;
import ru.kmorozov.gbd.core.logic.extractors.base.IImageExtractor;
import ru.kmorozov.gbd.core.logic.extractors.base.IPostProcessor;
import ru.kmorozov.gbd.core.logic.library.ILibraryMetadata;
import ru.kmorozov.gbd.core.logic.library.LibraryFactory;
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage;
import ru.kmorozov.gbd.core.logic.model.book.base.BookInfo;
import ru.kmorozov.gbd.core.logic.progress.IProgress;
import ru.kmorozov.gbd.core.utils.QueuedThreadPoolExecutor;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static ru.kmorozov.gbd.core.utils.QueuedThreadPoolExecutor.THREAD_POOL_SIZE;

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
    private File outputDir;
    private IImageExtractor extractor;
    private long pagesBefore, pagesProcessed;

    BookContext(String bookId, IProgress progress, IPostProcessor postProcessor) {
        this.bookInfo = LibraryFactory.getMetadata(bookId).getBookExtractor(bookId).getBookInfo();
        this.progress = progress;
        this.postProcessor = postProcessor;
        this.metadata = LibraryFactory.getMetadata(bookId);

        pagesBefore = getPagesStream().filter(pageInfo -> pageInfo.fileExists.get()).count();
        sigExecutor = new QueuedThreadPoolExecutor<>(1, THREAD_POOL_SIZE, x -> true, "Sig_" + bookId);
        imgExecutor = new QueuedThreadPoolExecutor<>(0, THREAD_POOL_SIZE, pagePredicate, "Img_" + bookId);
    }

    public String getBookId() {
        return bookInfo.getBookId();
    }

    public BookInfo getBookInfo() {
        return bookInfo;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public IProgress getProgress() {
        return progress;
    }

    public IPostProcessor getPostProcessor() {
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

    public void setPagesBefore(long pagesBefore) {
        this.pagesBefore = pagesBefore;
    }

    public Stream<AbstractPage> getPagesStream() {
        return Arrays.stream(bookInfo.getPages().getPages());
    }

    public long getPagesProcessed() {
        return pagesProcessed;
    }

    public void setPagesProcessed(long pagesProcessed) {
        this.pagesProcessed = pagesProcessed;
    }

    @Override
    public String toString() {
        return bookInfo.getBookId() + " " + bookInfo.getBookData().getTitle();
    }

    public IImageExtractor getExtractor() {
        if (extractor == null) extractor = metadata.getExtractor(this);

        return extractor;
    }
}
