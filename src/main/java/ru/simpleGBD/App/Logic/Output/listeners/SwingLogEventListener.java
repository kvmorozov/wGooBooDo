package ru.simpleGBD.App.Logic.Output.listeners;

import ru.simpleGBD.App.Logic.Output.events.BaseEvent;
import ru.simpleGBD.App.Logic.Output.events.LogEvent;
import ru.simpleGBD.App.Logic.model.log.LogTableModel;

import javax.swing.*;

/**
 * Created by km on 15.12.2015.
 */
public class SwingLogEventListener extends AbstractLogEventListener {

    @Override
    public void receiveEvent(BaseEvent event) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> {
                LogTableModel.INSTANCE.addEvent((LogEvent) event);
            });
        }
    }
}
