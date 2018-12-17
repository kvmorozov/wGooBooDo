package ru.kmorozov.gbd.logger.listeners

import ru.kmorozov.gbd.logger.events.BaseEvent
import ru.kmorozov.gbd.logger.events.LogEvent

/**
 * Created by km on 15.12.2015.
 */
abstract class AbstractLogEventListener : IEventListener {

    override fun eventMatched(event: BaseEvent): Boolean {
        return event is LogEvent
    }
}
