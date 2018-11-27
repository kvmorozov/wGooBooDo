package ru.kmorozov.gbd.logger.model;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.logging.Level;

/**
 * Created by km on 18.12.2015.
 */
public class LogIconColumnRenderer extends DefaultTableCellRenderer {

    private final JLabel labelInfo = new JLabel(null, new ImageIcon(this.getClass().getResource("/images/icons/info.png")), SwingConstants.CENTER);
    private final JLabel labelSevere = new JLabel(null, new ImageIcon(this.getClass().getResource("/images/icons/error.png")), SwingConstants.CENTER);
    private final JLabel labelFinest = new JLabel(null, new ImageIcon(this.getClass().getResource("/images/icons/warning.png")), SwingConstants.CENTER);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Level level = (Level) value;

        if (level == Level.INFO) return this.labelInfo;
        else if (level == Level.SEVERE) return this.labelSevere;
        else return this.labelFinest;
    }
}
