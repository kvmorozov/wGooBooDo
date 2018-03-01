package com.kmorozov.onedrive.client.walker;

import com.kmorozov.onedrive.client.OneDriveItem;
import com.kmorozov.onedrive.client.OneDriveProvider;
import com.kmorozov.onedrive.client.walker.OneDriveWalker.Event;

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
    private Event next;

    OneDriveIterator(final OneDriveProvider api, final T root, final int maxDepth, final Predicate<OneDriveItem> skipCondition) {
        this.walker = new OneDriveWalker(api, maxDepth, skipCondition);
        this.next = this.walker.walk(root);

        assert OneDriveWalker.EventType.ENTRY == this.next.type() || OneDriveWalker.EventType.START_DIRECTORY == this.next.type();
    }

    @Override
    public void close() {
        this.walker.close();
    }

    @Override
    public boolean hasNext() {
        if (!this.walker.isOpen()) {
            throw new IllegalStateException();
        } else {
            this.fetchNextIfNeeded();
            return null != this.next;
        }
    }

    private void fetchNextIfNeeded() {
        if (null == this.next) {
            for (Event event = this.walker.next(); null != event; event = this.walker.next()) {
                final IOException exception = event.ioeException();
                if (null != exception) {
                    throw new UncheckedIOException(exception);
                }

                if (OneDriveWalker.EventType.END_DIRECTORY != event.type()) {
                    this.next = event;
                    return;
                }
            }
        }
    }

    @Override
    public OneDriveItem next() {
        if (!this.walker.isOpen()) {
            throw new IllegalStateException();
        } else {
            this.fetchNextIfNeeded();
            if (null == this.next) {
                throw new NoSuchElementException();
            } else {
                final Event event = this.next;
                this.next = null;
                return event.getItem();
            }
        }
    }
}
