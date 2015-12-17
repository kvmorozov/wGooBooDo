package ru.simpleGBD.App.Logic.model.log;

import ru.simpleGBD.App.Logic.Output.events.LogEvent;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by km on 17.12.2015.
 */
public class LogTableModel extends AbstractTableModel {

    public static final LogTableModel INSTANCE = new LogTableModel();

    private List<LogEvent> logEvents = Collections.synchronizedList(new ArrayList<>());
    ;

    private LogTableModel() {
    }

    @Override
    public int getRowCount() {
        return logEvents.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        LogEvent logEntry = logEvents.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return 1;
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
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> {
                logEvents.add(event);
            });
        }
    }
}
