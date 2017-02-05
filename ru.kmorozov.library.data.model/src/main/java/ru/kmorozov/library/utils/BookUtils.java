package ru.kmorozov.library.utils;

import ru.kmorozov.library.data.model.book.BookInfo;

import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by km on 26.12.2016.
 */
public class BookUtils {

    private static final Logger logger = Logger.getLogger(BookUtils.class.getName());

    public static BookInfo.BookFormat getFormat(Path filePath) {
        String pathStr = filePath.toString();
        for (BookInfo.BookFormat format : BookInfo.BookFormat.values())
            if (pathStr.endsWith(format.getExt()))
                return format;

        logger.log(Level.INFO, "Unknown format for file " + pathStr);
        return BookInfo.BookFormat.UNKNOWN;
    }
}
