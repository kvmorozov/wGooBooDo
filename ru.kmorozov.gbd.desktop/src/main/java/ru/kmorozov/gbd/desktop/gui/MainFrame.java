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

        if (INSTANCE != null) return;

        INSTANCE = this;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setContentPane(new MainBookForm().getMainPanel());
        pack();

        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - getHeight()) / 2);
        setLocation(x, y);
    }

    public static MainFrame getINSTANCE() {
        return INSTANCE;
    }

    public void setVisible() {
        setVisible(true);
    }
}
