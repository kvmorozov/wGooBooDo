package ru.kmorozov.library.data.loader.processors.gbd

import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.extractors.base.IPostProcessor

class ServerPostProcessor : IPostProcessor {

    override lateinit var uniqueObject: BookContext

    override fun run() {
        uniqueObject.pdfCompleted.set(true)
    }
}
