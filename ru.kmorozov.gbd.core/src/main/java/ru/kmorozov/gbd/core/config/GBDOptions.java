package ru.kmorozov.gbd.core.config;

/**
 * Created by km on 01.12.2015.
 */
public class GBDOptions {

    private static IGBDOptions INSTANCE;

    public static void init(IGBDOptions optionHolder) {
        INSTANCE = optionHolder;
    }

    public static String getBookId() {return INSTANCE.getBookId();}
    public static String getOutputDir() {return INSTANCE.getOutputDir();}
    public static String getProxyListFile() {return INSTANCE.getProxyListFile();}
    public static int getImageWidth() {return INSTANCE.getImageWidth();}
    public static boolean reloadImages() {return INSTANCE.reloadImages();}
    public static boolean fillGaps() {return INSTANCE.fillGaps();}
    public static boolean secureMode() {return INSTANCE.secureMode();}
}