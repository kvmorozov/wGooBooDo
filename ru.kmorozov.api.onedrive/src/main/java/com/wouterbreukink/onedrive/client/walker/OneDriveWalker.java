package com.wouterbreukink.onedrive.client.walker;

import com.wouterbreukink.onedrive.client.OneDriveItem;
import com.wouterbreukink.onedrive.client.OneDriveProvider;
import com.wouterbreukink.onedrive.client.exceptions.OneDriveException;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Created by sbt-morozov-kv on 14.03.2017.
 */
public class OneDriveWalker implements Closeable {

    private final boolean followLinks = true;
    private final OneDriveProvider api;
    private final ArrayDeque<OneDriveWalker.DirectoryNode> stack = new ArrayDeque();
    private final int maxDepth;
    private boolean closed;

    OneDriveWalker(OneDriveProvider api, int maxDepth) {
        this.api = api;
        this.maxDepth = maxDepth;
    }

    @Override
    public void close() {
        if (!this.closed) {
            while (true) {
                if (this.stack.isEmpty()) {
                    this.closed = true;
                    break;
                }

                this.pop();
            }
        }
    }

    private void pop() {
        if (!this.stack.isEmpty()) {
            DirectoryNode node = this.stack.pop();

            node.stream().close();
        }
    }

    Event walk(OneDriveItem root) {
        if (this.closed) {
            throw new IllegalStateException("Closed");
        } else {
            Event event = this.visit(root);

            assert event != null;

            return event;
        }
    }

    /**
     * Based on {@link java.nio.file.FileTreeWalker#visit}
     */
    private Event visit(OneDriveItem item) {
        int size = this.stack.size();
        if (size < this.maxDepth && item.isDirectory()) {
            if (this.followLinks && wouldLoop(item)) {
                return new Event(EventType.ENTRY, item, new OneDriveException(item.toString()));
            } else {
                Stream<OneDriveItem> itemStream;

                try {
                    itemStream = Arrays.asList(api.getChildren(item)).stream();
                } catch (IOException ioe) {
                    return new Event(EventType.ENTRY, item, ioe);
                }

                this.stack.push(new DirectoryNode(item, item.getId(), itemStream));
                return new Event(EventType.START_DIRECTORY, item);
            }
        } else {
            return new Event(EventType.ENTRY, item);
        }
    }

    /**
     * TODO сделать проверку на зацикливание, по аналогии с {@link java.nio.file.FileTreeWalker#wouldLoop}
     */
    private boolean wouldLoop(OneDriveItem item) {
        return false;
    }

    public boolean isOpen() {
        return !this.closed;
    }

    /**
     * Based on {@link java.nio.file.FileTreeWalker#next}
     */
    Event next() {
        DirectoryNode node = this.stack.peek();
        if (node == null) {
            return null;
        } else {
            Event event;
            do {
                OneDriveItem item = null;
                IOException exception = null;
                if (!node.skipped()) {
                    Iterator iterator = node.iterator();

                    if (iterator.hasNext())
                        item = (OneDriveItem) iterator.next();
                }

                if (item == null) {
                    node.stream().close();

                    this.stack.pop();
                    return new Event(EventType.END_DIRECTORY, node.item(), exception);
                }

                event = this.visit(item);
            } while (event == null);

            return event;
        }
    }

    static class Event {
        private final OneDriveWalker.EventType type;
        private final OneDriveItem item;
        private final IOException ioe;

        public Event(EventType type, OneDriveItem item, IOException ioe) {
            this.type = type;
            this.item = item;
            this.ioe = ioe;
        }

        public Event(EventType type, OneDriveItem item) {
            this(type, item, null);
        }

        public EventType type() {
            return type;
        }

        public IOException ioeException() {
            return ioe;
        }

        public OneDriveItem getItem() {
            return item;
        }
    }

    enum EventType {
        START_DIRECTORY,
        END_DIRECTORY,
        ENTRY;

        EventType() {
        }
    }

    private static class DirectoryNode {
        private final OneDriveItem item;
        private final Object key;
        private final Stream<OneDriveItem> stream;
        private final Iterator<OneDriveItem> iterator;
        private boolean skipped;

        DirectoryNode(OneDriveItem item, Object key, Stream<OneDriveItem> stream) {
            this.item = item;
            this.key = key;
            this.stream = stream;
            this.iterator = stream.iterator();
        }

        OneDriveItem item() {
            return this.item;
        }

        Object key() {
            return this.key;
        }

        Stream<OneDriveItem> stream() {
            return this.stream;
        }

        Iterator<OneDriveItem> iterator() {
            return this.iterator;
        }

        void skip() {
            this.skipped = true;
        }

        boolean skipped() {
            return this.skipped;
        }
    }
}
