package ru.simpleGBD.App.GUI;

import javax.swing.*;

/**
 * Created by km on 05.12.2015.
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        super();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setContentPane(new MainForm().getMainPanel());
        pack();
    }

    public void setVisible() {
        setVisible(true);
    }
}
