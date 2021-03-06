package ru.kmorozov.gbd.logger.listeners

import ru.kmorozov.gbd.logger.events.LogEvent

/**
 * Created by km on 15.12.2015.
 */
interface IEventListener {

    fun receiveEvent(event: LogEvent)

    fun eventMatched(event: LogEvent): Boolean
}
