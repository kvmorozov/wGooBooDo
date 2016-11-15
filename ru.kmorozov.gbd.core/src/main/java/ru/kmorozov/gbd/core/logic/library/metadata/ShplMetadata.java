package ru.kmorozov.gbd.core.logic.library.metadata;

import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.extractors.IImageExtractor;
import ru.kmorozov.gbd.core.logic.library.ILibraryMetadata;

/**
 * Created by sbt-morozov-kv on 15.11.2016.
 */
public class ShplMetadata implements ILibraryMetadata {

    public static final ILibraryMetadata SHPL_METADATA = new ShplMetadata();

    private ShplMetadata() {
    }

    @Override
    public boolean isValidId(String bookId) {
        return bookId != null && bookId.contains("elib.shpl.ru");
    }

    @Override
    public IImageExtractor getExtractor(BookContext bookContext) {
        return null;
    }
}
