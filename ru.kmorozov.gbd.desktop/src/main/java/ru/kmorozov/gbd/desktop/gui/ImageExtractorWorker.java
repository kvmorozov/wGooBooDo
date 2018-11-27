package ru.kmorozov.gbd.desktop.gui;

import ru.kmorozov.gbd.core.logic.extractors.google.GoogleImageExtractor;
import ru.kmorozov.gbd.logger.events.IEventSource;
import ru.kmorozov.gbd.logger.progress.IProgress;

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
    protected Void doInBackground() {
        this.extractor.process();

        return null;
    }

    @Override
    public IProgress getProcessStatus() {
        return this._processStatus;
    }

    protected void setProcessStatus(IProgress processStatus) {
        _processStatus = processStatus;
    }
}
