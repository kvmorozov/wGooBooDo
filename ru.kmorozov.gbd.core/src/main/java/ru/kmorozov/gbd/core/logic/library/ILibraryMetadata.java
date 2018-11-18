package ru.kmorozov.gbd.core.logic.library;

import ru.kmorozov.gbd.core.logic.connectors.HttpConnector;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookExtractor;
import ru.kmorozov.gbd.core.logic.extractors.base.IImageExtractor;

import java.util.List;

/**
 * Created by sbt-morozov-kv on 15.11.2016.
 */
public interface ILibraryMetadata {

    boolean isValidId(String bookId);

    IImageExtractor getExtractor(BookContext bookContext);

    AbstractBookExtractor getBookExtractor(String bookId);

    boolean needSetCookies();

    List<HttpConnector> preferredConnectors();
}
