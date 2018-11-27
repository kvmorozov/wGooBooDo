package ru.kmorozov.gbd.desktop.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by km on 05.12.2015.
 */
public class MainFrame extends JFrame {

    private static MainFrame INSTANCE;

    public MainFrame() {
        super("wGooBooDo");

        if (null != INSTANCE) return;

        INSTANCE = this;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setContentPane(new MainBookForm().getMainPanel());
        pack();

        final Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        final int x = (int) ((dimension.getWidth() - (double) getWidth()) / 2.0);
        final int y = (int) ((dimension.getHeight() - (double) getHeight()) / 2.0);
        setLocation(x, y);
    }

    public static MainFrame getINSTANCE() {
        return INSTANCE;
    }

    public void setVisible() {
        setVisible(true);
    }
}
