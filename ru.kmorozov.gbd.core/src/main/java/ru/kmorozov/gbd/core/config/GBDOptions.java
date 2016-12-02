package ru.kmorozov.gbd.core.config;

import java.io.File;

/**
 * Created by km on 01.12.2015.
 */
public class GBDOptions {

    private static IGBDOptions INSTANCE;

    private static File booksDir;

    public static void init(IGBDOptions optionHolder) {
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

    public static int getImageWidth(int defaultValue) {
        return INSTANCE.getImageWidth() == 0 ? defaultValue : INSTANCE.getImageWidth();
    }

    public static boolean isValidDirectory() {
        if (booksDir == null)
            booksDir = new File(GBDOptions.getOutputDir());

        return booksDir.exists() && booksDir.isDirectory();
    }

    public static File getBooksDir() {
        return booksDir;
    }
}
