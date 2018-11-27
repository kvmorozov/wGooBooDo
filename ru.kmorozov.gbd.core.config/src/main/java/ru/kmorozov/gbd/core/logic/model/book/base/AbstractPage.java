package ru.kmorozov.gbd.core.logic.model.book.base;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by km on 18.11.2016.
 */
public abstract class AbstractPage implements IPage {

    private final AtomicBoolean sigChecked = new AtomicBoolean(false);
    private final AtomicBoolean dataProcessed = new AtomicBoolean(false);
    private final AtomicBoolean fileExists = new AtomicBoolean(false);
    private final AtomicBoolean loadingStarted = new AtomicBoolean(false);

    @Override
    public boolean isDataProcessed() {
        return this.dataProcessed.get();
    }

    @Override
    public boolean isFileExists() {
        return this.fileExists.get();
    }

    @Override
    public boolean isSigChecked() {
        return this.sigChecked.get();
    }

    @Override
    public boolean isLoadingStarted() {
        return this.loadingStarted.get();
    }

    public boolean isProcessed() {
        return this.isDataProcessed() || this.sigChecked.get() || this.isFileExists();
    }

    public boolean isNotProcessed() {
        return !this.isProcessed();
    }

    public abstract String getImgUrl();

    public void setSigChecked(final boolean value) {
        this.sigChecked.set(value);
    }

    public void setDataProcessed(final boolean value) {
        this.dataProcessed.set(value);
    }

    public void setFileExists(final boolean value) {
        this.fileExists.set(value);
    }

    public void setLoadingStarted(final boolean value) {
        this.loadingStarted.set(value);
    }
}
