package ru.kmorozov.library.data.loader.processors.gbd

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.ComponentScan
import org.springframework.stereotype.Component
import ru.kmorozov.gbd.core.loader.ListBasedContextLoader
import ru.kmorozov.gbd.core.logic.context.ContextProvider
import ru.kmorozov.gbd.core.producers.AppendableProducer
import ru.kmorozov.library.data.loader.processors.IGbdProcessor
import ru.kmorozov.library.data.server.options.InMemoryOptions

@Component
@ComponentScan(basePackageClasses = arrayOf(InMemoryOptions::class))
@Qualifier("inMemory")
open class InMemoryProcessor : IGbdProcessor {

    private val producer: AppendableProducer = AppendableProducer()

    init {
        ContextProvider.contextProvider = ListBasedContextLoader(producer)
    }

    @Autowired
    private lateinit var options: InMemoryOptions

    override fun load(bookId: String) {
        producer.appendBook(bookId)
    }

    override fun process() {

    }
}