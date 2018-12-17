package ru.kmorozov.gbd.desktop.output.consumers

import ru.kmorozov.gbd.desktop.gui.MainBookForm
import ru.kmorozov.gbd.logger.consumers.AbstractOutputReceiver
import ru.kmorozov.gbd.logger.listeners.SwingLogEventListener
import ru.kmorozov.gbd.logger.model.ILoggableObject

import javax.swing.*

/**
 * Created by km on 13.12.2015.
 */
class SwingOutputReceiver(form: MainBookForm) : AbstractOutputReceiver() {

    init {
        addListener(SwingLogEventListener())
    }

    override fun receive(bookInfo: ILoggableObject) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater { MainBookForm.instance!!.tfBookTitle!!.setText(bookInfo.description) }
        }
    }
}
