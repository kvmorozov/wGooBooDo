package ru.kmorozov.gbd.core.logic.library;

import ru.kmorozov.gbd.core.logic.connectors.HttpConnector;
import ru.kmorozov.gbd.core.logic.library.metadata.GoogleBooksMetadata;
import ru.kmorozov.gbd.core.logic.library.metadata.RfbrMetadata;
import ru.kmorozov.gbd.core.logic.library.metadata.ShplMetadata;

import java.util.List;

/**
 * Created by sbt-morozov-kv on 15.11.2016.
 */
public final class LibraryFactory {

    private static final ILibraryMetadata[] METADATA = {GoogleBooksMetadata.GOOGLE_METADATA, ShplMetadata.SHPL_METADATA, RfbrMetadata.RFBR_METADATA};

    private static ILibraryMetadata selectedMetadata;

    private LibraryFactory() {
    }

    public static ILibraryMetadata getMetadata(final String bookId) {
        for (final ILibraryMetadata _metadata : METADATA)
            if (_metadata.isValidId(bookId)) {
                selectedMetadata = _metadata;
                return _metadata;
            }

        return null;
    }

    public static boolean isValidId(final String bookId) {
        return null != getMetadata(bookId);
    }

    public static boolean needSetCookies() {
        return selectedMetadata.needSetCookies();
    }

    public static List<HttpConnector> preferredConnectors() {
        return selectedMetadata.preferredConnectors();
    }
}
