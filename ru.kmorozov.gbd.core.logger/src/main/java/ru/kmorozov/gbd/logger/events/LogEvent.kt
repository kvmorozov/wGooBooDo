package ru.kmorozov.gbd.logger.events

import java.util.logging.Level

/**
 * Created by km on 15.12.2015.
 */
class LogEvent(val level: Level, eventInfo: String) : BaseEvent(eventInfo)
