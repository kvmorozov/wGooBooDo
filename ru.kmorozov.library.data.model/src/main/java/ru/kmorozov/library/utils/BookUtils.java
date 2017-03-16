package ru.kmorozov.library.utils;

import ru.kmorozov.library.data.model.book.BookInfo;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by km on 26.12.2016.
 */
public class BookUtils {

    private static final Logger logger = Logger.getLogger(BookUtils.class.getName());

    public static BookInfo.BookFormat getFormat(String fileName) {
        for (BookInfo.BookFormat format : BookInfo.BookFormat.values())
            if (fileName.endsWith(format.getExt()))
                return format;

        logger.log(Level.INFO, "Unknown format for file " + fileName);
        return BookInfo.BookFormat.UNKNOWN;
    }
}
