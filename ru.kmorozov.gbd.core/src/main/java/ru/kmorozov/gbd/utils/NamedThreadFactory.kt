package ru.kmorozov.gbd.utils

import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

class NamedThreadFactory(private val baseName: String) : ThreadFactory {

    private val threadNum = AtomicInteger(0)

    @Synchronized
    override fun newThread(r: Runnable): Thread {
        val t = Executors.defaultThreadFactory().newThread(r)
        t.name = baseName + "-" + threadNum.getAndIncrement()
        return t
    }
}