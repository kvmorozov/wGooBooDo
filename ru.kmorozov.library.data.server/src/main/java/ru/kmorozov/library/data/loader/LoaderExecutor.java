package ru.kmorozov.library.data.loader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.kmorozov.library.data.model.book.Book;
import ru.kmorozov.library.data.model.book.Storage;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by sbt-morozov-kv on 04.04.2017.
 */

@Component
public class LoaderExecutor {

    public enum State {
        STARTED, STOPPED, PAUSED
    }

    @Autowired
    private OneDriveLoader oneLoader;

    private static final Executor loaderExecutor = Executors.newSingleThreadExecutor();

    public boolean isStarted() {
        return State.STARTED == oneLoader.getState();
    }

    public synchronized void start() {
        if (State.STARTED == oneLoader.getState())
            throw new IllegalStateException("Loader is already started!");

        oneLoader.setState(State.STARTED);

        loaderExecutor.execute(oneLoader);
    }

    public synchronized void stop() {
        oneLoader.setState(State.STOPPED);
    }

    public synchronized void pause() {
        oneLoader.setState(State.PAUSED);
    }

    public Storage refresh(final Storage storage) {
        return oneLoader.refresh(storage);
    }

    public void resolveLink(final Book lnkBook) {
        oneLoader.resolveLink(lnkBook);
    }

    public void downloadBook(final Book book) {
        oneLoader.downloadBook(book);
    }
}
