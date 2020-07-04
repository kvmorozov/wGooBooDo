package ru.kmorozov.gbd.utils

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue

class SetBlockingQueue<T> : LinkedBlockingQueue<T>() {
    private val set = Collections.newSetFromMap(ConcurrentHashMap<T, Boolean>())

    /**
     * Add only element, that is not already enqueued.
     * The method is synchronized, so that the duplicate elements can't get in during race condition.
     * @param t object to put in
     * @return true, if the queue was changed, false otherwise
     */
    @Synchronized
    override fun add(t: T): Boolean {
        return if (set.contains(t)) {
            false
        } else {
            set.add(t)
            super.add(t)
        }
    }

    /**
     * Takes the element from the queue.
     * Note that no synchronization with [.add] is here, as we don't care about the element staying in the set longer needed.
     * @return taken element
     * @throws InterruptedException
     */
    @Throws(InterruptedException::class)
    override fun take(): T {
        val t = super.take()
        set.remove(t)
        return t
    }
}