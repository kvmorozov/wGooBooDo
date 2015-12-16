package ru.simpleGBD.App.Utils;

import ru.simpleGBD.App.Logic.Output.consumers.IEventConsumer;
import ru.simpleGBD.App.Logic.Output.events.BaseEvent;

/**
 * Created by km on 15.12.2015.
 */
public class Logger {

    private IEventConsumer eventConsumer;
    private String name;

    private Logger (IEventConsumer eventConsumer, String name) {
        this.eventConsumer = eventConsumer;
        this.name = name;
    }

    public static Logger getLogger(IEventConsumer eventConsumer, String name) {
        return new Logger(eventConsumer, name);
    }

    public void info(String msg) {
        eventConsumer.consumeEvent(new BaseEvent(msg));
    }

    public void severe(String msg) {
        eventConsumer.consumeEvent(new BaseEvent(msg));
    }

    public void finest(String msg) {
        eventConsumer.consumeEvent(new BaseEvent(msg));
    }
}
