package com.wouterbreukink.onedrive.client.walker;

import com.wouterbreukink.onedrive.client.OneDriveItem;
import com.wouterbreukink.onedrive.client.OneDriveProvider;

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
        this.walker = new OneDriveWalker(api, maxDepth, skipCondition);
        this.next = this.walker.walk(root);

        assert this.next.type() == OneDriveWalker.EventType.ENTRY || this.next.type() == OneDriveWalker.EventType.START_DIRECTORY;
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
            return this.next != null;
        }
    }

    private void fetchNextIfNeeded() {
        if (this.next == null) {
            for (OneDriveWalker.Event event = this.walker.next(); event != null; event = this.walker.next()) {
                IOException exception = event.ioeException();
                if (exception != null) {
                    throw new UncheckedIOException(exception);
                }

                if (event.type() != OneDriveWalker.EventType.END_DIRECTORY) {
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
            if (this.next == null) {
                throw new NoSuchElementException();
            } else {
                OneDriveWalker.Event event = this.next;
                this.next = null;
                return event.getItem();
            }
        }
    }
}
