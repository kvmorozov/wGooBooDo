package ru.simpleGBD.App.Logic.Output.listeners;

import ru.simpleGBD.App.Logic.Output.events.BaseEvent;
import ru.simpleGBD.App.Logic.Output.events.LogEvent;

/**
 * Created by km on 15.12.2015.
 */
public abstract class AbstractLogEventListener implements IEventListener {

    @Override
    public boolean eventMatched(BaseEvent event) {
        return event instanceof LogEvent;
    }
}
