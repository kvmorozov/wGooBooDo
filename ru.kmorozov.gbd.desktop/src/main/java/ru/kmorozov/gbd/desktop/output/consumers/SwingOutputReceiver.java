package ru.kmorozov.gbd.desktop.output.consumers;

import ru.kmorozov.gbd.desktop.gui.MainBookForm;
import ru.kmorozov.gbd.logger.consumers.AbstractOutputReceiver;
import ru.kmorozov.gbd.logger.listeners.SwingLogEventListener;
import ru.kmorozov.gbd.logger.model.ILoggableObject;

import javax.swing.*;

/**
 * Created by km on 13.12.2015.
 */
public class SwingOutputReceiver extends AbstractOutputReceiver {

    public SwingOutputReceiver(final MainBookForm form) {
        addListener(new SwingLogEventListener());
    }

    @Override
    public void receive(final ILoggableObject bookInfo) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> MainBookForm.getINSTANCE().getTfBookTitle().setText(bookInfo.getDescription()));
        }
    }
}
