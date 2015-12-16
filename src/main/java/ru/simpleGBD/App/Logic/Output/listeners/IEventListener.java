package ru.simpleGBD.App.Logic.Output.listeners;

import ru.simpleGBD.App.Logic.Output.events.BaseEvent;

/**
 * Created by km on 15.12.2015.
 */
public interface IEventListener {

    void receiveEvent(BaseEvent event);

    boolean eventMatched(BaseEvent event);
}
