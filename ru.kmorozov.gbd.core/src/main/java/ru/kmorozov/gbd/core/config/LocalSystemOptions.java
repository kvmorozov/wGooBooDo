package ru.kmorozov.gbd.core.config;

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
        return SystemConfigs.getResolution();
    }

    @Override
    public boolean reloadImages() {
        return SystemConfigs.getReload();
    }

    @Override
    public boolean secureMode() {
        return SystemConfigs.getSecureMode();
    }

    @Override
    public String pdfOptions() {
        return SystemConfigs.getPdfMode();
    }
}
