package ru.kmorozov.gbd.core.logic.output.consumers;

import ru.kmorozov.gbd.core.logic.output.events.BaseEvent;
import ru.kmorozov.gbd.core.logic.output.listeners.IEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by km on 15.12.2015.
 */
public abstract class AbstractOutput implements IBookInfoOutput, IEventConsumer {

    private final List<IEventListener> listeners = new ArrayList<>();

    @Override
    public void addListener(IEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void consumeEvent(BaseEvent event) {
        listeners.stream().filter(listener -> listener.eventMatched(event)).forEachOrdered(listener -> listener.receiveEvent(event));
    }
}