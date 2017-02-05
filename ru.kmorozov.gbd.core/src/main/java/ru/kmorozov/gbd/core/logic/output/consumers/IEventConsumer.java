package ru.kmorozov.gbd.core.logic.output.consumers;

import ru.kmorozov.gbd.core.logic.output.events.BaseEvent;
import ru.kmorozov.gbd.core.logic.output.listeners.IEventListener;

/**
 * Created by km on 15.12.2015.
 */
public interface IEventConsumer {

    void addListener(IEventListener listener);

    void consumeEvent(BaseEvent event);
}
