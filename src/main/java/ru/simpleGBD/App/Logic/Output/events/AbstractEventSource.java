package ru.simpleGBD.App.Logic.Output.events;

import ru.simpleGBD.App.Logic.Output.progress.ProcessStatus;

import javax.swing.*;

/**
 * Created by km on 25.12.2015.
 */
public abstract class AbstractEventSource extends SwingWorker<Void, Void> implements IEventSource{

    private ProcessStatus _processStatus;

    protected void setProcessStatus(ProcessStatus processStatus) {
        this._processStatus = processStatus;
    }

    @Override
    public ProcessStatus getProcessStatus() {
        return _processStatus;
    }
}
