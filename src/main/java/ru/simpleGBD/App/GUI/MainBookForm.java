package ru.simpleGBD.App.GUI;

import ru.simpleGBD.App.Config.SystemConfigs;
import ru.simpleGBD.App.Logic.DataModel.Resolutions;
import ru.simpleGBD.App.Logic.Output.SwingBookInfoOutput;
import ru.simpleGBD.App.Logic.Runtime.ImageExtractor;

import javax.swing.*;

/**
 * Created by km on 05.12.2015.
 */
public class MainBookForm {
    private JTabbedPane tabbedPane1;
    private JPanel mainPanel;
    private JTextField tfBookId;
    private JButton bLoad;
    private JTextField tfRootOutDir, tfProxyListFile;
    private JButton bRootOutDir, bProxyList;
    private JComboBox cbResolution;
    private JCheckBox cbReload;
    private JTabbedPane tpBookInfo;
    private JTextField tfBookTitle;
    private SwingWorker worker;

    private final MainBookForm _mainForm;

    public MainBookForm() {
        _mainForm = this;

        tfRootOutDir.setText(SystemConfigs.getRootDir());
        tfProxyListFile.setText(SystemConfigs.getProxyListFile());
        tfBookId.setText(SystemConfigs.getLastBookId());
        cbResolution.setModel(new DefaultComboBoxModel<>(Resolutions.values()));
        cbReload.setSelected(SystemConfigs.getReload());

        if (SystemConfigs.getResolution() > 0)
            cbResolution.setSelectedItem(Resolutions.getEnum(SystemConfigs.getResolution()));

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

            worker = new SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() throws Exception {
                    (new ImageExtractor(new SwingBookInfoOutput(_mainForm))).process();

                    return null;
                }

                @Override
                public void done() {
                    bLoad.setEnabled(true);
                }
            };

            worker.execute();
        });

        cbResolution.addItemListener(event -> SystemConfigs.setResolution(((Resolutions) event.getItem()).getResolution()));
        cbReload.addChangeListener(event -> SystemConfigs.setReload(((AbstractButton) event.getSource()).getModel().isSelected()));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
           if (worker != null && worker.getState() == SwingWorker.StateValue.STARTED)
               worker.cancel(true);
        }));
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JTextField getTfBookTitle() {
        return tfBookTitle;
    }
}
