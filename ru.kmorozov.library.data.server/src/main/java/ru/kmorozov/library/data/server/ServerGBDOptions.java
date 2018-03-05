package ru.kmorozov.library.data.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.kmorozov.gbd.core.config.IGBDOptions;
import ru.kmorozov.gbd.core.config.IStorage;
import ru.kmorozov.gbd.core.config.options.CtxOptions;
import ru.kmorozov.onedrive.client.OneDriveItem;
import ru.kmorozov.onedrive.client.OneDriveProvider;

@Component
public class ServerGBDOptions implements IGBDOptions {

    @Autowired
    @Lazy
    private OneDriveProvider api;

    @Autowired
    @Lazy
    private OneDriveItem root;

    @Override
    public String getBookId() {
        return null;
    }

    @Override
    public IStorage getStorage() {
        return new ServerStorage(api, root);
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
