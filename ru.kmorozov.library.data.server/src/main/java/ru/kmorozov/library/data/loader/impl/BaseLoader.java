package ru.kmorozov.library.data.loader.impl;

import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.library.data.loader.ILoader;
import ru.kmorozov.library.data.loader.impl.LoaderExecutor.State;
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
    protected volatile State state = State.STOPPED;

    @Override
    public void run() {
        try {
            load();
        } catch (IOException | UncheckedIOException e) {
            this.state = State.STOPPED;

            e.printStackTrace();
        }
    }

    void setState(final State state) {
        this.state = state;
    }

    State getState() {
        return state;
    }

    public boolean isStopped() {
        return State.STOPPED == state;
    }

    public abstract Storage refresh(Storage storage);
}
