package ru.simpleGBD.App.Config;

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
}
