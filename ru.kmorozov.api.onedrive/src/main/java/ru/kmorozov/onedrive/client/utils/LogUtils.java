package ru.kmorozov.onedrive.client.utils;

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

    public static String readableFileSize(double size) {
        return LogUtils.readableFileSize((long) size);
    }

    public static String readableFileSize(long size) {
        if (0L >= size) return "0";
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10((double) size) / Math.log10(1024.0));
        return new DecimalFormat("#,##0.#").format((double) size / Math.pow(1024.0, (double) digitGroups)) + ' ' + units[digitGroups];
    }

    public static String readableTime(long ms) {

        if (1000L > ms) {
            return ms + "ms";
        } else if (60000L > ms) {
            return String.format("%.1fs", (double) ms / 1000.0d);
        } else {
            long seconds = ms / 1000L;
            long s = seconds % 60L;
            long m = (seconds / 60L) % 60L;
            long h = (seconds / (long) (60 * 60)) % 24L;
            return String.format("%02d:%02d:%02d", h, m, s);
        }
    }

    public static String addFileLogger(String logFile) {

        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();

        // Default log layout
        Layout<? extends Serializable> layout =
                PatternLayout.createLayout("%d %p [%t] %m%n", null, null, null, Charset.defaultCharset(), true, true, null, null);

        // Create a new file appender for the given filename
        FileAppender appender = FileAppender.createAppender(
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
