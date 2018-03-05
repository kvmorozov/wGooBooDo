package ru.kmorozov.gbd.core.config;

import ru.kmorozov.gbd.core.config.options.CtxOptions;
import ru.kmorozov.gbd.core.loader.LocalFSStorage;

/**
 * Created by km on 06.12.2015.
 */
public class LocalSystemOptions implements IGBDOptions {

    @Override
    public String getBookId() {
        return SystemConfigs.getLastBookId();
    }

    @Override
    public IStorage getStorage() {
        return new LocalFSStorage(SystemConfigs.getRootDir());
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

    @Override
    public CtxOptions ctxOptions() {
        return CtxOptions.DEFAULT_CTX_OPTIONS;
    }
}
