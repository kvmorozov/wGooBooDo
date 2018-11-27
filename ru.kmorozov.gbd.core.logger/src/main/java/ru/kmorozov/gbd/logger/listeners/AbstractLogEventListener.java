package ru.kmorozov.gbd.logger.listeners;

import ru.kmorozov.gbd.logger.events.BaseEvent;
import ru.kmorozov.gbd.logger.events.LogEvent;

/**
 * Created by km on 15.12.2015.
 */
public abstract class AbstractLogEventListener implements IEventListener {

    @Override
    public boolean eventMatched(BaseEvent event) {
        return event instanceof LogEvent;
    }
}
