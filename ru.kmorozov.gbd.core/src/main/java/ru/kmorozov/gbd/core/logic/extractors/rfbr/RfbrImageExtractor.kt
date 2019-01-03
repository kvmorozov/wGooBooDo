package ru.kmorozov.gbd.core.logic.extractors.rfbr

import ru.kmorozov.db.core.logic.model.book.rfbr.RfbrPage
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.extractors.SimplePageImgProcessor
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractImageExtractor
import ru.kmorozov.gbd.logger.progress.IProgress
import java.util.concurrent.TimeUnit

class RfbrImageExtractor(bookContext: BookContext) : AbstractImageExtractor(bookContext, RfbrImageExtractor::class.java) {

    override var processStatus: IProgress
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    override fun preCheck(): Boolean {
        return true
    }

    override fun newProxyEvent(proxy: HttpHostExt) {
        if (!proxy.isLocal) return

        Thread(EventProcessor(proxy)).start()
    }

    private inner class EventProcessor internal constructor(private val proxy: HttpHostExt) : Runnable {

        override fun run() {
            while (!initComplete.get()) {
                waitingProxy.add(proxy)
                return
            }

            for (page in uniqueObject.bookInfo.pages.pages)
                uniqueObject.imgExecutor.execute(SimplePageImgProcessor(uniqueObject, page as RfbrPage, HttpHostExt.NO_PROXY))

            uniqueObject.imgExecutor.terminate(20L, TimeUnit.MINUTES)

            ExecutionContext.INSTANCE.updateProxyList()

            logger.info(uniqueObject.bookInfo.pages.missingPagesList)

            val pagesAfter = uniqueObject.pagesStream.filter { pageInfo -> pageInfo.isDataProcessed }.count()

            logger.info(String.format("Processed %s pages", pagesAfter - uniqueObject.pagesBefore))

            synchronized(uniqueObject) {
                ExecutionContext.INSTANCE.postProcessBook(uniqueObject)
            }
        }
    }
}
