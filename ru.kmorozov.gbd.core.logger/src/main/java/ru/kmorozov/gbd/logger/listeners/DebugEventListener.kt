package ru.kmorozov.gbd.logger.listeners

import ru.kmorozov.gbd.logger.events.LogEvent
import ru.kmorozov.gbd.logger.output.DefaultReceiver
import java.io.File
import java.util.logging.Level

class DebugEventListener : IEventListener {

    override fun receiveEvent(event: LogEvent) {
        dbgOutput.appendText(Thread.currentThread().name + " > " + event.eventInfo + System.lineSeparator())
    }

    override fun eventMatched(event: LogEvent): Boolean {
        return true
    }

    val dbgOutput: File

    init {
        dbgOutput = File.createTempFile("dbg", ".log")
        DefaultReceiver.INSTANCE.consumeEvent(LogEvent(Level.INFO, "Created debug file $dbgOutput"))
    }
}