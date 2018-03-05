package ru.kmorozov.library.data.loader.processors.gbd;

import ru.kmorozov.gbd.logger.progress.IProgress;

public class DummyProgress implements IProgress {

    @Override
    public int inc() {
        return 0;
    }

    @Override
    public int incrementAndProgress() {
        return 0;
    }

    @Override
    public void finish() {

    }

    @Override
    public IProgress getSubProgress(int maxValue) {
        return null;
    }

    @Override
    public void resetMaxValue(int maxValue) {

    }
}
