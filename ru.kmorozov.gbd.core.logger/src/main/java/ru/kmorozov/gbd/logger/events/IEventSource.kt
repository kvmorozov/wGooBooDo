package ru.kmorozov.gbd.logger.events

import ru.kmorozov.gbd.logger.progress.IProgress

/**
 * Created by km on 25.12.2015.
 */
@FunctionalInterface
interface IEventSource {

    var processStatus: IProgress
}
