package ru.kmorozov.gbd.logger.listeners;

import ru.kmorozov.gbd.logger.events.BaseEvent;
import ru.kmorozov.gbd.logger.events.LogEvent;
import ru.kmorozov.gbd.logger.model.LogTableModel;

import javax.swing.*;

/**
 * Created by km on 15.12.2015.
 */
public class SwingLogEventListener extends AbstractLogEventListener {

    @Override
    public void receiveEvent(final BaseEvent event) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> {
                LogTableModel.INSTANCE.addEvent((LogEvent) event);
//                JOptionPane.showMessageDialog(MainFrame.getINSTANCE(), event.getEventInfo(), "wGooBooDo error", JOptionPane.ERROR_MESSAGE);
            });
        }
    }
}
