package ru.kmorozov.gbd.logger.listeners

import ru.kmorozov.gbd.logger.events.LogEvent

/**
 * Created by km on 15.12.2015.
 */
abstract class AbstractLogEventListener : IEventListener {

    open override fun eventMatched(event: LogEvent): Boolean {
        return true
    }
}
