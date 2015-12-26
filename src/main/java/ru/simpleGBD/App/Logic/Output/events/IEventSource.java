package ru.simpleGBD.App.Logic.Output.events;

import ru.simpleGBD.App.Logic.Output.progress.ProcessStatus;

/**
 * Created by km on 25.12.2015.
 */
public interface IEventSource {

    ProcessStatus getProcessStatus();
}
