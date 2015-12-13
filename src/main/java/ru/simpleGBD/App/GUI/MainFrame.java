package ru.simpleGBD.App.GUI;

import javax.swing.*;
import java.awt.*;

/**
 * Created by km on 05.12.2015.
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        super("wGooBooDo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setContentPane(new MainBookForm().getMainPanel());
        pack();

        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - getHeight()) / 2);
        setLocation(x, y);
    }

    public void setVisible() {
        setVisible(true);
    }
}
