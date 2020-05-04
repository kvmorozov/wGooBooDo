package com.logicaldoc.gbd.dictionary;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.kmorozov.gbd.core.config.IGBDOptions;
import ru.kmorozov.gbd.core.config.IStorage;
import ru.kmorozov.gbd.core.config.options.AuthOptions;
import ru.kmorozov.gbd.core.config.options.CtxOptions;

public class DictionaryOptions implements IGBDOptions {

    @NotNull
    @Override
    public String getBookId() {
        return null;
    }

    @NotNull
    @Override
    public IStorage getStorage() {
        return null;
    }

    @NotNull
    @Override
    public String getProxyListFile() {
        return null;
    }

    @Override
    public int getImageWidth() {
        return 0;
    }

    @Override
    public boolean getReloadImages() {
        return false;
    }

    @Override
    public boolean getSecureMode() {
        return false;
    }

    @NotNull
    @Override
    public String pdfOptions() {
        return null;
    }

    @NotNull
    @Override
    public CtxOptions ctxOptions() {
        return null;
    }

    @Override
    public boolean getDebugEnabled() {
        return false;
    }

    @Override
    public boolean getScanEnabled() {
        return false;
    }

    @Nullable
    @Override
    public AuthOptions authOptions() {
        return null;
    }
}
