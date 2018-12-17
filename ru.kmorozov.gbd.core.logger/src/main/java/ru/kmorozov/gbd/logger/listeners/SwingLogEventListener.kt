package ru.kmorozov.gbd.logger.listeners

import ru.kmorozov.gbd.logger.events.BaseEvent
import ru.kmorozov.gbd.logger.events.LogEvent
import ru.kmorozov.gbd.logger.model.LogTableModel

import javax.swing.*

/**
 * Created by km on 15.12.2015.
 */
class SwingLogEventListener : AbstractLogEventListener() {

    override fun receiveEvent(event: BaseEvent) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater {
                LogTableModel.INSTANCE.addEvent(event as LogEvent)
                //                JOptionPane.showMessageDialog(MainFrame.getINSTANCE(), event.getEventInfo(), "wGooBooDo error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
