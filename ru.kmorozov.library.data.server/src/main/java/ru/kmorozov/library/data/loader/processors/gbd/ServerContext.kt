package ru.kmorozov.library.data.loader.processors.gbd

import org.springframework.stereotype.Component
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.context.IBookListProducer

@Component
class ServerContext {

    fun updateBookList(idsProducer: IBookListProducer) {
        ExecutionContext.INSTANCE.addBookContext(idsProducer, ServerPostProcessor())
    }

    fun execute() {
        if (ExecutionContext.INSTANCE.size() == 0 || inProcess())
            return

        ExecutionContext.INSTANCE.execute()
    }

    private fun inProcess(): Boolean {
        return ExecutionContext.INSTANCE.inProcess()
    }
}