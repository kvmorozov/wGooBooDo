package ru.kmorozov.gbd.logger.consumers

import ru.kmorozov.gbd.logger.events.BaseEvent
import ru.kmorozov.gbd.logger.listeners.IEventListener
import ru.kmorozov.gbd.logger.output.IOutputReceiver

import java.util.ArrayList

/**
 * Created by km on 15.12.2015.
 */
abstract class AbstractOutputReceiver : IOutputReceiver, IEventConsumer {

    private val listeners = ArrayList<IEventListener>()

    override fun addListener(listener: IEventListener) {
        listeners.add(listener)
    }

    override fun consumeEvent(event: BaseEvent) {
        listeners.stream().filter { listener -> listener.eventMatched(event) }.forEachOrdered { listener -> listener.receiveEvent(event) }
    }
}
