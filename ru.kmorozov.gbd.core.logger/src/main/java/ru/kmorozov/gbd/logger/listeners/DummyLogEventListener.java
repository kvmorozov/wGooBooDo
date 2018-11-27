package ru.kmorozov.gbd.logger.listeners;

import ru.kmorozov.gbd.logger.events.BaseEvent;

/**
 * Created by km on 15.12.2015.
 */
public class DummyLogEventListener extends AbstractLogEventListener {

    @Override
    public void receiveEvent(BaseEvent event) {
        System.out.println(event.getEventInfo());
    }
}
