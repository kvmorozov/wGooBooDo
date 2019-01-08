package ru.kmorozov.gbd.logger.output

import ru.kmorozov.gbd.logger.consumers.AbstractOutputReceiver
import ru.kmorozov.gbd.logger.listeners.DefaultLogEventListener

/**
 * Created by km on 13.12.2015.
 */
internal class DefaultReceiver : AbstractOutputReceiver() {
    init {
        addListener(DefaultLogEventListener())
    }

    companion object {
        val INSTANCE = DefaultReceiver()
    }
}
