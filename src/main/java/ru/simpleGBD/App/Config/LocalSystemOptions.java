package ru.simpleGBD.App.Config;

import ru.simpleGBD.App.Logic.Runtime.ImageExtractor;

/**
 * Created by km on 06.12.2015.
 */
public class LocalSystemOptions implements IGBDOptions {

    @Override
    public String getBookId() {
        return SystemConfigs.getLastBookId();
    }

    @Override
    public String getOutputDir() {
        return SystemConfigs.getRootDir();
    }

    @Override
    public String getProxyListFile() {
        return SystemConfigs.getProxyListFile();
    }

    @Override
    public int getImageWidth() {
        return ImageExtractor.DEFAULT_PAGE_WIDTH;
    }

    @Override
    public boolean reloadImages() {
        return false;
    }
}
