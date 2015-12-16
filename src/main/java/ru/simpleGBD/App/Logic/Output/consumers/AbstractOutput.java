package ru.simpleGBD.App.Logic.Output.consumers;

import ru.simpleGBD.App.Logic.Output.events.BaseEvent;
import ru.simpleGBD.App.Logic.Output.listeners.IEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by km on 15.12.2015.
 */
public abstract class AbstractOutput implements IBookInfoOutput, IEventConsumer {

    private List<IEventListener> listeners = new ArrayList<IEventListener>();

    @Override
    public void addListener(IEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void consumeEvent(BaseEvent event) {
        for (IEventListener listener : listeners)
            if (listener.eventMatched(event))
                listener.receiveEvent(event);
    }
}
