package ru.kmorozov.onedrive.client.walker;

import ru.kmorozov.onedrive.client.OneDriveItem;
import ru.kmorozov.onedrive.client.OneDriveProvider;
import ru.kmorozov.onedrive.client.exceptions.OneDriveException;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.BaseStream;
import java.util.stream.Stream;

/**
 * Created by sbt-morozov-kv on 14.03.2017.
 */
public class OneDriveWalker implements Closeable {

    private static final boolean followLinks = true;
    private final OneDriveProvider api;
    private final Deque<DirectoryNode> stack = new ArrayDeque();
    private final int maxDepth;
    private boolean closed;
    private final Predicate<OneDriveItem> skipCondition;

    OneDriveWalker(final OneDriveProvider api, final int maxDepth, final Predicate<OneDriveItem> skipCondition) {
        this.api = api;
        this.maxDepth = maxDepth;
        this.skipCondition = skipCondition;
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
            final DirectoryNode node = this.stack.pop();

            node.stream().close();
        }
    }

    Event walk(final OneDriveItem root) {
        if (this.closed) {
            throw new IllegalStateException("Closed");
        } else {
            final Event event = this.visit(root);

            assert null != event;

            return event;
        }
    }

    /**
     * Based on {@link java.nio.file.FileTreeWalker#visit}
     */
    private Event visit(final OneDriveItem item) {
        final int size = this.stack.size();
        if (size < this.maxDepth && item.isDirectory()) {
            if (this.followLinks && wouldLoop(item)) {
                return new Event(EventType.ENTRY, item, new OneDriveException(item.toString()));
            } else {
                final Stream<OneDriveItem> itemStream;

                try {
                    itemStream = Arrays.stream(api.getChildren(item));
                } catch (final IOException ioe) {
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
    private static boolean wouldLoop(final OneDriveItem item) {
        return false;
    }

    public boolean isOpen() {
        return !this.closed;
    }

    /**
     * Based on {@link java.nio.file.FileTreeWalker#next}
     */
    Event next() {
        final DirectoryNode node = this.stack.peek();
        if (null == node) {
            return null;
        } else {
            Event event;
            do {
                OneDriveItem item = null;
                final IOException exception = null;
                if (!node.skipped()) {
                    final Iterator<OneDriveItem> iterator = node.iterator();

                    if (iterator.hasNext())
                        item = iterator.next();
                }

                if (null == item) {
                    node.stream().close();

                    this.stack.pop();
                    return new Event(EventType.END_DIRECTORY, node.item(), exception);
                }

                event = this.visit(item);
            } while (null == event);

            return event;
        }
    }

    static class Event {
        private final EventType type;
        private final OneDriveItem item;
        private final IOException ioe;

        Event(final EventType type, final OneDriveItem item, final IOException ioe) {
            this.type = type;
            this.item = item;
            this.ioe = ioe;
        }

        Event(final EventType type, final OneDriveItem item) {
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
    }

    private class DirectoryNode {
        private final OneDriveItem item;
        private final Object key;
        private final Stream<OneDriveItem> stream;
        private final Iterator<OneDriveItem> iterator;

        DirectoryNode(final OneDriveItem item, final Object key, final Stream<OneDriveItem> stream) {
            this.item = item;
            this.key = key;
            this.stream = stream;
            this.iterator = stream.iterator();
        }

        OneDriveItem item() {
            return this.item;
        }

        BaseStream stream() {
            return this.stream;
        }

        Iterator<OneDriveItem> iterator() {
            return this.iterator;
        }

        boolean skipped() {
            return skipCondition.test(item);
        }
    }
}
