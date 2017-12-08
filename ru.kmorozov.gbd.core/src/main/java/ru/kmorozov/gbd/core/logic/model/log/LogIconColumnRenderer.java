package ru.kmorozov.gbd.core.logic.model.log;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.logging.Level;

/**
 * Created by km on 18.12.2015.
 */
public class LogIconColumnRenderer extends DefaultTableCellRenderer {

    private final JLabel labelInfo = new JLabel(null, new ImageIcon(getClass().getResource("/images/icons/info.png")), JLabel.CENTER);
    private final JLabel labelSevere = new JLabel(null, new ImageIcon(getClass().getResource("/images/icons/error.png")), JLabel.CENTER);
    private final JLabel labelFinest = new JLabel(null, new ImageIcon(getClass().getResource("/images/icons/warning.png")), JLabel.CENTER);

    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        final Level level = (Level) value;

        if (level == Level.INFO) return labelInfo;
        else if (level == Level.SEVERE) return labelSevere;
        else return labelFinest;
    }
}
