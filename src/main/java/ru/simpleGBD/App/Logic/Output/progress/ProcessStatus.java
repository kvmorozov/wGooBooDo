package ru.simpleGBD.App.Logic.Output.progress;

import ru.simpleGBD.App.GUI.MainBookForm;
import ru.simpleGBD.App.GUI.MainFrame;

import javax.swing.*;
import java.awt.*;
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

    public void inc() {
        SwingUtilities.invokeLater(() -> {
            prBar.setValue(value.incrementAndGet());

//            MainFrame.getINSTANCE().repaint();
            MainBookForm.getINSTANCE().getProgressPanel().repaint();
        });
    }

    private void start() {
        prBar = new JProgressBar();
        prBar.setMinimum(0);
        prBar.setMinimum(maxValue);

        SwingUtilities.invokeLater(() -> {
            MainBookForm.getINSTANCE().getProgressPanel().add(prBar);
            MainBookForm.getINSTANCE().getProgressPanel().repaint();
        });
    }

    public void finish() {
        SwingUtilities.invokeLater(() -> {
            MainBookForm.getINSTANCE().getProgressPanel().remove(prBar);
            MainBookForm.getINSTANCE().getProgressPanel().repaint();
        });
    }
}
