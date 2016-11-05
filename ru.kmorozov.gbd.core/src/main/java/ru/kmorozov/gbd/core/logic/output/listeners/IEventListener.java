package ru.kmorozov.gbd.core.logic.output.listeners;

import ru.kmorozov.gbd.core.logic.output.events.BaseEvent;

/**
 * Created by km on 15.12.2015.
 */
public interface IEventListener {

    void receiveEvent(BaseEvent event);

    boolean eventMatched(BaseEvent event);
}
