package ru.kmorozov.gbd.logger.events

import ru.kmorozov.gbd.logger.progress.IProgress

/**
 * Created by km on 25.12.2015.
 */
abstract class AbstractEventSource : IEventSource {

    abstract override var processStatus: IProgress
}
