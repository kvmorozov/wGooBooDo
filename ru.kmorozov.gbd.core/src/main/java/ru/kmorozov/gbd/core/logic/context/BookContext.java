package ru.kmorozov.gbd.core.logic.context;

import ru.kmorozov.gbd.core.logic.extractors.base.IImageExtractor;
import ru.kmorozov.gbd.core.logic.extractors.base.IPostProcessor;
import ru.kmorozov.gbd.core.logic.library.ILibraryMetadata;
import ru.kmorozov.gbd.core.logic.library.LibraryFactory;
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage;
import ru.kmorozov.gbd.core.logic.model.book.base.BookInfo;
import ru.kmorozov.gbd.core.logic.model.book.base.IPage;
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

    public final QueuedThreadPoolExecutor<BookContext> sigExecutor = new QueuedThreadPoolExecutor(1, THREAD_POOL_SIZE, x -> true);
    public final QueuedThreadPoolExecutor<AbstractPage> imgExecutor;

    public AtomicBoolean started = new AtomicBoolean(false);
    public AtomicBoolean pdfCompleted = new AtomicBoolean(false);

    private final BookInfo bookInfo;
    private File outputDir;
    private IImageExtractor extractor;
    private final IProgress progress;
    private final IPostProcessor postProcessor;
    private long pagesBefore;
    private final ILibraryMetadata metadata;

    BookContext(String bookId, IProgress progress, IPostProcessor postProcessor) {
        this.bookInfo = LibraryFactory.getMetadata(bookId).getBookExtractor(bookId).getBookInfo();
        this.progress = progress;
        this.postProcessor = postProcessor;
        this.metadata = LibraryFactory.getMetadata(bookId);

        Predicate<AbstractPage> pagePredicate = AbstractPage::isDataProcessed;
        long pagesToProcess = Arrays.asList(bookInfo.getPages().getPages()).stream().filter(pagePredicate.negate()).count();
        imgExecutor = new QueuedThreadPoolExecutor(pagesToProcess, THREAD_POOL_SIZE, pagePredicate);
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
        postProcessor.setBookContext(this);
        return postProcessor;
    }

    public boolean isPdfCompleted() {
        return pdfCompleted.get();
    }

    public boolean isImgStarted() {
        return started.get();
    }

    public long getPagesBefore() {
        if (pagesBefore == 0l)
            pagesBefore = getPagesStream().filter(pageInfo -> pageInfo.dataProcessed.get()).count();
        return pagesBefore;
    }

    public Stream<AbstractPage> getPagesStream() {
        return Arrays.asList(bookInfo.getPages().getPages()).stream();
    }

    @Override
    public String toString() {
        return bookInfo.getBookId() + " " + bookInfo.getBookData().getTitle();
    }

    public IImageExtractor getExtractor() {
        if (extractor == null)
            extractor = metadata.getExtractor(this);

        return extractor;
    }
}
