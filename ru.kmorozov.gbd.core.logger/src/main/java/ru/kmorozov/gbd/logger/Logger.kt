package ru.kmorozov.gbd.logger

import ru.kmorozov.gbd.logger.consumers.IEventConsumer
import ru.kmorozov.gbd.logger.events.LogEvent
import ru.kmorozov.gbd.logger.output.DummyReceiver

import java.util.logging.Level

/**
 * Created by km on 15.12.2015.
 */
class Logger(private val eventConsumer: IEventConsumer, private val name: String, private val prefix: String) {

    fun info(msg: String) {
        eventConsumer.consumeEvent(LogEvent(Level.INFO, prefix + msg))
    }

    fun severe(msg: String) {
        eventConsumer.consumeEvent(LogEvent(Level.SEVERE, prefix + msg))
    }

    fun warn(msg: String) {
        eventConsumer.consumeEvent(LogEvent(Level.WARNING, prefix + msg))
    }

    fun error(msg: String) {
        eventConsumer.consumeEvent(LogEvent(Level.SEVERE, prefix + msg))
    }

    fun error(ex: Throwable) {
        eventConsumer.consumeEvent(LogEvent(Level.SEVERE, prefix + ex.message))
    }

    fun error(msg: String, ex: Throwable) {
        eventConsumer.consumeEvent(LogEvent(Level.SEVERE, prefix + msg + ":" + ex.message))
    }

    fun finest(msg: String) {
        eventConsumer.consumeEvent(LogEvent(Level.FINEST, prefix + msg))
    }

    companion object {

        fun getLogger(eventConsumer: IEventConsumer, name: String, prefix: String): Logger {
            return Logger(eventConsumer, name, prefix)
        }

        fun getLogger(claszz: Class<*>): Logger {
            return Logger(DummyReceiver.INSTANCE, claszz.name, ": ")
        }

        fun getLogger(claszz: Class<*>, prefix: String): Logger {
            return Logger(DummyReceiver.INSTANCE, claszz.name, prefix + " : ")
        }
    }
}
