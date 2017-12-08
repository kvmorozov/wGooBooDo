package com.wouterbreukink.onedrive.client.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.text.DecimalFormat;

public final class LogUtils {

    private LogUtils() {
    }

    public static String readableFileSize(final double size) {
        return readableFileSize((long) size);
    }

    public static String readableFileSize(final long size) {
        if (0 >= size) return "0";
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        final int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + ' ' + units[digitGroups];
    }

    public static String readableTime(final long ms) {

        if (1000 > ms) {
            return ms + "ms";
        } else if (60000 > ms) {
            return String.format("%.1fs", ms / 1000d);
        } else {
            final long seconds = ms / 1000;
            final long s = seconds % 60;
            final long m = (seconds / 60) % 60;
            final long h = (seconds / (60 * 60)) % 24;
            return String.format("%02d:%02d:%02d", h, m, s);
        }
    }

    public static String addFileLogger(final String logFile) {

        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();

        // Default log layout
        final Layout<? extends Serializable> layout =
                PatternLayout.createLayout("%d %p [%t] %m%n", null, null, null, Charset.defaultCharset(), true, true, null, null);

        // Create a new file appender for the given filename
        final FileAppender appender = FileAppender.createAppender(
                logFile,
                "false",
                "false",
                "FileAppender",
                "false",
                "true",
                "true",
                null,
                layout,
                null,
                null,
                null,
                config);

        appender.start();
        ((Logger) LogManager.getRootLogger()).addAppender(appender);

        return appender.getFileName();
    }
}
