package ru.simpleGBD.App.Logic.Output.consumers;

import ru.simpleGBD.App.Logic.Output.events.BaseEvent;
import ru.simpleGBD.App.Logic.Output.listeners.IEventListener;

/**
 * Created by km on 15.12.2015.
 */
public interface IEventConsumer {

    void addListener(IEventListener listener);

    void consumeEvent(BaseEvent event);
}
