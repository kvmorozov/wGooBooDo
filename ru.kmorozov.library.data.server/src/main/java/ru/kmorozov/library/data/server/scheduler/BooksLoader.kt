package ru.kmorozov.library.data.server.scheduler

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import ru.kmorozov.library.data.loader.processors.gbd.ServerContext

@Component
@ComponentScan(basePackageClasses = arrayOf(ServerContext::class))
class BooksLoader {

    @Autowired
    private lateinit var serverContext: ServerContext

    @Scheduled(fixedDelay = 30 * 1000)
    fun loadBooks() {
        serverContext.execute()
    }
}