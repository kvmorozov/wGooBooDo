package ru.kmorozov.gbd.core.logic.model.book.base;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by km on 18.11.2016.
 */
public abstract class AbstractPage implements IPage {

    public AtomicBoolean sigChecked = new AtomicBoolean(false);
    public AtomicBoolean dataProcessed = new AtomicBoolean(false);
    public AtomicBoolean fileExists = new AtomicBoolean(false);
    public AtomicBoolean loadingStarted = new AtomicBoolean(false);

    public boolean isDataProcessed() {
        return dataProcessed.get();
    }

    public boolean isFileExists() {
        return fileExists.get();
    }

    public boolean isProcessed() {
        return isDataProcessed() || sigChecked.get() || isFileExists();
    }

    public boolean isNotProcessed() {
        return !isProcessed();
    }
}
