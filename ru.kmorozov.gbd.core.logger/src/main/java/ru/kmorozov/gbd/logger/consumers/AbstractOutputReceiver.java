package ru.kmorozov.gbd.logger.consumers;

import ru.kmorozov.gbd.logger.events.BaseEvent;
import ru.kmorozov.gbd.logger.listeners.IEventListener;
import ru.kmorozov.gbd.logger.output.IOutputReceiver;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by km on 15.12.2015.
 */
public abstract class AbstractOutputReceiver implements IOutputReceiver, IEventConsumer {

    private final Collection<IEventListener> listeners = new ArrayList<>();

    @Override
    public void addListener(final IEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void consumeEvent(final BaseEvent event) {
        listeners.stream().filter(listener -> listener.eventMatched(event)).forEachOrdered(listener -> listener.receiveEvent(event));
    }
}
