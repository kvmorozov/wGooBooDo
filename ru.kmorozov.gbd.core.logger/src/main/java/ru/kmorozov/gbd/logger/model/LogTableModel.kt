package ru.kmorozov.gbd.logger.model

import ru.kmorozov.gbd.logger.events.LogEvent

import javax.swing.*
import javax.swing.table.AbstractTableModel
import java.util.ArrayList
import java.util.Collections

/**
 * Created by km on 17.12.2015.
 */
class LogTableModel private constructor() : AbstractTableModel() {

    private val logEvents = Collections.synchronizedList(ArrayList<LogEvent>())

    override fun getRowCount(): Int {
        return logEvents.size
    }

    override fun getColumnCount(): Int {
        return 2
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val logEntry = logEvents[rowIndex]

        when (columnIndex) {
            0 -> return logEntry.level
            1 -> return logEntry.eventInfo
            else -> return 3
        }
    }

    override fun getColumnName(columnIndex: Int): String {
        when (columnIndex) {
            0 -> return ""
            1 -> return "Событие"
            else -> return ""
        }
    }

    fun addEvent(event: LogEvent) {
        logEvents.add(event)
        fireTableDataChanged()
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        when (columnIndex) {
            0 -> return JLabel::class.java
            1 -> return String::class.java
            else -> return super.getColumnClass(columnIndex)
        }
    }

    companion object {

        val INSTANCE = LogTableModel()
    }
}
