package ru.kmorozov.gbd.core.config;

import ru.kmorozov.gbd.core.config.options.CtxOptions;

/**
 * Created by km on 06.12.2015.
 */
public interface IGBDOptions {

    String getBookId();

    String getOutputDir();

    String getProxyListFile();

    int getImageWidth();

    boolean reloadImages();

    boolean secureMode();

    String pdfOptions();

    CtxOptions ctxOptions();
}
