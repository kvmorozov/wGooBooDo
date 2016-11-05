package ru.kmorozov.gbd.core.utils;

import ru.kmorozov.gbd.core.logic.output.consumers.IEventConsumer;
import ru.kmorozov.gbd.core.logic.output.events.LogEvent;

import java.util.logging.Level;

/**
 * Created by km on 15.12.2015.
 */
public class Logger {

    private final IEventConsumer eventConsumer;
    private String name;

    private Logger (IEventConsumer eventConsumer, String name) {
        this.eventConsumer = eventConsumer;
        this.name = name;
    }

    public static Logger getLogger(IEventConsumer eventConsumer, String name) {
        return new Logger(eventConsumer, name);
    }

    public void info(String msg) {
        eventConsumer.consumeEvent(new LogEvent(Level.INFO, msg));
    }

    public void severe(String msg) {
        eventConsumer.consumeEvent(new LogEvent(Level.SEVERE, msg));
    }

    public void finest(String msg) {
        eventConsumer.consumeEvent(new LogEvent(Level.FINEST, msg));
    }
}
