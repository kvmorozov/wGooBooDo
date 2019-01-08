package ru.kmorozov.gbd.logger.listeners

import ru.kmorozov.gbd.logger.events.LogEvent
import java.util.logging.Level.INFO

/**
 * Created by km on 15.12.2015.
 */
class DefaultLogEventListener : AbstractLogEventListener() {

    override fun receiveEvent(event: LogEvent) {
        println(event.eventInfo)
    }

    override fun eventMatched(event: LogEvent): Boolean {
        return event.level.intValue() >= INFO.intValue()
    }
}
