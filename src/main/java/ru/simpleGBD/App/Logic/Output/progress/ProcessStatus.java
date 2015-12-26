package ru.simpleGBD.App.Logic.Output.progress;

import ru.simpleGBD.App.Config.SystemConfigs;
import ru.simpleGBD.App.GUI.MainBookForm;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by km on 22.12.2015.
 */
public class ProcessStatus {

    private AtomicInteger value = new AtomicInteger(0);
    private JProgressBar prBar;
    private int maxValue;

    public ProcessStatus(int maxValue) {
        this.maxValue = maxValue;

        start();
    }

    public int inc() {
        return value.incrementAndGet();
    }

    public int get() {
        return value.get();
    }

    public int incrementAndProgress() {
        return maxValue == 0 ? 0 : Math.round(inc() * 100 / maxValue);
    }

    private void start() {
        if (!SystemConfigs.isGuiMode())
            return;

        prBar = new JProgressBar();
        prBar.setMinimum(0);
        prBar.setMinimum(maxValue);
        prBar.setIndeterminate(false);

        SwingUtilities.invokeLater(() -> {
            MainBookForm.getINSTANCE().getProgressPanel().add(prBar);
            SwingUtilities.updateComponentTreeUI(MainBookForm.getINSTANCE().getProgressPanel());
        });
    }

    public void finish() {
        if (!SystemConfigs.isGuiMode())
            return;

        SwingUtilities.invokeLater(() -> {
            MainBookForm.getINSTANCE().getProgressPanel().remove(prBar);
            SwingUtilities.updateComponentTreeUI(MainBookForm.getINSTANCE().getProgressPanel());
        });
    }

    public JProgressBar getProgressBar() {
        return prBar;
    }
}
