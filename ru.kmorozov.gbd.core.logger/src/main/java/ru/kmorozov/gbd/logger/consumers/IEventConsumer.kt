package ru.kmorozov.gbd.logger.consumers

import ru.kmorozov.gbd.logger.events.BaseEvent
import ru.kmorozov.gbd.logger.listeners.IEventListener

/**
 * Created by km on 15.12.2015.
 */
interface IEventConsumer {

    fun addListener(listener: IEventListener)

    fun consumeEvent(event: BaseEvent)
}
