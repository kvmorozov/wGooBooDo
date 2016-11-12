package ru.kmorozov.gbd.core.logic.context;

import ru.kmorozov.gbd.core.logic.extractors.BookInfoExtractor;
import ru.kmorozov.gbd.core.logic.extractors.IPostProcessor;
import ru.kmorozov.gbd.core.logic.extractors.ImageExtractor;
import ru.kmorozov.gbd.core.logic.model.book.BookInfo;
import ru.kmorozov.gbd.core.logic.progress.IProgress;
import ru.kmorozov.gbd.core.utils.QueuedThreadPoolExecutor;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import static ru.kmorozov.gbd.core.logic.extractors.ImageExtractor.BOOK_ID_PLACEHOLDER;
import static ru.kmorozov.gbd.core.logic.extractors.ImageExtractor.HTTPS_TEMPLATE;

/**
 * Created by km on 08.11.2016.
 */
public class BookContext {

    public final QueuedThreadPoolExecutor sigExecutor = new QueuedThreadPoolExecutor();
    public final QueuedThreadPoolExecutor imgExecutor = new QueuedThreadPoolExecutor();

    public AtomicBoolean started = new AtomicBoolean(false);

    private String baseUrl;
    private final BookInfo bookInfo;
    private File outputDir;
    private ImageExtractor extractor;
    private final IProgress progress;
    private final IPostProcessor postProcessor;

    BookContext(String bookId, IProgress progress, IPostProcessor postProcessor) {
        this.bookInfo = (new BookInfoExtractor(bookId)).getBookInfo();
        this.progress = progress;
        this.postProcessor = postProcessor;

        baseUrl = HTTPS_TEMPLATE.replace(BOOK_ID_PLACEHOLDER, bookInfo.getBookId());
    }

    public String getBookId() {
        return bookInfo.getBookId();
    }

    public String getBaseUrl() {
        return baseUrl;
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

    public ImageExtractor getExtractor() {
        return extractor;
    }

    public void setExtractor(ImageExtractor extractor) {
        this.extractor = extractor;
    }

    public IProgress getProgress() {
        return progress;
    }

    public IPostProcessor getPostProcessor() {
        postProcessor.setBookContext(this);
        return postProcessor;
    }
}
