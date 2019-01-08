package ru.kmorozov.gbd.logger.output

import ru.kmorozov.gbd.logger.consumers.AbstractOutputReceiver
import ru.kmorozov.gbd.logger.listeners.DebugEventListener

internal class DebugReceiver : AbstractOutputReceiver() {
    init {
        addListener(DebugEventListener())
    }

    companion object {
        val INSTANCE = DebugReceiver()
    }
}
