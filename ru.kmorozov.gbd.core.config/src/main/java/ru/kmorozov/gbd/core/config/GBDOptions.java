package ru.kmorozov.gbd.core.config;

import org.apache.commons.lang3.StringUtils;
import ru.kmorozov.gbd.core.config.options.CtxOptions;
import ru.kmorozov.gbd.core.config.options.PdfOptions;

/**
 * Created by km on 01.12.2015.
 */
public class GBDOptions {

    private static IGBDOptions INSTANCE;

    public static void init(IGBDOptions optionHolder) {
        GBDOptions.INSTANCE = optionHolder;
    }

    public static String getBookId() {
        return GBDOptions.INSTANCE.getBookId();
    }

    public static IStorage getStorage() {
        return GBDOptions.INSTANCE.getStorage();
    }

    public static String getProxyListFile() {
        return GBDOptions.INSTANCE.getProxyListFile();
    }

    public static int getImageWidth() {
        return GBDOptions.INSTANCE.getImageWidth();
    }

    public static boolean reloadImages() {
        return GBDOptions.INSTANCE.reloadImages();
    }

    public static boolean secureMode() {
        return GBDOptions.INSTANCE.secureMode();
    }

    public static PdfOptions pdfOptions() {
        return StringUtils.isEmpty(GBDOptions.INSTANCE.pdfOptions()) ? PdfOptions.DEFAULT_MODE : PdfOptions.getOption(GBDOptions.INSTANCE.pdfOptions());
    }

    public static int getImageWidth(int defaultValue) {
        return 0 == GBDOptions.INSTANCE.getImageWidth() ? defaultValue : GBDOptions.INSTANCE.getImageWidth();
    }

    public static boolean isValidConfig() {
        return GBDOptions.INSTANCE.getStorage().isValidOrCreate();
    }

    public static CtxOptions getCtxOptions() {
        return GBDOptions.INSTANCE.ctxOptions();
    }
}
