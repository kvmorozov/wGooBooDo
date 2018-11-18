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

    public ProcessStatus(final int maxValue) {
        this.maxValue = maxValue;

        start();
    }

    public ProcessStatus() {

    }

    @Override
    public void resetMaxValue(final int maxValue) {
        this.maxValue = maxValue;

        start();
    }

    @Override
    public int inc() {
        return value.incrementAndGet();
    }

    public int get() {
        return value.get();
    }

    @Override
    public int incrementAndProgress() {
        return 0 == maxValue ? 0 : Math.round((float) Math.min(inc() * 100 / maxValue, 100));
    }

    private void start() {
        if (SystemConfigs.isConsoleMode()) return;

        prBar = new JProgressBar();
        prBar.setMinimum(0);
        prBar.setMinimum(maxValue);
        prBar.setIndeterminate(false);

        SwingUtilities.invokeLater(() -> {
            MainBookForm.getINSTANCE().getProgressPanel().add(prBar);
            SwingUtilities.updateComponentTreeUI(MainBookForm.getINSTANCE().getProgressPanel());
        });
    }

    @Override
    public void finish() {
        if (SystemConfigs.isConsoleMode()) return;

        SwingUtilities.invokeLater(() -> {
            MainBookForm.getINSTANCE().getProgressPanel().remove(prBar);
            SwingUtilities.updateComponentTreeUI(MainBookForm.getINSTANCE().getProgressPanel());
        });
    }

    public JProgressBar getProgressBar() {
        return prBar;
    }

    @Override
    public IProgress getSubProgress(final int maxValue) {
        return new ProcessStatus(maxValue);
    }
}
