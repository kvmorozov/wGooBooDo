package ru.kmorozov.gbd.core.logic.library.metadata;

import ru.kmorozov.db.core.config.IContextLoader;
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector;
import ru.kmorozov.gbd.core.logic.connectors.apache.ApacheHttpConnector;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookExtractor;
import ru.kmorozov.gbd.core.logic.extractors.base.IImageExtractor;
import ru.kmorozov.gbd.core.logic.extractors.shpl.ShplBookExtractor;
import ru.kmorozov.gbd.core.logic.extractors.shpl.ShplImageExtractor;
import ru.kmorozov.gbd.core.logic.library.ILibraryMetadata;

import java.util.Arrays;
import java.util.List;

/**
 * Created by sbt-morozov-kv on 15.11.2016.
 */
public final class ShplMetadata implements ILibraryMetadata {

    public static final String SHPL_BASE_URL = "elib.shpl.ru";

    public static final ILibraryMetadata SHPL_METADATA = new ShplMetadata();

    private ShplMetadata() {
    }

    @Override
    public boolean isValidId(String bookId) {
        return null != bookId && bookId.contains(ShplMetadata.SHPL_BASE_URL);
    }

    @Override
    public IImageExtractor getExtractor(BookContext bookContext) {
        return new ShplImageExtractor(bookContext);
    }

    @Override
    public AbstractBookExtractor getBookExtractor(String bookId) {
        return new ShplBookExtractor(bookId);
    }

    @Override
    public AbstractBookExtractor getBookExtractor(final String bookId, final IContextLoader storedLoader) {
        return this.getBookExtractor(bookId);
    }

    @Override
    public boolean needSetCookies() {
        return false;
    }

    @Override
    public List<HttpConnector> preferredConnectors() {
        return Arrays.asList(new ApacheHttpConnector());
    }
}
