package ru.kmorozov.gbd.core.logic.output.events;

import ru.kmorozov.gbd.core.logic.progress.IProgress;

/**
 * Created by km on 25.12.2015.
 */
@FunctionalInterface
public interface IEventSource {

    IProgress getProcessStatus();
}
