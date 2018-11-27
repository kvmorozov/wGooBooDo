package ru.kmorozov.library.data.server.options;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.kmorozov.gbd.core.config.IStorage;
import ru.kmorozov.gbd.core.config.options.CtxOptions;
import ru.kmorozov.gbd.core.loader.LocalFSStorage;

@Component
@Qualifier("local")
public class LocalServerGBDOptions extends AbstractServerGBDOptions {

    @Value("${library.local.directory}")
    private String localLibraryPath;

    @Override
    public IStorage getStorage() {
        return new LocalFSStorage(this.localLibraryPath);
    }

    @Override
    public CtxOptions ctxOptions() {
        return CtxOptions.DEFAULT_CTX_OPTIONS;
    }
}
