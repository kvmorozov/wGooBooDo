package ru.simpleGBD.App.Logic.Output.listeners;

import ru.simpleGBD.App.Logic.Output.events.BaseEvent;
import ru.simpleGBD.App.Logic.Output.events.LogEvent;

import java.util.logging.Logger;

/**
 * Created by km on 15.12.2015.
 */
public class DummyLogEventListener extends AbstractLogEventListener {

    @Override
    public void receiveEvent(BaseEvent event) {
        System.out.println(event.getEventInfo());
    }
}
