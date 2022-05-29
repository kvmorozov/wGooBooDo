package ru.kmorozov.gbd.core.logic.extractors.google

import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import ru.kmorozov.db.core.logic.model.book.google.GooglePageInfo
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractHttpProcessor
import ru.kmorozov.gbd.core.logic.extractors.base.IUniqueReusable
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import ru.kmorozov.gbd.utils.QueuedThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Created by km on 21.11.2015.
 */
open class GooglePageSigProcessor(bookContext: BookContext, proxy: HttpHostExt) : AbstractHttpProcessor(), IUniqueReusable<GooglePageSigProcessor> {
    lateinit var bookContext: BookContext
    lateinit var proxy: HttpHostExt
    override var reuseCallback: (IUniqueReusable<GooglePageSigProcessor>) -> Unit = {}
    private val sigPageExecutor: QueuedThreadPoolExecutor<GooglePageInfo>
    override lateinit var uniqueObject: GooglePageSigProcessor

    init {
        sigPageExecutor = QueuedThreadPoolExecutor(bookContext.pagesStream.filter { p -> (p as AbstractPage).isNotProcessed }.count().toInt(),
                QueuedThreadPoolExecutor.THREAD_POOL_SIZE, { it.isProcessed },
            "sigPage_$bookContext/$proxy"
        )
        initProcessor(bookContext, proxy)
    }

    private fun initProcessor(bookContext: BookContext, proxy: HttpHostExt) {
        this.bookContext = bookContext
        this.proxy = proxy
        uniqueObject = this
    }

    override fun initReusable(pattern: IUniqueReusable<GooglePageSigProcessor>): Boolean {
        return if (pattern is GooglePageSigProcessor) {
            initProcessor(pattern.bookContext, pattern.proxy)

            true
        } else
            false
    }

    override fun run() {
        if (GBDOptions.secureMode && proxy.isLocal || !proxy.isAvailable) return

        if (!proxy.isLocal && !(proxy.isAvailable && 0 < proxy.host.port)) return

        bookContext.pagesStream.filter { p -> (p as AbstractPage).isNotProcessed }.forEach { page ->
            sigPageExecutor.execute(SigProcessorInternal(bookContext, proxy, page as GooglePageInfo))
        }
        sigPageExecutor.terminate(3L, TimeUnit.MINUTES)
    }

    override fun toString(): String {
        return "Sig processor:$bookContext"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (null == other || javaClass != other.javaClass) return false

        val that = other as GooglePageSigProcessor?

        return EqualsBuilder().append(proxy, that!!.proxy).append(bookContext, that.bookContext).isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37).append(proxy).append(bookContext).toHashCode()
    }

    companion object {
        protected val logger = ExecutionContext.getLogger(GooglePageSigProcessor::class.java)
    }
}
