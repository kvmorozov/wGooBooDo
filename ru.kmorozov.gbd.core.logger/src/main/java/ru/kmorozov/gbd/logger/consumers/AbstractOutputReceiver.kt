package ru.kmorozov.gbd.logger.consumers

import ru.kmorozov.gbd.logger.events.LogEvent
import ru.kmorozov.gbd.logger.listeners.IEventListener
import ru.kmorozov.gbd.logger.output.IOutputReceiver
import java.util.*

/**
 * Created by km on 15.12.2015.
 */
abstract class AbstractOutputReceiver : IEventConsumer, IOutputReceiver {

    private val listeners = ArrayList<IEventListener>()

    override fun addListener(listener: IEventListener) {
        listeners.add(listener)
    }

    override fun consumeEvent(event: LogEvent) {
        listeners.filter { it.eventMatched(event) }.forEach { it.receiveEvent(event) }
    }
}
