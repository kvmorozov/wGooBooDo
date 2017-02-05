package ru.kmorozov.gbd.core.logic.output.events;

import ru.kmorozov.gbd.core.logic.extractors.google.GoogleImageExtractor;
import ru.kmorozov.gbd.core.logic.progress.IProgress;

import javax.swing.*;

/**
 * Created by km on 12.11.2016.
 */
public class ImageExtractorWorker extends SwingWorker<Void, Void> implements IEventSource {

    private final GoogleImageExtractor extractor;

    private IProgress _processStatus;

    public ImageExtractorWorker(GoogleImageExtractor extractor) {
        this.extractor = extractor;
    }

    @Override
    protected Void doInBackground() throws Exception {
        extractor.process();

        return null;
    }

    @Override
    public IProgress getProcessStatus() {
        return _processStatus;
    }

    protected void setProcessStatus(IProgress processStatus) {
        this._processStatus = processStatus;
    }
}
