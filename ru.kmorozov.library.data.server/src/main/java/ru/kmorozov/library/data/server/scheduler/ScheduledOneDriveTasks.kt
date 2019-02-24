package ru.kmorozov.library.data.server.scheduler

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Lazy
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.library.data.loader.impl.OneDriveLoader
import ru.kmorozov.library.data.server.condition.StorageEnabledCondition

import java.io.IOException

/**
 * Created by sbt-morozov-kv on 13.04.2017.
 */
@Component
@ComponentScan(basePackageClasses = arrayOf(OneDriveLoader::class))
@Conditional(StorageEnabledCondition::class)
class ScheduledOneDriveTasks {

    @Autowired
    @Lazy
    private lateinit var oneLoader: OneDriveLoader

    @Value("\${onedrive.scheduler.enabled}")
    private val schedulerEnabled: Boolean = false

    @Scheduled(fixedRate = SCHEDULE_INTERVAL)
    @Throws(IOException::class)
    fun refreshOneDrive() {
        if (!schedulerEnabled)
            return

        logger.info("Scheduled refresh started")
        oneLoader.load { oneDriveItem -> java.lang.Long.MAX_VALUE < System.currentTimeMillis() - oneDriveItem.lastModifiedDateTime.time }
        logger.info("Scheduled refresh finished")
    }

    @Scheduled(fixedRate = SCHEDULE_INTERVAL)
    @Throws(IOException::class)
    fun processLinks() {
        if (!schedulerEnabled)
            return

        logger.info("Scheduled links processing started")
        oneLoader.processLinks()
        logger.info("Scheduled links processing finished")
    }

    companion object {

        private const val SCHEDULE_INTERVAL = (1 * 60 * 60 * 1000).toLong()
        private val logger = Logger.getLogger(ScheduledOneDriveTasks::class.java)
    }
}
