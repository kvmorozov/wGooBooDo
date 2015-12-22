package ru.simpleGBD.App.GUI;

import ru.simpleGBD.App.Config.SystemConfigs;
import ru.simpleGBD.App.Logic.model.book.Resolutions;
import ru.simpleGBD.App.Logic.ExecutionContext;
import ru.simpleGBD.App.Logic.Output.consumers.SwingBookInfoOutput;
import ru.simpleGBD.App.Logic.extractors.ImageExtractor;
import ru.simpleGBD.App.Logic.model.log.LogIconColumnRenderer;
import ru.simpleGBD.App.Logic.model.log.LogTableModel;
import ru.simpleGBD.App.pdf.PdfMaker;

import javax.swing.*;

/**
 * Created by km on 05.12.2015.
 */
public class MainBookForm {
    private JTabbedPane tabbedPane1;
    private JPanel mainPanel;
    private JTextField tfBookId;
    private JTextField tfRootOutDir, tfProxyListFile;
    private JButton bRootOutDir, bProxyList, bLoad, bMakeBook;
    private JComboBox cbResolution;
    private JCheckBox cbReload, cbFillGaps;
    private JTabbedPane tpBookInfo;
    private JTextField tfBookTitle;
    private JTable tLog;
    private SwingWorker workerExtractor, workerPdfmaker;

    private static MainBookForm INSTANCE;

    public MainBookForm() {
        INSTANCE = this;

        ExecutionContext.output = new SwingBookInfoOutput(this);

        tfRootOutDir.setText(SystemConfigs.getRootDir());
        tfProxyListFile.setText(SystemConfigs.getProxyListFile());
        tfBookId.setText(SystemConfigs.getLastBookId());
        cbResolution.setModel(new DefaultComboBoxModel<>(Resolutions.values()));
        cbReload.setSelected(SystemConfigs.getReload());
        cbFillGaps.setSelected(SystemConfigs.getFillGaps());

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
            bMakeBook.setEnabled(false);

            workerExtractor = new SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() throws Exception {
                    (new ImageExtractor()).process();

                    return null;
                }

                @Override
                public void done() {
                    bLoad.setEnabled(true);
                    bMakeBook.setEnabled(true);
                }
            };

            workerExtractor.execute();
        });

        cbResolution.addItemListener(event -> SystemConfigs.setResolution(((Resolutions) event.getItem()).getResolution()));
        cbReload.addChangeListener(event -> SystemConfigs.setReload(((AbstractButton) event.getSource()).getModel().isSelected()));
        cbFillGaps.addChangeListener(event -> SystemConfigs.setFillGaps(((AbstractButton) event.getSource()).getModel().isSelected()));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (workerExtractor != null && workerExtractor.getState() == SwingWorker.StateValue.STARTED)
                workerExtractor.cancel(true);
        }));

        bMakeBook.addActionListener(e -> {
            bLoad.setEnabled(false);
            bMakeBook.setEnabled(false);

            workerPdfmaker = new SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() throws Exception {
                    (new PdfMaker()).make();

                    return null;
                }

                @Override
                public void done() {
                    bLoad.setEnabled(true);
                    bMakeBook.setEnabled(true);
                }
            };

            workerPdfmaker.execute();
        });

        tLog.setModel(LogTableModel.INSTANCE);
        tLog.getColumnModel().getColumn(0).setCellRenderer(new LogIconColumnRenderer());
        tLog.getColumnModel().getColumn(0).setMaxWidth(20);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JTextField getTfBookTitle() {
        return tfBookTitle;
    }

    public static MainBookForm getINSTANCE() {
        return INSTANCE;
    }
}
