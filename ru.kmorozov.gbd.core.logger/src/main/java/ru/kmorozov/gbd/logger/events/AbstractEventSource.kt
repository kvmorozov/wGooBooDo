package ru.kmorozov.gbd.logger.events

import ru.kmorozov.gbd.logger.progress.IProgress

/**
 * Created by km on 25.12.2015.
 */
abstract class AbstractEventSource : IEventSource {

    override var processStatus: IProgress
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
}
