package ru.kmorozov.gbd.core.logic.output.events;

import ru.kmorozov.gbd.core.logic.extractors.ImageExtractor;
import ru.kmorozov.gbd.core.logic.progress.IProgress;

import javax.swing.*;

/**
 * Created by km on 12.11.2016.
 */
public class ImageExtractorWorker extends SwingWorker<Void, Void> implements IEventSource {

    private final ImageExtractor extractor;

    private IProgress _processStatus;

    public ImageExtractorWorker(ImageExtractor extractor) {
        this.extractor = extractor;
    }

    @Override
    protected Void doInBackground() throws Exception {
        extractor.process();

        return null;
    }

    protected void setProcessStatus(IProgress processStatus) {
        this._processStatus = processStatus;
    }

    @Override
    public IProgress getProcessStatus() {
        return _processStatus;
    }
}
