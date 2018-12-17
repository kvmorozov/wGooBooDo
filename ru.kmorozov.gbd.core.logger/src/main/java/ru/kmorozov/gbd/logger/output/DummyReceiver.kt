package ru.kmorozov.gbd.logger.output

import ru.kmorozov.gbd.logger.consumers.AbstractOutputReceiver
import ru.kmorozov.gbd.logger.listeners.DummyLogEventListener
import ru.kmorozov.gbd.logger.model.ILoggableObject

/**
 * Created by km on 13.12.2015.
 */
class DummyReceiver : AbstractOutputReceiver() {
    init {
        addListener(DummyLogEventListener())
    }

    override fun receive(bookInfo: ILoggableObject) {

    }
}
