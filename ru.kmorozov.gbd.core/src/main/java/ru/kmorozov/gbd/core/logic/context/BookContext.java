package ru.kmorozov.gbd.core.logic.context;

import ru.kmorozov.gbd.core.logic.extractors.BookInfoExtractor;
import ru.kmorozov.gbd.core.logic.model.book.BookInfo;

import java.io.File;

import static ru.kmorozov.gbd.core.logic.extractors.ImageExtractor.BOOK_ID_PLACEHOLDER;
import static ru.kmorozov.gbd.core.logic.extractors.ImageExtractor.HTTPS_TEMPLATE;

/**
 * Created by km on 08.11.2016.
 */
public class BookContext {

    private String baseUrl;
    private final BookInfo bookInfo;
    private File outputDir;

    BookContext(String bookId) {
        this.bookInfo = (new BookInfoExtractor(bookId)).getBookInfo();

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
}
