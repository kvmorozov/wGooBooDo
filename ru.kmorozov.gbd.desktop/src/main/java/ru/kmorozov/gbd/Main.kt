package ru.kmorozov.gbd

import ru.kmorozov.gbd.core.config.CommandLineOptions
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.config.LocalSystemOptions
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.producers.OptionsBasedProducer
import ru.kmorozov.gbd.desktop.gui.MainFrame
import ru.kmorozov.gbd.desktop.output.progress.ProcessStatus
import ru.kmorozov.gbd.logger.output.ReceiverProvider
import ru.kmorozov.gbd.pdf.PdfMaker

internal object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        if (0 < args.size) {
            GBDOptions.init(CommandLineOptions(args))

            val producer = OptionsBasedProducer()
            ExecutionContext.initContext(ReceiverProvider.getReceiver(GBDOptions.debugEnabled), 1 == producer.bookIds.size)
            ExecutionContext.INSTANCE.addBookContext(producer, ProcessStatus(), PdfMaker())

            ExecutionContext.INSTANCE.execute()
        } else {
            GBDOptions.init(LocalSystemOptions())
            MainFrame().setVisible()
        }
    }
}