package ru.kmorozov.gbd.core.logic.library;

import ru.kmorozov.gbd.core.logic.connectors.HttpConnector;
import ru.kmorozov.gbd.core.logic.library.metadata.GoogleBooksMetadata;
import ru.kmorozov.gbd.core.logic.library.metadata.ShplMetadata;

/**
 * Created by sbt-morozov-kv on 15.11.2016.
 */
public final class LibraryFactory {

    private static final ILibraryMetadata[] METADATA = new ILibraryMetadata[]{GoogleBooksMetadata.GOOGLE_METADATA, ShplMetadata.SHPL_METADATA};

    private static ILibraryMetadata selectedMetadata;

    private LibraryFactory() {
    }

    public static ILibraryMetadata getMetadata(String bookId) {
        for (ILibraryMetadata _metadata : METADATA)
            if (_metadata.isValidId(bookId)) {
                selectedMetadata = _metadata;
                return _metadata;
            }

        return null;
    }

    public static boolean isValidId(String bookId) {
        return getMetadata(bookId) != null;
    }

    public static boolean needSetCookies() {
        return selectedMetadata.needSetCookies();
    }

    public static HttpConnector preferredConnector() {
        return selectedMetadata.preferredConnector();
    }
}
