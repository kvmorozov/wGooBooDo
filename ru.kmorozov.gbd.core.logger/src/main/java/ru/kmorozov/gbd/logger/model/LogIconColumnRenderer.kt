package ru.kmorozov.gbd.logger.model

import javax.swing.*
import javax.swing.table.DefaultTableCellRenderer
import java.awt.*
import java.util.logging.Level

/**
 * Created by km on 18.12.2015.
 */
class LogIconColumnRenderer : DefaultTableCellRenderer() {

    private val labelInfo = JLabel(null, ImageIcon(javaClass.getResource("/images/icons/info.png")), SwingConstants.CENTER)
    private val labelSevere = JLabel(null, ImageIcon(javaClass.getResource("/images/icons/error.png")), SwingConstants.CENTER)
    private val labelFinest = JLabel(null, ImageIcon(javaClass.getResource("/images/icons/warning.png")), SwingConstants.CENTER)

    override fun getTableCellRendererComponent(table: JTable?, value: Any, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
        val level = value as Level

        return if (level === Level.INFO)
            labelInfo
        else if (level === Level.SEVERE)
            labelSevere
        else
            labelFinest
    }
}
