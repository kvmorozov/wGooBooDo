package ru.kmorozov.library.data.loader.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.kmorozov.library.data.model.book.Book;
import ru.kmorozov.library.data.model.book.Storage;
import ru.kmorozov.library.data.server.condition.StorageEnabledCondition;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by sbt-morozov-kv on 04.04.2017.
 */

@Component
@Conditional(StorageEnabledCondition.class)
public class LoaderExecutor {

    public enum State {
        STARTED, STOPPED, PAUSED
    }

    @Autowired @Lazy
    private OneDriveLoader oneLoader;

    private static final Executor loaderExecutor = Executors.newSingleThreadExecutor();

    public boolean isStarted() {
        return LoaderExecutor.State.STARTED == this.oneLoader.getState();
    }

    public synchronized void start() {
        if (LoaderExecutor.State.STARTED == this.oneLoader.getState())
            throw new IllegalStateException("Loader is already started!");

        this.oneLoader.setState(LoaderExecutor.State.STARTED);

        LoaderExecutor.loaderExecutor.execute(this.oneLoader);
    }

    public synchronized void stop() {
        this.oneLoader.setState(LoaderExecutor.State.STOPPED);
    }

    public synchronized void pause() {
        this.oneLoader.setState(LoaderExecutor.State.PAUSED);
    }

    public Storage refresh(Storage storage) {
        return this.oneLoader.refresh(storage);
    }

    public void resolveLink(Book lnkBook) {
        this.oneLoader.resolveLink(lnkBook);
    }

    public void downloadBook(Book book) {
        this.oneLoader.downloadBook(book);
    }
}
