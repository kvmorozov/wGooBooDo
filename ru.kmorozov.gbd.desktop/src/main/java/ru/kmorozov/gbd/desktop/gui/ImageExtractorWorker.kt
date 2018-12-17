package ru.kmorozov.gbd.desktop.gui

import ru.kmorozov.gbd.core.logic.extractors.google.GoogleImageExtractor
import ru.kmorozov.gbd.logger.events.IEventSource
import ru.kmorozov.gbd.logger.progress.IProgress

import javax.swing.*

/**
 * Created by km on 12.11.2016.
 */
open class ImageExtractorWorker(private val extractor: GoogleImageExtractor) : SwingWorker<Void, Void>(), IEventSource {
    override var processStatus: IProgress
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    override fun doInBackground(): Void? {
        extractor.process()

        return null
    }
}
