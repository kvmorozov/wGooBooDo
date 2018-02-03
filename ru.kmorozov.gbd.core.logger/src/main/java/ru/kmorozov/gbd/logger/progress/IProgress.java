package ru.kmorozov.gbd.logger.progress;

/**
 * Created by km on 06.11.2016.
 */
public interface IProgress {

    int inc();

    int incrementAndProgress();

    void finish();

    IProgress getSubProgress(int maxValue);

    void resetMaxValue(int maxValue);
}
