package ru.kmorozov.library.data.server;

import ru.kmorozov.gbd.core.config.IGBDOptions;
import ru.kmorozov.gbd.core.config.IStorage;
import ru.kmorozov.gbd.core.config.options.CtxOptions;

public class ServerGBDOptions implements IGBDOptions {

    @Override
    public String getBookId() {
        return null;
    }

    @Override
    public IStorage getStorage() {
        return new ServerStorage();
    }

    @Override
    public String getProxyListFile() {
        return null;
    }

    @Override
    public int getImageWidth() {
        return 0;
    }

    @Override
    public boolean reloadImages() {
        return false;
    }

    @Override
    public boolean secureMode() {
        return false;
    }

    @Override
    public String pdfOptions() {
        return null;
    }

    @Override
    public CtxOptions ctxOptions() {
        return null;
    }
}
