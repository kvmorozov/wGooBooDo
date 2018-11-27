package ru.kmorozov.gbd.logger.model;

import ru.kmorozov.gbd.logger.events.LogEvent;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by km on 17.12.2015.
 */
public final class LogTableModel extends AbstractTableModel {

    public static final LogTableModel INSTANCE = new LogTableModel();

    private final List<LogEvent> logEvents = Collections.synchronizedList(new ArrayList<>());

    private LogTableModel() {
    }

    @Override
    public int getRowCount() {
        return this.logEvents.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        LogEvent logEntry = this.logEvents.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return logEntry.getLevel();
            case 1:
                return logEntry.getEventInfo();
            default:
                return 3;
        }
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "";
            case 1:
                return "Событие";
            default:
                return "";
        }
    }

    public void addEvent(LogEvent event) {
        this.logEvents.add(event);
        this.fireTableDataChanged();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return JLabel.class;
            case 1:
                return String.class;
            default:
                return super.getColumnClass(columnIndex);
        }
    }
}
