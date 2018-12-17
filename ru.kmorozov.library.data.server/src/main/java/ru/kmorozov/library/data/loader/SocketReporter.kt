package ru.kmorozov.library.data.loader

import ru.kmorozov.onedrive.tasks.TaskReporter
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.library.data.loader.netty.EventSender

/**
 * Created by sbt-morozov-kv on 22.06.2017.
 */
class SocketReporter : TaskReporter() {

    override fun info(message: String) {
        EventSender.INSTANCE.sendInfo(logger, message)
    }

    override fun warn(message: String) {
        EventSender.INSTANCE.sendInfo(logger, message)
    }

    companion object {

        private val logger = Logger.getLogger(SocketReporter::class.java)
    }
}
