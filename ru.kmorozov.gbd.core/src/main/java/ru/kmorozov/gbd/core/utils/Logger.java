package ru.kmorozov.gbd.core.utils;

import ru.kmorozov.gbd.core.logic.output.consumers.DummyBookInfoOutput;
import ru.kmorozov.gbd.core.logic.output.consumers.IEventConsumer;
import ru.kmorozov.gbd.core.logic.output.events.LogEvent;

import java.util.logging.Level;

/**
 * Created by km on 15.12.2015.
 */
public final class Logger {

    private final IEventConsumer eventConsumer;
    private final String name, prefix;

    private Logger(final IEventConsumer eventConsumer, final String name, final String prefix) {
        this.eventConsumer = eventConsumer;
        this.name = name;
        this.prefix = prefix;
    }

    public static Logger getLogger(final IEventConsumer eventConsumer, final String name, final String prefix) {
        return new Logger(eventConsumer, name, prefix);
    }

    public static Logger getLogger(final Class<?> claszz) {
        return new Logger(new DummyBookInfoOutput(), claszz.getName(), ": ");
    }

    public void info(final String msg) {
        eventConsumer.consumeEvent(new LogEvent(Level.INFO, prefix + msg));
    }

    public void severe(final String msg) {
        eventConsumer.consumeEvent(new LogEvent(Level.SEVERE, prefix + msg));
    }

    public void finest(final String msg) {
        eventConsumer.consumeEvent(new LogEvent(Level.FINEST, prefix + msg));
    }
}
