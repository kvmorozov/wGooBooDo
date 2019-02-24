package ru.kmorozov.library.data.loader.processors.gbd

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ComponentScan
import org.springframework.stereotype.Component
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.loader.ListBasedContextLoader
import ru.kmorozov.gbd.core.logic.context.ContextProvider
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.producers.SingleBookProducer
import ru.kmorozov.gbd.logger.output.ReceiverProvider
import ru.kmorozov.library.data.loader.processors.IGbdProcessor
import ru.kmorozov.library.data.server.options.LocalServerGBDOptions

@Component
@ComponentScan(basePackageClasses = arrayOf(LocalServerGBDOptions::class))
open class GbdLocalProcessor : IGbdProcessor {

    @Autowired
    private lateinit var options: LocalServerGBDOptions

    override fun load(bookId: String) {
        options.bookId = bookId
        GBDOptions.init(options)

        val producer = SingleBookProducer(bookId)
        ContextProvider.contextProvider = ListBasedContextLoader(producer)

        ExecutionContext.initContext(ReceiverProvider.getReceiver(), 1 == producer.bookIds.size)
        ExecutionContext.INSTANCE.addBookContext(producer, DummyProgress(), ServerPdfMaker())

        ExecutionContext.INSTANCE.execute()
    }

    override fun process() {

    }
}
