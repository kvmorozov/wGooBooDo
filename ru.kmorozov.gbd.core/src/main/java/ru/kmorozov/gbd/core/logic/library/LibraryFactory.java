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

    public static ILibraryMetadata getMetadata(String bookId) {
        for (ILibraryMetadata _metadata : LibraryFactory.METADATA)
            if (_metadata.isValidId(bookId)) {
                LibraryFactory.selectedMetadata = _metadata;
                return _metadata;
            }

        return null;
    }

    public static boolean isValidId(String bookId) {
        return null != LibraryFactory.getMetadata(bookId);
    }

    public static boolean needSetCookies() {
        return LibraryFactory.selectedMetadata.needSetCookies();
    }

    public static List<HttpConnector> preferredConnectors() {
        return LibraryFactory.selectedMetadata.preferredConnectors();
    }
}
