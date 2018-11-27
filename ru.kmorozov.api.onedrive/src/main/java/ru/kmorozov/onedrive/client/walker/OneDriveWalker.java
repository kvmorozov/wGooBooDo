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
    private final Deque<OneDriveWalker.DirectoryNode> stack = new ArrayDeque();
    private final int maxDepth;
    private boolean closed;
    private final Predicate<OneDriveItem> skipCondition;

    OneDriveWalker(OneDriveProvider api, int maxDepth, Predicate<OneDriveItem> skipCondition) {
        this.api = api;
        this.maxDepth = maxDepth;
        this.skipCondition = skipCondition;
    }

    @Override
    public void close() {
        if (!closed) {
            while (true) {
                if (stack.isEmpty()) {
                    closed = true;
                    break;
                }

                pop();
            }
        }
    }

    private void pop() {
        if (!stack.isEmpty()) {
            OneDriveWalker.DirectoryNode node = stack.pop();

            node.stream().close();
        }
    }

    OneDriveWalker.Event walk(OneDriveItem root) {
        if (closed) {
            throw new IllegalStateException("Closed");
        } else {
            OneDriveWalker.Event event = visit(root);

            assert null != event;

            return event;
        }
    }

    /**
     * Based on {@link java.nio.file.FileTreeWalker#visit}
     */
    private OneDriveWalker.Event visit(OneDriveItem item) {
        int size = stack.size();
        if (size < maxDepth && item.isDirectory()) {
            if (followLinks && OneDriveWalker.wouldLoop(item)) {
                return new OneDriveWalker.Event(OneDriveWalker.EventType.ENTRY, item, new OneDriveException(item.toString()));
            } else {
                Stream<OneDriveItem> itemStream;

                try {
                    itemStream = Arrays.stream(this.api.getChildren(item));
                } catch (IOException ioe) {
                    return new OneDriveWalker.Event(OneDriveWalker.EventType.ENTRY, item, ioe);
                }

                stack.push(new OneDriveWalker.DirectoryNode(item, item.getId(), itemStream));
                return new OneDriveWalker.Event(OneDriveWalker.EventType.START_DIRECTORY, item);
            }
        } else {
            return new OneDriveWalker.Event(OneDriveWalker.EventType.ENTRY, item);
        }
    }

    /**
     * TODO сделать проверку на зацикливание, по аналогии с {@link java.nio.file.FileTreeWalker#wouldLoop}
     */
    private static boolean wouldLoop(OneDriveItem item) {
        return false;
    }

    public boolean isOpen() {
        return !closed;
    }

    /**
     * Based on {@link java.nio.file.FileTreeWalker#next}
     */
    OneDriveWalker.Event next() {
        OneDriveWalker.DirectoryNode node = stack.peek();
        if (null == node) {
            return null;
        } else {
            OneDriveWalker.Event event;
            do {
                OneDriveItem item = null;
                IOException exception = null;
                if (!node.skipped()) {
                    Iterator<OneDriveItem> iterator = node.iterator();

                    if (iterator.hasNext())
                        item = iterator.next();
                }

                if (null == item) {
                    node.stream().close();

                    stack.pop();
                    return new OneDriveWalker.Event(OneDriveWalker.EventType.END_DIRECTORY, node.item(), exception);
                }

                event = visit(item);
            } while (null == event);

            return event;
        }
    }

    static class Event {
        private final OneDriveWalker.EventType type;
        private final OneDriveItem item;
        private final IOException ioe;

        Event(OneDriveWalker.EventType type, OneDriveItem item, IOException ioe) {
            this.type = type;
            this.item = item;
            this.ioe = ioe;
        }

        Event(OneDriveWalker.EventType type, OneDriveItem item) {
            this(type, item, null);
        }

        public OneDriveWalker.EventType type() {
            return this.type;
        }

        public IOException ioeException() {
            return this.ioe;
        }

        public OneDriveItem getItem() {
            return this.item;
        }
    }

    enum EventType {
        START_DIRECTORY,
        END_DIRECTORY,
        ENTRY
    }

    private class DirectoryNode {
        private final OneDriveItem item;
        private final Object key;
        private final Stream<OneDriveItem> stream;
        private final Iterator<OneDriveItem> iterator;

        DirectoryNode(OneDriveItem item, Object key, Stream<OneDriveItem> stream) {
            this.item = item;
            this.key = key;
            this.stream = stream;
            iterator = stream.iterator();
        }

        OneDriveItem item() {
            return item;
        }

        BaseStream stream() {
            return stream;
        }

        Iterator<OneDriveItem> iterator() {
            return iterator;
        }

        boolean skipped() {
            return OneDriveWalker.this.skipCondition.test(this.item);
        }
    }
}
