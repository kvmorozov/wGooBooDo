package ru.kmorozov.gbd.desktop.gui;

import ru.kmorozov.db.core.logic.model.book.google.Resolutions;
import ru.kmorozov.gbd.core.config.SystemConfigs;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.extractors.google.GoogleImageExtractor;
import ru.kmorozov.gbd.core.producers.SingleBookProducer;
import ru.kmorozov.gbd.desktop.output.consumers.SwingOutputReceiver;
import ru.kmorozov.gbd.desktop.output.progress.ProcessStatus;
import ru.kmorozov.gbd.logger.events.AbstractEventSource;
import ru.kmorozov.gbd.logger.events.IEventSource;
import ru.kmorozov.gbd.logger.model.LogIconColumnRenderer;
import ru.kmorozov.gbd.logger.model.LogTableModel;
import ru.kmorozov.gbd.pdf.PdfMaker;

import javax.swing.*;
import javax.swing.SwingWorker.StateValue;

/**
 * Created by km on 05.12.2015.
 */
public class MainBookForm {
    private static MainBookForm INSTANCE;
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
        if (null != MainBookForm.INSTANCE) return;

        MainBookForm.INSTANCE = this;

        ExecutionContext.initContext(new SwingOutputReceiver(this), true);

        this.tfRootOutDir.setText(SystemConfigs.getRootDir());
        this.tfProxyListFile.setText(SystemConfigs.getProxyListFile());
        this.tfBookId.setText(SystemConfigs.getLastBookId());
        this.cbResolution.setModel(new DefaultComboBoxModel<>(Resolutions.values()));
        this.cbReload.setSelected(SystemConfigs.getReload());
        this.cbSecureMode.setSelected(SystemConfigs.getSecureMode());

        if (0 < SystemConfigs.getResolution())
            this.cbResolution.setSelectedItem(Resolutions.getEnum(SystemConfigs.getResolution()));

        this.bRootOutDir.addActionListener(e -> {
            JFileChooser fcRootDir = new JFileChooser();
            fcRootDir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = fcRootDir.showOpenDialog(this.mainPanel);
            if (JFileChooser.APPROVE_OPTION == returnVal) {
                this.tfRootOutDir.setText(fcRootDir.getSelectedFile().getAbsolutePath());
                SystemConfigs.setRootDir(fcRootDir.getSelectedFile().getAbsolutePath());
            }
        });

        this.bProxyList.addActionListener(e -> {
            JFileChooser fcProxyList = new JFileChooser();
            fcProxyList.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int returnVal = fcProxyList.showOpenDialog(this.mainPanel);
            if (JFileChooser.APPROVE_OPTION == returnVal) {
                this.tfProxyListFile.setText(fcProxyList.getSelectedFile().getAbsolutePath());
                SystemConfigs.setProxyListFile(fcProxyList.getSelectedFile().getAbsolutePath());
            }
        });

        this.bLoad.addActionListener(e -> {
            SystemConfigs.setLastBookId(this.tfBookId.getText());
            this.bLoad.setEnabled(false);
            this.bMakeBook.setEnabled(false);


            ExecutionContext.INSTANCE.addBookContext(new SingleBookProducer(this.tfBookId.getText()), new ProcessStatus(), new PdfMaker());
            this.workerExtractor = new ImageExtractorWorker(new GoogleImageExtractor(ExecutionContext.INSTANCE.getContexts(false).get(0))) {

                @Override
                public void done() {
                    MainBookForm.this.bLoad.setEnabled(true);
                    MainBookForm.this.bMakeBook.setEnabled(true);
                }
            };

            this.workerExtractor.addPropertyChangeListener(event -> {
                if ("progress".equals(event.getPropertyName()) && event.getSource() instanceof AbstractEventSource) {
                    ProcessStatus status = (ProcessStatus) ((IEventSource) event.getSource()).getProcessStatus();
                    status.getProgressBar().setValue(status.get());
                }
            });

            this.workerExtractor.execute();
        });

        this.cbResolution.addItemListener(event -> SystemConfigs.setResolution(((Resolutions) event.getItem()).getResolution()));
        this.cbReload.addChangeListener(event -> SystemConfigs.setReload(((AbstractButton) event.getSource()).getModel().isSelected()));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (null != this.workerExtractor && StateValue.STARTED == this.workerExtractor.getState())
                this.workerExtractor.cancel(true);
        }));

        this.bMakeBook.addActionListener(e -> {
            this.bLoad.setEnabled(false);
            this.bMakeBook.setEnabled(false);

            this.workerPdfmaker = new SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() {
                    ExecutionContext.INSTANCE.addBookContext(new SingleBookProducer(MainBookForm.this.tfBookId.getText()), new ProcessStatus(), new PdfMaker());
                    (new PdfMaker(ExecutionContext.INSTANCE.getContexts(false).get(0))).make();

                    return null;
                }

                @Override
                public void done() {
                    MainBookForm.this.bLoad.setEnabled(true);
                    MainBookForm.this.bMakeBook.setEnabled(true);
                }
            };

            this.workerPdfmaker.execute();
        });

        this.tLog.setModel(LogTableModel.INSTANCE);
        this.tLog.getColumnModel().getColumn(0).setCellRenderer(new LogIconColumnRenderer());
        this.tLog.getColumnModel().getColumn(0).setMaxWidth(20);
    }

    public static MainBookForm getINSTANCE() {
        return MainBookForm.INSTANCE;
    }

    public JPanel getMainPanel() {
        return this.mainPanel;
    }

    public JTextField getTfBookTitle() {
        return this.tfBookTitle;
    }

    public JPanel getProgressPanel() {
        return this.pProgress;
    }
}
