package ru.kmorozov.gbd.core.utils;

import ru.kmorozov.gbd.core.logic.output.consumers.IEventConsumer;
import ru.kmorozov.gbd.core.logic.output.events.LogEvent;

import java.util.logging.Level;

/**
 * Created by km on 15.12.2015.
 */
public class Logger {

    private final IEventConsumer eventConsumer;
    private final String name, prefix;

    private Logger (IEventConsumer eventConsumer, String name, String prefix) {
        this.eventConsumer = eventConsumer;
        this.name = name;
        this.prefix = prefix;
    }

    public static Logger getLogger(IEventConsumer eventConsumer, String name, String prefix) {
        return new Logger(eventConsumer, name, prefix);
    }

    public void info(String msg) {
        eventConsumer.consumeEvent(new LogEvent(Level.INFO, prefix + msg));
    }

    public void severe(String msg) {
        eventConsumer.consumeEvent(new LogEvent(Level.SEVERE, prefix + msg));
    }

    public void finest(String msg) {
        eventConsumer.consumeEvent(new LogEvent(Level.FINEST, prefix + msg));
    }
}
