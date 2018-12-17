package ru.kmorozov.gbd.logger.events

/**
 * Created by km on 15.12.2015.
 */
open class BaseEvent internal constructor(val eventInfo: String) {

    override fun toString(): String {
        return eventInfo
    }
}
