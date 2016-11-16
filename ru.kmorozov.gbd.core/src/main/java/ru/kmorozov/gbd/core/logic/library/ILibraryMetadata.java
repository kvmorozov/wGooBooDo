package ru.kmorozov.gbd.core.logic.library;

import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.extractors.base.IImageExtractor;

/**
 * Created by sbt-morozov-kv on 15.11.2016.
 */
public interface ILibraryMetadata {

    boolean isValidId(String bookId);

    IImageExtractor getExtractor(BookContext bookContext);
}
