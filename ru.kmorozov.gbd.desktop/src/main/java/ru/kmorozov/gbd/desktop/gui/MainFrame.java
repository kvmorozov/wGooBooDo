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

        if (null != MainFrame.INSTANCE) return;

        MainFrame.INSTANCE = this;

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.setContentPane(new MainBookForm().getMainPanel());
        this.pack();

        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - (double) this.getWidth()) / 2.0);
        int y = (int) ((dimension.getHeight() - (double) this.getHeight()) / 2.0);
        this.setLocation(x, y);
    }

    public static MainFrame getINSTANCE() {
        return MainFrame.INSTANCE;
    }

    public void setVisible() {
        this.setVisible(true);
    }
}
