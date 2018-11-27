package ru.kmorozov.library.data.server.options;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.kmorozov.gbd.core.config.IGBDOptions;
import ru.kmorozov.gbd.core.config.IStorage;
import ru.kmorozov.gbd.core.config.options.CtxOptions;
import ru.kmorozov.library.data.server.storage.ServerStorage;
import ru.kmorozov.onedrive.client.OneDriveItem;
import ru.kmorozov.onedrive.client.OneDriveProvider;

@Component
@Qualifier("remote")
public class ServerGBDOptions extends AbstractServerGBDOptions {

    @Autowired
    @Lazy
    private OneDriveProvider api;

    @Autowired
    @Lazy
    private OneDriveItem root;

    @Override
    public IStorage getStorage() {
        return new ServerStorage(this.api, this.root);
    }

    @Override
    public CtxOptions ctxOptions() {
        return null;
    }

}
