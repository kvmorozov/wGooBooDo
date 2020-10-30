package ru.kmorozov.gbd.desktop.gui

import ru.kmorozov.db.core.logic.model.book.google.Resolutions
import ru.kmorozov.gbd.core.config.SystemConfigs
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.extractors.google.GoogleImageExtractor
import ru.kmorozov.gbd.core.producers.SingleBookProducer
import ru.kmorozov.gbd.desktop.output.consumers.SwingOutputReceiver
import ru.kmorozov.gbd.logger.events.AbstractEventSource
import ru.kmorozov.gbd.logger.model.LogIconColumnRenderer
import ru.kmorozov.gbd.logger.model.LogTableModel
import ru.kmorozov.gbd.pdf.PdfMaker
import javax.swing.*

/**
 * Created by km on 05.12.2015.
 */
class MainBookForm {
    private val tabbedPane1: JTabbedPane? = null
    val mainPanel: JPanel? = null
    val progressPanel: JPanel? = null
    private val pFooter: JPanel? = null
    private val pManage: JPanel? = null
    private val tfBookId: JTextField? = null
    private val tfRootOutDir: JTextField? = null
    private val tfProxyListFile: JTextField? = null
    val tfBookTitle: JTextField? = null
    private val bRootOutDir: JButton? = null
    private val bProxyList: JButton? = null
    private val bLoad: JButton? = null
    private val bMakeBook: JButton? = null
    private val bImport: JButton? = null
    private var cbResolution: JComboBox<Resolutions>
    private val cbReload: JCheckBox? = null
    private val cbSecureMode: JCheckBox? = null
    private val tpBookInfo: JTabbedPane? = null
    private val tLog: JTable? = null
    private var workerExtractor: SwingWorker<*, *>? = null
    private var workerPdfmaker: SwingWorker<*, *>? = null

    init {
        instance = this

//        ExecutionContext.initContext(SwingOutputReceiver(this), true)

        tfRootOutDir!!.text = SystemConfigs.rootDir
        tfProxyListFile!!.text = SystemConfigs.proxyListFile
        tfBookId!!.text = SystemConfigs.lastBookId

        cbResolution = JComboBox(Resolutions.values())
        cbReload!!.isSelected = SystemConfigs.reloadImages
        cbSecureMode!!.isSelected = SystemConfigs.secureMode

        if (0 < SystemConfigs.resolution)
            cbResolution.selectedItem = Resolutions.getEnum(SystemConfigs.resolution)

        bRootOutDir!!.addActionListener {
            val fcRootDir = JFileChooser()
            fcRootDir.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            val returnVal = fcRootDir.showOpenDialog(mainPanel)
            if (JFileChooser.APPROVE_OPTION == returnVal) {
                tfRootOutDir.text = fcRootDir.selectedFile.absolutePath
                SystemConfigs.rootDir = fcRootDir.selectedFile.absolutePath
            }
        }

        bProxyList!!.addActionListener {
            val fcProxyList = JFileChooser()
            fcProxyList.fileSelectionMode = JFileChooser.FILES_ONLY
            val returnVal = fcProxyList.showOpenDialog(mainPanel)
            if (JFileChooser.APPROVE_OPTION == returnVal) {
                tfProxyListFile.text = fcProxyList.selectedFile.absolutePath
                SystemConfigs.proxyListFile = fcProxyList.selectedFile.absolutePath
            }
        }

        bLoad!!.addActionListener {
            SystemConfigs.lastBookId = tfBookId.text
            bLoad.isEnabled = false
            bMakeBook!!.isEnabled = false


            ExecutionContext.INSTANCE.addBookContext(SingleBookProducer(tfBookId.text), PdfMaker())
            workerExtractor = object : ImageExtractorWorker(GoogleImageExtractor(ExecutionContext.INSTANCE.getContexts(false)[0])) {

                public override fun done() {
                    bLoad.isEnabled = true
                    bMakeBook.isEnabled = true
                }
            }

            workerExtractor!!.addPropertyChangeListener { event ->
                if ("progress" == event.propertyName && event.source is AbstractEventSource) {

                }
            }

            workerExtractor!!.execute()
        }

        cbResolution.addItemListener { event -> SystemConfigs.resolution = (event.item as Resolutions).resolution }
        cbReload.addChangeListener { event -> SystemConfigs.reloadImages = (event.source as AbstractButton).model.isSelected }

        Runtime.getRuntime().addShutdownHook(Thread {
            if (null != workerExtractor && SwingWorker.StateValue.STARTED == workerExtractor!!.state)
                workerExtractor!!.cancel(true)
        })

        bMakeBook!!.addActionListener {
            bLoad.isEnabled = false
            bMakeBook.isEnabled = false

            workerPdfmaker = object : SwingWorker<Void, Void>() {

                override fun doInBackground(): Void? {
                    ExecutionContext.INSTANCE.addBookContext(SingleBookProducer(tfBookId.text), PdfMaker())
                    PdfMaker(ExecutionContext.INSTANCE.getContexts(false)[0]).make()

                    return null
                }

                public override fun done() {
                    bLoad.isEnabled = true
                    bMakeBook.isEnabled = true
                }
            }

            workerPdfmaker!!.execute()
        }

        tLog!!.model = LogTableModel.INSTANCE
        tLog.columnModel.getColumn(0).cellRenderer = LogIconColumnRenderer()
        tLog.columnModel.getColumn(0).maxWidth = 20
    }

    companion object {
        lateinit var instance: MainBookForm
    }
}
