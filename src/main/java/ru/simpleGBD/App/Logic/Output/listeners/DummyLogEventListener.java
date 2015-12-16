package ru.simpleGBD.App.Logic.Output.listeners;

import ru.simpleGBD.App.Logic.Output.events.BaseEvent;
import ru.simpleGBD.App.Logic.Output.events.LogEvent;

import java.util.logging.Logger;

/**
 * Created by km on 15.12.2015.
 */
public class DummyLogEventListener extends AbstractLogEventListener {

    private static Logger logger = Logger.getLogger("");

    @Override
    public void receiveEvent(BaseEvent event) {
        LogEvent logEvent = (LogEvent) event;
        logger.log(logEvent.getLevel(), logEvent.getEventInfo());
    }
}
