package ru.kmorozov.gbd.core.logic.model.book.base;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by km on 18.11.2016.
 */
public abstract class AbstractPage implements IPage {

    private AtomicBoolean sigChecked = new AtomicBoolean(false);
    private AtomicBoolean dataProcessed = new AtomicBoolean(false);
    private AtomicBoolean fileExists = new AtomicBoolean(false);
    private AtomicBoolean loadingStarted = new AtomicBoolean(false);

    @Override
    public boolean isDataProcessed() {
        return dataProcessed.get();
    }

    @Override
    public boolean isFileExists() {
        return fileExists.get();
    }

    @Override
    public boolean isSigChecked() {
        return sigChecked.get();
    }

    @Override
    public boolean isLoadingStarted() {
        return loadingStarted.get();
    }

    public boolean isProcessed() {
        return isDataProcessed() || sigChecked.get() || isFileExists();
    }

    public boolean isNotProcessed() {
        return !isProcessed();
    }

    public abstract String getImgUrl();

    public void setSigChecked(boolean value) {
        sigChecked.set(value);
    }

    public void setDataProcessed(boolean value) {
        dataProcessed.set(value);
    }

    public void setFileExists(boolean value) {
        fileExists.set(value);
    }

    public void setLoadingStarted(boolean value) {
        loadingStarted.set(value);
    }
}
