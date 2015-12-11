package ru.simpleGBD.App.GUI;

import ru.simpleGBD.App.Config.SystemConfigs;
import ru.simpleGBD.App.Logic.Runtime.ImageExtractor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by km on 05.12.2015.
 */
public class MainForm {
    private JTabbedPane tabbedPane1;
    private JPanel mainPanel;
    private JTextField tfBookId;
    private JButton bLoad;
    private JTextField tfRootOutDir, tfProxyListFile;
    private JButton bRootOutDir, bProxyList;

    public MainForm() {
        tfRootOutDir.setText(SystemConfigs.getRootDir());
        tfProxyListFile.setText(SystemConfigs.getProxyListFile());
        tfBookId.setText(SystemConfigs.getLastBookId());

        bRootOutDir.addActionListener(e -> {
            JFileChooser fcRootDir = new JFileChooser();
            fcRootDir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = fcRootDir.showOpenDialog(mainPanel);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                tfRootOutDir.setText(fcRootDir.getSelectedFile().getAbsolutePath());
                SystemConfigs.setRootDir(fcRootDir.getSelectedFile().getAbsolutePath());
            }
        });

        bProxyList.addActionListener(e -> {
            JFileChooser fcProxyList = new JFileChooser();
            fcProxyList.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int returnVal = fcProxyList.showOpenDialog(mainPanel);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                tfProxyListFile.setText(fcProxyList.getSelectedFile().getAbsolutePath());
                SystemConfigs.setProxyListFile(fcProxyList.getSelectedFile().getAbsolutePath());
            }
        });

        bLoad.addActionListener(e -> {
            SystemConfigs.setLastBookId(tfBookId.getText());
            bLoad.setEnabled(false);

            SwingWorker worker = new SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() throws Exception {
                    (new ImageExtractor()).process();

                    return null;
                }

                @Override
                public void done() {
                    bLoad.setEnabled(true);
                }
            };

            worker.execute();
        });
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
