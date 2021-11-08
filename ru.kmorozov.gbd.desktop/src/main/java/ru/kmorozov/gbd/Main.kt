package ru.kmorozov.gbd

import org.apache.commons.cli.ParseException
import ru.kmorozov.gbd.core.config.CommandLineOptions
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.config.LocalSystemOptions
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.producers.OptionsBasedProducer
import ru.kmorozov.gbd.desktop.ProxyInitiator
import ru.kmorozov.gbd.desktop.gui.MainFrame
import ru.kmorozov.gbd.pdf.PdfMaker

internal object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        if (0 < args.size) {
            try {
                GBDOptions.init(CommandLineOptions(args))

                val producer = OptionsBasedProducer()
                ExecutionContext.initContext(1 == producer.bookIds.size)

                val trProxy = Thread { ProxyInitiator().proxyInit() }
                trProxy.start()

                ExecutionContext.INSTANCE.addBookContext(producer, PdfMaker())

                ExecutionContext.INSTANCE.execute()
            }
            catch (pe: ParseException) {
                System.out.println("Invalid CLI arguments!")
            }
        } else {
            GBDOptions.init(LocalSystemOptions())
            MainFrame().setVisible()
        }
    }
}