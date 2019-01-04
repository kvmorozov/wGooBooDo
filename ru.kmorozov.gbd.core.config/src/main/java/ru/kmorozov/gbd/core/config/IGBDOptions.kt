package ru.kmorozov.gbd.core.config

import ru.kmorozov.gbd.core.config.options.AuthOptions
import ru.kmorozov.gbd.core.config.options.CtxOptions

/**
 * Created by km on 06.12.2015.
 */
interface IGBDOptions {

    val bookId: String

    val storage: IStorage

    val proxyListFile: String

    val imageWidth: Int

    fun reloadImages(): Boolean

    fun secureMode(): Boolean

    fun pdfOptions(): String

    fun ctxOptions(): CtxOptions

    fun authOptions(): AuthOptions? {
        TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    }
}
