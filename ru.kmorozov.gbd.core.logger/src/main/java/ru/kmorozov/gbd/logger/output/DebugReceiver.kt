package ru.kmorozov.gbd.logger.output

import ru.kmorozov.gbd.logger.consumers.AbstractOutputReceiver
import ru.kmorozov.gbd.logger.listeners.DebugEventListener
import ru.kmorozov.gbd.logger.listeners.DefaultLogEventListener

internal class DebugReceiver : AbstractOutputReceiver() {

    init {
        addListener(DefaultLogEventListener())
        addListener(DebugEventListener())
    }

    companion object {
        val INSTANCE = DebugReceiver()
    }
}
