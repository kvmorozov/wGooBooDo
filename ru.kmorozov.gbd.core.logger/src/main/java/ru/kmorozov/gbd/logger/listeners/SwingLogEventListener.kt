package ru.kmorozov.gbd.logger.listeners

import ru.kmorozov.gbd.logger.events.LogEvent
import ru.kmorozov.gbd.logger.model.LogTableModel
import javax.swing.SwingUtilities

/**
 * Created by km on 15.12.2015.
 */
class SwingLogEventListener : AbstractLogEventListener() {

    override fun receiveEvent(event: LogEvent) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater {
                LogTableModel.INSTANCE.addEvent(event)
                //                JOptionPane.showMessageDialog(MainFrame.getINSTANCE(), event.getEventInfo(), "wGooBooDo error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
