package ru.kmorozov.gbd.logger.listeners;

import ru.kmorozov.gbd.logger.events.BaseEvent;

/**
 * Created by km on 15.12.2015.
 */
public interface IEventListener {

    void receiveEvent(BaseEvent event);

    boolean eventMatched(BaseEvent event);
}
