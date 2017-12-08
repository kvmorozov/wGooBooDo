package ru.kmorozov.gbd.core.logic.output.events;

import ru.kmorozov.gbd.core.logic.progress.IProgress;

/**
 * Created by km on 25.12.2015.
 */
public abstract class AbstractEventSource implements IEventSource {

    private IProgress _processStatus;

    @Override
    public IProgress getProcessStatus() {
        return _processStatus;
    }

    protected void setProcessStatus(final IProgress processStatus) {
        this._processStatus = processStatus;
    }
}
