package ru.kmorozov.gbd.desktop.output.progress;

import ru.kmorozov.gbd.core.config.SystemConfigs;
import ru.kmorozov.gbd.logger.progress.IProgress;
import ru.kmorozov.gbd.desktop.gui.MainBookForm;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by km on 22.12.2015.
 */
public class ProcessStatus implements IProgress {

    private final AtomicInteger value = new AtomicInteger(0);
    private JProgressBar prBar;
    private int maxValue;

    public ProcessStatus(int maxValue) {
        this.maxValue = maxValue;

        this.start();
    }

    public ProcessStatus() {

    }

    @Override
    public void resetMaxValue(int maxValue) {
        this.maxValue = maxValue;

        this.start();
    }

    @Override
    public int inc() {
        return this.value.incrementAndGet();
    }

    public int get() {
        return this.value.get();
    }

    @Override
    public int incrementAndProgress() {
        return 0 == this.maxValue ? 0 : Math.round((float) Math.min(this.inc() * 100 / this.maxValue, 100));
    }

    private void start() {
        if (SystemConfigs.isConsoleMode()) return;

        this.prBar = new JProgressBar();
        this.prBar.setMinimum(0);
        this.prBar.setMinimum(this.maxValue);
        this.prBar.setIndeterminate(false);

        SwingUtilities.invokeLater(() -> {
            MainBookForm.getINSTANCE().getProgressPanel().add(this.prBar);
            SwingUtilities.updateComponentTreeUI(MainBookForm.getINSTANCE().getProgressPanel());
        });
    }

    @Override
    public void finish() {
        if (SystemConfigs.isConsoleMode()) return;

        SwingUtilities.invokeLater(() -> {
            MainBookForm.getINSTANCE().getProgressPanel().remove(this.prBar);
            SwingUtilities.updateComponentTreeUI(MainBookForm.getINSTANCE().getProgressPanel());
        });
    }

    public JProgressBar getProgressBar() {
        return this.prBar;
    }

    @Override
    public IProgress getSubProgress(int maxValue) {
        return new ProcessStatus(maxValue);
    }
}
