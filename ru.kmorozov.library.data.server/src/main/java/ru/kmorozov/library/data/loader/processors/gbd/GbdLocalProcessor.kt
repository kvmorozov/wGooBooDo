package ru.kmorozov.library.data.loader.processors.gbd

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.ComponentScan
import org.springframework.stereotype.Component
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.loader.ListBasedContextLoader
import ru.kmorozov.gbd.core.logic.context.ContextProvider
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.producers.AppendableProducer
import ru.kmorozov.gbd.logger.output.ReceiverProvider
import ru.kmorozov.library.data.loader.processors.IGbdProcessor
import ru.kmorozov.library.data.server.options.LocalServerGBDOptions
import javax.annotation.PostConstruct

@Component
@ComponentScan(basePackageClasses = arrayOf(LocalServerGBDOptions::class))
@Qualifier("local")
open class GbdLocalProcessor : IGbdProcessor {

    @Autowired
    private lateinit var options: LocalServerGBDOptions

    private val producer: AppendableProducer = AppendableProducer()

    @PostConstruct
    private fun init() {
        GBDOptions.init(options)
        ContextProvider.contextProvider = ListBasedContextLoader(producer)
        ExecutionContext.initContext(ReceiverProvider.getReceiver(), true)
    }

    override fun load(bookId: String) {
        producer.appendBook(bookId)

        ExecutionContext.INSTANCE.addBookContext(producer, DummyProgress(), ServerPdfMaker())
        ExecutionContext.INSTANCE.execute()
    }

    override fun process() {

    }
}
