package ru.kmorozov.gbd.logger;

import ru.kmorozov.gbd.logger.consumers.IEventConsumer;
import ru.kmorozov.gbd.logger.events.LogEvent;
import ru.kmorozov.gbd.logger.output.DummyReceiver;

import java.util.logging.Level;

/**
 * Created by km on 15.12.2015.
 */
public final class Logger {

    private final IEventConsumer eventConsumer;
    private final String name, prefix;

    public Logger(IEventConsumer eventConsumer, String name, String prefix) {
        this.eventConsumer = eventConsumer;
        this.name = name;
        this.prefix = prefix;
    }

    public static Logger getLogger(IEventConsumer eventConsumer, String name, String prefix) {
        return new Logger(eventConsumer, name, prefix);
    }

    public static Logger getLogger(Class<?> claszz) {
        return new Logger(new DummyReceiver(), claszz.getName(), ": ");
    }

    public void info(String msg) {
        this.eventConsumer.consumeEvent(new LogEvent(Level.INFO, this.prefix + msg));
    }

    public void severe(String msg) {
        this.eventConsumer.consumeEvent(new LogEvent(Level.SEVERE, this.prefix + msg));
    }

    public void warn(String msg) {
        this.eventConsumer.consumeEvent(new LogEvent(Level.WARNING, this.prefix + msg));
    }

    public void error(String msg) {
        this.eventConsumer.consumeEvent(new LogEvent(Level.SEVERE, this.prefix + msg));
    }

    public void error(Throwable ex) {
        this.eventConsumer.consumeEvent(new LogEvent(Level.SEVERE, this.prefix + ex.getMessage()));
    }

    public void error(String msg, Throwable ex) {
        this.eventConsumer.consumeEvent(new LogEvent(Level.SEVERE, this.prefix + msg + ":" + ex.getMessage()));
    }

    public void finest(String msg) {
        this.eventConsumer.consumeEvent(new LogEvent(Level.FINEST, this.prefix + msg));
    }
}
