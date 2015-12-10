package ru.simpleGBD.App.Config;

/**
 * Created by km on 06.12.2015.
 */
public interface IGBDOptions {

    String getBookId();
    String getOutputDir();
    String getProxyListFile();
    int getImageWidth();
    boolean reloadImages();
}
