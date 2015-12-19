package ru.simpleGBD.App.Logic.model.log;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.logging.Level;

/**
 * Created by km on 18.12.2015.
 */
public class LogIconColumnRenderer extends DefaultTableCellRenderer {

    private ImageIcon iconInfo = new ImageIcon(getClass().getResource("/images/icons/exclamation_red_frame.ico"));
    private ImageIcon iconSevere = new ImageIcon(getClass().getResource("/images/icons/exclamation_red_frame.ico"));
    private ImageIcon iconFinest = new ImageIcon(getClass().getResource("/images/icons/exclamation.ico"));

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Level level = (Level) value;
        JLabel lbl = new JLabel();

        ImageIcon icon;

        if (level == Level.INFO)
            icon = iconInfo;
        else if (level == Level.SEVERE)
            icon = iconSevere;
        else
            icon = iconFinest;

        lbl.setIcon(icon);

        return lbl;
    }
}
