package ru.kmorozov.library.utils;

import ru.kmorozov.library.data.model.book.BookInfo.BookFormat;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by km on 26.12.2016.
 */
public class BookUtils {

    private static final Logger logger = Logger.getLogger(BookUtils.class.getName());

    public static BookFormat getFormat(final String fileName) {
        for (final BookFormat format : BookFormat.values())
            if (fileName.endsWith(format.getExt()))
                return format;

        logger.log(Level.INFO, "Unknown format for file " + fileName);
        return BookFormat.UNKNOWN;
    }

    public static String humanReadableByteCount(final long bytes, final boolean si) {
        final int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        final int exp = (int) (Math.log(bytes) / Math.log(unit));
        final String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
