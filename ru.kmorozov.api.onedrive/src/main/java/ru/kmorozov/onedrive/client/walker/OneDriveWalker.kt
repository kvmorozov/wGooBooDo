package ru.kmorozov.onedrive.client.walker

import ru.kmorozov.onedrive.client.OneDriveItem
import ru.kmorozov.onedrive.client.OneDriveProvider
import ru.kmorozov.onedrive.client.exceptions.OneDriveException
import java.io.Closeable
import java.io.IOException
import java.util.*
import java.util.stream.BaseStream
import java.util.stream.Stream

/**
 * Created by sbt-morozov-kv on 14.03.2017.
 */
class OneDriveWalker internal constructor(private val api: OneDriveProvider, private val maxDepth: Int, private val skipCondition: (OneDriveItem) -> Boolean) : Closeable {
    private val stack = ArrayDeque<DirectoryNode>()
    private var closed: Boolean = false

    val isOpen: Boolean
        get() = !this.closed

    override fun close() {
        if (!this.closed) {
            while (true) {
                if (this.stack.isEmpty()) {
                    this.closed = true
                    break
                }

                this.pop()
            }
        }
    }

    private fun pop() {
        if (!this.stack.isEmpty()) {
            val node = this.stack.pop()

            node.stream().close()
        }
    }

    internal fun walk(root: OneDriveItem?): Event {
        if (this.closed) {
            throw IllegalStateException("Closed")
        } else {
            val event = root?.let { this.visit(it) }

            return event!!
        }
    }

    /**
     * Based on [java.nio.file.FileTreeWalker.visit]
     */
    private fun visit(item: OneDriveItem): Event {
        val size = this.stack.size
        if (size < this.maxDepth && item.isDirectory) {
            if (OneDriveWalker.followLinks && wouldLoop(item)) {
                return Event(EventType.ENTRY, item, OneDriveException(item.toString()))
            } else {
                val itemStream: Stream<OneDriveItem>

                try {
                    itemStream = Arrays.stream(api.getChildren(item))
                } catch (ioe: IOException) {
                    return Event(EventType.ENTRY, item, ioe)
                }

                this.stack.push(DirectoryNode(item, item.id!!, itemStream))
                return Event(EventType.START_DIRECTORY, item)
            }
        } else {
            return Event(EventType.ENTRY, item)
        }
    }

    /**
     * Based on [java.nio.file.FileTreeWalker.next]
     */
    internal operator fun next(): Event? {
        val node = this.stack.peek()
        if (null == node) {
            return null
        } else {
            var event: Event?
            do {
                var item: OneDriveItem? = null
                val exception: IOException? = null
                if (!node.skipped()) {
                    val iterator = node.iterator()

                    if (iterator.hasNext())
                        item = iterator.next()
                }

                if (null == item) {
                    node.stream().close()

                    this.stack.pop()
                    return Event(EventType.END_DIRECTORY, node.item(), exception)
                }

                event = this.visit(item)
            } while (null == event)

            return event
        }
    }

    internal class Event @JvmOverloads constructor(private val type: EventType, val item: OneDriveItem, private val ioe: IOException? = null) {

        fun type(): EventType {
            return type
        }

        fun ioeException(): IOException? {
            return ioe
        }
    }

    internal enum class EventType {
        START_DIRECTORY,
        END_DIRECTORY,
        ENTRY
    }

    private inner class DirectoryNode(private val item: OneDriveItem, private val key: Any, private val stream: Stream<OneDriveItem>) {
        private val iterator: Iterator<OneDriveItem>

        init {
            this.iterator = stream.iterator()
        }

        fun item(): OneDriveItem {
            return this.item
        }

        fun stream(): BaseStream<*, *> {
            return this.stream
        }

        operator fun iterator(): Iterator<OneDriveItem> {
            return this.iterator
        }

        fun skipped(): Boolean {
            return skipCondition.invoke(item)
        }
    }

    companion object {

        private const val followLinks = true

        /**
         * TODO сделать проверку на зацикливание, по аналогии с [java.nio.file.FileTreeWalker.wouldLoop]
         */
        private fun wouldLoop(item: OneDriveItem): Boolean {
            return false
        }
    }
}
