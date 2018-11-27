package ru.kmorozov.library.data.loader.impl;

import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.library.data.loader.ILoader;
import ru.kmorozov.library.data.model.book.Storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by sbt-morozov-kv on 14.03.2017.
 */
public abstract class BaseLoader implements ILoader, Runnable {

    private static final Logger logger = Logger.getLogger(BaseLoader.class);
    protected Collection<Object> links = new ArrayList<>();
    protected volatile LoaderExecutor.State state = LoaderExecutor.State.STOPPED;

    @Override
    public void run() {
        try {
            this.load();
        } catch (final IOException | UncheckedIOException e) {
            state = LoaderExecutor.State.STOPPED;

            e.printStackTrace();
        }
    }

    void setState(LoaderExecutor.State state) {
        this.state = state;
    }

    LoaderExecutor.State getState() {
        return this.state;
    }

    public boolean isStopped() {
        return LoaderExecutor.State.STOPPED == this.state;
    }

    public abstract Storage refresh(Storage storage);
}
