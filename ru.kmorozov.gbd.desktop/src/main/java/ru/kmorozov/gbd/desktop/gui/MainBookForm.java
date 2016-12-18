package ru.kmorozov.gbd.desktop.gui;

import ru.kmorozov.gbd.core.config.SystemConfigs;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.extractors.google.GoogleImageExtractor;
import ru.kmorozov.gbd.core.logic.model.book.google.Resolutions;
import ru.kmorozov.gbd.core.logic.model.log.LogIconColumnRenderer;
import ru.kmorozov.gbd.core.logic.model.log.LogTableModel;
import ru.kmorozov.gbd.core.logic.output.events.AbstractEventSource;
import ru.kmorozov.gbd.core.logic.output.events.ImageExtractorWorker;
import ru.kmorozov.gbd.desktop.library.SingleBookProducer;
import ru.kmorozov.gbd.desktop.output.consumers.SwingBookInfoOutput;
import ru.kmorozov.gbd.desktop.output.progress.ProcessStatus;
import ru.kmorozov.gbd.pdf.PdfMaker;

import javax.swing.*;

/**
 * Created by km on 05.12.2015.
 */
public class MainBookForm {
    private static MainBookForm INSTANCE;
    private static ManageController manageController = new ManageController();
    private JTabbedPane tabbedPane1;
    private JPanel mainPanel, pProgress, pFooter, pManage;
    private JTextField tfBookId, tfRootOutDir, tfProxyListFile, tfBookTitle;
    private JButton bRootOutDir, bProxyList, bLoad, bMakeBook, bImport;
    private JComboBox cbResolution;
    private JCheckBox cbReload, cbSecureMode;
    private JTabbedPane tpBookInfo;
    private JTable tLog;
    private SwingWorker workerExtractor, workerPdfmaker;

    public MainBookForm() {
        if (INSTANCE != null) return;

        INSTANCE = this;

        ExecutionContext.initContext(new SwingBookInfoOutput(this), true);

        tfRootOutDir.setText(SystemConfigs.getRootDir());
        tfProxyListFile.setText(SystemConfigs.getProxyListFile());
        tfBookId.setText(SystemConfigs.getLastBookId());
        cbResolution.setModel(new DefaultComboBoxModel<>(Resolutions.values()));
        cbReload.setSelected(SystemConfigs.getReload());
        cbSecureMode.setSelected(SystemConfigs.getSecureMode());

        configureManage();

        if (SystemConfigs.getResolution() > 0) cbResolution.setSelectedItem(Resolutions.getEnum(SystemConfigs.getResolution()));

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

            ExecutionContext.INSTANCE.addBookContext(new SingleBookProducer(tfBookId.getText()), new ProcessStatus(), new PdfMaker());
            workerExtractor = new ImageExtractorWorker(new GoogleImageExtractor(ExecutionContext.INSTANCE.getContexts(false).get(0))) {

                @Override
                public void done() {
                    bLoad.setEnabled(true);
                    bMakeBook.setEnabled(true);
                }
            };

            workerExtractor.addPropertyChangeListener(event -> {
                if ("progress".equals(event.getPropertyName()) && event.getSource() instanceof AbstractEventSource) {
                    ProcessStatus status = (ProcessStatus) ((AbstractEventSource) event.getSource()).getProcessStatus();
                    status.getProgressBar().setValue(status.get());
                }
            });

            workerExtractor.execute();
        });

        cbResolution.addItemListener(event -> SystemConfigs.setResolution(((Resolutions) event.getItem()).getResolution()));
        cbReload.addChangeListener(event -> SystemConfigs.setReload(((AbstractButton) event.getSource()).getModel().isSelected()));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (workerExtractor != null && workerExtractor.getState() == SwingWorker.StateValue.STARTED) workerExtractor.cancel(true);
        }));

        bMakeBook.addActionListener(e -> {
            bLoad.setEnabled(false);
            bMakeBook.setEnabled(false);

            workerPdfmaker = new SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() throws Exception {
                    ExecutionContext.INSTANCE.addBookContext(new SingleBookProducer(tfBookId.getText()), new ProcessStatus(), new PdfMaker());
                    (new PdfMaker(ExecutionContext.INSTANCE.getContexts(false).get(0))).make();

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

    public static MainBookForm getINSTANCE() {
        return INSTANCE;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JTextField getTfBookTitle() {
        return tfBookTitle;
    }

    public JPanel getProgressPanel() {
        return pProgress;
    }

    private void configureManage() {
        tabbedPane1.setEnabledAt(2, manageController.isManageAllowed());
        bImport.setVisible(manageController.isImportAllowed());
        bImport.addActionListener(e -> manageController.importBooks());
    }
}
