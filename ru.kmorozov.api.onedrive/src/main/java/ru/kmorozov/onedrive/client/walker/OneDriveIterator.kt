package ru.kmorozov.onedrive.client.walker

import ru.kmorozov.onedrive.client.OneDriveItem
import ru.kmorozov.onedrive.client.OneDriveProvider
import ru.kmorozov.onedrive.client.walker.OneDriveWalker.Event

import java.io.Closeable
import java.io.UncheckedIOException
import java.util.NoSuchElementException

/**
 * Created by sbt-morozov-kv on 14.03.2017.
 */
class OneDriveIterator<T : OneDriveItem> internal constructor(api: OneDriveProvider, root: T?, maxDepth: Int, skipCondition: (OneDriveItem) -> Boolean) : Iterator<OneDriveItem>, Closeable {

    private val walker: OneDriveWalker
    private var next: Event? = null

    init {
        this.walker = OneDriveWalker(api, maxDepth, skipCondition)
        this.next = this.walker.walk(root)

        assert(OneDriveWalker.EventType.ENTRY == this.next!!.type() || OneDriveWalker.EventType.START_DIRECTORY == this.next!!.type())
    }

    override fun close() {
        this.walker.close()
    }

    override fun hasNext(): Boolean {
        if (!this.walker.isOpen) {
            throw IllegalStateException()
        } else {
            this.fetchNextIfNeeded()
            return null != this.next
        }
    }

    private fun fetchNextIfNeeded() {
        if (null == this.next) {
            var event = this.walker.next()
            while (null != event) {
                val exception = event.ioeException()
                if (null != exception) {
                    throw UncheckedIOException(exception)
                }

                if (OneDriveWalker.EventType.END_DIRECTORY != event.type()) {
                    this.next = event
                    return
                }
                event = this.walker.next()
            }
        }
    }

    override fun next(): OneDriveItem {
        if (!this.walker.isOpen) {
            throw IllegalStateException()
        } else {
            this.fetchNextIfNeeded()
            if (null == this.next) {
                throw NoSuchElementException()
            } else {
                val event = this.next
                this.next = null
                return event!!.item
            }
        }
    }
}
