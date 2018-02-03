package ru.kmorozov.gbd.core.config;

import org.apache.commons.lang3.StringUtils;
import ru.kmorozov.gbd.core.config.options.CtxOptions;
import ru.kmorozov.gbd.core.config.options.PdfOptions;

import java.io.File;

/**
 * Created by km on 01.12.2015.
 */
public class GBDOptions {

    private static IGBDOptions INSTANCE;

    private static File booksDir;

    public static void init(final IGBDOptions optionHolder) {
        INSTANCE = optionHolder;
    }

    public static String getBookId() {
        return INSTANCE.getBookId();
    }

    public static String getOutputDir() {
        return INSTANCE.getOutputDir();
    }

    public static String getProxyListFile() {
        return INSTANCE.getProxyListFile();
    }

    public static int getImageWidth() {
        return INSTANCE.getImageWidth();
    }

    public static boolean reloadImages() {
        return INSTANCE.reloadImages();
    }

    public static boolean secureMode() {
        return INSTANCE.secureMode();
    }

    public static PdfOptions pdfOptions() {
        return StringUtils.isEmpty(INSTANCE.pdfOptions()) ? PdfOptions.DEFAULT_MODE : PdfOptions.getOption(INSTANCE.pdfOptions());
    }

    public static int getImageWidth(final int defaultValue) {
        return 0 == INSTANCE.getImageWidth() ? defaultValue : INSTANCE.getImageWidth();
    }

    public static boolean isValidDirectory() {
        if (null == booksDir) booksDir = new File(getOutputDir());

        return booksDir.exists() && booksDir.isDirectory();
    }

    public static File getBooksDir() {
        return booksDir;
    }

    public static CtxOptions getCtxOptions() {
        return INSTANCE.ctxOptions();
    }
}
