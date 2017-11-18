package ru.kmorozov.gbd.core.logic.library.metadata;

import ru.kmorozov.gbd.core.logic.connectors.HttpConnector;
import ru.kmorozov.gbd.core.logic.connectors.google.GoogleHttpConnector;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookExtractor;
import ru.kmorozov.gbd.core.logic.extractors.base.IImageExtractor;
import ru.kmorozov.gbd.core.logic.extractors.google.GoogleBookInfoExtractor;
import ru.kmorozov.gbd.core.logic.extractors.google.GoogleImageExtractor;
import ru.kmorozov.gbd.core.logic.library.ILibraryMetadata;

/**
 * Created by sbt-morozov-kv on 15.11.2016.
 */
public class GoogleBooksMetadata implements ILibraryMetadata {

    public static final ILibraryMetadata GOOGLE_METADATA = new GoogleBooksMetadata();

    private GoogleBooksMetadata() {
    }

    @Override
    public boolean isValidId(String bookId) {
        return bookId != null && bookId.length() == 12;
    }

    @Override
    public IImageExtractor getExtractor(BookContext bookContext) {
        return new GoogleImageExtractor(bookContext);
    }

    @Override
    public AbstractBookExtractor getBookExtractor(String bookId) {
        return new GoogleBookInfoExtractor(bookId);
    }

    @Override
    public boolean needSetCookies() {
        return true;
    }

    @Override
    public HttpConnector preferredConnector() {
        return new GoogleHttpConnector();
    }
}
