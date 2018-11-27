package ru.kmorozov.onedrive.client.walker;

import ru.kmorozov.onedrive.client.OneDriveItem;
import ru.kmorozov.onedrive.client.OneDriveProvider;
import ru.kmorozov.onedrive.client.walker.OneDriveWalker.EventType;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * Created by sbt-morozov-kv on 14.03.2017.
 */
public class OneDriveIterator<T extends OneDriveItem> implements Iterator<OneDriveItem>, Closeable {

    private final OneDriveWalker walker;
    private OneDriveWalker.Event next;

    OneDriveIterator(OneDriveProvider api, T root, int maxDepth, Predicate<OneDriveItem> skipCondition) {
        walker = new OneDriveWalker(api, maxDepth, skipCondition);
        next = walker.walk(root);

        assert EventType.ENTRY == next.type() || EventType.START_DIRECTORY == next.type();
    }

    @Override
    public void close() {
        walker.close();
    }

    @Override
    public boolean hasNext() {
        if (!walker.isOpen()) {
            throw new IllegalStateException();
        } else {
            fetchNextIfNeeded();
            return null != next;
        }
    }

    private void fetchNextIfNeeded() {
        if (null == next) {
            for (OneDriveWalker.Event event = walker.next(); null != event; event = walker.next()) {
                IOException exception = event.ioeException();
                if (null != exception) {
                    throw new UncheckedIOException(exception);
                }

                if (EventType.END_DIRECTORY != event.type()) {
                    next = event;
                    return;
                }
            }
        }
    }

    @Override
    public OneDriveItem next() {
        if (!walker.isOpen()) {
            throw new IllegalStateException();
        } else {
            fetchNextIfNeeded();
            if (null == next) {
                throw new NoSuchElementException();
            } else {
                OneDriveWalker.Event event = next;
                next = null;
                return event.getItem();
            }
        }
    }
}
