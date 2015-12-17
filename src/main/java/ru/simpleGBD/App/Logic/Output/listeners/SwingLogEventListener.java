package ru.simpleGBD.App.Logic.Output.listeners;

import ru.simpleGBD.App.Logic.Output.events.BaseEvent;
import ru.simpleGBD.App.Logic.Output.events.LogEvent;
import ru.simpleGBD.App.Logic.model.log.LogTableModel;

/**
 * Created by km on 15.12.2015.
 */
public class SwingLogEventListener extends AbstractLogEventListener {

    @Override
    public void receiveEvent(BaseEvent event) {
        LogTableModel.INSTANCE.addEvent((LogEvent) event);
    }
}
