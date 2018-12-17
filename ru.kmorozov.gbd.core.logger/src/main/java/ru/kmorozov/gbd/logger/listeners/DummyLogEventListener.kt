package ru.kmorozov.gbd.logger.listeners

import ru.kmorozov.gbd.logger.events.BaseEvent

/**
 * Created by km on 15.12.2015.
 */
class DummyLogEventListener : AbstractLogEventListener() {

    override fun receiveEvent(event: BaseEvent) {
        println(event.eventInfo)
    }
}
