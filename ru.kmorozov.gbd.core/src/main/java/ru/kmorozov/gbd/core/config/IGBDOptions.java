package ru.kmorozov.gbd.core.config;

/**
 * Created by km on 06.12.2015.
 */
interface IGBDOptions {

    String getBookId();
    String getOutputDir();
    String getProxyListFile();
    int getImageWidth();
    boolean reloadImages();
    boolean fillGaps();
    boolean secureMode();
}
