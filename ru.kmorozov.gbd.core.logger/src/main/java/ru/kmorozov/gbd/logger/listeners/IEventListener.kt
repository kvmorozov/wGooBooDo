package ru.kmorozov.gbd.logger.listeners

import ru.kmorozov.gbd.logger.events.BaseEvent

/**
 * Created by km on 15.12.2015.
 */
interface IEventListener {

    fun receiveEvent(event: BaseEvent)

    fun eventMatched(event: BaseEvent): Boolean
}
