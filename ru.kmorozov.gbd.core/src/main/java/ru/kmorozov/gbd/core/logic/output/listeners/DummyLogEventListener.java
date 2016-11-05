package ru.kmorozov.gbd.core.logic.output.listeners;

import ru.kmorozov.gbd.core.logic.output.events.BaseEvent;

/**
 * Created by km on 15.12.2015.
 */
public class DummyLogEventListener extends AbstractLogEventListener {

    @Override
    public void receiveEvent(BaseEvent event) {
        System.out.println(event.getEventInfo());
    }
}
