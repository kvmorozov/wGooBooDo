package ru.kmorozov.gbd.core.logic.library.metadata;

import ru.kmorozov.gbd.core.logic.connectors.HttpConnector;
import ru.kmorozov.gbd.core.logic.connectors.google.GoogleHttpConnector;
import ru.kmorozov.gbd.core.logic.connectors.http2native.Http2Connector;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookExtractor;
import ru.kmorozov.gbd.core.logic.extractors.base.IImageExtractor;
import ru.kmorozov.gbd.core.logic.extractors.google.GoogleBookInfoExtractor;
import ru.kmorozov.gbd.core.logic.extractors.google.GoogleImageExtractor;
import ru.kmorozov.gbd.core.logic.library.ILibraryMetadata;

/**
 * Created by sbt-morozov-kv on 15.11.2016.
 */
public final class GoogleBooksMetadata implements ILibraryMetadata {

    public static final ILibraryMetadata GOOGLE_METADATA = new GoogleBooksMetadata();

    private GoogleBooksMetadata() {
    }

    @Override
    public boolean isValidId(final String bookId) {
        return null != bookId && 12 == bookId.length();
    }

    @Override
    public IImageExtractor getExtractor(final BookContext bookContext) {
        return new GoogleImageExtractor(bookContext);
    }

    @Override
    public AbstractBookExtractor getBookExtractor(final String bookId) {
        return new GoogleBookInfoExtractor(bookId);
    }

    @Override
    public boolean needSetCookies() {
        return true;
    }

    @Override
    public HttpConnector preferredConnector() {
        return new Http2Connector();
    }
}
